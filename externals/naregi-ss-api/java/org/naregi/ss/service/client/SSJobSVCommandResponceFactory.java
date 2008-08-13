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
 *      Id: SSJobSVCommandResponceFactory.java 882 2007-03-29 10:58:42Z kawamura 
 */
package org.naregi.ss.service.client;

/**
 * 
 * Create SSJobSVCommandResponce instance
 * 
 */
public class SSJobSVCommandResponceFactory {

	/**
	 * 
	 * Create SSJobSVCommandResponce instance
	 * 
	 * @return SSJobSVCommandResponce instance
	 * 
	 */
	public static SSJobSVCommandResponce create() {

		return new SSJobSVCommandResponceImpl();
		
	}
}
