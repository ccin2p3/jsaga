package org.ogf.saga.job;

import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;


public class StartJob extends Thread {

	private JobService service ;
	private Exception threadException;
	private int index ;
	private  boolean isLong;
		
	public StartJob(JobService m_jobservice, int i, boolean isLong) throws Exception {
		this.service = m_jobservice;
		this.index = i;
		this.isLong = isLong;
	}
	
	/**
     * Executes the process.
     */
    public void run() {
    	
        try {
        	// prepare
	    	JobDescription desc = JobFactory.createJobDescription();
	    	if(isLong) {
	    		desc.setAttribute(JobDescription.EXECUTABLE, "/bin/sleep");
	    		desc.setVectorAttribute(JobDescription.ARGUMENTS, new String[]{"30"});
	    	}
	    	else {
	    		desc.setAttribute(JobDescription.EXECUTABLE, "/bin/date");
	    	}
	        desc.setAttribute(JobDescription.OUTPUT, index+"-stdout.txt");
	        desc.setAttribute(JobDescription.ERROR, index+"-stderr.txt");
	        
	        // submit
	        Job job = service.createJob(desc);
	        job.run();
	        
	        // wait the end 
	        job.waitFor();
	        
	        if(!job.getState().toString().equals("DONE")) {
	        	threadException = new NoSuccess("The job number '"+index+"' is not DONE :"+job.getState().toString());
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
		}
    }
    
    public Exception getException () {
    	return threadException ;
    }
}
