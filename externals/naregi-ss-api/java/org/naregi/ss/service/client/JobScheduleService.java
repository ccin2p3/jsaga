/*
 *
 * COPYRIGHT (C) 2003-2006 National Institute of Informatics, Japan
 *                         All Rights Reserved
 * COPYRIGHT (C) 2003-2006 Fujitsu Limited
 *                         All Rights Reserved
 * 
 * This file is part of the NAREGI Grid Super Scheduler software package.
 * For license information, see the docs/LICENSE file in the top level 
 * directory of the NAREGI Grid Super Scheduler source distribution.
 *
 *
 * Revision history:
 *      $Revision: 1.1 $
 *      $Id: JobScheduleService.java,v 1.1 2008/08/07 15:24:32 sreynaud Exp $
 */
package org.naregi.ss.service.client;

import org.ietf.jgss.GSSCredential;
import org.w3c.dom.Document;

/**
 *
 * Interfaces of the API definitions for job management to 
 * the SuperScheduler like submition, control etc.
 * Use JobScheduleService instance for managing a job.
 * And create JobScheduleService instance by JobScheduleService Factory.
 * 
 */
public interface JobScheduleService {

	/**
	 * Submite jobs to NAREGI SuperScheduler.
	 * 
	 * @param job        job description in XML format
	 * @param userName   user name (for myproxy-logon)
	 * @param passPhrase user passphrase (for myproxy-logon)
	 * @return JobID
	 * @throws JobScheduleServiceException failed to submit job
	 */
	public String submitJob(Document job,
							String userName,
							String passPhrase)
					throws JobScheduleServiceException;
	
    /**
     * Submite jobs to NAREGI SuperScheduler.
     * 
     * @param job        job description in XML format
     * @param userName   user name (for myproxy-logon)
     * @param passPhrase user passphrase (for myproxy-logon)
     * @param expirationTime job expiration time (second)
     * @return JobID
     * @throws JobScheduleServiceException failed to submit job
     */
    public String submitJob(Document job,
                            String userName,
                            String passPhrase,
                            int expirationTime)
    			throws JobScheduleServiceException;

	/**
	 * Request the job kill/cancel to NAREGI SuperScheduler.
	 * Since this method doesn't delete job, it is possible to
	 * confirm the job status by queryJob method.
	 * 
	 * @param jobID      JobID
	 * @param userName   user name(for myproxy-logon)
	 * @param passPhrase user passphrase (for myproxy-logon)
	 * @throws JobScheduleServiceException failed to cancel job
	 */
	public void cancelJob(String jobID,
							String userName,
							String passPhrase) 
					throws JobScheduleServiceException;
	
	/**
	 * Request the job delete to NAREGI SuperScheduler.
	 * The job must be finished, aborted or canceled.
	 * It is impossible to delete running job.
	 * 
	 * @param jobID      JobID
	 * @param userName   user name(for myproxy-logon)
	 * @param passPhrase user passphrase (for myproxy-logon)
	 * @throws JobScheduleServiceException failed to delete job
	 * 
	 */
	public void deleteJob(String jobID,
							String userName,
							String passPhrase) 
					throws JobScheduleServiceException;
	
	/**
	 * Query the job state to NAREGI SuperScheduler.
	 * 
	 * @param jobID      JobID
	 * @param userName   user name(for myproxy-logon)
	 * @param passPhrase user passphrase (for myproxy-logon)
	 * @return job status
	 * @throws JobScheduleServiceException failed to query job status
	 */
	public Document queryJob(String jobID,
							String userName,
							String passPhrase) 
					throws JobScheduleServiceException;
	
	/**
	 * Submite jobs to NAREGI SuperScheduler.
	 * Currently this method ignores serviceURL parameter and
	 * works as same as submitJob(job,userName,passPhrase).
	 * And this method is deprecated.
	 * 
	 * @param serviceURL serviceURL for NAREGI SuperScheduler
	 * @param job        job description in XML format
	 * @param userName   user name (for myproxy-logon)
	 * @param passPhrase user passphrase (for myproxy-logon)
	 * @return JobID
	 * @throws JobScheduleServiceException failed to submit job
	 * @deprecated
	 */
	public String submitJob(String serviceURL,
							Document job,
							String userName,
							String passPhrase)
					throws JobScheduleServiceException;
	
	/**
	 * Request the job kill/cancel to NAREGI SuperScheduler.
	 * Since this method doesn't delete job, it is possible to
	 * confirm the job status by queryJob method.
	 * Currently this method ignores serviceURL parameter and
	 * works as same as cancelJob(jobID,userName,passPhrase)
	 * And this method is deprecated.
	 * 
	 * @param serviceURL serviceURL for NAREGI SuperScheduler
	 * @param jobID      JobID
	 * @param userName   user name (for myproxy-logon)
	 * @param passPhrase user passphrase (for myproxy-logon)
	 * @throws JobScheduleServiceException failed to cancel job
	 * @deprecated
	 */
	public void cancelJob(String serviceURL,
							String jobID,
							String userName,
							String passPhrase) 
					throws JobScheduleServiceException;
	
	/**
	 * Request the job delete to NAREGI SuperScheduler.
	 * The job must be finished, aborted or canceled.
	 * It is impossible to delete running job.
	 * Currently this method ignores serviceURL parameter and
	 * works as same as deleteJob(jobID,userName,passPhrase)
	 * And this method is deprecated.
	 * 
	 * @param serviceURL serviceURL for NAREGI SuperScheduler
	 * @param jobID      JobID
	 * @param userName   user name(for myproxy-logon)
	 * @param passPhrase user passphrase (for myproxy-logon)
	 * @throws JobScheduleServiceException failed to delete job
	 * @deprecated
	 * 
	 */
	public void deleteJob(String serviceURL,
							String jobID,
							String userName,
							String passPhrase) 
					throws JobScheduleServiceException;
	
	/**
	 * Query the job state to NAREGI SuperScheduler.
	 * Currently this method ignores serviceURL parameter and
	 * works as same as queryJob(jobID,userName,passPhrase)
	 * And this method is deprecated.
	 * 
	 * @param serviceURL serviceURL for NAREGI SuperScheduler
	 * @param jobID      JobID
	 * @param userName   user name(for myproxy-logon)
	 * @param passPhrase user passphrase (for myproxy-logon)
	 * @return job status
	 * @throws JobScheduleServiceException failed to query job status
	 * @deprecated
	 */
	public Document queryJob(String serviceURL,
							String jobID,
							String userName,
							String passPhrase) 
					throws JobScheduleServiceException;
	
	/**
	 * Submite jobs to NAREGI SuperScheduler.
	 * 
	 * @param job        job description in XML format
	 * @param cred       user proxy credential
	 * @return JobID
	 * @throws JobScheduleServiceException failed to submit job
	 */
	public String submitJob(Document job,
							GSSCredential cred)
					throws JobScheduleServiceException;
	
    /**
     * Submite jobs to NAREGI SuperScheduler.
     * 
     * @param job        job description in XML format
     * @param cred       user proxy credential
     * @param expirationTime job expiration time (second)
     * @return JobID
     * @throws JobScheduleServiceException failed to submit job
     */
    public String submitJob(Document job,
                            GSSCredential cred,
                            int expirationTime)
    				throws JobScheduleServiceException;
    
	/**
	 * Request the job kill/cancel to NAREGI SuperScheduler.
	 * Since this method doesn't delete job, it is possible to
	 * confirm the job status by queryJob method.
	 * 
	 * @param jobID      JobID
	 * @param cred       user proxy credential
	 * @throws JobScheduleServiceException failed to cancel job
	 */
	public void cancelJob(String jobID,
							GSSCredential cred) 
					throws JobScheduleServiceException;
	
	/**
	 * Request the job delete to NAREGI SuperScheduler.
	 * The job must be finished, aborted or canceled.
	 * It is impossible to delete running job.
	 * 
	 * @param jobID      JobID
	 * @param cred       user proxy credential
	 * @throws JobScheduleServiceException failed to delete job
	 * 
	 */
	public void deleteJob(String jobID,
							GSSCredential cred) 
					throws JobScheduleServiceException;
	
	/**
	 * Query the job state to NAREGI SuperScheduler.
	 * 
	 * @param jobID      JobID
	 * @param cred       user proxy credential
	 * @return job status
	 * @throws JobScheduleServiceException failed to query job status
	 */
	public Document queryJob(String jobID,
							GSSCredential cred) 
					throws JobScheduleServiceException;
	
    /**
     * Set hostname of SS(jm) server
     * 
     * @param jmHost      Hostname of SS(jm) server
     * 
     */
    public void setJmHost(String jmHost);
    
    /**
     * Set Port number for the SS(jm) server
     * 
     * @param jmPort      Port number for the SS(jm) server
     * 
     */
    public void setJmPort(int jmPort);
    
    /**
     * Get hostname of SS(jm) server
     * 
     * @return jmHost     Hostname of SS(jm) server
     * 
     */
    public String getJmHost();
    
    /**
     * Get Port number for the SS(jm) server
     * 
     * @return jmPort     Port number for the SS(jm) server
     * 
     */
    public int getJmPort();
}
