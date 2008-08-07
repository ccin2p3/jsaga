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
 *      $Id: SSJobSVCommandFactory.java,v 1.1 2008/08/07 15:24:32 sreynaud Exp $
 */
package org.naregi.ss.service.client;

/**
 * 
 * Create SSJobSVCommand instance
 * 
 */
public class SSJobSVCommandFactory {

	/**
	 * 
	 * Create SSJobSVCommand instance
	 * 
	 * @return SSJobSVCommand instance
	 * 
	 */
	public static SSJobSVCommand create() {

		return new SSJobSVCommandImpl();
		
	}

}
