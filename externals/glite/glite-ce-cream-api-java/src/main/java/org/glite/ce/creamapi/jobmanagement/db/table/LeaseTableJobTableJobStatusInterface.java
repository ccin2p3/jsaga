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

public interface LeaseTableJobTableJobStatusInterface {

	public List<String> executeSelectToRetrieveJobIdByLease(int[] jobStatusType, String leaseId, Calendar maxLeaseTime, String userId, Connection connection) throws SQLException;
}

