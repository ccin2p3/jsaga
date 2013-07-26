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

package org.glite.ce.creamapi.jobmanagement;

public class JobCommandConstant {
    public static final String JOB_MANAGEMENT = "JOB_MANAGEMENT";
    
    public static final int JOB_REGISTER        = 0;
    public static final int JOB_START           = 1;
    public static final int JOB_CANCEL          = 2;
    public static final int JOB_STATUS          = 3;
    public static final int JOB_SUSPEND         = 4;
    public static final int JOB_RESUME          = 5;
    public static final int JOB_PURGE           = 6;
    public static final int JOB_LIST            = 7;
    public static final int JOB_INFO            = 8;
    public static final int JOB_SET_LEASEID     = 9;
    public static final int SET_LEASE           = 10;
    public static final int GET_LEASE           = 11;
    public static final int DELETE_LEASE        = 12;
    public static final int PROXY_RENEW         = 13;
    public static final int SET_ACCEPT_NEW_JOBS  = 14;
    public static final int DOES_ACCEPT_NEW_JOBS = 15;
    public static final int GET_SERVICE_INFO     = 16;
    
    public static final String[] cmdName = new String[] { "JOB_REGISTER", 
                                                          "JOB_START", 
                                                          "JOB_CANCEL", 
                                                          "JOB_STATUS", 
                                                          "JOB_SUSPEND", 
                                                          "JOB_RESUME", 
                                                          "JOB_PURGE", 
                                                          "JOB_LIST", 
                                                          "JOB_INFO", 
                                                          "JOB_SET_LEASEID", 
                                                          "SET_LEASE", 
                                                          "GET_LEASE", 
                                                          "DELETE_LEASE", 
                                                          "PROXY_RENEW",
                                                          "SET_ACCEPT_NEW_JOBS",
                                                          "DOES_ACCEPT_NEW_JOBS",
                                                          "GET_SERVICE_INFO"
                                                         };
}
