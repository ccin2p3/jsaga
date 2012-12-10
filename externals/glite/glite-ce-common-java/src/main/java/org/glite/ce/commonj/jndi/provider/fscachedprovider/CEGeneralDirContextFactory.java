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
 * Version info: $Id: CEGeneralDirContextFactory.java,v 1.2 2007/12/19 15:58:02 zangran Exp $
 *
 */

package org.glite.ce.commonj.jndi.provider.fscachedprovider;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.InitialContextFactory;

import org.apache.log4j.Logger;

public class CEGeneralDirContextFactory 
    implements InitialContextFactory {

    private static Logger logger =
        Logger.getLogger(CEGeneralDirContextFactory.class.getName());

    public CEGeneralDirContextFactory(){}

    public Context getInitialContext(Hashtable env) throws NamingException{

        String url = (String)env.get(Context.PROVIDER_URL);
        if( url==null ) 
            throw new NoInitialContextException();
        return new CEGeneralDirContext(url, env);

    }

}
