package fr.in2p3.jsaga.impl.job;

import fr.in2p3.jsaga.adaptor.WaitForEverAdaptorAbstract;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ogf.saga.AbstractTest_JUNIT4;
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
public class TimeoutableJobImplTest extends AbstractTest_JUNIT4 {
    private static final String m_url = "waitforever://host";
    private Job m_job;

    public TimeoutableJobImplTest() throws Exception {
        super();
    }

    @Before
    public void setUp() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_url);
        JobService service = JobFactory.createJobService(emptySession, url);
        JobDescription description = JobFactory.createJobDescription();
        description.setAttribute(JobDescription.EXECUTABLE, "/usr/bin/ls");
        description.setAttribute(JobDescription.INTERACTIVE, "true");
        m_job = service.createJob(description);
        m_job.run();
    }

    @After
    public void tearDown() {
        // do nothing
    }

    @Test
    public void test_getState() throws Exception {
        try {
            m_job.getState();
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    @Test
    public void test_getStdin() throws Exception {
        // hang on getState()
        try{m_job.getStdin();} catch(TimeoutException e){}
    }

    @Test
    public void test_getStdout() throws Exception {
        // can not hang...
        m_job.getStdout();
    }

    @Test
    public void test_getStderr() throws Exception {
        // can not hang...
        m_job.getStderr();
    }

    @Test
    public void test_suspend() throws Exception {
        try {
            m_job.suspend();
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    @Test
    public void test_resume() throws Exception {
        try {
            m_job.resume();
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    @Test
    public void test_checkpoint() throws Exception {
        try {
            m_job.checkpoint();
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    @Test
    public void test_signal() throws Exception {
        try {
            m_job.signal(0);
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }
}
