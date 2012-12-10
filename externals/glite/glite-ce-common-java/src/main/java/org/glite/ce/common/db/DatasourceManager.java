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
 */

package org.glite.ce.common.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 * Class to manage <code>JDBC</code> datasources. Connection pooling is used.
 * <code>ConnectionManager</code> can register a valid data source
 * configuration.
 */
public class DatasourceManager {
    /** The logger */
    private static final Logger logger = Logger.getLogger(DatasourceManager.class);

    /** The datasource cache */
    private static final HashMap<String, DataSource> datasourceCache = new HashMap<String, DataSource>(0);

    public static void destroy() {
        datasourceCache.clear();
    }

    /**
     * Returns a connection from the connection pool associated to a given
     * datasource name.
     * 
     * @param datasourceName
     *            The datasource name.
     * @return a connection from the connection pool associated to a given
     *         datasource name.
     * @throws SQLException
     * @throws DatabaseException
     */
    public static Connection getConnection(String datasourceName) throws DatabaseException {
        if (datasourceName == null) {
            throw new DatabaseException("datasourceName not specified!");
        }

        logger.debug("getConnection " + datasourceName);

        DataSource dataSource = null;
        Connection connection = null;

        try {
            if (!datasourceCache.containsKey(datasourceName)) {
                logger.debug("Search datasource in JNDI context");

                Hashtable<String, String> env = new Hashtable<String, String>(0);
                env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
                env.put(Context.URL_PKG_PREFIXES, "org.apache.naming");

                Context context = new InitialContext(env);

                if (context == null) {
                    throw new DatabaseException("Cannot create the root context");
                }

                dataSource = (DataSource) context.lookup(datasourceName);

                if (dataSource != null) {
                    datasourceCache.put(datasourceName, dataSource);
                } else {
                    logger.debug("lookup: " + datasourceName + " not found!");
                    throw new DatabaseException("Datasource " + datasourceName + " not found");
                }
            } else {
                logger.debug("Search datasource in cache");
                dataSource = datasourceCache.get(datasourceName);
            }

            connection = dataSource.getConnection();

            if (connection != null) {
                logger.debug("Connection got from datasource named: " + datasourceName);
                connection.setAutoCommit(false);
            } else {
                logger.debug("Problem in opening connection to target database");
                throw new DatabaseException("Problem in opening connection to target database [" + datasourceName + "]");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            throw new DatabaseException("getConnection error: " + e.getMessage());
        }

        return connection;
    }
}
