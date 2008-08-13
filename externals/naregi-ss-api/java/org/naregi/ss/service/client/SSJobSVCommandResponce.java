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
 *      Id: SSJobSVCommandResponce.java 882 2007-03-29 10:58:42Z kawamura 
 */
package org.naregi.ss.service.client;

/**
 * Interface class for managing the result of the SuperScheduler job operation commands.
 * 
 * In this class, manage the command stdout/stderr output and exit code.
 * 
 */
public interface SSJobSVCommandResponce {

	/**
	 * Set stdout of job operation commnad
	 * 
	 * @param stdout Stdout
	 */
	public void setStdout(String stdout);

	/**
	 * Get stdout of job operation commnad
	 * 
	 * @return Stdout
	 */
	public String getStdout();

	/**
	 * Set stderr of job operation command
	 * 
	 * @param stderr Stderr
	 */
	public void setStderr(String stderr);

	/**
	 * Get stderr of job operation command
	 * 
	 * @return Stderr
	 */
	public String getStderr();

	/**
	 * Set command exit code
	 * @param value Exit Code
	 */
	public void setExitCode(int value);

	/**
	 * Get command exit code
	 * 
	 * @return Exit Code
	 */
	public int getExitCode();
	
}
