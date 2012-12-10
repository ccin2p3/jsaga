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
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.glite.ce.common.db.DatabaseException;
import org.glite.ce.common.db.DatasourceManager;
import org.glite.ce.commonj.certificate.EventDispatcher;
import org.glite.ce.commonj.certificate.ProxyCertificate;
import org.glite.ce.commonj.certificate.ProxyCertificateEvent;
import org.glite.ce.commonj.certificate.ProxyCertificateException;
import org.glite.ce.commonj.certificate.ProxyCertificateListener;
import org.glite.ce.commonj.certificate.ProxyCertificateStorageInterface;
import org.glite.ce.commonj.certificate.ProxyCertificate.ProxyCertificateType;

/**
 * Class managing a command queue in the database.
 * 
 */
public class ProxyCertificateDBManager implements ProxyCertificateStorageInterface {
    /** The logger */
    private static final Logger logger = Logger.getLogger(ProxyCertificateDBManager.class);

    private static ProxyCertificateDBManager proxyCertificateDBManager = null;
    
    public static ProxyCertificateDBManager getInstance() {
        if(proxyCertificateDBManager == null) {
            proxyCertificateDBManager = new ProxyCertificateDBManager();
        }
        
        return proxyCertificateDBManager;
    }

    /**
     * DAO for managing Authentication table.
     */
    private ProxyCertificateTable proxyCertificateTable = null;
    

    /**
     * /** Constructor.
     * 
     * @throws CommandQueueException
     */
    private ProxyCertificateDBManager() {
        proxyCertificateTable = new ProxyCertificateTable();
    }

    public void addProxyCertificateListener(ProxyCertificateListener l) {
        EventDispatcher.getInstance().addDelegationProxyListener(l);        
    }

    public void deleteProxyCertificate(String id, String userId) throws ProxyCertificateException, IllegalArgumentException {
        if (userId == null) {
            throw new IllegalArgumentException("id not specified!");
        }
        if (userId == null) {
            throw new IllegalArgumentException("DN not specified!");
        }
        
        String DN = null, FQAN = null;
        
        int index = userId.indexOf("_Role");
        if (index > 0) {
            DN = userId.substring(0, index);
            index =  DN.lastIndexOf("_");
            DN = DN.substring(0, index);
            FQAN = userId.substring(index);
        }
                
        if (DN == null || FQAN == null) {
            throw new IllegalArgumentException("wrong userId!");
        }
        
        deleteProxyCertificate(id, DN, FQAN);
    }
    
    public void deleteProxyCertificate(String id, String DN, String FQAN) throws ProxyCertificateException, IllegalArgumentException {
        logger.debug("deleteCommand begin");

        if (id == null) {
            throw new IllegalArgumentException("id not specified!");
        }
        if (DN == null) {
            throw new IllegalArgumentException("DN not specified!");
        }
        if (FQAN == null) {
            throw new IllegalArgumentException("FQAN not specified!");
        }
        
        Connection connection = getConnection();
       
        try {
            if (connection != null) {
                // Delete
                proxyCertificateTable.executeDelete(id, DN, FQAN, connection);

                // Commit
                connection.commit();
                
                ProxyCertificate proxyCert = new ProxyCertificate();
                proxyCert.setId(id);
                proxyCert.setDN(DN);               
                proxyCert.setDN(FQAN);               
                
                EventDispatcher.getInstance().fireProxyCertificateEvent(new ProxyCertificateEvent(ProxyCertificateEvent.PROXY_CERTIFICATE_REMOVED, proxyCert));
            } else {
                logger.error("Connection is null");
                throw new ProxyCertificateException("Connection is null");
            }
        } catch (SQLException sqle) {
            try {
                logger.error("Operation failed: " + sqle.getMessage());
                connection.rollback();
                logger.error("Rollback performed");
                throw new ProxyCertificateException(sqle.getMessage());
            } catch (SQLException sqle1) {
                logger.error("Rollback failed: " + sqle1.getMessage());
                throw new ProxyCertificateException(sqle1.getMessage());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle2) {
                    logger.error("Problem in closing connection: " + sqle2.getMessage());
                    throw new ProxyCertificateException(sqle2.getMessage());
                }
            }
        }
        logger.debug("deleteCommand end");
    }
        
    public void destroy() {
        EventDispatcher.getInstance().destroy();
    }

    /**
     * Returns the connection to the database.
     * 
     * @return The connection to the database.
     * @throws DatabaseException
     */
    private Connection getConnection() throws ProxyCertificateException {
        try {
            return DatasourceManager.getConnection(ProxyCertificateStorageInterface.PROXY_CERTIFICATE_DATASOURCE_NAME);
        } catch (DatabaseException e) {
            logger.error("Problem in getting connection: " + e.getMessage());
            throw new ProxyCertificateException(e.getMessage());
        }
    }

    /**
     * Retrieves the authentication in for a given <code>id</code>.
     *
     * @param id
     *            The <code>id</code>.
     * @return
     * @throws ProxyCertificateException
     * @throws IllegalArgumentException
     */
    public ProxyCertificate getProxyCertificate(String id, String userId) throws ProxyCertificateException, IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("id not specified!");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId not specified!");
        }

        String DN = null, FQAN = null;
        
        int index = userId.indexOf("_Role");
        if (index > 0) {
            DN = userId.substring(0, index);
            index =  DN.lastIndexOf("_");
            DN = DN.substring(0, index);
            FQAN = userId.substring(index);
        }
                
        if (DN == null || FQAN == null) {
            throw new IllegalArgumentException("wrong userId!");
        }
       
        return getProxyCertificate(id, DN, FQAN);
    }

    /**
     * Retrieves the authentication in for a given <code>id</code>.
     * 
     * @param id
     *            The <code>id</code>.
     * @return
     * @throws ProxyCertificateException
     * @throws IllegalArgumentException
     */
    public ProxyCertificate getProxyCertificate(String id, String DN, String FQAN) throws ProxyCertificateException, IllegalArgumentException {
        ProxyCertificate proxyCertificate = null;
        logger.debug("getProxyCertificate begin");
        
        if (id == null) {
            throw new IllegalArgumentException("id not specified!");
        }
        if (DN == null) {
            throw new IllegalArgumentException("DN not specified!");
        }
        if (FQAN == null) {
            throw new IllegalArgumentException("FQAN not specified!");
        }

        Connection connection = getConnection();

        try {
            if (connection != null) {
                // Select
                proxyCertificate = proxyCertificateTable.executeSelect(id, DN, FQAN, connection);

                // Commit
                connection.commit();
            } else {
                logger.error("Connection is null");
                throw new ProxyCertificateException("Connection is null");
            }
        } catch (SQLException sqle) {
            try {
                logger.error("Operation failed: " + sqle.getMessage());
                connection.rollback();
                logger.error("Rollback performed");
                throw new ProxyCertificateException(sqle.getMessage());
            } catch (SQLException sqle1) {
                logger.error("Rollback failed: " + sqle1.getMessage());
                throw new ProxyCertificateException(sqle1.getMessage());
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle2) {
                    logger.error("Problem in closing connection: " + sqle2.getMessage());
                    throw new ProxyCertificateException(sqle2.getMessage());
                }
            }
        }
        logger.debug("getProxyCertificate end");
        return proxyCertificate;
    }
    

    public List<ProxyCertificate> getProxyCertificateList() throws ProxyCertificateException, IllegalArgumentException {
        return getProxyCertificateList(null, null, null);
    }

    public List<ProxyCertificate> getProxyCertificateList(String userDN) throws ProxyCertificateException, IllegalArgumentException {
        return getProxyCertificateList(userDN, null, null);
    }
    
    public List<ProxyCertificate> getProxyCertificateList(String DN, String FQAN, ProxyCertificateType proxyType) throws ProxyCertificateException, IllegalArgumentException {
        List<ProxyCertificate> proxyCertificateList = null;
        logger.debug("getProxyCertificateList begin");

        Connection connection = getConnection();

        try {
            if (connection != null) {
                proxyCertificateList = proxyCertificateTable.executeSelect(DN, FQAN, proxyType, connection);                    
               
                // Commit
                connection.commit();
            } else {
                logger.error("Connection is null");
                throw new ProxyCertificateException("Connection is null");
            }
        } catch (SQLException sqle) {
            try {
                logger.error("Operation failed: " + sqle.getMessage());
                connection.rollback();
                logger.error("Rollback performed");
                throw new ProxyCertificateException(sqle.getMessage());
            } catch (SQLException sqle1) {
                logger.error("Rollback failed: " + sqle1.getMessage());
                throw new ProxyCertificateException(sqle1.getMessage());
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle2) {
                    logger.error("Problem in closing connection: " + sqle2.getMessage());
                    throw new ProxyCertificateException(sqle2.getMessage());
                }
            }
        }
        logger.debug("getProxyCertificateList end");
        return proxyCertificateList;        
    }


    public List<ProxyCertificateListener> getProxyCertificateListener() {
        return EventDispatcher.getInstance().getDelegationProxyListeners();
    }
        
    /**
     * Checks if a <code>id</code> already has a certificate registered.
     * id The id.
     * 
     * @return Returns true if the <code>id</code> exists, false
     *         otherwise.
     * @throws ProxyCertificateException
     */
    private boolean doesCertificateExist(String id, String DN, String FQAN) throws ProxyCertificateException {
        boolean result = false;
        logger.debug("isIdPresent begin");

        Connection connection = getConnection();

        try {
            if (connection != null) {
                // Searches for a given id
                result = proxyCertificateTable.executeDoesCertificateExist(id, DN, FQAN, connection);
                logger.debug("Executed executeIsIdPresent result=" + result);

                // Commit
                connection.commit();
            } else {
                logger.error("Connection is null");
                throw new ProxyCertificateException("Connection is null");
            }
        } catch (SQLException sqle) {
            try {
                logger.error("Operation failed: " + sqle.getMessage());
                if (connection != null) {
                    connection.rollback();
                    logger.error("Rollback performed");
                }
            } catch (SQLException sqle1) {
                logger.error("Rollback failed: " + sqle1.getMessage());
                throw new ProxyCertificateException(sqle1.getMessage());
            }
            throw new ProxyCertificateException(sqle.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle2) {
                    logger.error("Problem in closing connection: " + sqle2.getMessage());
                    throw new ProxyCertificateException(sqle2.getMessage());
                }
            }
        }
        logger.debug("isIdPresent end");
        return result;
    }

    public void removeProxyCertificateListener(ProxyCertificateListener l) {
        EventDispatcher.getInstance().removeDelegationProxyListener(l);        
    }

    public void setProxyCertificate(ProxyCertificate proxyCertificate) throws ProxyCertificateException, IllegalArgumentException {        
        logger.debug("setProxyCertificate begin");
        if(proxyCertificate == null) {
            throw new IllegalArgumentException("proxyCertificate not specified");
        }

        Connection connection = getConnection();
        
        try {
            if (connection != null) {
                if (doesCertificateExist(proxyCertificate.getId(), proxyCertificate.getDN(), proxyCertificate.getFQAN())) {
                    // Update
                    proxyCertificateTable.executeUpdate(proxyCertificate, connection);

                    // Commit
                    connection.commit();
                    
                    EventDispatcher.getInstance().fireProxyCertificateEvent(new ProxyCertificateEvent(ProxyCertificateEvent.PROXY_CERTIFICATE_UPDATED, proxyCertificate));
                } else {
                    // Insert
                    proxyCertificateTable.executeInsert(proxyCertificate, connection);

                    // Commit
                    connection.commit();
                    
                    EventDispatcher.getInstance().fireProxyCertificateEvent(new ProxyCertificateEvent(ProxyCertificateEvent.PROXY_CERTIFICATE_ADDED, proxyCertificate));
                }
            } else {
                logger.error("Connection is null");
                throw new ProxyCertificateException("Connection is null");
            }
        } catch (SQLException sqle) {
            try {
                logger.error("Operation failed: " + sqle.getMessage());
                connection.rollback();
                logger.error("Rollback performed");
                throw new ProxyCertificateException(sqle.getMessage());
            } catch (SQLException sqle1) {
                logger.error("Rollback failed: " + sqle1.getMessage());
                throw new ProxyCertificateException(sqle1.getMessage());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle2) {
                    logger.error("Problem in closing connection: " + sqle2.getMessage());
                    throw new ProxyCertificateException(sqle2.getMessage());
                }
            }
        }
        logger.debug("setProxyCertificate end");
    }
}
