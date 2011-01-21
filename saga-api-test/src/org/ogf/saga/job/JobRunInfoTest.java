package org.ogf.saga.job;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.ogf.saga.job.abstracts.AbstractJobTest;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.State;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobRunInfoTest
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   20 jan 2011
* ***************************************************
* Description: 
* This test suite is made to be sure that the plug-in 
* properly implements the  JobInfoAdaptor interface  */
/**
 *
 */
public abstract class JobRunInfoTest extends AbstractJobTest {
    private Logger logger = Logger.getLogger(this.getClass());
    private static final String FORMAT_DATETOSTRING = "EEE MMM dd HH:mm:ss z yyyy";
   
    protected JobRunInfoTest(String jobprotocol) throws Exception {
        super(jobprotocol);
    }

    /**
     * Runs simple job and expects 0 as return code
     */
    public void test_exitcode() throws Exception {
        
    	// prepare
    	JobDescription desc = createSimpleJob();
    	
        // submit
        Job job = runJob(desc);
        logger.info(job.getAttribute(Job.JOBID));   // for detecting hang in run()

        // wait for the END
        job.waitFor();
        logger.info("Job finished.");               // for detecting hang in waitFor()

        // check exit code
        assertEquals(
                String.valueOf(0),
                job.getAttribute(Job.EXITCODE));
    }

    /**
     * Runs simple job and get Creation date
     */
    public void test_created() throws Exception {
        
    	Date now = new Date();
    	
    	// prepare
    	JobDescription desc = createSimpleJob();
    	
        // submit
        Job job = runJob(desc);
        logger.info(job.getAttribute(Job.JOBID));   // for detecting hang in run()

        // wait for the END
        job.waitFor();
        logger.info("Job finished.");               // for detecting hang in waitFor()

        // check creation date
		try {
	        Date creationTime = parse(job.getAttribute(Job.CREATED));
	        // Compare to now
	        assertTrue(creationTime.after(now));
		} catch (ParseException e) {
			fail(e.getMessage());
		}

    }
    
    /**
     * Runs simple job and get Creation date
     */
    public void test_dates() throws Exception {
        
    	Date now = new Date();
    	
    	// prepare
    	JobDescription desc = createSimpleJob();
    	
        // submit
        Job job = runJob(desc);
        logger.info(job.getAttribute(Job.JOBID));   // for detecting hang in run()

        // wait for the END
        job.waitFor();
        logger.info("Job finished.");               // for detecting hang in waitFor()

        // check creation date
		try {
	        Date startTime = parse(job.getAttribute(Job.STARTED));
	        Date endTime = parse(job.getAttribute(Job.FINISHED));
	        // Compare to now
	        assertTrue(startTime.after(now) && startTime.before(endTime));
		} catch (ParseException e) {
			fail(e.getMessage());
		}

    }
    
    private Date parse(String date) throws ParseException {
		// Parse the CREATED attribute
		// built with Date.toString => "EEE MMM dd HH:mm:ss z yyyy" e.g. "Thu Jan 20 16:26:10 CET 2011"
		// This String has to be parsed in Locale.US
		DateFormat df = new SimpleDateFormat(FORMAT_DATETOSTRING, Locale.US);
        return df.parse(date);
    	
    }
}