package org.ogf.saga.job;

import org.ogf.saga.job.abstracts.AbstractJobTest;
import org.ogf.saga.task.State;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobRunMinimalTest
* Author: Nicolas Demesy (nicolas.demesy@bt.com)
* Date:   30 janv. 2008
* ***************************************************
* Description: 
* This test suite is made to be sure that the plug-in 
* can run the minimal job                         */
/**
 *
 */
public class JobRunMinimalTest extends AbstractJobTest {
    
    public JobRunMinimalTest(String jobprotocol) throws Exception {
        super(jobprotocol);
    }

    /**
     * Runs simple job and expects done status
     */
    public void test_run() throws Exception {
        
    	// prepare
    	JobDescription desc = createSimpleJob();
    	
        // submit
        Job job = runJob(desc);

        // wait for the END
        job.waitFor();

        // check job status
        assertEquals(
                State.DONE,
                job.getState());
    }
}