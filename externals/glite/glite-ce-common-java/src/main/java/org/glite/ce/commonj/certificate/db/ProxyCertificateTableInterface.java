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

import org.glite.ce.common.db.TableInterface;
import org.glite.ce.commonj.certificate.ProxyCertificate;
import org.glite.ce.commonj.certificate.ProxyCertificate.ProxyCertificateType;

/**
 * Interface to access the command table in the database.
 *
 */
public interface ProxyCertificateTableInterface extends TableInterface {    
    /**
     * Inserts the certificate into the db.
     * @param proxyCertificate The user proxy certificate.
     * @param connection The connection.
     * @throws SQLException
     * @throws IllegalArgumentException
     */    
    public void executeInsert(ProxyCertificate proxyCertificate, Connection connection) throws SQLException, IllegalArgumentException;
        
    /**
     * Inserts the certificate for a given <code>id</code> into the db.
     * @param id The certificate identifier.
     * @param DN The user DN.
     * @param FQAN The user FQAN.
     * @param VO The user VO.
     * @param proxyCert The user proxy certificate.
     * @param proxyCert The proxy certificate description.
     * @param startTime The start time.
     * @param expirationTime The expiration time.
     * @param isDelegProxy TRUE if it is a delegation proxy.
     * @param isAdmin TRUE if the user is the administrator.
     * @param connection The connection.
     * @throws SQLException
     * @throws IllegalArgumentException
     */    
 //   public void executeInsert(String id, String DN, String FQAN, String VO, String proxyCert, String description, Calendar startTime, Calendar expirationTime, boolean isDelegProxy, boolean isAdmin, Connection connection) throws SQLException, IllegalArgumentException;

    /**
     * Deletes a certificate identified by <code>id, userId</code> from the db.
     * @param id The certificate identifier.
     * @param userId The user identifier whose certificate is to be deleted.
     * @param connection The connection.
     * @throws SQLException
     * @throws IllegalArgumentException
     */
//    public void executeDelete(String id, String userId, Connection connection) throws SQLException, IllegalArgumentException;
    
    /**
     * Deletes a certificate identified by <code>idm DN, FQAN</code> from the db.
     * @param id The certificate identifier.
     * @param DN The user DN whose certificate is to be deleted.
     * @param FQAN The user FQAN whose certificate is to be deleted.
     * @param connection The connection.
     * @throws SQLException
     * @throws IllegalArgumentException
     */
    public void executeDelete(String id, String DN, String FQAN, Connection connection) throws SQLException, IllegalArgumentException;

    /**
     * Updates the certificate into the db.
     * @param proxyCertificate The user proxy certificate.
     * @param connection The connection.
     * @throws SQLException
     * @throws IllegalArgumentException
     */    
    public void executeUpdate(ProxyCertificate proxyCertificate, Connection connection) throws SQLException, IllegalArgumentException;
    
    /**
     * Updates a certificate identified by <code>userId</code> from the db.
     * @param id The certificate identifier to be updated.
     * @param proxyCert The proxy certificate.
     * @param expirationTime The start time.
     * @param expirationTime The expiration time.
     * @param isAdmin TRUE if the user is the administrator.
     * @param connection The connection.
     * @throws SQLException
     * @throws IllegalArgumentException
     */
//    public void executeUpdate(String id, String DN, String FQAN, String proxyCert, String description, Calendar startTime, Calendar expirationTime, boolean isAdmin, Connection connection) throws SQLException, IllegalArgumentException;
    
    /**
     * Retrieves all certificate info associated to a given <code>userDN</code> from the db.
     * @param DN The user DN.
     * @param proxyType The proxy type.
     * @param connection The connection.
     * @throws SQLException
     * @throws IllegalArgumentException
     */
    public List<ProxyCertificate> executeSelect(String DN, String FQAN, ProxyCertificateType proxyType, Connection connection) throws SQLException, IllegalArgumentException;

    /**
     * Retrieves the authentication info associated to a given <code>userId</code> from the db.
     * @param id The certificate identifier.
     * @param userId The user identifier.
     * @param connection The connection.
     * @throws SQLException
     * @throws IllegalArgumentException
     */
//    public ProxyCertificate executeSelect(String id, String userId, Connection connection) throws SQLException, IllegalArgumentException;

    /**
     * Retrieves the authentication info associated to a given <code>userId</code> from the db.
     * @param id The certificate identifier.
     * @param DN The user DN.
     * @param FQAN The user FQAN.
     * @param connection The connection.
     * @throws SQLException
     * @throws IllegalArgumentException
     */
    public ProxyCertificate executeSelect(String id, String DN, String FQAN, Connection connection) throws SQLException, IllegalArgumentException;
}
