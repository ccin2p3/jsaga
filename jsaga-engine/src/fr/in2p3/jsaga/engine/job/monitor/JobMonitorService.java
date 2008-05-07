package fr.in2p3.jsaga.engine.job.monitor;

import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.adaptor.job.monitor.*;
import fr.in2p3.jsaga.engine.job.monitor.listen.FilteredJobStatusListener;
import fr.in2p3.jsaga.engine.job.monitor.listen.IndividualJobStatusListener;
import fr.in2p3.jsaga.engine.job.monitor.poll.*;
import fr.in2p3.jsaga.engine.job.monitor.request.JobStatusRequestor;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;

import java.util.Timer;

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
    private JobStatusRequestor m_requestor;
    private JobRegistry m_registry;
    private Timer m_timer;

    /** constructor */
    public JobMonitorService(URL url, JobMonitorAdaptor adaptor) throws NotImplemented, Timeout, NoSuccess {
        // set URL
        m_url = url;

        // set requestor
        m_requestor = new JobStatusRequestor(adaptor);
        
        // set registry
        if (adaptor instanceof ListenFilteredJob) {
            m_registry = new FilteredJobStatusListener((ListenFilteredJob)adaptor, m_requestor);
        } else if (adaptor instanceof ListenIndividualJob) {
            m_registry = new IndividualJobStatusListener((ListenIndividualJob)adaptor, m_requestor);
        } else if (adaptor instanceof QueryJob) {
            AbstractJobStatusPoller poller;
            if (adaptor instanceof QueryFilteredJob) {
                poller = new FilteredJobStatusPoller((QueryFilteredJob)adaptor);
            } else if (adaptor instanceof QueryListJob) {
                poller = new ListJobStatusPoller((QueryListJob)adaptor);
            } else if (adaptor instanceof QueryIndividualJob) {
                poller = new IndividualJobStatusPoller((QueryIndividualJob)adaptor);
            } else {
                throw new NotImplemented("Querying job status not implemented for adaptor: "+adaptor.getClass().getName());
            }
            m_registry = poller;
            int pollPeriod = EngineProperties.getInteger(EngineProperties.JOB_MONITOR_POLL_PERIOD);
            m_timer = new Timer();
            m_timer.schedule(poller, 0, pollPeriod);
        } else {
            throw new NotImplemented("Adaptor does not implement any monitoring interface: "+adaptor.getClass().getName());
        }
    }

    /** destructor */
    protected void finalize() throws Throwable {
        super.finalize();
        m_timer.cancel();
    }

    public URL getURL() {
        return m_url;
    }

    public JobStatus getState(String nativeJobId) throws NotImplemented, Timeout, NoSuccess {
        return m_requestor.getJobStatus(nativeJobId);
    }

    public void startListening(String nativeJobId, JobMonitorCallback callback) throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        m_registry.subscribeJob(nativeJobId, callback);
    }

    public void stopListening(String nativeJobId) throws NotImplemented, Timeout, NoSuccess {
        m_registry.unsubscribeJob(nativeJobId);
    }
}
