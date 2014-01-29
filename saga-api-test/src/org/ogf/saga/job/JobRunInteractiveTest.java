package org.ogf.saga.job;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.job.abstracts.AbstractJobTest;
import org.ogf.saga.job.abstracts.AttributeVector;
import org.ogf.saga.task.*;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobRunOptionalTest
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   30 janv. 2008
* ***************************************************
* Description: 
* This test suite is made to be test  
* interactive jobs
*/
@Deprecated
public abstract class JobRunInteractiveTest extends AbstractJobTest {
	
	protected JobRunInteractiveTest(String jobprotocol) throws Exception {
        super(jobprotocol);
    }

	/**
     * Runs a job,write in stdin, wait for done and check stdout
     */
    public void test_setStdin() throws Exception {
        
    	// prepare interactive job
    	String stringToTestInStdout = "     1\tTest 1";
    	JobDescription desc = createJob("/bin/cat", null, null);
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
	        	}
	        }
        }
        
    	// check
        assertEquals(
        		stringToTestInStdout,
        		stdout);
    }
    
    /**
     * Runs a job, waits for failed and check stderr
     */
    public void test_getStderr() throws Exception {
        
    	// prepare interactive job
    	AttributeVector[] attributesV = new AttributeVector[1];
    	attributesV[0] = new AttributeVector(JobDescription.ARGUMENTS,new String[]{"-true"});    	
    	JobDescription desc = createJob("/bin/cat", null, attributesV);
    	desc.setAttribute(JobDescription.INTERACTIVE, JobDescription.TRUE);
    	
        // submit
        Job job = runJob(desc);

        // wait for the end
        job.waitFor();

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
		String myvar0="Testing0";
		String myvar1="\"Testing 1\"";
        String[] _myenv = new String[] {
                "MYVAR0="+myvar0,
                "MYVAR1="+myvar1
        };
		AttributeVector[] attributesV = new AttributeVector[2];
		attributesV[0] = new AttributeVector(JobDescription.ENVIRONMENT, _myenv);
    	attributesV[1] = new AttributeVector(JobDescription.ARGUMENTS,new String[]{"$MYVAR0:$MYVAR1"});    	
		JobDescription desc =  createJob("/bin/echo", null, attributesV);
		desc.setAttribute(JobDescription.INTERACTIVE, JobDescription.TRUE);
    	
		// submit
	    Job job = runJob(desc);
	    
	    // wait for the end
	    job.waitFor();  
	    
	    checkStatus(job.getState(), State.DONE);
	    
	    // check stdout
    	BufferedReader jobStdoutReader = new BufferedReader( new InputStreamReader(job.getStdout()));       
	    // check
        assertEquals(
        		myvar0 + ":" + myvar1,
        		jobStdoutReader.readLine());
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
    		desc.setAttribute(JobDescription.EXECUTABLE, "/bin/cat");
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
			throw new NoSuccessException(numberOfFailed + "jobs of "+numberOfJobs+" are failed.");
		if(numberOfFailed > 0) 
			throw new NoSuccessException(numberOfFailed + "job of "+numberOfJobs+" is failed.");
		
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