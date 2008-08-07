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
 *      $Id: SSJobSVCommand.java,v 1.1 2008/08/07 15:24:32 sreynaud Exp $
 */
package org.naregi.ss.service.client;

/**
 * Interface class for managing the SuperScheduler operation commands.
 */
public interface SSJobSVCommand {

	/**
	 * Execute the SuperScheduler job operation command.
	 * 
	 * @param request parametes for executing job operation command
	 * @return result of command execution
	 * @throws JobScheduleServiceException failed to execution of Job operation command
	 */
	public SSJobSVCommandResponce execute(SSJobSVCommandRequest request) 
		throws JobScheduleServiceException;

}
