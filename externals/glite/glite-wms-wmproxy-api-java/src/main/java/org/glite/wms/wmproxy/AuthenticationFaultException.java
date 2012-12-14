/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */
/*
 * Copyright (c) 2005 on behalf of the EU EGEE Project:
 * The European Organization for Nuclear Research (CERN),
 * Istituto Nazionale di Fisica Nucleare (INFN), Italy
 * Datamat Spa, Italy
 * Centre National de la Recherche Scientifique (CNRS), France
 * CS Systeme d'Information (CSSI), France
 * Royal Institute of Technology, Center for Parallel Computers (KTH-PDC), Sweden
 * Universiteit van Amsterdam (UvA), Netherlands
 * University of Helsinki (UH.HIP), Finland
 * University of Bergen (UiB), Norway
 * Council for the Central Laboratory of the Research Councils (CCLRC), United Kingdom
 * 
 * Authors: Marco Sottilaro (marco.sottilaro@datamat.it)
 */


package org.glite.wms.wmproxy;

/**
 * This exception is thrown to indicate that an authentication error occurs while accessing the WMProxy services.
 * An authentication error can happen, for example, when the user credentials is either invalid or expired.
*/

public class AuthenticationFaultException extends BaseException {
	/**
	* Constructs an Exception with no specified detail message.
	*/
	public AuthenticationFaultException () {
		super();
	}

	/**
	* Constructs an Exception with the specified detail message.
	*/
	public AuthenticationFaultException(String message) {
		super(message);
	}
}
