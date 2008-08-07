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
 *      $Id: JobScheduleServiceException.java,v 1.1 2008/08/07 15:24:32 sreynaud Exp $
 */
package org.naregi.ss.service.client;

/**
 * 
 * Exception for job scheduling service API error.
 * 
 */
public class JobScheduleServiceException extends Exception {

	/**
	 * Constructor
	 * Create exception class for job scheduling service API error.
	 * @param string
	 */
	public JobScheduleServiceException(String string) {
		super(string);
	}

	/**
	 * Get error messages.
	 * 
	 * @return error messages
	 */
	public String getMessage() {
		return super.getMessage();
	}
	
}
