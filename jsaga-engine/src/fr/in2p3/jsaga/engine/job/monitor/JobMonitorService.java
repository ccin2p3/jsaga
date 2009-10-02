package fr.in2p3.jsaga.engine.job.monitor;

import fr.in2p3.jsaga.adaptor.job.monitor.*;
import fr.in2p3.jsaga.engine.job.monitor.listen.FilteredJobStatusListener;
import fr.in2p3.jsaga.engine.job.monitor.listen.IndividualJobStatusListener;
import fr.in2p3.jsaga.engine.job.monitor.poll.*;
import fr.in2p3.jsaga.engine.job.monitor.request.JobStatusRequestor;
import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobMonitorService
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   22 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobMonitorService {
    private URL m_url;
    private JobMonitorAdaptor m_adaptor;
    private JobStatusRequestor m_requestor;
    private JobRegistry m_registry;

    /** constructor */
    public JobMonitorService(URL url, JobMonitorAdaptor adaptor) throws NotImplementedException, TimeoutException, NoSuccessException {
        // set URL
        m_url = url;
        m_adaptor = adaptor;

        // set requestor
        m_requestor = new JobStatusRequestor(adaptor);
        
        // set registry (listeners first, then pollers)
        if (adaptor instanceof ListenFilteredJob) {
            m_registry = new FilteredJobStatusListener((ListenFilteredJob)adaptor, m_requestor);
        } else if (adaptor instanceof ListenIndividualJob) {
            m_registry = new IndividualJobStatusListener((ListenIndividualJob)adaptor, m_requestor);
        } else if (adaptor instanceof QueryFilteredJob) {
            m_registry = new FilteredJobStatusPoller((QueryFilteredJob)adaptor);
        } else if (adaptor instanceof QueryListJob) {
            m_registry = new ListJobStatusPoller((QueryListJob)adaptor);
        } else if (adaptor instanceof QueryIndividualJob) {
            m_registry = new IndividualJobStatusPoller((QueryIndividualJob)adaptor);
        } else {
            throw new NotImplementedException("Adaptor does not implement any monitoring interface: "+adaptor.getClass().getName());
        }
    }

    public URL getURL() {
        return m_url;
    }

    public JobMonitorAdaptor getAdaptor() {
        return m_adaptor;
    }

    public JobStatus getState(String nativeJobId) throws NotImplementedException, TimeoutException, NoSuccessException {
        return m_requestor.getJobStatus(nativeJobId);
    }

    public void startListening(String nativeJobId, JobMonitorCallback callback) throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_registry.subscribeJob(nativeJobId, callback);
    }

    public void stopListening(String nativeJobId) throws NotImplementedException, TimeoutException, NoSuccessException {
        m_registry.unsubscribeJob(nativeJobId);
    }
}
