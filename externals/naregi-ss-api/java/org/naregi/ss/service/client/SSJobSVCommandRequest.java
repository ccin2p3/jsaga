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
 *      Id: SSJobSVCommandRequest.java 882 2007-03-29 10:58:42Z kawamura 
 */
package org.naregi.ss.service.client;

/**
 * Interface class for managing some parameters which required to operate
 * the SuperScheduler job commands.
 * 
 * Manage the job operation commands and the environment variables at
 * command execution.
 * 
 */
public interface SSJobSVCommandRequest {

	/**
	 * Set spawning command
	 * 
	 * @param commandArray array for command line arguments
	 */
	public void setCommand(String[] commandArray);
	
	/**
	 * Get spawning command
	 * 
	 * @return string array for command line arguments
	 */
	public String[] getCommand();

	/**
	 * Set environment variables for command spawning
	 * 
	 * @param envArray arrary for envirionment variables and values
	 *  				the environment variables are specified as "variable=value" (for example: "ENV1=123")
	 */
	public void setEnvironment(String[] envArray);
	
	/**
	 * Get environment variables at spawning a command.
	 * 
	 * @return arrary for envirionment variables and values
	 *  				the environment variables are specified as "variable=value" (for example: "ENV1=123")
	 */
	public String[] getEnvironment();

}
