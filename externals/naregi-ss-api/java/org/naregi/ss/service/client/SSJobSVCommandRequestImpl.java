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
 *      Id: SSJobSVCommandRequestImpl.java 882 2007-03-29 10:58:42Z kawamura 
 */
package org.naregi.ss.service.client;

/**
*
* Implementation class for SSJobSVCommandRequest interface
* 
*/
public class SSJobSVCommandRequestImpl implements SSJobSVCommandRequest {

	private String[] command = null;
	
	/* 
	 * @see org.naregi.ss.service.client.SSJobSVCommandRequest#setCommand(java.lang.String)
	 */
	public void setCommand(String[] command) {
		this.command = command;
	}

	/* 
	 * @see org.naregi.ss.service.client.SSJobSVCommandRequest#getCommand()
	 */
	public String[] getCommand() {
		return this.command;
	}

	private String[] environment = null;
	
	/*
	 * @see org.naregi.ss.service.client.SSJobSVCommandRequest#setEnvironment(java.lang.String[])
	 */
	public void setEnvironment(String[] envArray) {
		this.environment = envArray;
	}

	/*
	 * @see org.naregi.ss.service.client.SSJobSVCommandRequest#getEnvironment()
	 */
	public String[] getEnvironment() {
		return this.environment;
	}

}
