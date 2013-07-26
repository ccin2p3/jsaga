/*
 * Copyright (c) 2004 on behalf of the EU EGEE Project:
 * The European Organization for Nuclear Research (CERN),
 * Istituto Nazionale di Fisica Nucleare (INFN), Italy
 * Datamat Spa, Italy
 * Centre National de la Recherche Scientifique (CNRS), France
 * CS Systeme d'Information (CSSI), France
 * Royal Institute of Technology, Center for Parallel Computers (KTH-PDC), Sweden
 * Universiteit van Amsterdam (UvA), Netherlands
 * University of Helsinki (UH.HIP), Finland
 * University of Bergen (UiB), Norway
 * Council for the Central Laboratory of the Research Councils (CCLRC), United Kingdom
 *
 * Authors: Paolo Andreetto, <paolo.andreetto@pd.infn.it>
 *
 * Version info: $Id: CEEventManager.java,v 1.2 2007/12/19 15:58:02 zangran Exp $
 *
 */

package org.glite.ce.commonj.jndi.provider.fscachedprovider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.Name;
import javax.naming.event.NamingEvent;
import javax.naming.event.EventContext;
import javax.naming.event.NamingListener;
import javax.naming.event.ObjectChangeListener;
import javax.naming.event.NamespaceChangeListener;

import org.apache.log4j.Logger;

public class CEEventManager {

    private static Logger logger = Logger.getLogger(CEEventManager.class.getName());

    private CEEventItem root;

    public CEEventManager(){

        root = new CEEventItem();
        
    }

    public synchronized void register(Name target, int scope,
                                      NamingListener lsnr){

        CEEventItem currentItem = root;
        for(int k=0; k<target.size(); k++){
            currentItem = currentItem.createSubItem(target.get(k));
        }

        if( currentItem.addListener(lsnr, scope) ){

            currentItem = root;
            currentItem.incrementDeps(1);

            for(int k=0; k<target.size(); k++){
                currentItem = currentItem.getItem(target.get(k));
                currentItem.incrementDeps(1);
            }
        }

    }

    private void removeItem(Name target, NamingListener lsnr){

        CEEventItem currentItem = root;
        for(int k=0; k<target.size(); k++){
            currentItem = currentItem.getItem(target.get(k));
            if( currentItem==null )
                return;
        }

        int nLsnr = 0;
        if( lsnr!= null ){
            nLsnr = currentItem.removeListener(lsnr);
        }else{
            nLsnr = currentItem.removeAllListeners();
        }

        if( nLsnr==0 )
            return;

        currentItem = root;
        currentItem.decrementDeps(nLsnr);

        CEEventItem prevItem = root;

        for(int k=0; k<target.size(); k++){
            currentItem = currentItem.getItem(target.get(k));
            currentItem.decrementDeps(nLsnr);
            if( currentItem.hasDeps() ){
                prevItem = currentItem;
            }else{
                prevItem.removeItem(target.get(k));
                return;
            }
        }
    }

    public synchronized void remove(Name target, NamingListener lsnr){
        removeItem(target, lsnr);
    }

    public synchronized void removeAll(Name target){
        removeItem(target, null);
    }

    public synchronized Tuple[] getListeners(Name target, int type){
        CEEventItem currentItem = null;
        NamingListener[] lsnrs = null;
        ArrayList resList = new ArrayList();

        for(int k=0; k<=target.size(); k++){

            Name source = target.getPrefix(k);

            if( source.isEmpty() ){
                currentItem = root;
            }else{
                currentItem = currentItem.getItem(target.get(k-1));
                if( currentItem==null )
                    break;
            }

            collectListener(currentItem,EventContext.SUBTREE_SCOPE,type,
                            source,resList);

            switch( target.size()-k ){
            case 0:
                collectListener(currentItem,EventContext.OBJECT_SCOPE,
                                type,source,resList);
                break;
            case 1:
                collectListener(currentItem,EventContext.ONELEVEL_SCOPE,
                                type,source,resList);
                break;
            }

        }

        Tuple[] result = new Tuple[resList.size()];
        resList.toArray(result);
        return result;
    }

    private void collectListener(CEEventItem evnItem, int scopeType,
                                 int evnType, Name src, ArrayList list){

        NamingListener[] lsnrs = evnItem.getListeners(scopeType);

        for(int j=0; j<lsnrs.length; j++){
            if( ( evnType==NamingEvent.OBJECT_CHANGED &&
                  lsnrs[j] instanceof ObjectChangeListener ) ||
                ( evnType!=NamingEvent.OBJECT_CHANGED &&
                  lsnrs[j] instanceof NamespaceChangeListener ) ){
                list.add(new Tuple(lsnrs[j], src));
            }
        }

    }

    public class Tuple{

        private NamingListener listener;
        private Name source;

        public Tuple(NamingListener lsnr, Name src){
            listener = lsnr;
            source = src;
        }

        public NamingListener getListener(){
            return listener;
        }

        public Name getSource(){
            return source;
        }
    }
}
