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
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;


/**
 * Class representing a generic Data Access Object accessing the database.
 *
 */
public class AbstractTable implements TableInterface {
    
    /** The logger */
    private final static Logger logger = Logger.getLogger(AbstractTable.class);
    
    /** The sequence id query */
    protected Query sequenceIdQuery = null;
    
    /** The insert query */
    protected Query insertQuery = null;
    
    /** The delete query */
    protected Query deleteQuery = null;
    
    /** The update query */
    protected Query updateQuery = null;
    
    /** The select query */
    protected Query selectQuery = null;
 
    /**
     * Returns the query for retrieving the id from a sequence.
     * @param sequenceName The sequence name.
     * @return The query for retrieving the id from a sequence.
     */
    protected Query getSequenceIdQuery(String sequenceName) {
        if (sequenceIdQuery == null) {
            sequenceIdQuery = new Query();
            sequenceIdQuery.setName(Query.SEQUENCE_ID_STATEMENT);
            sequenceIdQuery.setStatement("select nextVal('" + sequenceName + "')");
        }
        logger.debug("sequenceIdQuery = " + sequenceIdQuery.getStatement());
        return sequenceIdQuery;
    }
    
    /**
     * @see org.glite.ce.common.db.TableInterface#getPreparedStatement(org.glite.ce.common.db.Query, java.sql.Connection)
     */
    public PreparedStatement getPreparedStatement(Query query, Connection connection) throws SQLException, IllegalArgumentException {
        if (query == null) {
            throw new IllegalArgumentException("query not specified!");
        }
        if (query.getName() == null) {
            throw new IllegalArgumentException("query name not specified!");
        }
        if (query.getStatement() == null) {
            throw new IllegalArgumentException("query statement not specified!");
        }
        if (connection == null) {
            throw new IllegalArgumentException("connection not specified!");
        }

        PreparedStatement statement = connection.prepareStatement(query.getStatement());
        
        if(statement == null) {
            throw new SQLException("cannot create a PrepareStatement for the query \"" + query.getStatement() + "\"");
        }
        
        return statement;
    }

    /**
     * @see org.glite.ce.common.db.TableInterface#executeSequenceId(java.lang.String, java.sql.Connection)
     */
    public long executeSequenceId(String sequenceName, Connection connection) throws SQLException, IllegalArgumentException {
        if (sequenceName == null) {
            throw new IllegalArgumentException("sequenceName not specified!");
        }
        if (connection == null) {
            throw new IllegalArgumentException("connection not specified!");
        }
        
        long id = -1;
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        
        try {
            // Get the id from the sequence named sequenceName
            pstmt = getPreparedStatement(getSequenceIdQuery(sequenceName), connection);
            rset = pstmt.executeQuery();
            if (rset != null) {
                rset.next();
                id = rset.getLong(1);
            }
        } catch (SQLException sqle) {
            throw sqle;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException sqle1) {
                    logger.error(sqle1);
                }
            }
            if (rset != null) {
                try {
                    rset.close();
                } catch (SQLException sqle2) {
                    logger.error(sqle2);
                }
            }
        }
        return id;
    }
}
