package org.ogf.saga.job;

import java.util.List;

import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.job.abstracts.AbstractJobTest;
import org.ogf.saga.task.State;
import org.ogf.saga.task.TaskContainer;
import org.ogf.saga.task.TaskFactory;
import org.ogf.saga.task.WaitMode;

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
    	JobDescription desc = createLongJob();
    	
        // submit
        Job job = runJob(desc);
        
        // wait for RUNNING state (deviation from SAGA specification)
        if (! super.waitForSubState(job, "RUNNING_ACTIVE")) {
        	job.waitFor(Float.valueOf(FINALY_TIMEOUT));
            fail("Job did not enter RUNNING_ACTIVE state within "+MAX_QUEUING_TIME+" seconds");
        }
        
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
        finally {
        	job.waitFor(Float.valueOf(FINALY_TIMEOUT));
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
        finally {
        	job.waitFor(Float.valueOf(FINALY_TIMEOUT));
        }
    }
    
    /**
     * Runs a long job, waits for running state, suspends it and resume it.
     */
    public void test_resume_running() throws Exception {
        
    	// prepare
    	JobDescription desc = createLongJob();
    	
        // submit
        Job job = runJob(desc);
        
       // wait for RUNNING state (deviation from SAGA specification)
        if (! super.waitForSubState(job, "RUNNING_ACTIVE")) {
        	job.waitFor(Float.valueOf(FINALY_TIMEOUT));
            fail("Job did not enter RUNNING_ACTIVE state within "+MAX_QUEUING_TIME+" seconds");
        }
        
        try {
        	// suspend job
        	job.suspend();
        }
        catch (NotImplemented notImplemented) {
        	job.waitFor(Float.valueOf(FINALY_TIMEOUT));
        	throw notImplemented;
        }
        
        // wait for SUSPENDED state
        if (! super.waitForSubState(job, "SUSPENDED")) {
        	job.waitFor(Float.valueOf(FINALY_TIMEOUT));
            fail("Job did not enter SUSPENDED state within "+MAX_QUEUING_TIME+" seconds");
        }
        
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
        finally {
        	job.waitFor(Float.valueOf(FINALY_TIMEOUT));
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
        finally {
        	job.waitFor(Float.valueOf(FINALY_TIMEOUT));
        }
    }
    
	/**
     * Runs a long job, list available jobs and check if the running job is in the resource manager list
     */
    public void test_listJob() throws Exception {
        
    	// prepare
    	JobDescription desc = createLongJob();
    	
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
        finally {
        	job.waitFor(Float.valueOf(FINALY_TIMEOUT));
        }
    }
    
	/**
     * Runs long jobs on the same time with one job service
     */
    public void test_simultaneousLongJob() throws Exception {
        
    	int numberOfJobs = Integer.parseInt(SIMULTANEOUS_JOB_NUMBER);

		// jobs
    	StartJob[] newJob = new StartJob[numberOfJobs];
    	
    	// create and start jobs
    	JobService m_service = JobFactory.createJobService(m_session, m_jobservice); 
		for (int i = 0; i < numberOfJobs; i++) {
        	newJob[i] = new StartJob(m_service, i, true);
    		newJob[i].start();
		}
    
    	for (int i = 0; i < numberOfJobs; i++) {
    		newJob[i].join();
		}
    	
    	// get job exception
		int numberOfFailed = 0;
		for (int i = 0; i < numberOfJobs; i++) {
    		if(newJob[i].getException() != null) {
    			numberOfFailed ++;
			}
		}
		if(numberOfFailed > 1) 
			throw new NoSuccess(numberOfFailed + " jobs of "+numberOfJobs+" are failed.");
		if(numberOfFailed > 0) 
			throw new NoSuccess(numberOfFailed + " job of "+numberOfJobs+" is failed.");

    }

	/**
     * Runs short jobs on the same time with one job service
     */
    public void test_simultaneousShortJob() throws Exception {
        
    	int numberOfJobs = Integer.parseInt(SIMULTANEOUS_JOB_NUMBER);

		// jobs
    	StartJob[] newJob = new StartJob[numberOfJobs];
    	
    	// create and start jobs
    	JobService m_service = JobFactory.createJobService(m_session, m_jobservice); 
		for (int i = 0; i < numberOfJobs; i++) {
        	newJob[i] = new StartJob(m_service, i, false);
    		newJob[i].start();
		}
    
    	for (int i = 0; i < numberOfJobs; i++) {
    		newJob[i].join();
		}
    	
    	// get job exception
		int numberOfFailed = 0;
		for (int i = 0; i < numberOfJobs; i++) {
    		if(newJob[i].getException() != null) {
    			numberOfFailed ++;
			}
		}
		if(numberOfFailed > 1) 
			throw new NoSuccess(numberOfFailed + " jobs of "+numberOfJobs+" are failed.");
		if(numberOfFailed > 0) 
			throw new NoSuccess(numberOfFailed + " job of "+numberOfJobs+" is failed.");
		
    	assertEquals(
	                0,
	                numberOfFailed);
    }
    
    /**
     * Runs short jobs on the same time with one task container
     */
    public void test_TaskContainer_ShortJob() throws Exception {
        
    	int numberOfJobs = Integer.parseInt(SIMULTANEOUS_JOB_NUMBER);

    	TaskContainer taskContainer = TaskFactory.createTaskContainer();
    	    	
    	// create and start jobs
    	JobService m_service = JobFactory.createJobService(m_session, m_jobservice);
    	Job[] jobs = new Job[numberOfJobs];		
		for (int index = 0; index < numberOfJobs; index++) {
			// create description
	    	JobDescription desc = JobFactory.createJobDescription();
    		desc.setAttribute(JobDescription.EXECUTABLE, "/bin/date");
	        desc.setAttribute(JobDescription.OUTPUT, index+"-stdout.txt");
	        desc.setAttribute(JobDescription.ERROR, index+"-stderr.txt");
	        // add job to task
	        jobs[index] = m_service.createJob(desc);
			taskContainer.add(jobs[index]);
		}
    
		// run
		taskContainer.run();
		
		// wait the end 
		taskContainer.waitFor(WaitMode.ALL);
    	
		// get failed jobs
		int numberOfFailed = 0;
		for (int index = 0; index < numberOfJobs; index++) {
			if(jobs[index].getState().getValue() == State.FAILED.getValue()) {
				numberOfFailed ++;
			}
		}
		
		if(numberOfFailed > 1) 
			throw new NoSuccess(numberOfFailed + " jobs of "+numberOfJobs+" are failed.");
		if(numberOfFailed > 0) 
			throw new NoSuccess(numberOfFailed + " job of "+numberOfJobs+" is failed.");
		
    	assertEquals(
	                0,
	                numberOfFailed);
    }
}