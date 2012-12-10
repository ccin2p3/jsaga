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
 * Authors: Silvano Squizzato (silvano.squizzato@pd.infn.it) 
 */

package org.glite.ce.common.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * Interface for a generic to access a database.
 *
 */
public interface TableInterface  {
    
    /**
     * Returns from the named sequence.
     * @param sequenceName The name of the sequence.
     * @connection The connection.
     * @return The id from the named sequence.
     * @throws SQLException
     */
    public long executeSequenceId(String sequenceName, Connection connection) throws SQLException;
    
    /**
     * Returns the prepared statement for the given query.
     * @return The prepared statement for the given query.
     * @param query The query to be executed.
     * @connection The connection.
     * @throws SQLException
     */
    public PreparedStatement getPreparedStatement(Query query, Connection connection) throws SQLException;
}
