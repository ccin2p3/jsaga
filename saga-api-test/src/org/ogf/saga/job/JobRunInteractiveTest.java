package org.ogf.saga.job;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.job.abstracts.AbstractJobTest;
import org.ogf.saga.job.abstracts.AttributeVector;
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

public class JobRunInteractiveTest extends AbstractJobTest {
	
	public JobRunInteractiveTest(String jobprotocol) throws Exception {
        super(jobprotocol);
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

        // get stdout
        try {
        	
        	// check stdout
        	boolean containsTest = false;
        	BufferedReader jobStdoutReader = new BufferedReader( new InputStreamReader(job.getStdout()));
            String input;
            System.out.println("Get stdout:");
            while ((input = jobStdoutReader.readLine()) !=null ){
            	System.out.println(input);
            	if(input.indexOf(stringToTestInStdout) > -1) {
            		containsTest = true;
            		break;
            	}
            }
        	// check
	        assertEquals(
	        		containsTest,
	                true);
        }
        catch (NotImplemented notImplemented) {
        	System.err.println("WARNING : "+this.getName()+" not implemented in plugin");
        }
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

        // get stderr
        try {
	        boolean stderrNotEmpty = false;
	        //test inputstream
	        BufferedReader jobStdoutReader = new BufferedReader( new InputStreamReader( job.getStderr()));
	        String firstLine = jobStdoutReader.readLine();
	        if(firstLine != null) {
	        	System.out.println("Stderr "+firstLine);
	        	stderrNotEmpty = true;
	        }
	        	
	        // check
	        assertEquals(
	                true,
	                stderrNotEmpty);
        }
        catch (NotImplemented notImplemented) {
        	System.err.println("WARNING : "+this.getName()+" not implemented in plugin");
        }
    }
	
	
	/**
	 * check the environment in job stdout 
	 * Runs job with requested environment variable
	 */
	public void test_run_environnement() throws Exception {
	    
		// prepare
		String env1 = "MYVAR1=Testing 1";
		String env2 = "MYVAR2=Testing two";
		AttributeVector[] attributesV = new AttributeVector[1];
		attributesV[0] = new AttributeVector(JobDescription.ENVIRONMENT, new String[]{env1, env2});
		JobDescription desc =  createJob("/bin/env", null, attributesV);
		
		// submit
	    Job job = runJob(desc);
	    
	    // wait for the end
	    job.waitFor();  
	    
	    // check stdout
	    boolean containsEnv1 = false, containsEnv2 = false;
    	BufferedReader jobStdoutReader = new BufferedReader( new InputStreamReader(job.getStdout()));       
	    String input;
	    while ((input = jobStdoutReader.readLine()) !=null ){
        	System.out.println(input);
        	if(input.indexOf(env1) > -1) {
        		containsEnv1 = true;
        	}
        	if(input.indexOf(env2) > -1) {
        		containsEnv2 = true;
        	}
        }
    	// check
        assertEquals(
        		containsEnv1 && containsEnv2,
                true);
	}
}