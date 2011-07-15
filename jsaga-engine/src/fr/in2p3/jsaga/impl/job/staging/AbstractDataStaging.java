package fr.in2p3.jsaga.impl.job.staging;

import fr.in2p3.jsaga.Base;
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
    protected static final String JSAGA_FACTORY = Base.getSagaFactory();
    protected static final boolean INPUT = true;
    protected static final boolean OUTPUT = false;

    protected URL m_localURL;
    protected boolean m_append;

    protected AbstractDataStaging(URL localURL, boolean append) {
        m_localURL = localURL;
        m_append = append;
    }

    public boolean isSubdirOf(URL baseURL) {
        return m_localURL.toString().startsWith(baseURL.toString()+"/");
    }

    public String getLocalProtocol() {
        return m_localURL.getScheme();
    }

    public abstract String getWorkerProtocol();
    public abstract boolean isInput();
}
