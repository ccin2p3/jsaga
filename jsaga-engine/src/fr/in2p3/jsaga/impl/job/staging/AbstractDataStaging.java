package fr.in2p3.jsaga.impl.job.staging;

import org.ogf.saga.url.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractDataStaging
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   20 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractDataStaging {
    protected URL m_localURL;
    protected boolean m_append;

    protected AbstractDataStaging(URL localURL, boolean append) {
        m_localURL = localURL;
        m_append = append;
    }
}