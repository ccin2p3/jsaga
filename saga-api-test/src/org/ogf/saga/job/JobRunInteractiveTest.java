package org.ogf.saga.job;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.job.abstracts.AbstractJobTest;
import org.ogf.saga.job.abstracts.AttributeVector;
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
* This test suite is made to be test "pseudo" 
* interactive jobs									 */

public class JobRunInteractiveTest extends AbstractJobTest {
	
	public JobRunInteractiveTest(String jobprotocol) throws Exception {
        super(jobprotocol);
    }

	/**
     * Runs a job,write in stdin, wait for done and check stdout
     */
    public void test_setStdin() throws Exception {
        
    	// prepare interactive job
    	String stringToTestInStdout = "     1\tTest 1";
    	JobDescription desc = createJob("cat", null, null);
    	desc.setAttribute(JobDescription.INTERACTIVE, JobDescription.TRUE);
    	desc.setVectorAttribute(JobDescription.ARGUMENTS, new String[]{"-n"});
    	
        // create
        Job job = createJob(desc);
        
        // get stdin
        OutputStream stdin = job.getStdin();
        stdin.write("Test 1".getBytes());
        stdin.close();
        
        // run
        job.run();
        
        // wait for the end
        job.waitFor();

        // check job for DONE status
        checkStatus(job.getState(), State.DONE);

        // check stdout
    	String stdout = "";
    	BufferedReader jobStdoutReader = new BufferedReader(new InputStreamReader(job.getStdout()));
        String input;
        if(jobStdoutReader != null) {
	        while ((input = jobStdoutReader.readLine()) !=null ){
	        	stdout += input;
	        }
        }
        
    	// check
        assertEquals(
        		stringToTestInStdout,
        		stdout);
    }
    
    /**
     * Runs a job, waits for done and check stdout
     */
    public void test_getStdout() throws Exception {
        
    	// prepare interactive job
    	String stringToTestInStdout = "Test";
    	JobDescription desc = createWriteJob(stringToTestInStdout);
    	desc.setAttribute(JobDescription.INTERACTIVE, JobDescription.TRUE);
    	
        // submit
        Job job = runJob(desc);
        
        // wait for the end
        job.waitFor();

        // check job for DONE status
        checkStatus(job.getState(), State.DONE);

        // check stdout
    	String stdout = "";
    	BufferedReader jobStdoutReader = new BufferedReader(new InputStreamReader(job.getStdout()));
        String input;
        if(jobStdoutReader != null) {
	        while ((input = jobStdoutReader.readLine()) !=null ){
	        	if(input.indexOf(stringToTestInStdout) > -1) {
	        		stdout = input;
	        		break;
	        	}
	        }
        }
        
    	// check
        assertEquals(
        		stringToTestInStdout,
        		stdout);
    }
    
    /**
     * Runs a job, waits for done and check stderr
     */
    public void test_getStderr() throws Exception {
        
    	// prepare interactive job
    	JobDescription desc = createErrorJob();
    	desc.setAttribute(JobDescription.INTERACTIVE, JobDescription.TRUE);
    	
        // submit
        Job job = runJob(desc);

        // wait for the end
        job.waitFor();

        // check job for FAILED status
        checkStatus(job.getState(), State.FAILED);

      	String stderrEmpty = "";
        //test stderr
        BufferedReader jobStdoutReader = new BufferedReader( new InputStreamReader( job.getStderr()));
        String firstLine = jobStdoutReader.readLine();
        if(firstLine != null) {
        	stderrEmpty = firstLine;
        }
        	
        // check
        assertNotSame(
                "",
                stderrEmpty);
    }
	
	
	/**
	 * check the environment in job stdout 
	 * Runs job with requested environment variables
	 */
	public void test_run_environnement() throws Exception {
	    
		// prepare
		String env0 = "MYVAR3=Testing";
		String env1 = "MYVAR1=Testing 1";
		String env2 = "MYVAR2=Testing two";
		AttributeVector[] attributesV = new AttributeVector[1];
		attributesV[0] = new AttributeVector(JobDescription.ENVIRONMENT, new String[]{env0, env1, env2});
		JobDescription desc =  createJob("/bin/env", null, attributesV);
		desc.setAttribute(JobDescription.INTERACTIVE, JobDescription.TRUE);
    	
		// submit
	    Job job = runJob(desc);
	    
	    // wait for the end
	    job.waitFor();  
	    
	    // check stdout
	    boolean containsEnv0 = false, containsEnv1 = false, containsEnv2 = false;
    	BufferedReader jobStdoutReader = new BufferedReader( new InputStreamReader(job.getStdout()));       
	    String input;
	    while ((input = jobStdoutReader.readLine()) !=null ){
        	if(input.indexOf(env0) > -1) {
        		containsEnv0 = true;
        	}
        	if(input.indexOf(env1) > -1) {
        		containsEnv1 = true;
        	}
        	if(input.indexOf(env2) > -1) {
        		containsEnv2 = true;
        	}
        }
	    
    	// check
        assertEquals(
        		true,
        		containsEnv0 && containsEnv1 && containsEnv2);
	}
	
	/**
     * Runs short jobs on the same time with one task container
     */
    public void test_simultaneousStdin() throws Exception {
        
    	int numberOfJobs = Integer.parseInt(SIMULTANEOUS_JOB_NUMBER);

    	TaskContainer taskContainer = TaskFactory.createTaskContainer();
    	    	
    	// create and start jobs
    	JobService m_service = JobFactory.createJobService(m_session, m_jobservice);
    	Job[] jobs = new Job[numberOfJobs];
		for (int index = 0; index < numberOfJobs; index++) {
			// create description
	    	JobDescription desc = JobFactory.createJobDescription();
    		desc.setAttribute(JobDescription.EXECUTABLE, "/bin/echo");
	        desc.setAttribute(JobDescription.INTERACTIVE, JobDescription.TRUE);
	    	
	        jobs[index] = m_service.createJob(desc);
	        // get stdin
	        OutputStream stdin = jobs[index].getStdin();
	        stdin.write(new String("Test "+index).getBytes());
	        stdin.close();
	        
	        // add job to task			
			taskContainer.add(jobs[index]);
		}
    
		// run
		taskContainer.run();
		
		// wait the end 
		taskContainer.waitFor(WaitMode.ALL);
    	
		// get failed jobs
		int numberOfFailed = 0;
		for (int j = 0; j < jobs.length; j++) {
			if(jobs[j].getState().getValue() == State.FAILED.getValue()) {
				numberOfFailed ++;
			}
		}
		
		if(numberOfFailed > 1) 
			throw new NoSuccess(numberOfFailed + "jobs of "+numberOfJobs+" are failed.");
		if(numberOfFailed > 0) 
			throw new NoSuccess(numberOfFailed + "job of "+numberOfJobs+" is failed.");
		
		int numberOfWrongStdout = 0;
		for (int j = 0; j < jobs.length; j++) {
			// check stdout
			String stdout = "";
	    	BufferedReader jobStdoutReader = new BufferedReader(new InputStreamReader(jobs[j].getStdout()));
	        String input;
	        if(jobStdoutReader != null) {
		        while ((input = jobStdoutReader.readLine()) !=null ){
		        	stdout += input;		        	
		        }
	        }
	        
	        if(!stdout.equals("Test "+j)) {
	        	numberOfWrongStdout ++;
	        }
		}
		
    	assertEquals(
	                0,
	                numberOfWrongStdout);
    }
}