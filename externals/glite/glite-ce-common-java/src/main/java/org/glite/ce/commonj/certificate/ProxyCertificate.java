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
 * Authors: Luigi Zangrando (luigi.zangrando@pd.infn.it) 
 */

package org.glite.ce.commonj.certificate;

import java.util.Calendar;

/**
 * Class representing the certificate information associated to a user.
 * 
 */
public class ProxyCertificate {

    public enum ProxyCertificateType { AUTHENTICATION(0, "AUTHN_PROXY"), DELEGATION(1, "DELEG_PROXY");
        private final int id;
        private final String name;
        
        ProxyCertificateType(int id, String name) {
            this.id = id;
            this.name = name;            
        }
        
        public int getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
    }
    
    /** The userId */
    private String id = null;

    /** The userDN */
    private String DN = null;

    /** The virtualOrganization */
    private String FQAN = null;

    /** The virtualOrganization */
    private String VO = null;

    /** The certificate */
    private String certificate = null;

    /** The description */
    private String description = null;

    /** The start time for user's certificate */
    private Calendar startTime = null;

    /** The expiration time for user's certificate */
    private Calendar expirationTime = null;

    /** The modification time for user's certificate */
    private Calendar modificationTime = null;

    private boolean administrator = false;
    
    private ProxyCertificateType proxyType = null;

    /**
     * Trivial constructor.
     */
    public ProxyCertificate() {
        this(null, null, null, null, null, null, null, null, false);
    }

    /**
     * Constructor.
     * 
     * @param userId
     *            The userId.
     * @param authNCert
     *            The certificate.
     * @param expirationTime
     *            Certificate expiration time.
     * @param modificationTime
     *            Modification time.
     */
    public ProxyCertificate(String id, String DN, String FQAN, String VO, String certificate, Calendar startTime, Calendar expirationTime, ProxyCertificateType proxyType, boolean isAdmin) {
        this.id = id;
        this.DN = DN;
        this.FQAN = FQAN;
        this.VO = VO;
        this.certificate = certificate;
        this.startTime = startTime;
        this.expirationTime = expirationTime;
        this.proxyType = proxyType;
        this.administrator = isAdmin;
        this.modificationTime = Calendar.getInstance();
    }

    public String getCertificate() {
        return certificate;
    }

    public String getDescription() {
        return description;
    }

    public String getDN() {
        return DN;
    }

    /**
     * @return the expirationTime
     */
    public Calendar getExpirationTime() {
        return expirationTime;
    }

    public String getFQAN() {
        return FQAN;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the modificationTime
     */
    public Calendar getModificationTime() {
        return modificationTime;
    }

    public ProxyCertificateType getProxyCertificateType() {
        return proxyType;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public String getUserId() {
        return DN + FQAN;
    }

    public String getVO() {
        return VO;
    }

    public boolean isAdministrator() {
        return administrator;
    }

    public boolean isValid() {
        if (startTime == null || expirationTime == null) {
            return false;
        }

        Calendar now = Calendar.getInstance();
        return now.after(startTime) && now.before(expirationTime);
    }

    public void setAdministrator(boolean administrator) {
        this.administrator = administrator;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDN(String dn) {
        DN = dn;
    }

    /**
     * @param expirationTime
     *            the expirationTime to set
     */
    public void setExpirationTime(Calendar expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void setFQAN(String fqan) {
        FQAN = fqan;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @param modificationTime
     *            the modificationTime to set
     */
    public void setModificationTime(Calendar modificationTime) {
        this.modificationTime = modificationTime;
    }

    public void setProxyCertificateType(ProxyCertificateType proxyType) {
        this.proxyType = proxyType;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public void setVO(String VO) {
        this.VO = VO;
    }
}
