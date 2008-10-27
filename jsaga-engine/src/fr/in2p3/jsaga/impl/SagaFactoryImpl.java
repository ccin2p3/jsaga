package fr.in2p3.jsaga.impl;

import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.factories.*;
import fr.in2p3.jsaga.engine.workflow.WorkflowFactoryImpl;
import fr.in2p3.jsaga.impl.buffer.BufferFactoryImpl;
import fr.in2p3.jsaga.impl.context.ContextFactoryImpl;
import fr.in2p3.jsaga.impl.file.FileFactoryImpl;
import fr.in2p3.jsaga.impl.job.JobFactoryImpl;
import fr.in2p3.jsaga.impl.jobcollection.JobCollectionFactoryImpl;
import fr.in2p3.jsaga.impl.logicalfile.LogicalFileFactoryImpl;
import fr.in2p3.jsaga.impl.monitoring.MonitoringFactoryImpl;
import fr.in2p3.jsaga.impl.namespace.NSFactoryImpl;
import fr.in2p3.jsaga.impl.session.SessionFactoryImpl;
import fr.in2p3.jsaga.impl.task.TaskFactoryImpl;
import fr.in2p3.jsaga.impl.unimplemented.RPCFactoryImpl;
import fr.in2p3.jsaga.impl.url.URLFactoryImpl;
import fr.in2p3.jsaga.jobcollection.JobCollectionFactory;
import fr.in2p3.jsaga.workflow.WorkflowFactory;
import org.ogf.saga.bootstrap.SagaFactory;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.job.JobFactory;
import org.ogf.saga.logicalfile.LogicalFileFactory;
import org.ogf.saga.monitoring.MonitoringFactory;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.rpc.RPCFactory;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.stream.StreamFactory;
import org.ogf.saga.task.TaskFactory;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SagaFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SagaFactoryImpl implements SagaFactory {
    // data
    private DataAdaptorFactory m_dataAdaptorFactory;
    // job
    private JobAdaptorFactory m_jobAdaptorFactory;
    private JobMonitorAdaptorFactory m_jobMonitorAdaptorFactory;
    // job collection
    private LanguageAdaptorFactory m_languageAdaptorFactory;
    private EvaluatorAdaptorFactory m_evaluatorAdaptorFactory;

    public SagaFactoryImpl() throws ConfigurationException {
        Configuration config = Configuration.getInstance();
        m_dataAdaptorFactory = new DataAdaptorFactory(config);
        m_jobAdaptorFactory = new JobAdaptorFactory(config);
        m_jobMonitorAdaptorFactory = new JobMonitorAdaptorFactory(config);
        m_languageAdaptorFactory = new LanguageAdaptorFactory(config);
        m_evaluatorAdaptorFactory = new EvaluatorAdaptorFactory(config);
    }

    public BufferFactory createBufferFactory() throws NotImplementedException {
        return new BufferFactoryImpl();
    }

    public ContextFactory createContextFactory() {
        return new ContextFactoryImpl();
    }

    public FileFactory createFileFactory() {
        return new FileFactoryImpl(m_dataAdaptorFactory);
    }

    public JobFactory createJobFactory() throws NotImplementedException {
        return new JobFactoryImpl(m_jobAdaptorFactory, m_jobMonitorAdaptorFactory);
    }

    public LogicalFileFactory createLogicalFileFactory() throws NotImplementedException {
        return new LogicalFileFactoryImpl(m_dataAdaptorFactory);
    }

    public MonitoringFactory createMonitoringFactory() throws NotImplementedException {
        return new MonitoringFactoryImpl();
    }

    public NSFactory createNamespaceFactory() throws NotImplementedException {
        return new NSFactoryImpl(m_dataAdaptorFactory);
    }

    public RPCFactory createRPCFactory() {
        return new RPCFactoryImpl();
    }

    public SessionFactory createSessionFactory() {
        return new SessionFactoryImpl();
    }

    public StreamFactory createStreamFactory() throws NotImplementedException {
        throw new NotImplementedException("Not implemented by the SAGA engine");
    }

    public TaskFactory createTaskFactory() throws NotImplementedException {
        return new TaskFactoryImpl();
    }

    public URLFactory createURLFactory() throws NotImplementedException {
        return new URLFactoryImpl();
    }

    public WorkflowFactory createWorkflowFactory() throws NotImplementedException {
        return new WorkflowFactoryImpl();
    }

    public JobCollectionFactory createJobCollectionFactory() throws NotImplementedException {
        return new JobCollectionFactoryImpl(m_languageAdaptorFactory, m_evaluatorAdaptorFactory);
    }
}
