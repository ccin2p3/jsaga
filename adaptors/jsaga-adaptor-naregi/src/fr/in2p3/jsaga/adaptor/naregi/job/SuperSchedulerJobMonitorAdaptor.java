package fr.in2p3.jsaga.adaptor.naregi.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;
import org.naregi.ss.service.client.JobScheduleServiceException;
import org.ogf.saga.error.*;
import org.w3c.dom.Document;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.*;
import java.util.Iterator;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SuperSchedulerJobMonitorAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   7 aout 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SuperSchedulerJobMonitorAdaptor extends SuperSchedulerJobAdaptorAbstract implements QueryIndividualJob {
    private XPath m_selector;

    public SuperSchedulerJobMonitorAdaptor() {
        m_selector = XPathFactory.newInstance().newXPath();
        m_selector.setNamespaceContext(new NamespaceContext(){
            public String getNamespaceURI(String prefix) {
                if("nwsbp".equals(prefix)){
                    return "http://www.naregi.org/ws/bpel/state/02";
                } else {
                    return null;
                }
            }
            public String getPrefix(String namespaceURI) {
                if ("http://www.naregi.org/ws/bpel/state/02".equals(namespaceURI)) {
                    return "nwsbp";
                } else {
                    return null;
                }
            }
            public Iterator getPrefixes(String string) {
                return null;
            }
        });
    }

    public Usage getUsage() {
        return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;
    }

    public int getDefaultPort() {
        return 0;   // no default port
    }

    public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
        String epr = new JobEPR(nativeJobId).getEPR();
        Document detail;
        try {
            if (m_credential != null) {
                detail = m_jss.queryJob(epr, m_credential);
            } else {
                detail = m_jss.queryJob(epr, m_account, m_passPhrase);
            }
        } catch (JobScheduleServiceException e) {
            if (e.getMessage().startsWith("Super Scheduler command error occurred.")) {
                throw new NoSuccessException("Failed to get status (job may have been cleaned up)");
            } else {
                throw new NoSuccessException(e);
            }
        }

/*
        try {
            System.out.println(XMLUtil.getStringFromDocument(detail) + "\n");
        } catch (Exception e) {
            // ignore exception
        }
*/

        String xpath = "/nwsbp:activityStates/nwsbp:activityState[nwsbp:name='Workflow']/nwsbp:state/text()";
        String stateString;
        try {
            String result = (String) m_selector.evaluate(xpath, detail, XPathConstants.STRING);
            if (!result.equals("")) {
                stateString = result;
            } else {
                stateString = null;
            }
        } catch (XPathExpressionException e) {
            throw new NoSuccessException(e);
        }

        return new SuperSchedulerJobStatus(epr, stateString);
    }
}
