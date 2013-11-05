package org.ogf.saga.job.run;

import org.junit.Assert;
import org.junit.Test;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.job.Job;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.job.abstracts.AbstractJobTest;
import org.ogf.saga.job.base.JobBaseTest;
import org.ogf.saga.task.State;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobRunRequiredTest
* Author: Nicolas Demesy (nicolas.demesy@bt.com)
* Date:   30 janv. 2008
* ***************************************************
* Description: 
* This test suite is made to be sure that the plug-in 
* has the minimum functions                         */
/**
 *
 */
public abstract class RequiredTest extends JobBaseTest {
    
    protected RequiredTest(String jobprotocol) throws Exception {
        super(jobprotocol);
    }

    /*
     * Runs long job and expects done status
     */
    @Test
    public void test_run_long() throws Exception {
        
    	// prepare
    	JobDescription desc = createLongJob();
    	
        // submit
        Job job = runJob(desc);

        // wait for the END
        job.waitFor();

        // check job status
        Assert.assertEquals(
                State.DONE,
                job.getState());
    }

	/*
     * Runs simple job and expects failed status
     */
    @Test
    public void test_run_error() throws Exception {
        
    	// prepare
    	JobDescription desc = createErrorJob();
    	
        // submit
        Job job = runJob(desc);
        
        // wait for the end
        job.waitFor();
        
        // check job status
        Assert.assertEquals(
                State.FAILED,
                job.getState());
    }
	
    /*
     * Runs a long job, waits for running state and cancels it
     */
    @Test
    public void test_cancel_running() throws Exception {
        
    	// prepare
    	JobDescription desc = createLongJob();
    	
        // submit
        Job job = runJob(desc);
        
        // wait for RUNNING Jsaga substate
        if (! super.waitForSubState(job, MODEL+":RUNNING_ACTIVE")) {
        	job.waitFor(Float.valueOf(MAX_QUEUING_TIME));
            Assert.fail("Job did not enter RUNNING_ACTIVE state within "+MAX_QUEUING_TIME+" seconds");
        }
        
        try {
	        // cancel job
	        job.cancel();
	        
	        // wait
	        job.waitFor(Float.valueOf(FINALY_TIMEOUT));
	        
	        // check job status
	        Assert.assertEquals(
	                State.CANCELED,
	                job.getState());
        }
        catch (NotImplementedException notImplemented) {
        	Assert.fail("WARNING : CANCEL not implemented in plugin");
        }
        finally {
        	job.waitFor(Float.valueOf(FINALY_TIMEOUT));
        }
    }
    
    /*
     * Create a new job cancels it and expects exception
     */
    @Test(expected=IncorrectStateException.class)
    public void test_cancel_new() throws Exception {
        
    	// prepare
    	JobDescription desc = createSimpleJob();
    	
        // submit
        Job job = createJob(desc);

        // check job for NEW status
        checkStatus(job.getState(), State.NEW);

        // cancel job
        job.cancel();
        
        // wait for 2 seconds because cancel is an asynchronous method
        Thread.sleep(2000);
        
    }
    
    /*
     * Runs a simple job, waits for done state, cancels it and expects exception
     */
    @Test(expected=IncorrectStateException.class)
    public void test_cancel_done() throws Exception {
        
    	// prepare
    	JobDescription desc = createSimpleJob();
    	
        // submit
        Job job = runJob(desc);

        // wait the end
        job.waitFor();

        // check job for DONE status
        checkStatus(job.getState(), State.DONE);

        // cancel job
        job.cancel();
        
        // wait 2 seconds because cancel is an asynchronous method
        Thread.sleep(2000);
        
        // check job for DONE status
        checkStatus(job.getState(), State.DONE);
    }
}