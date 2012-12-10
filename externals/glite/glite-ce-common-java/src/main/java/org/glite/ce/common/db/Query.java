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

/**
 * Class representing a query.
 *
 */
public class Query {
    
    /** Label for sequenceId prepared statement */
    public static final String SEQUENCE_ID_STATEMENT = "SEQUENCE_ID_STATEMENT";
    
    /** Label for insert prepared statement */
    public static final String INSERT_STATEMENT = "INSERT_STATEMENT";
    
    /** Label for delete prepared statement */
    public static final String DELETE_STATEMENT = "DELETE_STATEMENT";
    
    /** Label for delete prepared statement */
    public static final String UPDATE_STATEMENT = "UPDATE_STATEMENT";
    
    /** Label for select prepared statement */
    public static final String SELECT_STATEMENT = "SELECT_STATEMENT";
    
    /** Query name */
    private String name = null;
    
    /** SQL statement */
    private String statement = null;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the statement
     */
    public String getStatement() {
        return statement;
    }

    /**
     * @param statement the statement to set
     */
    public void setStatement(String statement) {
        this.statement = statement;
    }
}
