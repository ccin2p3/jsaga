package fr.in2p3.jsaga.adaptor.naregi.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import org.naregi.ss.service.client.JobScheduleServiceException;
import org.ogf.saga.error.*;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.Exception;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SuperSchedulerJobControlAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   7 août 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SuperSchedulerJobControlAdaptor extends SuperSchedulerJobAdaptorAbstract implements JobControlAdaptor, CleanableJobAdaptor {
    public Usage getUsage() {
        return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return null;
    }

    public String[] getSupportedSandboxProtocols() {
        return null;    // no sandbox management
    }

    public String getTranslator() {
        return "xsl/job/wfml.xsl";
    }

    public Map getTranslatorParameters() {
        return null;    // no parameter
    }

    public int getDefaultPort() {
        return 0;       // no default port
    }

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new SuperSchedulerJobMonitorAdaptor();
    }

    public String submit(String jobDesc, boolean checkMatch) throws PermissionDenied, Timeout, NoSuccess, BadResource {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(new ByteArrayInputStream(jobDesc.getBytes()));
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
        String epr;
        try {
            if (m_credential != null) {
                epr = m_jss.submitJob(doc, m_credential);
            } else {
                epr = m_jss.submitJob(doc, m_account, m_passPhrase);
            }
        } catch (JobScheduleServiceException e) {
            throw new NoSuccess(e);
        }
        return epr;
    }

    public void cancel(String nativeJobId) throws PermissionDenied, Timeout, NoSuccess {
        File epr = new File(nativeJobId);   //todo: remove this workaround
        String id = getJobID(epr);
        try {
            if (m_credential != null) {
                m_jss.cancelJob(id, m_credential);
            } else {
                m_jss.cancelJob(id, m_account, m_passPhrase);
            }
        } catch (JobScheduleServiceException e) {
            throw new NoSuccess(e);
        }
    }

    public void clean(String nativeJobId) throws PermissionDenied, Timeout, NoSuccess {
        File epr = new File(nativeJobId);   //todo: remove this workaround
        String id = getJobID(epr);
        try {
            if (m_credential != null) {
                m_jss.deleteJob(id, m_credential);
            } else {
                m_jss.deleteJob(id, m_account, m_passPhrase);
            }
        } catch (JobScheduleServiceException e) {
            throw new NoSuccess(e);
        }
    }
}
