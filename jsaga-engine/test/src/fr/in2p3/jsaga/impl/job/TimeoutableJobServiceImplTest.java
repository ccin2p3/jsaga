package fr.in2p3.jsaga.impl.job;

import fr.in2p3.jsaga.adaptor.WaitForEverAdaptorAbstract;
import org.ogf.saga.AbstractTest;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.job.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   TimeoutableJobServiceImplTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class TimeoutableJobServiceImplTest extends AbstractTest {
    private static final String m_url = "waitforever://host";
    private JobService m_service;

    public TimeoutableJobServiceImplTest() throws Exception {
        super();
    }

    public void setUp() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_url);
        m_service = JobFactory.createJobService(emptySession, url);
    }

    public void tearDown() {
        // do nothing
    }

    public void test_createJob() throws Exception {
        // can not hang...
        JobDescription description = JobFactory.createJobDescription();
        description.setAttribute(JobDescription.EXECUTABLE, "/usr/bin/ls");
        m_service.createJob(description);
    }

    public void test_runJob() throws Exception {
        try {
            m_service.runJob("hangatconnect");
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_list() throws Exception {
        try {
            m_service.list();
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_getJob() throws Exception {
        // can not hang...
        m_service.getJob("myjobid");
    }
}
