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
import java.util.Hashtable;

public interface ExtraAttributeTableInterface {
	public int executeInsert(String jobId, Hashtable<String, String> extraAttribute, Connection connection) throws SQLException;
	public int executeInsert(String jobId, String name, String value, Connection connection) throws SQLException;
	public void executeUpdate(String jobId, Hashtable<String, String> extraAttribute, Connection connection) throws SQLException;
	public int executeUpdate(String jobId, String name, String value, Connection connection) throws SQLException;
	public int executeDelete(String jobId, Connection connection) throws SQLException;
	public Hashtable<String, String> executeSelect(String jobId, Connection connection) throws SQLException;
}
