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

public interface JobTableJobStatusInterface {
	public List<String> executeSelectToRetrieveJobId(String userId, List<String> jobId, String delegationId, int[] jobStatusType, String leaseId, Calendar startStatusDate, Calendar endStatusDate, String queueName, String batchSystem, Connection connection) throws SQLException;
	public List<String> executeSelectToRetrieveJobIdByLeaseTimeExpiredQuery(String userId, String delegationId, int[] jobStatusType, Connection connection) throws SQLException;
	public long executeSelectToJobCountByStatus(int[] jobStatusType, String userId, Connection connection) throws SQLException;
    public String executeSelectToRetrieveOlderJobIdQuery(int[] jobStatusType, String batchSystem, String userId, Connection connection) throws SQLException;
}
