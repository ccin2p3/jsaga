/*
 * Copyright (c) 2004 on behalf of the EU EGEE Project:
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
 * Authors: Eric Frizziero (eric.frizziero@pd.infn.it) 
 */

package org.glite.ce.creamapi.jobmanagement.cmdexecutor.command;

public class JobIdFilterFailure {
   public static final int OK_ERRORCODE           = 0;
   public static final int JOBID_ERRORCODE        = 1;
   public static final int STATUS_ERRORCODE       = 2;
   public static final int LEASEID_ERRORCODE      = 3;
   public static final int DELEGATIONID_ERRORCODE = 4;
   public static final int DATE_ERRORCODE         = 5;
   
   
   public static final String[] failureReason = new String[] { null,
	                                                           "JobId unknown",
	                                                           "Status not compatible",
	                                                           "LeaseId Mismatch",
	                                                           "DelegationId Mismatch",
	                                                           "Date Mismatch"
                                                             };

}
