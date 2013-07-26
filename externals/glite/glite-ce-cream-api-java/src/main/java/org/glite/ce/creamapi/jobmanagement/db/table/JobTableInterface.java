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

package org.glite.ce.creamapi.jobmanagement.db.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.glite.ce.creamapi.jobmanagement.Job;
import org.glite.ce.creamapi.jobmanagement.Lease;

public interface JobTableInterface {
	public int executeInsert(Job job, Connection connection) throws SQLException;
	public boolean isUserEnable(String jobId, String userId, Connection connection);
	public List<String> executeSelectToRetrieveJobId(String userId, List<String> jobId, String leaseId, String delegationId,  List<String> gridJobId, Connection connection) throws SQLException;
	public int executeUpdate(Job job, Connection connection) throws SQLException;
	public int executeDelete(String jobId, Connection connection) throws SQLException;
    public Job executeSelectJobTable(String jobId, String userId, Connection connection)throws SQLException;
    public void setLeaseExpired(String jobId, Lease jobLease, Connection connection)throws SQLException;
	public String getReasonFaultSetLeaseId(String jobId, String userId, Connection connection)throws SQLException;
    public String getLeaseId(String jobId, String userId, Connection connection)throws SQLException;
} 
