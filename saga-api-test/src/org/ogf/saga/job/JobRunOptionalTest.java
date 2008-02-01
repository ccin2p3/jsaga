package org.ogf.saga.job;

import java.util.List;

import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.job.abstracts.AbstractJobTest;
import org.ogf.saga.task.State;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobRunOptionalTest
* Author: Nicolas Demesy (nicolas.demesy@bt.com)
* Date:   30 janv. 2008
* ***************************************************
* Description: 
* This test suite is made to be test advanced functions
* 											          */

public class JobRunOptionalTest extends AbstractJobTest {
	
	public JobRunOptionalTest(String jobprotocol) throws Exception {
        super(jobprotocol);
    }

	/**
     * Runs a long job, waits for running state and suspends it
     */
    public void test_suspend_running() throws Exception {
        
    	// prepare
    	JobDescription desc = createLongJob(DEFAULT_JOB_DURATION);
    	
        // submit
        Job job = runJob(desc);
        
        // wait for RUNNING state or TODO for 10 seconds
        job.waitFor(10);
        
        // check job for RUNNING status
        checkStatus(job.getState(), State.RUNNING);
        
        try {
        	
	        // suspend job
	        job.suspend();
	        
	        // wait for 1 second because suspend is an asynchronous method
	        job.waitFor(1);
	        
	        // check job status
	        assertEquals(
	                State.SUSPENDED,
	                job.getState());
        }
        catch (NotImplemented notImplemented) {
        	System.err.println("WARNING : "+this.getName()+" not implemented in plugin");
        }
        finally {
        	job.waitFor(DEFAULT_FINALY_TIMEOUT);
        }
    }
    
    /**
     * Runs a long job, waits for running state and suspends it
     */
    public void test_suspend_done() throws Exception {
        
    	// prepare
    	JobDescription desc = createSimpleJob();
    	
        // submit
        Job job = runJob(desc);
        
        // wait for the END
        job.waitFor();
        
        // check job for DONE status
        checkStatus(job.getState(), State.DONE);
        
        try {
            // suspend job
            job.suspend();
            fail("Expected IncorrectState exception");
        }
        catch (IncorrectState incorrectState) {
        }
        catch (NotImplemented notImplemented) {
        	System.err.println("WARNING : "+this.getName()+" not implemented in plugin");
        }
        finally {
        	job.waitFor(DEFAULT_FINALY_TIMEOUT);
        }
    }
    
    /**
     * Runs a long job, waits for running state, suspends it and resume it.
     */
    public void test_resume_running() throws Exception {
        
    	// prepare
    	JobDescription desc = createLongJob(DEFAULT_JOB_DURATION);
    	
        // submit
        Job job = runJob(desc);
        
        // wait for RUNNING state or TODO for 10 seconds
        job.waitFor(10);
        
        // check job for RUNNING status
        checkStatus(job.getState(), State.RUNNING);
        
        // resume job
        job.suspend();
        
        // wait for SUSPENDED state or TODO for 5 seconds
        job.waitFor(5);
        
        // check job for RUNNING status
        checkStatus(job.getState(), State.SUSPENDED);
        
        try {
	        // resume job
	        job.resume();
	        
	        // wait for 1 second
	        job.waitFor(1);
	        
	        // check job status
	        assertEquals(
	                State.RUNNING,
	                job.getState());
        }
        catch (IncorrectState incorrectState) {
        }
        catch (NotImplemented notImplemented) {
        	System.err.println("WARNING : "+this.getName()+" not implemented in plugin");
        }
        finally {
        	job.waitFor(DEFAULT_FINALY_TIMEOUT);
        }
    }
    
    /**
     * Runs a long job, waits for done state and resumes it
     */
    public void test_resume_done() throws Exception {
        
    	// prepare
    	JobDescription desc = createSimpleJob();
    	
        // submit
        Job job = runJob(desc);
        
        // wait the end
        job.waitFor();
        
        // check job for DONE status
        checkStatus(job.getState(), State.DONE);
        
        try {
            // resume job
            job.resume();
            fail("Expected IncorrectState exception");
        }
        catch (IncorrectState incorrectState) {
        }
        catch (NotImplemented notImplemented) {
        	System.err.println("WARNING : "+this.getName()+" not implemented in plugin");
        }
        finally {
        	job.waitFor(DEFAULT_FINALY_TIMEOUT);
        }
    }
    
	/**
     * Runs a long job, list available jobs and check if the running job is in the resource manager list
     */
    public void test_listJob() throws Exception {
        
    	// prepare
    	JobDescription desc = createLongJob(DEFAULT_JOB_DURATION);
    	
        // submit
        Job job = runJob(desc);

        // get jobs
        try {
	        JobService service = JobFactory.createJobService(m_session, m_jobservice);
	        List<String> jobList = service.list();
	        // test if the running job is in the job list
	        boolean jobIsInList = false;
	        if(jobList.contains(job.getId()))
	        	jobIsInList = true;
	        
	        assertEquals(
	                true,
	                jobIsInList);
        }
        catch (NotImplemented notImplemented) {
        	System.err.println("WARNING : "+this.getName()+" not implemented in plugin");
        }
        finally {
        	job.waitFor(DEFAULT_FINALY_TIMEOUT);
        }
    }
}