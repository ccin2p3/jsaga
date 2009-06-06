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
 * File:   TimeoutableJobImplTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class TimeoutableJobImplTest extends AbstractTest {
    private static final String m_url = "waitforever://host";
    private Job m_job;

    public TimeoutableJobImplTest() throws Exception {
        super();
    }

    public void setUp() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_url);
        JobService service = JobFactory.createJobService(emptySession, url);
        JobDescription description = JobFactory.createJobDescription();
        description.setAttribute(JobDescription.EXECUTABLE, "/usr/bin/ls");
        m_job = service.createJob(description);
        m_job.run();
    }

    public void tearDown() {
        // do nothing
    }

    public void test_getState() throws Exception {
        try {
            m_job.getState();
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

/*
    public void test_getStdin() throws Exception {
        try {
            m_job.getStdin();
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_getStdout() throws Exception {
        try {
            m_job.getStdout();
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_getStderr() throws Exception {
        try {
            m_job.getStderr();
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_suspend() throws Exception {
        try {
            m_job.suspend();
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_resume() throws Exception {
        try {
            m_job.resume();
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_checkpoint() throws Exception {
        try {
            m_job.checkpoint();
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_migrate() throws Exception {
        try {
            JobDescription description = JobFactory.createJobDescription();
            description.setAttribute(JobDescription.EXECUTABLE, "/usr/bin/ls");
            m_job.migrate(description);
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_signal() throws Exception {
        try {
            m_job.signal(0);
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }
*/
}
