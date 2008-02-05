package org.ogf.saga.job;

import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.job.abstracts.AbstractJobTest;
import org.ogf.saga.job.abstracts.Attribute;
import org.ogf.saga.job.abstracts.AttributeVector;
import org.ogf.saga.task.State;

/* **************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobRunDescriptionTest
* Author: Nicolas Demesy (nicolas.demesy@bt.com)
* Date:   30 janv. 2008
* ***************************************************
* Description: 
* This test suite is made to be sure that the plug-in 
* transmits well the requested attributes          */

public class JobRunDescriptionTest extends AbstractJobTest {
	
	public JobRunDescriptionTest(String jobprotocol) throws Exception {
        super(jobprotocol);
    }

    /**
     * Runs job on a requested working directory
     */
    public void test_run_inWorkingDirectory() throws Exception {
        
    	// prepare
    	Attribute[] attributes = new Attribute[1];
    	attributes[0] = new Attribute(JobDescription.WORKINGDIRECTORY, "/dummy");
    	JobDescription desc = createJob("/bin/pwd", attributes, null);
    	
    	Job job = null;
    	try {
	    	// submit
	        job = runJob(desc);
	        fail("Expected NoSuccess exception");
        }
        catch (NoSuccess noSuccess) {
        }
        finally {
        	if(job != null) {
        		job.waitFor(DEFAULT_FINALY_TIMEOUT);
        	}
        }        
    }
    
   
    /**
     * Runs job, requests an impossible queue and expects FAILED status
     */
    public void test_run_queueRequirement() throws Exception {
        
    	// prepare a simple job
    	JobDescription desc = createSimpleJob();
    	// ask for a impossible queue 
    	desc.setAttribute(JobDescription.QUEUE, "impossible_azerty");
    	
    	Job job = null;
    	try {
	    	// submit
	        job = runJob(desc);
	        fail("Expected NoSuccess exception");
        }
        catch (NoSuccess noSuccess) {
        }
        finally {
        	if(job != null) {
        		job.waitFor(DEFAULT_FINALY_TIMEOUT);
        	}
        }
    }
    
    /**
     * Runs job, estimated an impossible job duration and expects FAILED status
     */
    public void test_run_cpuTimeRequirement() throws Exception {
        
    	// prepare a simple job
    	JobDescription desc = createSimpleJob();
    	// and inform the scheduler to the estimate time is 14 days
    	desc.setAttribute(JobDescription.TOTALCPUTIME, String.valueOf(DEFAULT_JOB_DURATION*2*60*24*14));
    	
    	// submit
        Job job = runJob(desc);
        
        // wait the end
        job.waitFor();  
        
        // check job status
        assertEquals(
                State.FAILED,
                job.getState());       
    }
    
    /**
     * Runs simple job, requests impossible quantity of memory and expects failed status
     */
    public void test_run_memoryRequirement() throws Exception {
        
    	// prepare a job witch requires 250 Gb of RAM
    	Attribute[] attributes = new Attribute[1];
    	attributes[0] = new Attribute(JobDescription.TOTALPHYSICALMEMORY, "2500000000");
    	AttributeVector[] attributesV = new AttributeVector[1];
    	attributesV[0] = new AttributeVector(JobDescription.ARGUMENTS, new String[]{"30"});    
    	JobDescription desc =  createJob(DEFAULT_LONG_JOB_BINARY, attributes, attributesV);
    	    	
    	// submit
        Job job = runJob(desc);
        
        // wait for the end
        job.waitFor();  
        
        // check job status
        assertEquals(
                State.FAILED,
                job.getState());       
    }
        
    /**
     * Runs simple job and expects done status
     */
    public void test_run_processRequirement() throws Exception {
        
    	// prepare a job witch requires 1 million of nodes
    	Attribute[] attributes = new Attribute[2];
    	attributes[0] = new Attribute(JobDescription.NUMBEROFPROCESSES, "1000000");
    	attributes[1] = new Attribute(JobDescription.PROCESSESPERHOST, "1");
    	JobDescription desc =  createJob(DEFAULT_SIMPLE_JOB_BINARY, attributes, null);
    	
    	// submit
        Job job = runJob(desc);
        
        // wait for the end
        job.waitFor();  
        
        // check job status
        assertEquals(
                State.FAILED,
                job.getState());
    }

}