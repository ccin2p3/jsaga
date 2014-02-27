package fr.in2p3.jsaga.impl.job;

import fr.in2p3.jsaga.adaptor.WaitForEverAdaptorAbstract;

import org.junit.Test;
import org.ogf.saga.JSAGABaseTest;
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
 * File:   TimeoutableJobFactoryImplTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   5 juin 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class TimeoutableJobFactoryImplTest extends JSAGABaseTest {
    private static final String m_url = "waitforever://host";

    public TimeoutableJobFactoryImplTest() throws Exception {
        super();
    }

    @Test
    public void test_createJobService() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_url+"?hangatconnect");
        try {
            JobFactoryImpl.createJobService(emptySession, url);
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    @Test
    public void test_run() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_url);
        JobService service = JobFactory.createJobService(emptySession, url);
        JobDescription description = JobFactory.createJobDescription();
        description.setAttribute(JobDescription.EXECUTABLE, "hangatconnect");
        Job job = service.createJob(description);
        try {
            job.run();
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }
}
