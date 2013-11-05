package org.ogf.saga.job.run;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.ogf.saga.job.Job;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.job.base.JobBaseTest;
import org.ogf.saga.task.State;

public class MinimalTest extends JobBaseTest {
	    private Logger logger = Logger.getLogger(this.getClass());
	    
	    public MinimalTest(String jobprotocol) throws Exception {
	        super(jobprotocol);
	    }

	    /**
	     * Runs simple job and expects done status
	     */
	    @Test
	    public void test_run() throws Exception {
	        
	    	// prepare
	    	JobDescription desc = createSimpleJob();
	    	
	        // submit
	        Job job = runJob(desc);
	        logger.info(job.getAttribute(Job.JOBID));   // for detecting hang in run()

	        // wait for the END
	        job.waitFor();
	        logger.info("Job finished.");               // for detecting hang in waitFor()

	        // check job status
	        Assert.assertEquals(
	                State.DONE,
	                job.getState());
	    }
	
	
}
