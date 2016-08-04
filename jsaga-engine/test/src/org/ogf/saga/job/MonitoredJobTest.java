package org.ogf.saga.job;

import org.junit.Test;
import org.ogf.saga.JSAGABaseTest;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.SagaException;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.State;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   MonitoredJobTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************/
public class MonitoredJobTest extends JSAGABaseTest {
    public MonitoredJobTest() throws Exception {
        super();
    }

    @Test(expected = NoSuccessException.class)
    public void getState_ListenIndividualJob_FAILED() throws SagaException {
        Job job = runJob("listen-individual://host");
        assertEquals(State.DONE, job.getState());
        job.waitFor();
    }

    @Test
    public void getState_ListenFilteredJob() throws SagaException {
        Job job = runJob("listen-filtered://host");
        job.waitFor();
        assertEquals(State.DONE, job.getState());
    }

    @Test
    public void getState_ListenIndividualJob() throws SagaException {
        Job job = runJob("listen-individual://host");
        job.waitFor();
        assertEquals(State.DONE, job.getState());
    }

    @Test
    public void getState_QueryFilteredJob() throws SagaException {
        Job job = runJob("query-filtered://host");
        assertEquals(State.DONE, job.getState());
        job.waitFor();
    }

    @Test
    public void getState_QueryIndividualJob() throws SagaException {
        Job job = runJob("query-individual://host");
        assertEquals(State.DONE, job.getState());
        job.waitFor();
    }

    @Test
    public void getState_QueryListJob() throws SagaException {
        Job job = runJob("query-list://host");
        assertEquals(State.DONE, job.getState());
        job.waitFor();
    }

    private static Job runJob(String url) throws SagaException {
        Session emptySession = SessionFactory.createSession(false);
        JobService service = JobFactory.createJobService(emptySession, URLFactory.createURL(url));
        JobDescription description = JobFactory.createJobDescription();
        description.setAttribute(JobDescription.EXECUTABLE, "/usr/bin/ls");
        Job job = service.createJob(description);
        job.run();
        return job;
    }
}
