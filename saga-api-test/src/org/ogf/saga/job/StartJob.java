package org.ogf.saga.job;

import org.ogf.saga.error.*;


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
	        	threadException = new NoSuccessException("The job number '"+index+"' is not DONE :"+job.getState().toString());
	        }
		} catch (NotImplementedException e) {
			threadException = e; 
		} catch (IncorrectStateException e) {
			threadException = e;
		} catch (TimeoutException e) {
			threadException = e;
		} catch (NoSuccessException e) {
			threadException = e;
		} catch (AuthenticationFailedException e) {
			threadException = e;
		} catch (AuthorizationFailedException e) {
			threadException = e;
		} catch (PermissionDeniedException e) {
			threadException = e;
		} catch (BadParameterException e) {
			threadException = e;
		} catch (DoesNotExistException e) {
			threadException = e;
		}
    }
    
    public Exception getException () {
    	return threadException ;
    }
}
