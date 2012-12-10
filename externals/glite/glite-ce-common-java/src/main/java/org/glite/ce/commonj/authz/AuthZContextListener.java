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
 * Version info: $Id: AuthZContextListener.java,v 1.2 2009/03/09 11:18:26 zangran Exp $
 */

package org.glite.ce.commonj.authz;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.glite.voms.VOMSValidator;
import org.glite.voms.PKIStore;
import org.glite.voms.ac.ACValidator;
import org.glite.voms.ac.VOMSTrustStore;

import org.apache.log4j.Logger;

public class AuthZContextListener implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(AuthZContextListener.class.getName());
    public static VOMSTrustStore vomsStore = null;

    public void contextInitialized(ServletContextEvent event) {
        try {
            vomsStore = new PKIStore(PKIStore.DEFAULT_VOMSDIR, PKIStore.TYPE_VOMSDIR, true);
            VOMSValidator.setTrustStore(vomsStore);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException("Cannot configure VOMS support");
        }
        logger.info("VOMS store initialized");
    }

    public void contextDestroyed(ServletContextEvent event) {
        if (vomsStore != null) {
            vomsStore.stopRefresh();
        }
    }

    public static ACValidator getACValidator() {
        if (vomsStore != null) {
            return ACValidator.getInstance(vomsStore);
        }
        throw new RuntimeException("VOMS store not initialized");
    }
}
