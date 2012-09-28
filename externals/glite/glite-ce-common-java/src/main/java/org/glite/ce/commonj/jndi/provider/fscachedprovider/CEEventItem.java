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
 * Version info: $Id: CEEventItem.java,v 1.2 2007/12/19 15:58:02 zangran Exp $
 *
 */

package org.glite.ce.commonj.jndi.provider.fscachedprovider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.event.EventContext;
import javax.naming.event.NamingListener;

import org.apache.log4j.Logger;

public final class CEEventItem {

    private static Logger logger = Logger.getLogger(CEEventItem.class.getName());

    private HashMap children;
    private HashMap scopes;
    private int numberOfObjectScopes;
    private int numberOfOneLevelScopes;
    private int numberOfSubtreeScopes;
    private int dependencies;

    public CEEventItem(){

        children = new HashMap(0);
        scopes = new HashMap(0);

        numberOfObjectScopes = 0;
        numberOfOneLevelScopes = 0;
        numberOfSubtreeScopes = 0;
        dependencies = 0;
    }

    public boolean addListener(NamingListener lsnr,  int scope){

        boolean result = removeListener(lsnr) == 0;

        scopes.put(lsnr, new Integer(scope));

        if( scope==EventContext.OBJECT_SCOPE ){
            numberOfObjectScopes++;
        }else if( scope==EventContext.ONELEVEL_SCOPE ){
            numberOfOneLevelScopes++;
        }else{
            numberOfSubtreeScopes++;
        }

        return result;
    }

    public int removeListener(NamingListener lsnr){
        Integer scope = (Integer)scopes.remove(lsnr);
        if( scope!=null ){
            int sc = scope.intValue();
            if( sc==EventContext.OBJECT_SCOPE ){
                numberOfObjectScopes--;
            }else if( sc==EventContext.ONELEVEL_SCOPE ){
                numberOfOneLevelScopes--;
            }else{
                numberOfSubtreeScopes--;
            }
            return 1;
        }

        return 0;
    }

    public int removeAllListeners(){
        int result = scopes.size();
        scopes.clear();
        numberOfObjectScopes = 0;
        numberOfOneLevelScopes = 0;
        numberOfSubtreeScopes = 0;
        return result;
    }

    public NamingListener[] getListeners(int scope){

        NamingListener[] result = null;
        if( scope==EventContext.OBJECT_SCOPE ){
            result = new NamingListener[numberOfObjectScopes];
        }else if( scope==EventContext.ONELEVEL_SCOPE ){
            result = new NamingListener[numberOfOneLevelScopes];
        }else{
            result = new NamingListener[numberOfSubtreeScopes];
        }

        if( result.length==0 ) return result;

        Iterator lsnrItem = scopes.keySet().iterator();
        for(int k=0; lsnrItem.hasNext(); k++){
            NamingListener lsnr = (NamingListener)lsnrItem.next();
            int tmpsc = ((Integer)scopes.get(lsnr)).intValue();
            if( tmpsc==scope ) result[k] = lsnr;
        }

        return result;
    }

    public CEEventItem createSubItem(String child){
        CEEventItem result = (CEEventItem)children.get(child);
        if( result==null ){
            result = new CEEventItem();
            children.put(child, result);
        }
        return result;
    }

    public CEEventItem getItem(String child){
        return (CEEventItem)children.get(child);
    }

    public boolean removeItem(String child){
        return children.remove(child)!=null;
    }

    public void incrementDeps(int d){
        dependencies = dependencies + d;
    }

    public void decrementDeps(int d){
        if( dependencies>=d )
            dependencies = dependencies - d;
        else
            dependencies = 0;
    }

    public boolean hasDeps(){
        return dependencies>0;
    }

}
