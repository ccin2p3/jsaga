package org.ogf.saga.job.abstracts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.ogf.saga.AbstractTest;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.Timeout;
import org.ogf.saga.job.Job;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.job.JobFactory;
import org.ogf.saga.job.JobService;
import org.ogf.saga.monitoring.Callback;
import org.ogf.saga.monitoring.Metric;
import org.ogf.saga.monitoring.Monitorable;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.State;
import org.ogf.saga.task.Task;

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
	
	// values
	protected String SIMPLE_JOB_BINARY 			= "simpleJobBinary";
	protected String MAX_QUEUING_TIME 			= "maxQueuingTime";
	protected String LONG_JOB_BINARY  			= "longJobBinary";
	protected String LONG_JOB_DURATION 			= "longJobDuration";
	protected String FINALY_TIMEOUT  			= "finalyTimeout";
	protected String SIMULTANEOUS_JOB_NUMBER	= "simultaneousJobNumber";
		
	// set default values
	private static final String DEFAULT_LONG_JOB_DURATION 			= "30";
	private static final String DEFAULT_FINALY_TIMEOUT 				= "60" ;
	private static final String DEFAULT_SIMPLE_JOB_BINARY 			= "/bin/date" ;
	private static final String DEFAULT_LONG_JOB_BINARY 			= "/bin/sleep" ;
	private static final String DEFAULT_MAX_QUEUING_TIME 			= "60";
	private static final String DEFAULT_SIMULTANEOUS_JOB_NUMBER 	= "5";

    // configuration
    protected URL m_jobservice;
    protected Session m_session;

    public AbstractJobTest(String jobprotocol) throws Exception {
        super();

        // configure
        m_jobservice = URLFactory.createURL(getRequiredProperty(jobprotocol, CONFIG_JOBSERVICE_URL).replaceAll(" ", "%20"));
        m_session = SessionFactory.createSession(true);
        
        // init values
        SIMPLE_JOB_BINARY = super.getOptionalProperty(jobprotocol, SIMPLE_JOB_BINARY, DEFAULT_SIMPLE_JOB_BINARY);
       	LONG_JOB_BINARY = super.getOptionalProperty(jobprotocol, LONG_JOB_BINARY, DEFAULT_LONG_JOB_BINARY);
       	LONG_JOB_DURATION = super.getOptionalProperty(jobprotocol, LONG_JOB_DURATION, DEFAULT_LONG_JOB_DURATION);	
        FINALY_TIMEOUT = super.getOptionalProperty(jobprotocol, FINALY_TIMEOUT, DEFAULT_FINALY_TIMEOUT);
        MAX_QUEUING_TIME = super.getOptionalProperty(jobprotocol, MAX_QUEUING_TIME, DEFAULT_MAX_QUEUING_TIME);
        SIMULTANEOUS_JOB_NUMBER = super.getOptionalProperty(jobprotocol, SIMULTANEOUS_JOB_NUMBER, DEFAULT_SIMULTANEOUS_JOB_NUMBER);       	
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
        return job;
    }
    
    /**
     * Creates a new job description
     * @param executable A string with the executable path
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
    	return createJob(SIMPLE_JOB_BINARY, null, null);
    }
    
    
    /**
     * Job which write 'Test' on stdout
     * @param textToPrint The string to print in stdout
     * @return The job description
     * @throws Exception
     */
    protected JobDescription createWriteJob(String textToPrint) throws Exception {
    	AttributeVector[] attributesV = new AttributeVector[1];
    	attributesV[0] = new AttributeVector(JobDescription.ARGUMENTS,new String[]{textToPrint});    	
    	return createJob("/bin/echo", null, attributesV);
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
    protected JobDescription createLongJob() throws Exception {
    	AttributeVector[] attributesV = new AttributeVector[1];
    	attributesV[0] = new AttributeVector(JobDescription.ARGUMENTS, new String[]{LONG_JOB_DURATION});
    	return createJob(LONG_JOB_BINARY, null, attributesV);
    }

    /**
     * Check if the required status is the job status
     * @param jobState the job status
     * @param wantedStatus The required status
     * @throws Exception
     */
    protected void checkStatus(State jobState, State wantedStatus) throws Exception {
    	if(jobState != wantedStatus) {
        	fail("Invalid status "+jobState+": must be "+wantedStatus+".");
        }
	}    
    
    protected void printCurrentDate() {
    	DateFormat df = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
        System.out.println(df.format(new Date()));
	}

    protected void printStatus(Job job) throws NotImplemented, Timeout, NoSuccess {
		System.out.println("Status: "+job.getState());
	}


    private String m_subState;
    protected boolean waitForSubState(Job job, String subState) throws Exception {
    	float timeoutInSeconds = Float.valueOf(MAX_QUEUING_TIME);
    	int cookie = job.addCallback("job.sub_state", new Callback(){
            public boolean cb(Monitorable mt, Metric metric, Context ctx) throws NotImplemented, AuthorizationFailed {
                try {
                	m_subState = metric.getAttribute(Metric.VALUE);
                } catch (Exception e) {
                    throw new NotImplemented(e);
                }
                return true;
            }
        });
    	m_subState = job.getMetric("job.sub_state").getAttribute(Metric.VALUE);        
        try {
            boolean forever;
            long endTime;
            if (timeoutInSeconds == Task.WAIT_FOREVER) {
                forever = true;
                endTime = -1;
            } else if (timeoutInSeconds == Task.NO_WAIT) {
                forever = false;
                endTime = -1;
            } else {
                forever = false;
                endTime = System.currentTimeMillis() + (long) timeoutInSeconds*1000;
            }
            while(!this.isEndedOrSubState(subState) && (forever || System.currentTimeMillis()<endTime)) {
            	Thread.currentThread().sleep(100);
            }
        } catch (InterruptedException e) {/*ignore*/}
        job.removeCallback("job.sub_state", cookie);
        return this.isEndedOrSubState(subState);
    }
    
    private boolean isEndedOrSubState( String subState) {
    	return m_subState.equals(subState) ||
    		m_subState.equals("CANCELED") ||
    		m_subState.equals("DONE") ||    		
    		m_subState.equals("FAILED_ERROR") ||
    		m_subState.equals("FAILED_ABORTED");
    }
}
