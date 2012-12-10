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
import java.util.Calendar;
import java.util.List;

import org.glite.ce.creamapi.eventmanagement.Event;
import org.glite.ce.creamapi.jobmanagement.JobStatus;

public interface JobStatusTableInterface {

	public int executeInsert(List<JobStatus> jobStatusList, Connection connection) throws SQLException;
	public JobStatus executeSelectLastJobStatus(String jobId, Connection connection) throws SQLException;
	public List<JobStatus> executeSelectJobStatusHistory(String jobId, Connection connection) throws SQLException;
	public List<JobStatus> executeSelectToRetrieveJobStatus(String fromJobStatusId, String toJobStatusId, Calendar fromDate, Calendar toDate, int maxElements, String iceId, String userId,  Connection connection) throws SQLException;
	public List<Event> executeSelectToRetrieveJobStatusAsEvent(String fromJobStatusId, String toJobStatusId, Calendar fromDate, Calendar toDate, int maxElements, String iceId, String userId, Connection connection) throws SQLException;
	public void executeUpdateStatusHistory(String jobId, List<JobStatus> jobStatusList, Connection connection) throws SQLException;
	public void executeUpdateJobStatus(JobStatus jobStatus, Connection connection) throws SQLException;
	public int executeDelete(String jobId, Connection connection) throws SQLException;
}
