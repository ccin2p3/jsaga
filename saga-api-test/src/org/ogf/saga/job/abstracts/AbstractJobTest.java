package org.ogf.saga.job.abstracts;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.ogf.saga.AbstractTest;
import org.ogf.saga.URL;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.Timeout;
import org.ogf.saga.job.Job;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.job.JobFactory;
import org.ogf.saga.job.JobService;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.State;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractJobTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 nov. 2007
* ***************************************************
* Description:                                      */

public class AbstractJobTest extends AbstractTest {
	
	// set default values
	protected int DEFAULT_JOB_DURATION = 30 ;
	protected int DEFAULT_FINALY_TIMEOUT = 60 ;
	protected String DEFAULT_SIMPLE_JOB_BINARY = "/bin/date" ;
	protected String DEFAULT_LONG_JOB_BINARY = "/bin/sleep" ;

    // configuration
    protected URL m_jobservice;
    protected Session m_session;

    public AbstractJobTest(String jobprotocol) throws Exception {
        super();

        // configure
        m_jobservice = new URL(getRequiredProperty(jobprotocol, CONFIG_JOBSERVICE_URL).replaceAll(" ", "%20"));
        if (m_jobservice.getFragment() != null) {
            m_session = SessionFactory.createSession(true);
        }
    }
    
    /**
     * Creates a new job
     * @param desc The job description
     * @return The new job
     * @throws Exception
     */
    protected Job createJob(JobDescription desc) throws Exception  {
        JobService service = JobFactory.createJobService(m_session, m_jobservice);
        Job job = service.createJob(desc);
        return job;
    }
    
    /**
     * Runs a job
     * @param desc The job description
     * @return The running job
     * @throws Exception
     */
    protected Job runJob(JobDescription desc) throws Exception  {
        Job job = createJob(desc);
        job.run();
        //System.out.println("Job Id :"+job.getAttribute(Job.JOBID));
        return job;
    }
    
    /**
     * Creates a new job description
     * @param executable A string with the executable path
     * @param arguments A string array with the executable arguments
     * @param attributes A string array with the job attributes
     * @return The job description
     * @throws Exception
     */
    protected JobDescription createJob(String executable, Attribute[] attributes, AttributeVector[] attributesVector) throws Exception {
    	// prepare
        JobDescription desc = JobFactory.createJobDescription();
        desc.setAttribute(JobDescription.EXECUTABLE, executable);
        desc.setAttribute(JobDescription.OUTPUT, "stdout.txt");
        desc.setAttribute(JobDescription.ERROR, "stderr.txt");
        if(attributes != null) {
        	for (int i = 0; i < attributes.length; i++) {
        		desc.setAttribute(attributes[i].getKey(), attributes[i].getValue());
			}
        }
        if(attributesVector != null) {
        	for (int i = 0; i < attributesVector.length; i++) {
        		desc.setVectorAttribute(attributesVector[i].getKey(), attributesVector[i].getValue());
			}
        }
        return desc;
    }

    /**
     *  Very simple job which prints the execution date
     * @return The job description
     * @throws Exception
     */
    protected JobDescription createSimpleJob() throws Exception {
    	return createJob(DEFAULT_SIMPLE_JOB_BINARY, null, null);
    }
    
    /**
     * Job which write 'Test' on stdout
     * @param textToPrint The string to print in stdout
     * @return The job description
     * @throws Exception
     */
    protected JobDescription createWriteJob(String textToPrint) throws Exception {
    	AttributeVector[] attributes = new AttributeVector[1];
    	attributes[0] = new AttributeVector(JobDescription.ARGUMENTS,new String[]{textToPrint});    	
    	return createJob("/bin/echo", null, attributes);
    }
    
    /**
     * Job which generate error like 'Command not found' on stderr
     * @return The job description
     * @throws Exception
     */
    protected JobDescription createErrorJob() throws Exception {
    	return createJob("/bin/command-error", null, null);
    }
    
    /**
     * Long job which sleeps 30 seconds
     * @return The job description
     * @throws Exception
     */
    protected JobDescription createLongJob(int duration) throws Exception {
    	AttributeVector[] attributes = new AttributeVector[1];
    	attributes[0] = new AttributeVector(JobDescription.ARGUMENTS, new String[]{String.valueOf(duration)});
    	return createJob(DEFAULT_LONG_JOB_BINARY, null, attributes);
    }

    /**
     * Check if the required status is the job status
     * @param jobState the job status
     * @param wantedStatus The required status
     * @throws Exception
     */
    protected void checkStatus(State jobState, State wantedStatus) throws Exception {
    	if(jobState != wantedStatus) {
        	throw new Exception("Invalid status "+jobState+": must be "+wantedStatus+".");
        }
	}
    
    /**
     * Checks if the job output contains a string
     * @param stringToTestInEnv The string to check in job output
     * @return True is the job output contains the string
     * @throws IOException
     */
    protected boolean containStringInOutput(String stringToTestInOutput) throws NotImplemented {
    	throw new NotImplemented();
	}
    
    protected void printCurrentDate() {
    	DateFormat df = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
        System.out.println(df.format(new Date()));
	}

    protected void printStatus(Job job) throws NotImplemented, Timeout, NoSuccess {
		System.out.println("Status: "+job.getState());
	}

}
