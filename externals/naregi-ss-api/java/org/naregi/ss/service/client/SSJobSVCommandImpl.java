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
 *      $Id: SSJobSVCommandImpl.java,v 1.1 2008/08/07 15:24:32 sreynaud Exp $
 */
package org.naregi.ss.service.client;

import java.io.*;
import java.util.logging.Logger;

import org.naregi.ss.service.client.logging.LoggerManager;

/**
*
* Implimentation class for SSJobSVCommand interface
* 
*/
public class SSJobSVCommandImpl implements SSJobSVCommand {

	/* 
	 * @see org.naregi.ss.service.client.SSJobSVCommand#execute(org.naregi.ss.service.client.SSJobSVCommandRequest)
	 */
	public SSJobSVCommandResponce execute(SSJobSVCommandRequest request) throws JobScheduleServiceException {
		SSJobSVCommandResponce resp = SSJobSVCommandResponceFactory.create();
		
		Logger logger = LoggerManager.getLogger();
		logger.fine("start");
		
		logRequest(logger, request);

		Runtime rtm = Runtime.getRuntime();
		Process pc = null;
		String[] cmd = request.getCommand();
		String[] env = request.getEnvironment();

		logger.fine("Command Execute start.");
		try {
			pc = rtm.exec(cmd, env, null);
		} catch (IOException e) {
			e.printStackTrace();
			String msg = getErrorMessage("Job Execute Request Error!", cmd[0], e);
			logger.severe(msg);
			
			throw new JobScheduleServiceException(msg);
		}

		try {
			SSJobSVCommandStdoutStderrCheck checkStderr = 
				new SSJobSVCommandStdoutStderrCheck(pc.getErrorStream(), logger);
			Thread thStderr = new Thread(checkStderr);
			thStderr.start();

			SSJobSVCommandStdoutStderrCheck checkStdout = 
				new SSJobSVCommandStdoutStderrCheck(pc.getInputStream(), logger);
			checkStdout.run();

			while (true) {
				try {
					thStderr.join();
					break;
				} catch (InterruptedException e) {}
			}
			if (checkStdout.getError() != null) {
				String msg = getErrorMessage("SS Command Execute Stdout IO Error!", cmd[0], 
								checkStdout.getError());
				logger.severe(msg);
	
				throw new JobScheduleServiceException(msg);
			}
			if (checkStderr.getError() != null) {
				String msg = getErrorMessage("SS Command Execute Stderr IO Error!", cmd[0], 
								checkStderr.getError());
				logger.severe(msg);
	
				throw new JobScheduleServiceException(msg);
			}

			int exitCode = 0;
			while(true) {
				try {
					logger.fine("Command Wait start");
					pc.waitFor();
					break;
				} catch (InterruptedException e) {
					logger.fine("Command Wait Interrupted :" + e.getMessage());
					continue;
				}
			}
			exitCode = pc.exitValue();
			logger.fine("ExitCode = " + exitCode);
			
			logger.fine("Command Execute end.");

			resp.setStdout(checkStdout.getResult());
			resp.setStderr(checkStderr.getResult());
			resp.setExitCode(exitCode);
					
			logResponce(logger, resp);
		} finally {
			pc.destroy();
			pc = null;
			logger.fine("end");
		}
		return resp;
	}
	
	private void logRequest(Logger logger, SSJobSVCommandRequest req) {
		String[] cmd = req.getCommand();
		String cmd_line = "";
		
		for (int i=0;i < cmd.length; i++) {
			cmd_line += cmd[i] + " ";
		}
		logger.info("Execute Command = " + cmd_line);
	}
	
	private void logResponce(Logger logger, SSJobSVCommandResponce resp) {
		logger.fine("ExitCode = " + resp.getExitCode());
		
		if (resp.getStdout() == null) {
			logger.fine("Stdout = null");
		} else {
			logger.fine("Stdout = " + resp.getStdout());
		}
		
		if (resp.getStderr() == null) {
			logger.fine("Stderr = null");
		} else {
			logger.fine("Stderr = " + resp.getStderr());
		}
	}
	
	private String getErrorMessage(String msg, String cmd, Exception e) {
		return msg 
				+ "Because : " + e.getMessage()
				+ "[Command = " + cmd + "]\n";
	}
}
class SSJobSVCommandStdoutStderrCheck implements Runnable {
	private InputStream is = null;
	private Logger logger = null;
	private StringBuffer sb = new StringBuffer();
	private Exception ex = null;

	SSJobSVCommandStdoutStderrCheck(InputStream is, Logger logger) {
		this.is = is;
		this.logger = logger;
	}

	public String getResult() {
		return this.sb.toString();
	}

	public Exception getError() {
		return this.ex;
	}

	public void run() {
		String line = null;
		BufferedReader br = new BufferedReader(
					new InputStreamReader(is));

		try {
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
				if (false) {
					System.out.println(line);
				}
				logger.finest(line);
			}
		} catch (IOException e) {
			this.ex = e;
		}
	}
}
