package org.ogf.saga.job.abstracts;

import org.ogf.saga.AbstractTest;
import org.ogf.saga.URL;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractJobTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class AbstractJobTest extends AbstractTest {
    // configuration
    protected URL m_jobservice;
    protected Session m_session;

    public AbstractJobTest(String jobprotocol) throws Exception {
        super();

        // configure
        m_jobservice = new URL(getRequiredProperty(jobprotocol, CONFIG_JOBSERVICE_URL).replaceAll(" ", "%20"));
        if (m_jobservice.getFragment() != null) {
            m_session = SessionFactory.createSession(true);
        }
    }
}
