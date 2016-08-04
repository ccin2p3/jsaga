package org.ogf.saga.job;

import fr.in2p3.jsaga.adaptor.WaitForEverAdaptorAbstract;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ogf.saga.JSAGABaseTest;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   TimeoutableJobFactoryTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   5 juin 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class TimeoutableJobFactoryTest extends JSAGABaseTest {
    private static final String m_url = "waitforever://host";

    public TimeoutableJobFactoryTest() throws Exception {
        super();
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test_createJobService() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_url+"?hangatconnect");
        try {
            JobFactory.createJobService(emptySession, url);
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    @Test
    public void test_invalidAttribute() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_url+"?invalid");
        thrown.expect(BadParameterException.class);
        thrown.expectMessage("Invalid");
        JobFactory.createJobService(emptySession, url);
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
