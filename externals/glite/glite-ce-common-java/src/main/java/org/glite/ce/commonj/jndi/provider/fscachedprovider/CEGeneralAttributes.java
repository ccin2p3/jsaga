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
 *          Luigi Zangrando, <luigi.zangrando@pd.infn.it>
 *
 * Version info: $Id: CEGeneralAttributes.java,v 1.2 2007/12/19 15:58:02 zangran Exp $
 *
 */

package org.glite.ce.commonj.jndi.provider.fscachedprovider;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import org.glite.ce.commonj.CEResource;

import org.apache.log4j.Logger;

public class CEGeneralAttributes extends BasicAttributes implements CEResource{

    private static Logger logger = Logger.getLogger(CEGeneralAttributes.class.getName());

    public CEGeneralAttributes(){
        super();
    }

    public CEGeneralAttributes(boolean ignoreCase){
        super(ignoreCase);
    }

    public CEGeneralAttributes(String attrID, Object val){
        super(attrID, val);
    }

    public CEGeneralAttributes(String attrID, Object val, boolean ignoreCase){
        super(attrID, val, ignoreCase);
    }

    public CEGeneralAttributes(Attributes inAttrs){

        super();

        if( inAttrs!=null ){
            NamingEnumeration allAttrs = inAttrs.getAll();
            while( allAttrs.hasMoreElements() ){
                this.put((Attribute)allAttrs.nextElement());
            }
        }
    }

    public boolean match(Attributes attrs){
        NamingEnumeration items = attrs.getAll();
        while( items.hasMoreElements() ){
            Attribute source = (Attribute)items.nextElement();
            Attribute target = this.get(source.getID());
            if( target==null ) return false;

            try{
                NamingEnumeration values = source.getAll();
                while( values.hasMoreElements() ){
                    if( !target.contains(values.nextElement()) )
                        return false;
                }
            }catch(NamingException nEx){
                logger.error(nEx.getMessage(), nEx);
                return false;
            }
        }
        return true;
    }

    public CEGeneralAttributes clone(String[] attributesRequired){

        if( attributesRequired==null )
            return (CEGeneralAttributes)this.clone();

        CEGeneralAttributes result = new CEGeneralAttributes();

        for(int k=0; k<attributesRequired.length; k++){
            Attribute tmpAttr = this.get(attributesRequired[k]);
            if( tmpAttr!=null ) result.put(tmpAttr);
        }

        return result;
    }
}
