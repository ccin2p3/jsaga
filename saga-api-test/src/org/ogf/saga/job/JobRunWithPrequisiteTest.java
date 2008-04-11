package org.ogf.saga.job;

import org.ogf.saga.job.abstracts.AbstractJobTest;
import org.ogf.saga.job.abstracts.Attribute;
import org.ogf.saga.job.abstracts.AttributeVector;
import org.ogf.saga.task.State;

/* ***************************************************
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

public class JobRunWithPrequisiteTest extends AbstractJobTest {
	
		
	public JobRunWithPrequisiteTest(String jobprotocol) throws Exception {
        super(jobprotocol);
    }

	/** 
	 * Pre-Requisite : add or copy the binary helloMpi in $HOME
     * Runs MPI job and expects done status
     */
    public void test_run_MPI() throws Exception {
        
    	// prepare a job start a mpi binary
    	Attribute[] attributes = new Attribute[3];
    	attributes[0] = new Attribute(JobDescription.SPMDVARIATION, "MPI");
    	attributes[1] = new Attribute(JobDescription.NUMBEROFPROCESSES, "2");
    	attributes[2] = new Attribute(JobDescription.PROCESSESPERHOST, "2");
    	JobDescription desc =  createJob("helloMpi", attributes, null);
    	
    	// submit
        Job job = runJob(desc);
        
        // wait for the end
        job.waitFor();  
        
        // check job status
        assertEquals(
                State.DONE,
                job.getState());       
    }
    
    /*
    >> more hello.c
    #include <stdio.h>
    #include <mpi.h>

    int main(int argc, char *argv[]) {
      int numprocs, rank, namelen;
      char processor_name[MPI_MAX_PROCESSOR_NAME];

      MPI_Init(&argc, &argv);
      MPI_Comm_size(MPI_COMM_WORLD, &numprocs);
      MPI_Comm_rank(MPI_COMM_WORLD, &rank);
      MPI_Get_processor_name(processor_name, &namelen);

      printf("Process %d on %s out of %d\n", rank, processor_name, numprocs);

      MPI_Finalize();
    }
    
    >> compile : mpicc hello.c -o helloMpi
    */
    

    /**
     * Post-Requisite : check the environment in job stdout 
     * Runs job with requested environment variable
     */
    public void test_run_environnement() throws Exception {
        
    	// prepare
    	String key1 = "MYVAR1", value1="Testing1";
    	String key2 = "MYVAR2", value2="Testing2";
    	AttributeVector[] attributesV = new AttributeVector[1];
    	attributesV[0] = new AttributeVector(JobDescription.ENVIRONMENT, new String[]{key1+"="+value1, key2+"="+value2 });
    	JobDescription desc =  createJob("/bin/env", null, attributesV);
    	
    	// submit
        Job job = runJob(desc);
        
        // wait for the end
        job.waitFor();       

        // check job for DONE status
        assertEquals(
    		job.getState(),
    		State.DONE);
    }
}