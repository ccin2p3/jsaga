package fr.in2p3.jsaga.impl;

import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.engine.session.SessionConfiguration;
import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.descriptors.AdaptorDescriptors;
import fr.in2p3.jsaga.engine.factories.*;
import fr.in2p3.jsaga.impl.buffer.BufferFactoryImpl;
import fr.in2p3.jsaga.impl.context.ContextFactoryImpl;
import fr.in2p3.jsaga.impl.file.FileFactoryImpl;
import fr.in2p3.jsaga.impl.job.JobFactoryImpl;
import fr.in2p3.jsaga.impl.logicalfile.LogicalFileFactoryImpl;
import fr.in2p3.jsaga.impl.monitoring.MonitoringFactoryImpl;
import fr.in2p3.jsaga.impl.namespace.NSFactoryImpl;
import fr.in2p3.jsaga.impl.session.SessionFactoryImpl;
import fr.in2p3.jsaga.impl.task.TaskFactoryImpl;
import fr.in2p3.jsaga.impl.unimplemented.RPCFactoryImpl;
import fr.in2p3.jsaga.impl.url.URLFactoryImpl;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

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
    private SessionConfiguration m_config;

    // security
    private SecurityAdaptorFactory m_securityAdaptorFactory;
    // data
    private DataAdaptorFactory m_dataAdaptorFactory;
    // job
    private JobAdaptorFactory m_jobAdaptorFactory;
    private JobMonitorAdaptorFactory m_jobMonitorAdaptorFactory;

    public SagaFactoryImpl() throws ConfigurationException {
        // configure log4j
        URL log4jCfgURL = EngineProperties.getURL(EngineProperties.LOG4J_CONFIGURATION);
        if (log4jCfgURL != null) {
            Properties prop = new Properties();
            try {
                InputStream log4jCfgStream = log4jCfgURL.openStream();
                prop.load(log4jCfgStream);
                log4jCfgStream.close();
            } catch (IOException e) {
                Logger.getLogger(SagaFactoryImpl.class).warn("Failed to load log4j properties, using defaults ["+e.getMessage()+"]");
            }
            PropertyConfigurator.configure(prop);
        }
        if (EngineProperties.getException() != null) {
            Logger.getLogger(SagaFactoryImpl.class).warn("Failed to load engine properties, using defaults ["+EngineProperties.getException().getMessage()+"]");
        }

        // configure adaptors factories
        URL url = EngineProperties.getURL(EngineProperties.JSAGA_DEFAULT_CONTEXTS);
        m_config = new SessionConfiguration(url);

        AdaptorDescriptors descriptors = AdaptorDescriptors.getInstance();
        m_securityAdaptorFactory = new SecurityAdaptorFactory(descriptors);
        m_dataAdaptorFactory = new DataAdaptorFactory(descriptors);
        m_jobAdaptorFactory = new JobAdaptorFactory(descriptors);
        m_jobMonitorAdaptorFactory = new JobMonitorAdaptorFactory();
    }

    public BufferFactory createBufferFactory() throws NotImplementedException {
        return new BufferFactoryImpl();
    }

    public ContextFactory createContextFactory() {
        return new ContextFactoryImpl(m_config, m_securityAdaptorFactory);
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
        return new SessionFactoryImpl(m_config);
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
}
