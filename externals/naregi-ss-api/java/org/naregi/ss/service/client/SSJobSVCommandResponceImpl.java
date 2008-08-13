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
 *      Id: SSJobSVCommandResponceImpl.java 882 2007-03-29 10:58:42Z kawamura 
 */
package org.naregi.ss.service.client;

/**
*
* Implementation class for SSJobSVCommandResponce interface
* 
*/
public class SSJobSVCommandResponceImpl implements SSJobSVCommandResponce {

	private String stdout = null;
	/*
	 * @see org.naregi.ss.service.client.SSJobSVCommandResponce#setStdout(java.lang.String)
	 */
	public void setStdout(String stdout) {
		this.stdout = stdout;
	}

	/*
	 * @see org.naregi.ss.service.client.SSJobSVCommandResponce#getStdout()
	 */
	public String getStdout() {
		return this.stdout;
	}

	private String stderr = null;
	/*
	 * @see org.naregi.ss.service.client.SSJobSVCommandResponce#setStderr(java.lang.String)
	 */
	public void setStderr(String stderr) {
		this.stderr = stderr;
	}

	/*
	 * @see org.naregi.ss.service.client.SSJobSVCommandResponce#getStderr()
	 */
	public String getStderr() {
		return this.stderr;
	}

	private int exitCode = 0;
	/*
	 * @see org.naregi.ss.service.client.SSJobSVCommandResponce#setExitCode(int)
	 */
	public void setExitCode(int value) {
		this.exitCode = value;
	}

	/*
	 * @see org.naregi.ss.service.client.SSJobSVCommandResponce#getExitCode()
	 */
	public int getExitCode() {
		return this.exitCode;
	}

}
