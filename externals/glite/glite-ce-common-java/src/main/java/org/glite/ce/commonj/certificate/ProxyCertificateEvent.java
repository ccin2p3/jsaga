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
 * Authors: Luigi Zangrando, <luigi.zangrando@pd.infn.it>
 *
 * Version info: $Id: ProxyCertificateEvent.java,v 1.3 2009/03/30 09:05:09 zangran Exp $
 *
 */

package org.glite.ce.commonj.certificate;

import org.glite.ce.commonj.certificate.ProxyCertificate;

public class ProxyCertificateEvent implements Cloneable {
    public final static int PROXY_CERTIFICATE_ADDED = 0;
    public final static int PROXY_CERTIFICATE_UPDATED = 1;
    public final static int PROXY_CERTIFICATE_REMOVED = 2;
    private int eventType = -1;
    private ProxyCertificate proxyCertificate;
    
    
    public ProxyCertificateEvent(int eventType, ProxyCertificate proxyCertificate) {
        this.eventType = eventType;
        this.proxyCertificate = proxyCertificate;
    }

    public Object clone() {
        return new ProxyCertificateEvent(eventType, proxyCertificate);
    }
    
    public ProxyCertificate getProxyCertificate() {
        return proxyCertificate;
    }

    public int getEventType() {
        return eventType;
    }
}
