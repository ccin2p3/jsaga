package org.ogf.saga.job;

import org.ogf.saga.URL;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.IncorrectURL;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;
import org.ogf.saga.session.Session;


public class StartJob extends Thread {

	private Session m_session;
	private URL m_jobservice;
	private Exception threadException;
	private int index ;
	
	public StartJob(Session m_session, URL m_jobservice, int i) throws Exception {
		this.m_session = m_session;
		this.m_jobservice = m_jobservice;
		this.index = i;
	}
	
	/**
     * Executes the process.
     */
    public void run() {
    	
        try {
        	// prepare
	    	JobDescription desc = JobFactory.createJobDescription();
	    	desc.setAttribute(JobDescription.EXECUTABLE, "/bin/date");
	        desc.setAttribute(JobDescription.OUTPUT, index+"-stdout.txt");
	        desc.setAttribute(JobDescription.ERROR, index+"-stderr.txt");
	        
	        // submit
	        JobService service = JobFactory.createJobService(m_session, m_jobservice);
	        Job job = service.createJob(desc);
	        job.run();
	        
	        // wait the end 
	        job.waitFor();
	        
	        if(!job.getState().toString().equals("DONE")) {
	        	threadException = new NoSuccess("The job number '"+index+" is not DONE :"+job.getState().toString());
	        }
	        	
		} catch (NotImplemented e) {
			threadException = e; 
		} catch (IncorrectState e) {
			threadException = e;
		} catch (Timeout e) {
			threadException = e;
		} catch (NoSuccess e) {
			threadException = e;
		} catch (AuthenticationFailed e) {
			threadException = e;
		} catch (AuthorizationFailed e) {
			threadException = e;
		} catch (PermissionDenied e) {
			threadException = e;
		} catch (BadParameter e) {
			threadException = e;
		} catch (DoesNotExist e) {
			threadException = e;
		} catch (IncorrectURL e) {
			threadException = e;
		}
    }
    
    public Exception getException () {
    	return threadException ;
    }
}
