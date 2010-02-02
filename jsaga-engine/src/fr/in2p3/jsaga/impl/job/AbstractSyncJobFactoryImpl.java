package fr.in2p3.jsaga.impl.job;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.engine.factories.JobAdaptorFactory;
import fr.in2p3.jsaga.engine.factories.JobMonitorAdaptorFactory;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorService;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorServiceFactory;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import fr.in2p3.jsaga.impl.job.description.SAGAJobDescriptionImpl;
import fr.in2p3.jsaga.impl.job.service.JobServiceImpl;
import fr.in2p3.jsaga.impl.job.service.LateBindedJobServiceImpl;
import fr.in2p3.jsaga.sync.job.SyncJobFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.job.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractSyncJobFactoryImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   5 juin 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractSyncJobFactoryImpl extends JobFactory implements SyncJobFactory {
    private JobAdaptorFactory m_adaptorFactory;
    private JobMonitorServiceFactory m_monitorServiceFactory;

    public AbstractSyncJobFactoryImpl(JobAdaptorFactory adaptorFactory, JobMonitorAdaptorFactory monitorAdaptorFactory) {
        m_adaptorFactory = adaptorFactory;
        m_monitorServiceFactory = new JobMonitorServiceFactory(monitorAdaptorFactory);
    }

    protected JobDescription doCreateJobDescription() throws NotImplementedException {
        try {
            return new SAGAJobDescriptionImpl();
        } catch (Exception e) {
            throw new NotImplementedException("INTERNAL ERROR: Unexpected exception");
        }
    }

    //NOTICE: resource discovery should NOT be done here because it should depend on the job description
    public JobService doCreateJobServiceSync(Session session, URL rm) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        if (rm!=null && !rm.toString().equals("")) {
            // get config
            fr.in2p3.jsaga.engine.schema.config.JobService config = m_adaptorFactory.getConfig(rm);
            // get adaptor
            JobControlAdaptor controlAdaptor = m_adaptorFactory.getJobControlAdaptor(config);
            // get context
            ContextImpl context = m_adaptorFactory.getContextImpl(rm, controlAdaptor, config, session);
            // get attributes
            Map attributes = m_adaptorFactory.getAttributes(rm, config);

            // connect to control/monitor services
            JobMonitorService monitorService;
            try {
                m_adaptorFactory.connect(rm, controlAdaptor, attributes, context);
                monitorService = m_monitorServiceFactory.getJobMonitorService(rm, config, context);
            } catch (BadParameterException e) {
                throw new NoSuccessException(e);
            }

            // create JobService
            JobServiceImpl jobService = new JobServiceImpl(session, rm, controlAdaptor, monitorService);

            // register
            if (context != null) {
                context.registerJobService(jobService, attributes);
            }
            return jobService;
        } else {
            return new LateBindedJobServiceImpl(session);
        }
    }
}
