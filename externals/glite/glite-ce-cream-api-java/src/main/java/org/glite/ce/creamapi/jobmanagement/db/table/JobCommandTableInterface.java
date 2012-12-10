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

import org.glite.ce.creamapi.jobmanagement.JobCommand;

public interface JobCommandTableInterface {
	public int executeInsert(List<JobCommand> jobStatusList, Connection connection) throws SQLException;
	public JobCommand executeSelectLastJobCommmand(String jobId, Connection connection) throws SQLException;
	public List<String> executeSelectToRetrieveJobIdByDate(List<String> jobId, String userId, Calendar startDate, Calendar endDate, Connection connection) throws SQLException;
	public List<JobCommand> executeSelectJobCommandHistory(String jobId, Connection connection) throws SQLException;
	public void executeUpdateCommandHistory(String jobId, List<JobCommand> jobStatusList, Connection connection) throws SQLException;
	public void executeUpdateJobCommand(JobCommand jobCommand, Connection connection) throws SQLException;
    public int executeUpdateAllUnterminatedJobCommandQuery(int newStatus, int[] oldStatus, String failureReason, Calendar executionCompletedTime, Connection connection) throws SQLException;
	public int executeDelete(String jobId, Connection connection) throws SQLException;
}
