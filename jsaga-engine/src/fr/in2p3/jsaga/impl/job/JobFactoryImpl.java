package fr.in2p3.jsaga.impl.job;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.engine.factories.JobAdaptorFactory;
import fr.in2p3.jsaga.engine.factories.JobMonitorAdaptorFactory;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorService;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorServiceFactory;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import fr.in2p3.jsaga.impl.job.description.SAGAJobDescriptionImpl;
import fr.in2p3.jsaga.impl.job.service.JobServiceImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTask;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.job.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobFactoryImpl extends JobFactory {
    private JobAdaptorFactory m_adaptorFactory;
    private JobMonitorServiceFactory m_monitorServiceFactory;

    public JobFactoryImpl(JobAdaptorFactory adaptorFactory, JobMonitorAdaptorFactory monitorAdaptorFactory) {
        m_adaptorFactory = adaptorFactory;
        m_monitorServiceFactory = new JobMonitorServiceFactory(monitorAdaptorFactory);
    }

    protected JobDescription doCreateJobDescription() throws NotImplemented {
        try {
            return new SAGAJobDescriptionImpl();
        } catch (Exception e) {
            throw new NotImplemented("INTERNAL ERROR: Unexpected exception");
        }
    }

    protected JobService doCreateJobService(Session session, URL rm) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        if (rm==null || rm.toString().equals("")) {
            //NOTICE: resource discovery should NOT be done here because it should depend on the job description
            throw new NotImplemented("Resource discovery is not implemented");
        }
        JobControlAdaptor controlAdaptor;
        try {
            controlAdaptor = m_adaptorFactory.getJobControlAdaptor(rm, session);
        } catch (BadParameter e) {
            throw new NoSuccess(e);
        } catch (IncorrectState e) {
            throw new NoSuccess(e);
        }
        JobMonitorService monitorService;
        try {
            monitorService = m_monitorServiceFactory.getJobMonitorService(rm, session);
        } catch (BadParameter e) {
            throw new NoSuccess(e);
        } catch (IncorrectState e) {
            throw new NoSuccess(e);
        }
        return new JobServiceImpl(session, rm, controlAdaptor, monitorService);
    }

    protected Task<JobService> doCreateJobService(TaskMode mode, Session session, URL rm) throws NotImplemented {
        try {
            return AbstractSagaObjectImpl.prepareTask(mode, new GenericThreadedTask(
                    null,
                    this,
                    JobFactoryImpl.class.getMethod("doCreateJobService", new Class[]{Session.class, URL.class}),
                    new Object[]{session, rm}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
}
