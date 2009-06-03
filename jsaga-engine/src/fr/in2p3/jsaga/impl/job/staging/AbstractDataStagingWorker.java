package fr.in2p3.jsaga.impl.job.staging;

import org.ogf.saga.url.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractDataStagingWorker
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   3 juin 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractDataStagingWorker extends AbstractDataStaging {
    protected String m_workerPath;

    protected AbstractDataStagingWorker(URL localURL, String workerPath, boolean append) {
        super(localURL, append);
        m_workerPath = workerPath;
    }

    public String getWorkerProtocol() {
        return "file";
    }
}
