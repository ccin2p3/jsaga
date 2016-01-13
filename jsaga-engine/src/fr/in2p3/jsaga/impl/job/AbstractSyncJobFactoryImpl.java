package fr.in2p3.jsaga.impl.job;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.engine.factories.JobAdaptorFactory;
import fr.in2p3.jsaga.engine.factories.JobMonitorAdaptorFactory;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorService;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import fr.in2p3.jsaga.impl.job.description.SAGAJobDescriptionImpl;
import fr.in2p3.jsaga.impl.job.service.JobServiceImpl;
import fr.in2p3.jsaga.impl.session.SessionImpl;
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
    private JobMonitorAdaptorFactory m_monitorAdaptorFactory;

    public AbstractSyncJobFactoryImpl(JobAdaptorFactory adaptorFactory, JobMonitorAdaptorFactory monitorAdaptorFactory) {
        m_adaptorFactory = adaptorFactory;
        m_monitorAdaptorFactory = monitorAdaptorFactory;
    }

    protected JobDescription doCreateJobDescription() throws NotImplementedException {
        try {
            return new SAGAJobDescriptionImpl();
        } catch (Exception e) {
            throw new NotImplementedException("INTERNAL ERROR: Unexpected exception");
        }
    }

    //NOTICE: resource discovery should NOT be done here because it should depend on the job description
    public JobService doCreateJobServiceSync(Session session, URL rm) throws NotImplementedException, BadParameterException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        if (rm!=null && !rm.toString().equals("")) {
            // get context (security + config)
            ContextImpl context;
            try {
                context = ((SessionImpl)session).getBestMatchingContext(rm);
            } catch (DoesNotExistException e) {
                throw new NoSuccessException(e);
            }

            // create adaptor instance
            JobControlAdaptor controlAdaptor = m_adaptorFactory.getJobControlAdaptor(rm, context);
            JobMonitorAdaptor monitorAdaptor = m_monitorAdaptorFactory.getJobMonitorAdaptor(controlAdaptor);

            // get attributes
            Map attributes = m_adaptorFactory.getAttribute(rm, context);

            // connect to control/monitor services
            m_adaptorFactory.connect(rm, controlAdaptor, attributes, context);
            m_monitorAdaptorFactory.connect(rm, monitorAdaptor, attributes, context);

            // initialize translator
            JobDescriptionTranslator translator = controlAdaptor.getJobDescriptionTranslator();
            if (rm.getHost()!=null) {
                translator.setAttribute(JobDescriptionTranslator.HOSTNAME, rm.getHost());
            }
            for (Object o : attributes.entrySet()) {
                Map.Entry attr = (Map.Entry) o;
                translator.setAttribute(""+attr.getKey(), ""+attr.getValue());
            }

            // create JobService
            JobMonitorService monitorService = new JobMonitorService(rm, monitorAdaptor, attributes);
            JobServiceImpl jobService = new JobServiceImpl(session, rm, controlAdaptor, monitorService, translator);

            // register
            context.registerJobService(jobService);
            return jobService;
        } else {
            throw new NotImplementedException("Resource discovery not yet implemented");
        }
    }
}
