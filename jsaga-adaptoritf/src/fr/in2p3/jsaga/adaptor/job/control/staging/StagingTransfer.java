package fr.in2p3.jsaga.adaptor.job.control.staging;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   StagingTransfer
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   18 mars 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class StagingTransfer {
    private String m_from;
    private String m_to;
    private boolean m_append;

    public StagingTransfer(String from, String to, boolean append) {
        m_from = from;
        m_to = to;
        m_append = append;
    }

    public String getFrom() {
        return m_from;
    }

    public String getTo() {
        return m_to;
    }

    public boolean isAppend() {
        return m_append;
    }
}
