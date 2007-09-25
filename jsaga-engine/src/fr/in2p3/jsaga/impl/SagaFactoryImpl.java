package fr.in2p3.jsaga.impl;

import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import fr.in2p3.jsaga.impl.buffer.BufferFactoryImpl;
import fr.in2p3.jsaga.impl.context.ContextFactoryImpl;
import fr.in2p3.jsaga.impl.file.FileFactoryImpl;
import fr.in2p3.jsaga.impl.job.JobFactoryImpl;
import fr.in2p3.jsaga.impl.logicalfile.LogicalFileFactoryImpl;
import fr.in2p3.jsaga.impl.monitoring.MonitoringFactoryImpl;
import fr.in2p3.jsaga.impl.namespace.NamespaceFactoryImpl;
import fr.in2p3.jsaga.impl.session.SessionFactoryImpl;
import fr.in2p3.jsaga.impl.task.TaskFactoryImpl;
import fr.in2p3.jsaga.impl.unimplemented.RPCFactoryImpl;
import org.ogf.saga.bootstrap.SagaFactory;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.job.JobFactory;
import org.ogf.saga.logicalfile.LogicalFileFactory;
import org.ogf.saga.monitoring.MonitoringFactory;
import org.ogf.saga.namespace.NamespaceFactory;
import org.ogf.saga.rpc.RPCFactory;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.stream.StreamFactory;
import org.ogf.saga.task.TaskFactory;

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
    private DataAdaptorFactory m_adaptorFactory;

    public SagaFactoryImpl() throws ConfigurationException {
        Configuration config = Configuration.getInstance();
        m_adaptorFactory = new DataAdaptorFactory(config);
    }

    public BufferFactory createBufferFactory() throws NotImplemented {
        return new BufferFactoryImpl();
    }

    public ContextFactory createContextFactory() {
        return new ContextFactoryImpl();
    }

    public FileFactory createFileFactory() {
        return new FileFactoryImpl(m_adaptorFactory);
    }

    public JobFactory createJobFactory() throws NotImplemented {
        return new JobFactoryImpl();
    }

    public LogicalFileFactory createLogicalFileFactory() throws NotImplemented {
        return new LogicalFileFactoryImpl(m_adaptorFactory);
    }

    public MonitoringFactory createMonitoringFactory() throws NotImplemented {
        return new MonitoringFactoryImpl();
    }

    public NamespaceFactory createNamespaceFactory() throws NotImplemented {
        return new NamespaceFactoryImpl(m_adaptorFactory);
    }

    public RPCFactory createRPCFactory() {
        return new RPCFactoryImpl();
    }

    public SessionFactory createSessionFactory() {
        return new SessionFactoryImpl();
    }

    public StreamFactory createStreamFactory() throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }

    public TaskFactory createTaskFactory() throws NotImplemented {
        return new TaskFactoryImpl();
    }
}
