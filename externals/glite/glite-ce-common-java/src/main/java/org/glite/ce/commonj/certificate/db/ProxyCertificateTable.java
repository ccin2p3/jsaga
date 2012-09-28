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

package org.glite.ce.commonj.certificate.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.glite.ce.common.db.AbstractTable;
import org.glite.ce.common.db.Query;
import org.glite.ce.commonj.certificate.ProxyCertificate;
import org.glite.ce.commonj.certificate.ProxyCertificate.ProxyCertificateType;

/**
 * Class representing the Data Access Object (DAO) to the authentication table
 * in the database.
 * 
 */
public class ProxyCertificateTable extends AbstractTable implements ProxyCertificateTableInterface {
    /** The logger */
    private static final Logger logger = Logger.getLogger(ProxyCertificateTable.class);

    /** The table name */
    public static final String NAME_TABLE = "proxy_certificate";

    // The table columns
    public static final String ID_FIELD = "id";
    public static final String DN_FIELD = "DN";
    public static final String FQAN_FIELD = "FQAN";
    public static final String VO_FIELD = "VO";
    public static final String DESCRIPTION_FIELD = "description";
    public static final String PROXY_CERTIFICATE_FIELD = "proxy_cert";
    public static final String START_TIME_FIELD = "start_time";
    public static final String EXPIRATION_TIME_FIELD = "expiration_time";
    public static final String MODIFICATION_TIME_FIELD = "modification_time";
    public static final String IS_ADMIN_FIELD = "is_admin";
    public static final String PROXY_TYPE_FIELD = "proxy_type";

    /** Query name to check if a <code>id</code> exists in the db */
    public static final String IS_USERID_PRESENT = "IS_USERID_PRESENT";

    /** Query to check if a <code>id</code> exists in the db */
    protected Query isIdPresentQuery = null;

    public void executeDelete(String id, String DN, String FQAN, Connection connection) throws SQLException, IllegalArgumentException {
        logger.debug("executeDelete begin");
        PreparedStatement pstmt = null;

        if (id == null) {
            throw new IllegalArgumentException("id not specified!");
        }
        if (DN == null) {
            throw new IllegalArgumentException("DN not specified!");
        }
        if (FQAN == null) {
            throw new IllegalArgumentException("FQAN not specified!");
        }

        try {
            pstmt = getPreparedStatement(getDeleteQuery(), connection);
            pstmt.setString(1, id);
            pstmt.setString(2, DN);
            pstmt.setString(3, FQAN);

            int rowCount = pstmt.executeUpdate();
            logger.debug("deleted " + rowCount + " rows");
        } catch (SQLException sqle) {
            logger.error("executeDelete error: " + sqle.getMessage());
            throw sqle;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException sqle1) {
                    logger.error(sqle1.getMessage());
                }
            }
        }
        logger.debug("executeDelete end");
    }

    public void executeInsert(ProxyCertificate proxyCertificate, Connection connection) throws SQLException, IllegalArgumentException {
        logger.debug("executeInsert begin");
        if (proxyCertificate == null) {
            throw new IllegalArgumentException("proxyCertificate not specified!");
        }
        if (proxyCertificate.getId() == null) {
            throw new IllegalArgumentException("id not specified!");
        }
        if (proxyCertificate.getDN() == null) {
            throw new IllegalArgumentException("DN not specified!");
        }
        if (proxyCertificate.getFQAN() == null) {
            throw new IllegalArgumentException("FQAN not specified!");
        }
        if (proxyCertificate.getVO() == null) {
            throw new IllegalArgumentException("VO not specified!");
        }
        if (proxyCertificate.getCertificate() == null) {
            throw new IllegalArgumentException("proxyCert not specified!");
        }
        if (proxyCertificate.getDescription() == null) {
            throw new IllegalArgumentException("description not specified!");
        }
        if (proxyCertificate.getStartTime() == null) {
            throw new IllegalArgumentException("startTime not specified!");
        }
        if (proxyCertificate.getExpirationTime() == null) {
            throw new IllegalArgumentException("expirationTime not specified!");
        }

        PreparedStatement pstmt = null;
        try {
            pstmt = getPreparedStatement(getInsertQuery(), connection);
            pstmt.setString(1, proxyCertificate.getId());
            pstmt.setString(2, proxyCertificate.getDN());
            pstmt.setString(3, proxyCertificate.getFQAN());
            pstmt.setString(4, proxyCertificate.getVO());
            pstmt.setString(5, proxyCertificate.getCertificate());
            pstmt.setString(6, proxyCertificate.getDescription());
            pstmt.setTimestamp(7, new Timestamp(proxyCertificate.getStartTime().getTimeInMillis()));
            pstmt.setTimestamp(8, new Timestamp(proxyCertificate.getExpirationTime().getTimeInMillis()));
            pstmt.setTimestamp(9, new Timestamp((new GregorianCalendar(TimeZone.getTimeZone("GMT"))).getTimeInMillis()));
            pstmt.setInt(10, proxyCertificate.getProxyCertificateType().getId());
            pstmt.setBoolean(11, proxyCertificate.isAdministrator());

            int rowCount = pstmt.executeUpdate();
            logger.debug("inserted " + rowCount + " rows");
        } catch (SQLException sqle) {
            logger.error("executeInsert error: " + sqle.getMessage());
            throw sqle;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException sqle1) {
                    logger.error(sqle1.getMessage());
                }
            }
        }
        logger.debug("executeInsert end");
    }

    /**
     * Returns true if a <code>id, DN, FQAN</code> already exists.
     * 
     * @param id
     *            The <code>id</code>.
     * @param connection
     *            The connection to db.
     * @return
     * @throws SQLException
     * @throws IllegalArgumentException
     */
    public boolean executeDoesCertificateExist(String id, String DN, String FQAN, Connection connection) throws SQLException, IllegalArgumentException {
        boolean result = false;
        logger.debug("executeIsIdPresent begin");
        PreparedStatement pstmt = null;
        ResultSet rset = null;

        if (id == null) {
            throw new IllegalArgumentException("id not specified!");
        }
        if (DN == null) {
            throw new IllegalArgumentException("DN not specified!");
        }
        if (FQAN == null) {
            throw new IllegalArgumentException("FQAN not specified!");
        }

        try {
            // Check if a queue name is registered
            pstmt = getPreparedStatement(getIsIdPresentQuery(), connection);
            pstmt.setString(1, id);
            pstmt.setString(2, DN);
            pstmt.setString(3, FQAN);

            rset = pstmt.executeQuery();

            if (rset != null) {
                rset.next();

                if (rset.getRow() > 0) {
                    long count = rset.getLong(1);

                    if (count > 0) {
                        result = true;
                    }
                }
            }
            logger.debug("check result = " + result);
        } catch (SQLException sqle) {
            logger.error("executeDoesCertificateExist error: " + sqle.getMessage());
            throw sqle;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException sqle1) {
                    logger.error(sqle1.getMessage());
                }
            }
            if (rset != null) {
                try {
                    rset.close();
                } catch (SQLException sqle2) {
                    logger.error(sqle2.getMessage());
                }
            }
        }
        logger.debug("executeIsIdPresent end");
        return result;
    }

    public List<ProxyCertificate> executeSelect(String DN, String FQAN, ProxyCertificateType proxyType, Connection connection) throws SQLException, IllegalArgumentException {
        logger.debug("executeSelect begin");
        PreparedStatement pstmt = null;
        ResultSet rset = null;

        List<ProxyCertificate> result = new ArrayList<ProxyCertificate>(1);

        try {
            pstmt = getPreparedStatement(getSelectQuery(null, DN, FQAN, proxyType), connection);
            rset = pstmt.executeQuery();

            if (rset == null) {
                logger.error("prepared statement null");
            } else {
                while (rset.next()) {
                    result.add(makeProxyCertificate(rset));
                }
            }
        } catch (SQLException sqle) {
            logger.error("executeSelect error: " + sqle.getMessage());
            throw sqle;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException sqle1) {
                    logger.error(sqle1.getMessage());
                }
            }
            if (rset != null) {
                try {
                    rset.close();
                } catch (SQLException sqle2) {
                    logger.error(sqle2.getMessage());
                }
            }
        }

        logger.debug("executeSelectByDN end");
        return result;
    }

    public ProxyCertificate executeSelect(String id, String DN, String FQAN, Connection connection) throws SQLException, IllegalArgumentException {
        logger.debug("executeSelect begin");
        ProxyCertificate proxyCertificate = null;
        PreparedStatement pstmt = null;
        ResultSet rset = null;

        if (id == null) {
            throw new IllegalArgumentException("id not specified!");
        }
        if (DN == null) {
            throw new IllegalArgumentException("DN not specified!");
        }
        if (FQAN == null) {
            throw new IllegalArgumentException("FQAN not specified!");
        }

        try {
            pstmt = getPreparedStatement(getSelectQuery(id, DN, FQAN, null), connection);

            rset = pstmt.executeQuery();

            if (rset != null && rset.next()) {
                proxyCertificate = makeProxyCertificate(rset);
            }
        } catch (SQLException sqle) {
            logger.error("executeSelect error: " + sqle.getMessage());
            throw sqle;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException sqle1) {
                    logger.error(sqle1.getMessage());
                }
            }
            if (rset != null) {
                try {
                    rset.close();
                } catch (SQLException sqle2) {
                    logger.error(sqle2.getMessage());
                }
            }
        }
        logger.debug("executeSelect end");
        return proxyCertificate;
    }

    /**
     * Updates the authentication information.
     * 
     * @param authenticationInfo
     *            The authentication information.
     * @param connection
     *            The connection to db.
     * @throws SQLException
     * @throws IllegalArgumentException
     */
    public void executeUpdate(ProxyCertificate proxyCertificate, Connection connection) throws SQLException, IllegalArgumentException {
        logger.debug("executeUpdate begin");

        if (proxyCertificate == null) {
            throw new IllegalArgumentException("proxyCertificate not specified!");
        }
        if (proxyCertificate.getId() == null) {
            throw new IllegalArgumentException("id not specified!");
        }
        if (proxyCertificate.getDN() == null) {
            throw new IllegalArgumentException("DN not specified!");
        }
        if (proxyCertificate.getFQAN() == null) {
            throw new IllegalArgumentException("FQAN not specified!");
        }
        if (proxyCertificate.getVO() == null) {
            throw new IllegalArgumentException("VO not specified!");
        }
        if (proxyCertificate.getCertificate() == null) {
            throw new IllegalArgumentException("proxyCert not specified!");
        }
        if (proxyCertificate.getDescription() == null) {
            throw new IllegalArgumentException("description not specified!");
        }
        if (proxyCertificate.getStartTime() == null) {
            throw new IllegalArgumentException("startTime not specified!");
        }
        if (proxyCertificate.getExpirationTime() == null) {
            throw new IllegalArgumentException("expirationTime not specified!");
        }

        PreparedStatement pstmt = null;
        try {
            pstmt = getPreparedStatement(getUpdateQuery(), connection);
            pstmt.setString(1, proxyCertificate.getCertificate());
            pstmt.setString(2, proxyCertificate.getDescription());
            pstmt.setTimestamp(3, new Timestamp(proxyCertificate.getStartTime().getTimeInMillis()));
            pstmt.setTimestamp(4, new Timestamp(proxyCertificate.getExpirationTime().getTimeInMillis()));
            pstmt.setTimestamp(5, new Timestamp(Calendar.getInstance().getTimeInMillis()));
            pstmt.setBoolean(6, proxyCertificate.isAdministrator());
            pstmt.setString(7, proxyCertificate.getId());
            pstmt.setString(8, proxyCertificate.getDN());
            pstmt.setString(9, proxyCertificate.getFQAN());

            int rowCount = pstmt.executeUpdate();
            logger.debug("updated " + rowCount + " rows");
        } catch (SQLException sqle) {
            logger.error("executeUpdate error: " + sqle.getMessage());
            throw sqle;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException sqle1) {
                    logger.error(sqle1.getMessage());
                }
            }
        }
        logger.debug("executeUpdate end");
    }

    /**
     * Returns the query for deleting.
     * 
     * @return The query for deleting.
     */
    protected Query getDeleteQuery() {
        if (deleteQuery == null) {
            StringBuffer query = new StringBuffer();
            query.append("delete from ");
            query.append(NAME_TABLE);
            query.append(" where ");
            query.append(ID_FIELD);
            query.append(" = ? AND ");
            query.append(DN_FIELD);
            query.append(" = ? AND ");
            query.append(FQAN_FIELD);
            query.append(" = ?");

            deleteQuery = new Query();
            deleteQuery.setName(Query.DELETE_STATEMENT);
            deleteQuery.setStatement(query.toString());
        }
        return deleteQuery;
    }

    /**
     * Returns the query for inserting
     * 
     * @return The query for inserting.
     */
    protected Query getInsertQuery() {
        if (insertQuery == null) {
            StringBuffer query = new StringBuffer();
            query.append("insert into ");
            query.append(NAME_TABLE);
            query.append(" ( ");
            query.append(ID_FIELD + ", ");
            query.append(DN_FIELD + ", ");
            query.append(FQAN_FIELD + ", ");
            query.append(VO_FIELD + ", ");
            query.append(PROXY_CERTIFICATE_FIELD + ", ");
            query.append(DESCRIPTION_FIELD + ", ");
            query.append(START_TIME_FIELD + ", ");
            query.append(EXPIRATION_TIME_FIELD + ", ");
            query.append(MODIFICATION_TIME_FIELD + ", ");
            query.append(PROXY_TYPE_FIELD + ", ");
            query.append(IS_ADMIN_FIELD);
            query.append(" ) ");
            query.append("values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            insertQuery = new Query();
            insertQuery.setName(Query.INSERT_STATEMENT);
            insertQuery.setStatement(query.toString());
        }
        return insertQuery;
    }

    /**
     * Returns the query for checking if a <code>id</code> is registered.
     * 
     * @return The query for for checking if a <code>id</code> is registered.
     */
    protected Query getIsIdPresentQuery() {
        if (isIdPresentQuery == null) {
            StringBuffer query = new StringBuffer();
            query.append("select count(*) from ");
            query.append(NAME_TABLE);
            query.append(" where ");
            query.append(ID_FIELD);
            query.append(" = ? AND ");
            query.append(DN_FIELD);
            query.append(" = ? AND ");
            query.append(FQAN_FIELD);
            query.append(" = ?");
            isIdPresentQuery = new Query();
            isIdPresentQuery.setName(IS_USERID_PRESENT);
            isIdPresentQuery.setStatement(query.toString());
        }
        return isIdPresentQuery;
    }

    /**
     * Returns the query for selecting.
     * 
     * @return The query for selecting.
     */
    protected Query getSelectQuery(String id, String DN, String FQAN, ProxyCertificateType proxyType) {
        StringBuffer query = new StringBuffer();
        query.append("select ");
        query.append(ID_FIELD).append(" AS ").append(ID_FIELD).append(", ");
        query.append(DN_FIELD).append(" AS ").append(DN_FIELD).append(", ");
        query.append(FQAN_FIELD).append(" AS ").append(FQAN_FIELD).append(", ");
        query.append(VO_FIELD).append(" AS ").append(VO_FIELD).append(", ");
        query.append(PROXY_CERTIFICATE_FIELD).append(" AS ").append(PROXY_CERTIFICATE_FIELD).append(", ");
        query.append(DESCRIPTION_FIELD).append(" AS ").append(DESCRIPTION_FIELD).append(", ");
        query.append(START_TIME_FIELD).append(" AS ").append(START_TIME_FIELD).append(", ");
        query.append(EXPIRATION_TIME_FIELD).append(" AS ").append(EXPIRATION_TIME_FIELD).append(", ");
        query.append(MODIFICATION_TIME_FIELD).append(" AS ").append(MODIFICATION_TIME_FIELD).append(", ");
        query.append(PROXY_TYPE_FIELD).append(" AS ").append(PROXY_TYPE_FIELD).append(", ");
        query.append(IS_ADMIN_FIELD).append(" AS ").append(IS_ADMIN_FIELD);
        query.append(" from ").append(NAME_TABLE).append(" where true ");

        if (id != null) {
            query.append(" AND ").append(ID_FIELD).append(" = '").append(id).append("'");
        }

        if (DN != null) {
            query.append(" AND ").append(DN_FIELD).append(" = '").append(DN).append("'");
        }

        if (FQAN != null) {
            query.append(" AND ").append(FQAN_FIELD).append(" = '").append(FQAN).append("'");
        }

        if (proxyType != null) {
            query.append(" AND ").append(PROXY_TYPE_FIELD).append(" = ").append(proxyType.getId());
        }

        selectQuery = new Query();
        selectQuery.setName(Query.SELECT_STATEMENT);
        selectQuery.setStatement(query.toString());
        return selectQuery;
    }

    /**
     * Returns the query string for updating.
     * 
     * @param proxyCertificate
     *            The proxyCertificate info to be updated.
     * @return The query string for updating.
     */
    protected Query getUpdateQuery() {
        if (updateQuery == null) {
            StringBuffer query = new StringBuffer();
            query.append("update ");
            query.append(NAME_TABLE);
            query.append(" set ");
            query.append(PROXY_CERTIFICATE_FIELD).append(" = ?, ");
            query.append(DESCRIPTION_FIELD).append(" = ?, ");
            query.append(START_TIME_FIELD).append(" = ?, ");
            query.append(EXPIRATION_TIME_FIELD).append(" = ?, ");
            query.append(MODIFICATION_TIME_FIELD).append(" = ?, ");
            query.append(IS_ADMIN_FIELD).append(" = ? where ");
            query.append(ID_FIELD).append(" = ? AND ");
            query.append(DN_FIELD).append(" = ? AND ");
            query.append(FQAN_FIELD).append(" = ?");

            updateQuery = new Query();
            updateQuery.setName(Query.UPDATE_STATEMENT);
            updateQuery.setStatement(query.toString());
        }
        return updateQuery;
    }

    private ProxyCertificate makeProxyCertificate(ResultSet rset) throws IllegalArgumentException, SQLException {
        if (rset == null) {
            throw new IllegalArgumentException("makeProxyCertificate: ResultSet is null");
        }

        ProxyCertificate proxyCertificate = new ProxyCertificate();
        proxyCertificate.setId(rset.getString(ID_FIELD));
        proxyCertificate.setDN(rset.getString(DN_FIELD));
        proxyCertificate.setFQAN(rset.getString(FQAN_FIELD));
        proxyCertificate.setVO(rset.getString(VO_FIELD));
        proxyCertificate.setCertificate(rset.getString(PROXY_CERTIFICATE_FIELD));
        proxyCertificate.setDescription(rset.getString(DESCRIPTION_FIELD));

        Timestamp timestamp = rset.getTimestamp(START_TIME_FIELD);
        Calendar calendar = null;
        if (timestamp != null) {
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp.getTime());
        }
        proxyCertificate.setStartTime(calendar);

        calendar = null;
        timestamp = rset.getTimestamp(EXPIRATION_TIME_FIELD);
        if (timestamp != null) {
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp.getTime());
        }
        proxyCertificate.setExpirationTime(calendar);

        calendar = null;
        timestamp = rset.getTimestamp(MODIFICATION_TIME_FIELD);
        if (timestamp != null) {
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp.getTime());
        }        
        proxyCertificate.setModificationTime(calendar);
        
        proxyCertificate.setProxyCertificateType(rset.getInt(PROXY_TYPE_FIELD) == ProxyCertificateType.AUTHENTICATION.getId() ? ProxyCertificateType.AUTHENTICATION
                : ProxyCertificateType.DELEGATION);
        proxyCertificate.setAdministrator(rset.getBoolean(IS_ADMIN_FIELD));

        return proxyCertificate;
    }
}
