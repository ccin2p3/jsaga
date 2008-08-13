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
 *      Revision: 882
 *      Id: JobScheduleServiceImpl.java 882 2007-03-29 10:58:42Z kawamura
 */
package org.naregi.ss.service.client;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.URL;
import java.util.Calendar;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.globus.gsi.gssapi.auth.IdentityAuthorization;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import org.naregi.ss.service.client.logging.LoggerManager;
import org.w3c.dom.Document;

/**
 *
 * Implementation class for JobScheduleService interface
 * 
 */
public class JobScheduleServiceImpl implements JobScheduleService {

	static {
		TempFileManager.initialize();
		
        START_TIME = Calendar.getInstance();
        try
        {
        	HOST_NAME = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
        	HOST_NAME = null;
        }

        String dir = null;
        try
        {
        	String value = ConfigManager.getProperty("ss.command.location", dir);
        	File fdir = new File(value);
        	if (fdir.exists() && fdir.isDirectory()) {
        		NAREGI_SS_TOOL_LOCATION = value;
        	} else {
        		NAREGI_SS_TOOL_LOCATION = dir;
        	}
        } catch (Exception e) {
    		NAREGI_SS_TOOL_LOCATION = dir;
        }

        String split_string;
        String split_string_df = ",";
        if (false) {
            try
            {
            	split_string = ConfigManager.getProperty(
            					"ss.command.env.split_string",
            					split_string_df);
            } catch (Exception e) {
            	split_string = split_string_df;
            }
        } else {
        	split_string = split_string_df;
        }

        String[] def_env = null;
        try
        {
        	String value = ConfigManager.getProperty("ss.command.env", null);
        	if (value != null) {
        		String[] splt = value.split(split_string);
        		for (int i=0;i < splt.length;i++) {
        			splt[i] = splt[i].trim();
        			if (false) {	// for debug
            			System.out.println("env[" + i + "] = " + splt[i]);
        			}
        		}
            	NAREGI_SS_TOOL_ENVIRONMENT = splt;
            	
        	} else {
            	NAREGI_SS_TOOL_ENVIRONMENT = def_env;
        	}
        } catch (Exception e) {
        	NAREGI_SS_TOOL_ENVIRONMENT = def_env;
        }

        try
        {
        	String expiretion_time = ConfigManager.getProperty(
        						"ss.job.expiretion.time");
        	JOB_EXPIRATION_TIME = Integer.parseInt(expiretion_time);
        } catch (Exception e) {
        	JOB_EXPIRATION_TIME = 60;
        }
        
        try
        {
        	MYPROXY_HOST = 
        		ConfigManager.getProperty("myproxy.server.host");
        } catch (Exception e) {
        	MYPROXY_HOST = null;
        }

        try
        {
        	MYPROXY_HOST_DN = 
        		ConfigManager.getProperty("myproxy.server.hostDN");
        } catch (Exception e) {
        	MYPROXY_HOST_DN = null;
        }

        int mp_port_def = 7512;
        try
        {
        	String port = 
        		ConfigManager.getProperty("myproxy.server.port");
        	MYPROXY_PORT = Integer.parseInt(port);
        } catch (Exception e) {
        	MYPROXY_PORT = mp_port_def;
        }

        try
        {
        	MYPROXY_PLUS_HOST = 
        		ConfigManager.getProperty("myproxy_plus.server.host");
        } catch (Exception e) {
        	MYPROXY_PLUS_HOST = null;
        }

        try
        {
        	MYPROXY_PLUS_HOST_DN = 
        		ConfigManager.getProperty("myproxy_plus.server.hostDN");
        } catch (Exception e) {
        	MYPROXY_PLUS_HOST_DN = null;
        }

        int mpp_port_def = 7512;
        try
        {
        	String port = 
        		ConfigManager.getProperty("myproxy_plus.server.port");
        	MYPROXY_PLUS_PORT = Integer.parseInt(port);
        } catch (Exception e) {
        	MYPROXY_PLUS_PORT = mpp_port_def;
        }

		try
		{
			USER_AUTHENTICATION_ENABLE = 
				ConfigManager.getProperty("user.authentication.enable", "true");
		} catch (Exception e) {
			USER_AUTHENTICATION_ENABLE = "true";
		}
	}

	private void initializeCheck() throws JobScheduleServiceException {
		if (NAREGI_SS_TOOL_LOCATION == null) {
			String msg = "Unable to get SS command location path.";
			throw new JobScheduleServiceException(msg);
		}
		if (HOST_NAME == null) {
			String msg = "Internal Error!. Unable to get Host Name.";
			throw new JobScheduleServiceException(msg);
		}
		if (MYPROXY_HOST == null) {
			String msg = "Unable to get MyProxy Host Name.";
			throw new JobScheduleServiceException(msg);
		}
		if (MYPROXY_HOST_DN == null) {
			String msg = "Unable to get MyProxy Host Cert DN.";
			throw new JobScheduleServiceException(msg);
		}
		if (MYPROXY_PLUS_HOST == null) {
			String msg = "Unable to get MyProxy+ Host Name.";
			throw new JobScheduleServiceException(msg);
		}
		if (MYPROXY_PLUS_HOST_DN == null) {
			String msg = "Unable to get MyProxy+ Host Cert DN.";
			throw new JobScheduleServiceException(msg);
		}
	}
	
	/**
	 * Hostname of MyProxy server
	 **/
	private static String MYPROXY_HOST;
	
	/**
	 * DN of host certification where the MyProxy server runs
	 **/
	private static String MYPROXY_HOST_DN;
	
	/**
	 * Port number for the MyProxy server
	 **/
	private static int MYPROXY_PORT;
	
	/**
	 * Hostname of MyProxy+ server
	 **/
	private static String MYPROXY_PLUS_HOST;
	
	/**
	 * DN of host certification where the MyProxy+ server runs
	 **/
	private static String MYPROXY_PLUS_HOST_DN;
	
	/**
	 * Port number for the MyProxy+ server
	 **/
	private static int MYPROXY_PLUS_PORT;
	
	/**
	 * Counter of requested job control (for generating uniq file name)
	 */
	private static long jobRequestCounter = 0;
	
	/**
	 * Counter of requested job submission (for generating uniq file name)
	 */
	private static long jobSubmitCounter = 0;

	/**
	 * First involked time
	 * (for generating uniq file name and user for MyProxy+)
	 *
	 */
	private static Calendar START_TIME;
	
	/**
	 * Hostname of involked SS Java API
	 * (for generating user for MyProxy+)
	 *
	 */
	private static String HOST_NAME;
	
	private static int JOB_EXPIRATION_TIME;

	/**
	 * Time to live when retriving proxy credential from MyProxy server
	 * To retrive the same period as proxy credential, the long TTL is 
	 * assigned.
	 * (If the longer TTL than MyProxy credential is specified, 
	 * the new proxy's valided period is same as original credential.)
	 *
	 */
	private static int JOB_TIME_TO_LIVE = 300000; // hours

	/**
	 * Installed path of the NAREGI Super Scheduler Job operation commands.
	 */
	private static String NAREGI_SS_TOOL_LOCATION;
	
	/**
	 * Environment variables while the spawnning NAREGI Super Scheduler
	 * Job operation commands.
	 */
	private static String[] NAREGI_SS_TOOL_ENVIRONMENT;
	
	/**
	 *  Enabling the user authentification of NAREGI Super Scheduler.
	 */
	private static String USER_AUTHENTICATION_ENABLE;
	
	/**
	 * Class for managing temporay files.
	 */
	private static TempFileManager fempFileManager;
	
    /**
     * Hostname of SS(jm) server
     */
    private static String SS_HOST = null;
    
    /**
     * Port number for the SS(jm) server
     */
    private static int SS_PORT = 0;

	/*
	 * @see org.naregi.ss.service.client.JobScheduleService#submitJob(org.w3c.dom.Document, java.lang.String, java.lang.String)
	 */
	public String submitJob(Document job, String userName, String passPhrase) throws JobScheduleServiceException {
		return submitJob(null, job, userName, passPhrase);
	}

	/*
	 * @see org.naregi.ss.service.client.JobScheduleService#cancelJob(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void cancelJob(String jobID, String userName, String passPhrase) throws JobScheduleServiceException {
		cancelJob(null, jobID, userName, passPhrase);
	}

	/*
	 * @see org.naregi.ss.service.client.JobScheduleService#deleteJob(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void deleteJob(String jobID, String userName, String passPhrase) throws JobScheduleServiceException {
		deleteJob(null, jobID, userName, passPhrase);
	}

	/*
	 * @see org.naregi.ss.service.client.JobScheduleService#queryJob(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Document queryJob(String jobID, String userName, String passPhrase) throws JobScheduleServiceException {
		return queryJob(null, jobID, userName, passPhrase);
	}

	/*
	 * @see org.naregi.ss.service.client.JobScheduleService#submitJob(java.lang.String, org.w3c.dom.Document, java.lang.String, java.lang.String)
	 */
	public String submitJob(String serviceURL, Document job, String userName, String passPhrase)
		throws JobScheduleServiceException {
		
		initializeCheck();
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(cal.getTimeInMillis()
						+ JOB_EXPIRATION_TIME * 60 * 1000);
		String expirationDate = changeCalToDateTime(cal);
		return submitJob(serviceURL, job, userName, passPhrase, expirationDate);
	}
	
	/*
	 * @see org.naregi.ss.service.client.JobScheduleService#submitJob(org.w3c.dom.Document, java.lang.String, java.lang.String, int)
	 */
	public String submitJob(Document job, String userName, String passPhrase, int expirationTime)
		throws JobScheduleServiceException {
		
		initializeCheck();
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(cal.getTimeInMillis() + expirationTime * 1000);
		String expirationDate = changeCalToDateTime(cal);
		return submitJob(null, job, userName, passPhrase, expirationDate);
	}

	private String submitJob(String serviceURL, Document job, String userName,
							String passPhrase, String expirationDate)
		throws JobScheduleServiceException {

		Logger logger = LoggerManager.getLogger();
		logger.fine("start");
		
		if (job == null) {
			String msg = "'Job XML document' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		if (userName == null || userName.equals("")) {
			String msg = "'User Name' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		if (passPhrase == null || passPhrase.equals("")) {
			String msg = "'Pass Phrase' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		if (expirationDate == null || expirationDate.equals("")) {
			String msg = "'Expiration Date' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		

		String tmpFilePrefix = getTempFileName();
		String jobid = null;
		SSJobSVCommandResponce resp;
		try {
			{
				String[] command = {
						  getSSCommandNameWFMLToBPEL()
						, makeJobWFMLFile(tmpFilePrefix, job, logger)
						, makeBpelFileName(tmpFilePrefix)
						};
				resp = command(command, NAREGI_SS_TOOL_ENVIRONMENT);
				if (resp.getExitCode() != 0) {
					String msg = getCommandErrMsg(resp, command);
					logger.warning(msg);
					throw new JobScheduleServiceException(msg);
				}
			}

			{
				String[] command = {
						  "md5sum"
						, makeBpelFileName(tmpFilePrefix)
						}; 
				resp = command(command, null);
				if (resp.getExitCode() != 0) {
					String msg = getCommandErrMsg(resp, command);
					logger.warning(msg);
					throw new JobScheduleServiceException(msg);
				}
			}
			
			String md5sum_result = resp.getStdout();
			String[] splt = md5sum_result.split(" ");
			String job_pass = splt[0].trim();
			
			int lifetime = JOB_TIME_TO_LIVE * 3600;
			String job_acount = getMyProxyPlusAccountName();
			userProxy2JobProxy(userName, passPhrase,
					job_acount, job_pass, lifetime, logger);
			
			{
				String[] command = {
						  getSSCommandNameJMClient()
						, "submit"
						, makeJobIDFileName(tmpFilePrefix)
						, makeAccountNameFile(tmpFilePrefix, job_acount, logger)
						, makeBpelFileName(tmpFilePrefix)
						, "\"" + expirationDate + "\""
						};
				resp = command(command, NAREGI_SS_TOOL_ENVIRONMENT);
				if (resp.getExitCode() != 0) {
					String msg = getCommandErrMsg(resp, command);
					logger.warning(msg);
					throw new JobScheduleServiceException(msg);
				}
			}

			jobid = getJobID(tmpFilePrefix, logger);
			
			if (jobid == null || jobid.equals("")) {
				String msg = "Unable to get Job ID. Job ID is null";
				logger.warning(msg);
				throw new JobScheduleServiceException(msg);
			}
		} finally {
			
			removeTempFile(tmpFilePrefix + TEMPFILE_WFML_PFX, logger);
			removeTempFile(tmpFilePrefix + TEMPFILE_BPEL_PFX, logger);
			removeTempFile(tmpFilePrefix + TEMPFILE_ACCOUNT_PFX, logger);
			removeTempFile(tmpFilePrefix + TEMPFILE_JOBID_PFX, logger);
			logger.fine("end");
		}
		return jobid;
	}

	/* 
	 * @see org.naregi.ss.service.client.JobScheduleService#cancelJob(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void cancelJob(String serviceURL, String jobID, String userName,
			String passPhrase) throws JobScheduleServiceException {

		initializeCheck();
		
		Logger logger = LoggerManager.getLogger();
		logger.fine("start");
		
		if (jobID == null || jobID.equals("")) {
			String msg = "'Job ID' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		if (userName == null || userName.equals("")) {
			String msg = "'User Name' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		if (passPhrase == null || passPhrase.equals("")) {
			String msg = "'Pass Phrase' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		
		String tmpFilePrefix = getTempFileName();
		SSJobSVCommandResponce resp ;
		try {
			{
				String[] command = {
						  getSSCommandNameJMClient()
						, "cancel"
						, makeJobIDFile(tmpFilePrefix, jobID, logger)
						, makeAccountNameFile(tmpFilePrefix, userName, logger)
						};
				resp = command(command, NAREGI_SS_TOOL_ENVIRONMENT);
				if (resp.getExitCode() != 0) {
					String msg = getCommandErrMsg(resp, command);
					logger.warning(msg);
					throw new JobScheduleServiceException(msg);
				}
			}

		} finally {
			removeTempFile(tmpFilePrefix + TEMPFILE_ACCOUNT_PFX, logger);
			removeTempFile(tmpFilePrefix + TEMPFILE_JOBID_PFX, logger);
			logger.fine("end");
		}
	}

	/* 
	 * @see org.naregi.ss.service.client.JobScheduleService#deleteJob(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void deleteJob(String serviceURL, String jobID, String userName,
			String passPhrase) throws JobScheduleServiceException {

		initializeCheck();
		
		Logger logger = LoggerManager.getLogger();
		logger.fine("start");
		
		if (jobID == null || jobID.equals("")) {
			String msg = "'Job ID' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		if (userName == null || userName.equals("")) {
			String msg = "'User Name' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		if (passPhrase == null || passPhrase.equals("")) {
			String msg = "'Pass Phrase' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		
		String tmpFilePrefix = getTempFileName();
		SSJobSVCommandResponce resp;
		try {
			{
				String[] command = {
						  getSSCommandNameJMClient()
						, "delete"
						, makeJobIDFile(tmpFilePrefix, jobID, logger)
						, makeAccountNameFile(tmpFilePrefix, userName, logger)
						};
				resp = command(command, NAREGI_SS_TOOL_ENVIRONMENT);
				if (resp.getExitCode() != 0) {
					String msg = getCommandErrMsg(resp, command);
					logger.warning(msg);
					throw new JobScheduleServiceException(msg);
				}
			}
		} finally {
			removeTempFile(tmpFilePrefix + TEMPFILE_ACCOUNT_PFX, logger);
			removeTempFile(tmpFilePrefix + TEMPFILE_JOBID_PFX, logger);
			logger.fine("end");
		}
	}

	/* 
	 * @see org.naregi.ss.service.client.JobScheduleService#queryJob(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public Document queryJob(String serviceURL, String jobID, String userName,
			String passPhrase) throws JobScheduleServiceException {

		initializeCheck();
		
		Logger logger = LoggerManager.getLogger();
		logger.fine("start");
		
		if (jobID == null || jobID.equals("")) {
			String msg = "'Job ID' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		if (userName == null || userName.equals("")) {
			String msg = "'User Name' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		if (passPhrase == null || passPhrase.equals("")) {
			String msg = "'Pass Phrase' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		
		String tmpFilePrefix = getTempFileName();
		Document ret = null;
		SSJobSVCommandResponce resp;
		try {
			{
				String[] command = {
						  getSSCommandNameJMClient()
						, "status"
						, makeJobIDFile(tmpFilePrefix, jobID, logger)
						, makeAccountNameFile(tmpFilePrefix, userName, logger)
						, makeStatusFileName(tmpFilePrefix)
						};
				resp = command(command, NAREGI_SS_TOOL_ENVIRONMENT);
				if (resp.getExitCode() != 0) {
					String msg = getCommandErrMsg(resp, command);
					logger.warning(msg);
					throw new JobScheduleServiceException(msg);
				}
			}
			{
				String[] command = {
						  getSSCommandNameStatusToWFST()
						, makeStatusFileName(tmpFilePrefix)
						, makeWFSTFileName(tmpFilePrefix)
				};
				resp = command(command, NAREGI_SS_TOOL_ENVIRONMENT);
				if (resp.getExitCode() != 0) {
					String msg = getCommandErrMsg(resp, command);
					logger.warning(msg);
					throw new JobScheduleServiceException(msg);
				}
			}
			ret = getStatus(tmpFilePrefix, logger);
			if (ret == null || ret.equals("")) {
				String msg = "Unable to get status. Job status document is null.";
				logger.warning(msg);
				throw new JobScheduleServiceException(msg);
			}
		} finally {
			removeTempFile(tmpFilePrefix + TEMPFILE_ACCOUNT_PFX, logger);
			removeTempFile(tmpFilePrefix + TEMPFILE_JOBID_PFX, logger);
			removeTempFile(tmpFilePrefix + TEMPFILE_STATUS_PFX, logger);
			removeTempFile(tmpFilePrefix + TEMPFILE_WFST_PFX, logger);
			logger.fine("end");
		}
		return ret;
	}
	
	/*
	 * @see org.naregi.ss.service.client.JobScheduleService#submitJob(org.w3c.dom.Document, org.ietf.jgss.GSSCredential)
	 */
	public String submitJob(Document jobDescription, GSSCredential credential)
	throws JobScheduleServiceException {
		
		initializeCheck();
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(cal.getTimeInMillis()
						+ JOB_EXPIRATION_TIME * 60 * 1000);
		String expirationDate = changeCalToDateTime(cal);
		return submitJob(jobDescription, credential, expirationDate);
	}
	
	/*
	 * @see org.naregi.ss.service.client.JobScheduleService#submitJob(org.w3c.dom.Document, org.ietf.jgss.GSSCredential, int)
	 */
	public String submitJob(Document jobDescription, GSSCredential credential, int expirationTime)
		throws JobScheduleServiceException {
		
		initializeCheck();
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(cal.getTimeInMillis() + expirationTime * 1000);
		String expirationDate = changeCalToDateTime(cal);
		return submitJob(jobDescription, credential, expirationDate);
	}
	
	private String submitJob(Document jobDescription, GSSCredential credential, String expirationDate)
		throws JobScheduleServiceException {
		
		Logger logger = LoggerManager.getLogger();
		logger.fine("start");
		
		if (jobDescription == null) {
			String msg = "'Job XML document' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		if (credential == null) {
			String msg = "'User Credential' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		
		String tmpFilePrefix = getTempFileName();
		String jobId = null;
		
		try {
			wfml2bpel(
					makeJobWFMLFile(tmpFilePrefix, jobDescription, logger),
					makeBpelFileName(tmpFilePrefix),
					logger);
			
			String myProxyPlusAccountName = getMyProxyPlusAccountName();
			
			if (USER_AUTHENTICATION_ENABLE.equals("true"))
				setCredential(
						MYPROXY_PLUS_HOST,
						MYPROXY_PLUS_PORT,
						MYPROXY_PLUS_HOST_DN,
						myProxyPlusAccountName,
						getMyProxyPlusPass(makeBpelFileName(tmpFilePrefix), logger),
						credential,
						logger);
			
			jmClientSubmit(
					makeJobIDFileName(tmpFilePrefix),
					makeAccountNameFile(tmpFilePrefix, myProxyPlusAccountName, logger),
					makeBpelFileName(tmpFilePrefix),
					expirationDate,
					logger);
			
			jobId = getJobID(tmpFilePrefix, logger);
			
			if (jobId == null || jobId.equals("")) {
				String msg = "Unable to get Job ID. Job ID is null";
				logger.warning(msg);
				throw new JobScheduleServiceException(msg);
			}
		}
		finally {
			removeTempFile(tmpFilePrefix + TEMPFILE_WFML_PFX, logger);
			removeTempFile(tmpFilePrefix + TEMPFILE_BPEL_PFX, logger);
			removeTempFile(tmpFilePrefix + TEMPFILE_ACCOUNT_PFX, logger);
			removeTempFile(tmpFilePrefix + TEMPFILE_JOBID_PFX, logger);
			logger.fine("end");
		}
		return jobId;
	}
	
	/*
	 * @see org.naregi.ss.service.client.JobScheduleService#cancelJob(java.lang.String, org.ietf.jgss.GSSCredential)
	 */
	public void cancelJob(String jobID, GSSCredential credential)
	throws JobScheduleServiceException {
		initializeCheck();
		
		Logger logger = LoggerManager.getLogger();
		logger.fine("start");
		
		if (jobID == null || jobID.equals("")) {
			String msg = "'Job ID' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		if (credential == null) {
			String msg = "'User Credential' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		
		String tmpFilePrefix = getTempFileName();
		
		try {
			jmClientCancel(
					makeJobIDFile(tmpFilePrefix, jobID, logger),
					makeAccountNameFile(tmpFilePrefix, getMyProxyPlusAccountName(), logger),
					logger);
		}
		finally {
			removeTempFile(tmpFilePrefix + TEMPFILE_ACCOUNT_PFX, logger);
			removeTempFile(tmpFilePrefix + TEMPFILE_JOBID_PFX, logger);
			logger.fine("end");
		}
	}
	
	/*
	 * @see org.naregi.ss.service.client.JobScheduleService#deleteJob(java.lang.String, org.ietf.jgss.GSSCredential)
	 */
	public void deleteJob(String jobID, GSSCredential credential)
	throws JobScheduleServiceException {
		initializeCheck();
		
		Logger logger = LoggerManager.getLogger();
		logger.fine("start");
		
		if (jobID == null || jobID.equals("")) {
			String msg = "'Job ID' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		if (credential == null) {
			String msg = "'User Credential' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		
		String tmpFilePrefix = getTempFileName();
		
		try {
			jmClientDelete(
					makeJobIDFile(tmpFilePrefix, jobID, logger),
					makeAccountNameFile(tmpFilePrefix, getMyProxyPlusAccountName(), logger),
					logger);
		}
		finally {
			removeTempFile(tmpFilePrefix + TEMPFILE_ACCOUNT_PFX, logger);
			removeTempFile(tmpFilePrefix + TEMPFILE_JOBID_PFX, logger);
			logger.fine("end");
		}
	}
	
	/*
	 * @see org.naregi.ss.service.client.JobScheduleService#queryJob(java.lang.String, org.ietf.jgss.GSSCredential)
	 */
	public Document queryJob(String jobID, GSSCredential credential)
	throws JobScheduleServiceException {
		initializeCheck();
		
		Logger logger = LoggerManager.getLogger();
		logger.fine("start");
		
		if (jobID == null || jobID.equals("")) {
			String msg = "'Job ID' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		if (credential == null) {
			String msg = "'User Credential' is not specified.";
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		
		String tmpFilePrefix = getTempFileName();
		Document status = null;
		
		try {
			jmClientQuery(
					makeJobIDFile(tmpFilePrefix, jobID, logger),
					makeAccountNameFile(tmpFilePrefix, getMyProxyPlusAccountName(), logger),
					makeStatusFileName(tmpFilePrefix),
					logger);
			
			bpel2wfst(
					makeStatusFileName(tmpFilePrefix),
					makeWFSTFileName(tmpFilePrefix),
					logger);
			
			status = getStatus(tmpFilePrefix, logger);
			
			if (status == null || status.equals("")) {
				String msg = "Unable to get status. Job status document is null.";
				logger.warning(msg);
				throw new JobScheduleServiceException(msg);
			}
		} finally {
			removeTempFile(tmpFilePrefix + TEMPFILE_ACCOUNT_PFX, logger);
			removeTempFile(tmpFilePrefix + TEMPFILE_JOBID_PFX, logger);
			removeTempFile(tmpFilePrefix + TEMPFILE_STATUS_PFX, logger);
			removeTempFile(tmpFilePrefix + TEMPFILE_WFST_PFX, logger);
			logger.fine("end");
		}
		return status;
	}
	
    public void setJmHost(String ssHost)
    {
        SS_HOST = ssHost;
    }
    
    public void setJmPort(int ssPort)
    {
        SS_PORT = ssPort;
    }
    
    public String getJmHost()
    {
        return SS_HOST;
    }
    
    public int getJmPort()
    {
        return SS_PORT;
    }
    
	private void wfml2bpel(String wfmlFileName, String bpelFileName, Logger logger)
	throws JobScheduleServiceException {
		SSJobSVCommandResponce responce;
		String[] command = {
			getSSCommandNameWFMLToBPEL(), wfmlFileName, bpelFileName
		};
		responce = command(command, NAREGI_SS_TOOL_ENVIRONMENT);
		
		if (responce.getExitCode() != 0) {
			String msg = getCommandErrMsg(responce, command);
			if (logger != null) logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
	}
	
	private void bpel2wfst(String statusFileName, String wfstFileName, Logger logger)
	throws JobScheduleServiceException {
		SSJobSVCommandResponce responce;
		String[] command = {
			getSSCommandNameStatusToWFST(), statusFileName, wfstFileName
		};
		responce = command(command, NAREGI_SS_TOOL_ENVIRONMENT);
		
		if (responce.getExitCode() != 0) {
			String msg = getCommandErrMsg(responce, command);
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
	}
	
	private void jmClientSubmit(String jobIdFileName,
			String accountNameFile, String bpelFileName, String expirationDate, Logger logger)
	throws JobScheduleServiceException {
		String[] command = { getSSCommandNameJMClient(), "submit",
				jobIdFileName, accountNameFile, bpelFileName, "\"" + expirationDate + "\""};
		jmClient(command, logger);
	}
	
	private void jmClientCancel(String jobIdFileName, String accountNameFile, Logger logger)
	throws JobScheduleServiceException {
		String[] command = { getSSCommandNameJMClient(), "cancel", jobIdFileName, accountNameFile };
		jmClient(command, logger);
	}
	
	private void jmClientDelete(String jobIdFileName, String accountNameFile, Logger logger)
	throws JobScheduleServiceException {
		String[] command = { getSSCommandNameJMClient(), "delete", jobIdFileName, accountNameFile };
		jmClient(command, logger);
	}
	
	private void jmClientQuery(String jobIdFileName,
			String accountNameFile,String statusFileName, Logger logger) 
	throws JobScheduleServiceException {
		String[] command =
			{ getSSCommandNameJMClient(), "status", jobIdFileName, accountNameFile, statusFileName };
		jmClient(command, logger);
	}
	
	private void jmClient(String[] command, Logger logger) throws JobScheduleServiceException {
		SSJobSVCommandResponce responce = command(command, NAREGI_SS_TOOL_ENVIRONMENT);
		if (responce.getExitCode() != 0) {
			String msg = getCommandErrMsg(responce, command);
			if (logger != null) logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
	}
	
	private String getMyProxyPlusPass(String bpelFileName, Logger logger)
	throws JobScheduleServiceException {
		SSJobSVCommandResponce responce;
		String[] command = { "md5sum", bpelFileName };
		responce = command(command, null);
		
		if (responce.getExitCode() != 0) {
			String msg = getCommandErrMsg(responce, command);
			if (logger != null) logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		String hash = responce.getStdout();
		String[] splt = hash.split(" ");
		return splt[0].trim();
	}
	
/*	private GSSCredential getCredential(String serverName, int serverPort,
			String serverSubject, String username, String password, Logger logger)
	throws JobScheduleServiceException {
		logger.fine( "[myproxy-get-delegation] access to "+MYPROXY_HOST+"  ..." );
		int lifetime = JOB_TIME_TO_LIVE * 3600;
		GSSCredential cred = null;
		
		try {
			MyProxy myProxy = new MyProxy(serverName, serverPort);
			myProxy.setAuthorization(new IdentityAuthorization(serverSubject));
			cred = myProxy.get(username, password, lifetime);
		}
		catch (MyProxyException e) {
			e.printStackTrace();
			String msg =  "failed to get of job credential from myproxy.\n"
						+ "Because : " + e.getMessage();
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		logger.fine("...get seccessful\n");
		
		return cred;
	}
*/	
	private void setCredential(String serverName, int serverPort, String serverSubject,
			String username, String password, GSSCredential cred, Logger logger)
	throws JobScheduleServiceException {
		logger.fine("[myproxy-init] access to " + MYPROXY_PLUS_HOST + "  ...");
		int lifetime = JOB_TIME_TO_LIVE * 3600;
		
		try {
			MyProxy myProxy = new MyProxy(serverName, serverPort);
			myProxy.setAuthorization(new IdentityAuthorization(serverSubject));
			myProxy.put(cred, username, password, lifetime);
		}
		catch (MyProxyException e) {
			e.printStackTrace();
			String msg =  "job credential cannot be stored in myproxy.\n"
						+ "Because : " + e.getMessage();
			logger.warning(msg);
			throw new JobScheduleServiceException(msg);
		}
		logger.fine("...set seccessful\n");
	}

	private Document getStatus(String tempFilePrefix, Logger logger)
		throws JobScheduleServiceException {

		Document ret = null;
		String fn = makeWFSTFileName(tempFilePrefix);
		logger.fine("Get status from : " + fn);
		try {
			ret = XMLUtil.loadFileToDocument(new File(fn));
		} catch (Exception e) {
			e.printStackTrace();
			String msg = "Unable to change from 'file' to 'Document type'"
				+ " <" + fn + "> . "
				+ "Because : " + e.getMessage();
			logger.severe(msg);
			throw new JobScheduleServiceException(msg);
		}

		return ret;
	}

	private String getSSCommandNameWFMLToBPEL() {

		return NAREGI_SS_TOOL_LOCATION + "/bin/wfml2bpel";
	}

	private String getSSCommandNameJMClient() {
	
		return NAREGI_SS_TOOL_LOCATION + "/bin/jm-client";
	}

	private String getSSCommandNameStatusToWFST() {

		return NAREGI_SS_TOOL_LOCATION + "/bin/bpel2wfst";
	}

	private String makeAccountNameFile(String tempFilePrefix, String accountName, Logger logger)
		throws JobScheduleServiceException {
	
		String fn = makeAccountNameFileName(tempFilePrefix);
		logger.fine("make account name file : " + fn);
		
		makeFileFromString(fn, accountName, logger);
		
		return fn;
	}

	private String makeJobIDFile(String tempFilePrefix, String jobid, Logger logger)
		throws JobScheduleServiceException {

		String fn = makeJobIDFileName(tempFilePrefix);
		logger.fine("make JobID file : " + fn);
		
		makeFileFromString(fn, jobid, logger);
		
		return fn;
	}
	
	private void makeFileFromString(String fn, String data, Logger logger)
		throws JobScheduleServiceException {
		
		try {
			_makeFileFromString(data, fn);
		} catch (IOException e) {
			e.printStackTrace();
			String msg = "Unable to make file"
				+ " <" + fn + "> . "
				+ "Because : " + e.getMessage();
			logger.severe(msg);
	
			throw new JobScheduleServiceException(msg);
		}
	}

	private void _makeFileFromString(String source, String distFileName) 
		throws IOException {
	
		BufferedWriter br = null;
		try {
			br = new BufferedWriter(
					new FileWriter(distFileName) );
	
			br.write(source);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch(Exception e) {
				}
			}
		}
	}

	private String getJobID(String tempFilePrefix, Logger logger)
		throws JobScheduleServiceException {
	
		StringBuffer bfs = new StringBuffer();
		String fn = makeJobIDFileName(tempFilePrefix);
		logger.fine("Get JobID from : " + fn);
		BufferedReader br = null;
		try {
			br = new BufferedReader(
						new InputStreamReader(
								new FileInputStream(fn) ) );
	
			String line = null;
			while( (line = br.readLine()) != null) {
				bfs.append(line + "\n");
			}
				
		} catch (Exception e) {
			e.printStackTrace();
			String msg = "Unable to get Job ID"
				+ " <" + fn + "> . "
				+ "Because : " + e.getMessage();
			logger.severe(msg);
	
			throw new JobScheduleServiceException(msg);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch(Exception e) {
				}
			}
		}
		return bfs.toString();
	}

	private static final String TEMPFILE_ACCOUNT_PFX = ".anam";

	private String makeAccountNameFileName(String tempFilePrefix)
		throws JobScheduleServiceException {
	
		return TempFileManager.getLogDir().getAbsolutePath()
				+ "/" + tempFilePrefix
				+ TEMPFILE_ACCOUNT_PFX;
	}

	private static final String TEMPFILE_JOBID_PFX = ".xxid";

	private String makeJobIDFileName(String tempFilePrefix)
		throws JobScheduleServiceException {
	
		return TempFileManager.getLogDir().getAbsolutePath()
				+ "/" + tempFilePrefix
				+ TEMPFILE_JOBID_PFX;
	}
	
	private static final String TEMPFILE_BPEL_PFX = ".bpel";

	private String makeBpelFileName(String tempFilePrefix)
		throws JobScheduleServiceException {
	
		return TempFileManager.getLogDir().getAbsolutePath()
				+ "/" + tempFilePrefix
				+ TEMPFILE_BPEL_PFX;
	}
	
	private static final String TEMPFILE_WFML_PFX = ".wfml";

	private String makeWFMLFileName(String tempFilePrefix)
		throws JobScheduleServiceException {
	
		return TempFileManager.getLogDir().getAbsolutePath()
				+ "/" + tempFilePrefix
				+ TEMPFILE_WFML_PFX;
	}

	private static final String TEMPFILE_STATUS_PFX = ".stat";

	private String makeStatusFileName(String tempFilePrefix)
		throws JobScheduleServiceException {
	
		return TempFileManager.getLogDir().getAbsolutePath()
			+ "/" + tempFilePrefix
			+ TEMPFILE_STATUS_PFX;
	}

	private static final String TEMPFILE_WFST_PFX = ".wfst";

	private String makeWFSTFileName(String tempFilePrefix)
		throws JobScheduleServiceException {
	
		return TempFileManager.getLogDir().getAbsolutePath()
			+ "/" + tempFilePrefix
			+ TEMPFILE_WFST_PFX;
	}

	private String makeJobWFMLFile(String tempFilePrefix, Document job, Logger logger)
		throws JobScheduleServiceException {
		
		logger.fine("Create wfml file start.");
		String fn = makeWFMLFileName(tempFilePrefix);
		logger.fine("Create Job wfml file : " + fn);
		try {
			String replaceXmlSrc = 
				XMLUtil.getStringFromDocument(job);
			String replaceXmlDist = 
				getImplicitDefinitionEntityReplaceString(replaceXmlSrc);
		
			makeFileFromString(fn, replaceXmlDist, logger);
		} catch (Exception e) {
			e.printStackTrace();
			String msg =  "Unable to create temporary wfml file"
						+ " <" + fn + "> . "
						+ "Because : " + e.getMessage();
			logger.severe(msg);
			throw new JobScheduleServiceException(msg);
		}
		logger.fine("Create wfml file end.");
		return fn;
	}
	
	/**
	 * Expand following escape strings.
	 * 
	 * &lt; to <
	 * &gt; to >
	 * &quot; to "
	 * &apos; to '
	 * &amp; to &
	 * 
	 * @param src target XML document
	 * @return expanded XML document
	 */
	private String getImplicitDefinitionEntityReplaceString(String src) {
		String ret = null;
		ret = getPatternReplaceString(src, "&lt;"	, "<");
		ret = getPatternReplaceString(ret, "&gt;"	, ">");
		ret = getPatternReplaceString(ret, "&quot;"	, "\"");
		ret = getPatternReplaceString(ret, "&apos;"	, "'");
		ret = getPatternReplaceString(ret, "&amp;"	, "&");
	
		if (false) {	// for debugging
			System.out.println("----- Replaced Job XML\n" + ret);	
			System.out.println("-----");							// for debugging
		}
	
		return ret;
	}
	
	private String getPatternReplaceString(String src, String regexp, String replace) {
		String ret = null;
	
		Pattern pattern = Pattern.compile(regexp);
		String[] retsp = src.split("\n");
		Matcher matcher = pattern.matcher(src);
		ret = matcher.replaceAll(replace);
	
		return ret;
	}
	
	private String getTempFileName() throws JobScheduleServiceException {
		String ret =  START_TIME.getTimeInMillis()
				+ "-" + incrementJobRequestCounter()
				+ "_";
		
		return ret;
	}
	
	private String getMyProxyPlusAccountName() throws JobScheduleServiceException {
		File dir = TempFileManager.getLogDir();
		String proc = dir.getName();
		return HOST_NAME + "_" + proc + "_" + START_TIME.getTimeInMillis()
					+ "_" + incrementJobSubmitCounter()
					+ "_";
	}
	
	private String getCommandErrMsg(SSJobSVCommandResponce resp, String[] command) {
		return getCommandErrMsg(null, resp, command);
	}
	
	private String getCommandErrMsg(String message, SSJobSVCommandResponce resp, String[] command) {

        StringBuffer buf = new StringBuffer();
        
        buf.append("Super Scheduler command error occurred.\n");
        if (message != null && !message.equals("")) {
            buf.append("[error message] " + message);
            buf.append("\n");
        }
        buf.append("[exec command] ");
        for (int i=0;i < command.length;i++) {
        	buf.append(command[i] + " ");
        }
        buf.append("\n");

        buf.append("[exit code] " + resp.getExitCode());
        buf.append("\n");

        buf.append("[Stdout] \n");
        buf.append(resp.getStdout());
        buf.append("\n");
        buf.append("[Stderr] \n");
        buf.append(resp.getStderr());
        buf.append("\n");

        return buf.toString();
	}

	private SSJobSVCommandResponce command(String[] command, String[] env) 
							throws JobScheduleServiceException {

		Logger logger = LoggerManager.getLogger();
		logger.fine("start");
		
		SSJobSVCommand ssjsvcmd = SSJobSVCommandFactory.create();
		SSJobSVCommandRequest req = SSJobSVCommandRequestFactory.create();
		req.setCommand(command);
		if (SS_HOST != null || SS_PORT != 0){
			env = alterServerEnv(env);
		}
		req.setEnvironment(env);

		logger.fine("end");
		return ssjsvcmd.execute(req);
	}

	private String[] alterServerEnv(String[] env) throws JobScheduleServiceException {
		String	targetName = "BSC_CLIENT_SERVICE_URL";
		String	schm = "http";
		String	host = null;
		int	port = 8080;
		String	path = "/wsrf/services/BpelWFServiceContainer2";
		
		try {
			host = InetAddress.getLocalHost().getHostAddress();
		}
		catch (UnknownHostException e) {
			host = "127.0.0.1";
		}
		
		if (SS_HOST != null) {
			host = SS_HOST;
		}
		if (SS_PORT != 0) {
			port = SS_PORT;
		}
		URL alternativeURL = null;
		
		try {
			alternativeURL = new URL(schm, host, port, path);
		}
		catch(MalformedURLException e) {
			e.printStackTrace();
			String msg =  "unknown protocol is specified.\n"
						+ "Because : " + e.getMessage();
			throw new JobScheduleServiceException(msg);
		}
		int index = -1;
		
		if (env == null) {
			String newEnv[] = { targetName + "=" + alternativeURL.toString() };
			return newEnv;
		}
		
		for (int i = 0; i < env.length; i++) {
			if (env[i].startsWith(targetName + "=")) {
				index = i;
				break;
			}
		}
		
		if (index != -1) {
			env[index] = targetName + "=" + alternativeURL.toString();
			
			return env;
		}
		else {
			String newEnv[] = new String[env.length + 1];
			
			for (int i = 0; i < env.length; i++) {
				newEnv[i] = env[i];
			}
			newEnv[env.length] = targetName + "=" + alternativeURL.toString();
			
			return newEnv;
		}
	}
	
	private String getPatternMatchString(String src, String regexp) {
		String ret = null;
	
		Pattern pattern = Pattern.compile(regexp);
		String[] retsp = src.split("\n");
	
		int i;
		for (i=0;i < retsp.length;i++) {
			Matcher matcher = pattern.matcher(retsp[i]);
			if (matcher.matches()) {
				break;
			}
		}
		if (i < retsp.length) {
			ret = retsp[i];
		}
		return ret;
	}


	private void removeTempFile(String tempFile, Logger logger) throws JobScheduleServiceException {
		TempFileManager.removeTempFile(tempFile, logger);
	}

	private String changeCalToDateTime(Calendar cal) {
		int yyyy = cal.get(Calendar.YEAR);
		int mm = cal.get(Calendar.MONTH) + 1;
		int dd = cal.get(Calendar.DAY_OF_MONTH);
		int HH = cal.get(Calendar.HOUR_OF_DAY);
		int MM = cal.get(Calendar.MINUTE);
		int SS = cal.get(Calendar.SECOND);

		return "" + yyyy +"-" 
				+ numericReplace(mm) + "-" 
				+ numericReplace(dd) + "T"
				+ numericReplace(HH) + ":" 
				+ numericReplace(MM) + ":" 
				+ numericReplace(SS)
				+ numericReplaceTZ(cal.getTimeZone().getRawOffset())
				;
	}

	private String numericReplace(int v) {
		String s = "00000" + v;
		return s.substring(s.length() - 2);
	}

	private String numericReplaceTZ(long v) {
		String f = "+";
		if (v < 0) {
			v *= -1;
			f = "-";
		}
		int mm = (int) (v / (1000 * 60));
		int HH = mm / 60;
		int MM = mm % 60;
		
		return f 
			+ numericReplace(HH) + ":" 
			+ numericReplace(MM);
	}

	private static synchronized long incrementJobRequestCounter() {
		long ret;
		ret = ++jobRequestCounter;
		return ret;
	}

	private static synchronized long incrementJobSubmitCounter() {
		long ret;
		ret = ++jobSubmitCounter;
		return ret;
	}
	
    /**
     * 
     * Assinging to the Job to the MyProxy+ Server
     * Retrieve the user's proxy credential from MyProxy server and 
     * copy it to the MyProxy+ server for the job exection
     * (Super schduler retrives the proxy credential from MyProxy+ server and
     *  uses to spawn job.)
     * 
     * @param user     user name for retriving proxy credential from MyProxy server
     * @param pass     user passphrase for retriving proxy credential from MyProxy server
     * @param job      user name for putting the new proxy to MyProxy+ server
     * @param job_pass user passphrase for putting the new proxy to MyProxy+ server
     * @param lifetime time to live the proxy in sec.
     * @param logger   handler for managing logging
     * @throws JobScheduleServiceException
     */
	private void userProxy2JobProxy(
            String user, String pass,
            String job, String job_pass,
            int lifetime, Logger logger ) throws JobScheduleServiceException {

        String enable;
        try
        {
        	enable = ConfigManager.getProperty(
        					"user.authentication.enable", "true");
        } catch (Exception e) {
        	enable = "true";
        }

        if (enable.equals("true")) {
        	try {
    			MyProxy myProxy = new MyProxy( MYPROXY_HOST, MYPROXY_PORT );
    			myProxy.setAuthorization( new IdentityAuthorization( MYPROXY_HOST_DN ) );
    			
    		    /***** myproxy-get-delegation *****/
    			logger.fine( "[myproxy-get-delegation] access to "+MYPROXY_HOST+"  ..." );
    		    GSSCredential mCred;			mCred = myProxy.get( user, pass, lifetime );
    			logger.fine( "...get seccussful\n" );
    			
    		    /***** myproxy-init *****/
    		    logger.fine( "[myproxy-init] access to "+MYPROXY_PLUS_HOST+"  ..." );
    		    MyProxy.put( MYPROXY_PLUS_HOST, MYPROXY_PLUS_PORT, 
    		    		mCred, job, job_pass, lifetime, MYPROXY_PLUS_HOST_DN );
    		    logger.fine( "...set seccessful\n" );
    			
    		} catch (MyProxyException e) {
    			e.printStackTrace();
    			String msg =  "Unable to create job proxy cert.\n"
    						+ " user       : " + user + "\n"
    						+ " job account: " + job + "\n"
    						+ "Because : " + e.getMessage();
    			logger.warning(msg);
    			throw new JobScheduleServiceException(msg);
    		}
        }
	}

}

/**
 * Class for managing temporary files.
 */
class TempFileManager {

	private static String LHD = "ss_api_";
	private static String PFX = "tmp";
	private static String LOCK = ".lck";
	private Logger logger;
    private static File	tmpdir;

    TempFileManager(Logger logger) {
    	this.logger = logger;
    }

    public static void initialize() {
        try
        {
            File toptmpdir;
            try
	        {
            	String value = ConfigManager.getProperty("tmp.file.location");
            	toptmpdir = new File(value);
	        } catch (Exception e) {
	        	toptmpdir = null;
	        }
            
        	String user;
            try
	        {
            	user = System.getProperty("user.name",	"");
	        } catch (Exception e) {
            	throw new Exception("Unable to get user name ");
	        }
	        
	        LHD += user;

	        tmpdir = File.createTempFile(LHD, PFX, toptmpdir);
		tmpdir.delete();
		tmpdir.mkdir();

            File lockfile = new File(tmpdir.getAbsolutePath() + LOCK);
            if (lockfile.createNewFile() == false) {
            	throw new IOException("Unable to create lock file " + lockfile.getAbsolutePath());
            }
            lockfile.deleteOnExit();
            
            String saveTmpFile;
            try
	        {
            	saveTmpFile = ConfigManager.getProperty("tmp.file.remain_enable", "false");
	        } catch (Exception e) {
	        	saveTmpFile = "false";
	        }
	        
            if (!saveTmpFile.equals("true")) {
            	removeTmpDir();
            }
            
        }
        catch( Exception e )
        {
            tmpdir = null;
        }
	}
    
	private static void removeTmpDir() throws JobScheduleServiceException {
		
		File dir = getLogDir().getParentFile();

		File[] flist = dir.listFiles(
				new RemoveFileFilter(LHD + ".*" + PFX));
		
		for (int i=0;i < flist.length;i++) {
			File lockfile = new File(flist[i].getAbsolutePath() + LOCK);
			if (lockfile.exists() == false) {
				_removeTmpDir(flist[i]);
				flist[i].delete();
			}
		} 
	}

	private static void _removeTmpDir(File dir)  {
		
		File[] flist = dir.listFiles();
		
		for (int i=0;i < flist.length;i++) {
			if (flist[i].isDirectory() == true) {
				_removeTmpDir(flist[i]);
			} else {
				flist[i].delete();
			}
		} 
	}

    /**
     * Get File type instance which has directory information of
     * storing temporary files.
     * 
     * @return File type instance which has directory information of storing temporary files.
     * @throws JobScheduleServiceException
     */
    public static File  getLogDir() throws JobScheduleServiceException {
    	if (tmpdir == null) {
    		throw new JobScheduleServiceException("Unable to create temporary directory.");
    	}
    	return tmpdir;
    }

	public static void removeTempFile(String tempFile, Logger logger)
		throws JobScheduleServiceException {
		
        String saveTmpFile;
        try
        {
        	saveTmpFile = ConfigManager.getProperty("tmp.file.remain_enable", "false");
        } catch (Exception e) {
        	saveTmpFile = "false";
        }
        
        if (saveTmpFile.equals("true")) {
        	return;
        }
        
		File dir = getLogDir();

		File fl = new File(dir.getAbsolutePath() + "/" + tempFile);
		if (fl.delete() == false) {
			logger.fine("Unable to delete " + fl.getAbsolutePath());
		}
	}

}

/**
 * FilenameFilter class for classifying the file is old temporary one or
 * not.
 * These are used for deleting old temporary files.
 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
 */
class RemoveFileFilter implements FilenameFilter {

	String lockFileReqext;
	public RemoveFileFilter(String lockFileReqext) {
		this.lockFileReqext = lockFileReqext;
	}

	/*
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	public boolean accept(File file, String fname) {
		Pattern pattern = Pattern.compile(lockFileReqext);
		Matcher matcher = pattern.matcher(fname);
		if (matcher.matches()) {
			if (file.isDirectory()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
}
