package org.ogf.saga.bootstrap;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.job.JobFactory;
import org.ogf.saga.logicalfile.LogicalFileFactory;
import org.ogf.saga.monitoring.MonitoringFactory;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.rpc.RPCFactory;
import org.ogf.saga.sd.SDFactory;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.stream.StreamFactory;
import org.ogf.saga.task.TaskFactory;
import org.ogf.saga.url.URLFactory;

/**
 * The idea of this class is that the SAGA user sets the environment variable
 * (Java property) <code>saga.factory</code> to the classname of an
 * implementation of the {@link SagaFactory} interface. The
 * <code>ImplementationBootstrapLoader</code> instantiates
 * exactly one instance of this class, which must have a public parameter-less
 * constructor.
 */
public class ImplementationBootstrapLoader {

    private static final String PROPERTY_NAME = "saga.factory";

    private static SagaFactory factory;

    private static synchronized void initFactory() throws NoSuccessException {

        if (factory == null) {
            Properties sagaProperties = SagaProperties.getDefaultProperties();
            // Obtain the name of the SAGA factory.
            String factoryName = sagaProperties.getProperty(PROPERTY_NAME);
            if (factoryName == null) {
                throw new NoSuccessException("No SAGA factory name specified");
            }

            // Try to obtain a class instance of it, using the current
            // class loader, and running the static initializers.
            Class<?> factoryClass;
            try {
                factoryClass = Class.forName(factoryName);
            } catch (ClassNotFoundException e) {
                throw new NoSuccessException("Could not load class "
                        + factoryName, e);
            }

            // Now try to obtain an instance of this class, using a
            // parameter-less constructor.
            try {
                factory = (SagaFactory) factoryClass.getConstructor()
                        .newInstance();
            } catch (NoSuchMethodException e) {
                throw new NoSuccessException("Factory " + factoryName
                        + " has no public noargs constructor", e);
            } catch (InvocationTargetException e1) {
                throw new NoSuccessException("Constructor of " + factoryName
                        + " threw an exception", e1.getCause());
            } catch (Throwable e2) {
                throw new NoSuccessException("Instantiation of " + factoryName
                        + " failed", e2);
            }
        }
    }

    /**
     * Creates a buffer factory.
     * 
     * @return a buffer factory.
     * @throws NotImplementedException
     *             is thrown when buffers are not implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static BufferFactory createBufferFactory()
            throws NotImplementedException, NoSuccessException {
        initFactory();
        return factory.createBufferFactory();
    }

    /**
     * Creates a context factory. Cannot throw NotImplemented.
     * 
     * @return a context factory.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static ContextFactory createContextFactory()
            throws NoSuccessException {
        initFactory();
        return factory.createContextFactory();
    }

    /**
     * Creates a file factory. Cannot throw NotImplemented, because the IOVec
     * constructor cannot (according to the SAGA specs).
     * 
     * @return a file factory.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static FileFactory createFileFactory() throws NoSuccessException {
        initFactory();
        return factory.createFileFactory();
    }

    /**
     * Creates a job factory.
     * 
     * @return a job factory.
     * @throws NotImplementedException
     *             is thrown when jobs are not implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static JobFactory createJobFactory() throws NotImplementedException,
            NoSuccessException {
        initFactory();
        return factory.createJobFactory();
    }

    /**
     * Creates a logical file factory.
     * 
     * @return a logical file factory.
     * @throws NotImplementedException
     *             is thrown when logical files are not implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static LogicalFileFactory createLogicalFileFactory()
            throws NotImplementedException, NoSuccessException {
        initFactory();
        return factory.createLogicalFileFactory();
    }

    /**
     * Creates a monitoring factory.
     * 
     * @return a monitoring factory.
     * @throws NotImplementedException
     *             is thrown when monitoring is not implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static MonitoringFactory createMonitoringFactory()
            throws NotImplementedException, NoSuccessException {
        initFactory();
        return factory.createMonitoringFactory();
    }

    /**
     * Creates a namespace factory.
     * 
     * @return a namespace factory.
     * @throws NotImplementedException
     *             is thrown when namespace is not implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static NSFactory createNamespaceFactory()
            throws NotImplementedException, NoSuccessException {
        initFactory();
        return factory.createNamespaceFactory();
    }

    /**
     * Creates an RPC factory.
     * 
     * @return an RPC factory.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     * @throws NotImplementedException 
     * @throws NotImplementedException
     *             is thrown when RPC is not implemented.            
     */
    public static RPCFactory createRPCFactory() throws NoSuccessException,
            NotImplementedException {
        initFactory();
        return factory.createRPCFactory();
    }

    /**
     * Creates a SD factory for Service Discovery.
     * 
     * @return a SD factory.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     * @throws NotImplementedException
     *             is thrown when SD is not implemented.   
     */
    public static SDFactory createSDFactory()
            throws NotImplementedException, NoSuccessException {
        initFactory();
        return factory.createSDFactory();
    } 

    /**
     * Creates a Session factory.
     * 
     * @return a Session factory.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static SessionFactory createSessionFactory()
            throws NoSuccessException {
        initFactory();
        return factory.createSessionFactory();
    }

    /**
     * Creates a stream factory.
     * 
     * @return a stream factory.
     * @throws NotImplementedException
     *             is thrown when streams are not implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static StreamFactory createStreamFactory()
            throws NotImplementedException, NoSuccessException {
        initFactory();
        return factory.createStreamFactory();
    }

    /**
     * Creates a task factory.
     * 
     * @return a task factory.
     * @throws NotImplementedException
     *             is thrown when tasks are not implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static TaskFactory createTaskFactory()
            throws NotImplementedException, NoSuccessException {
        initFactory();
        return factory.createTaskFactory();
    }
    
    /**
     * Creates an URL factory.
     * 
     * @return an URL factory.
     * @throws NotImplementedException
     *             is thrown when URLs are not implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static URLFactory createURLFactory()
            throws NotImplementedException, NoSuccessException {
        initFactory();
        return factory.createURLFactory();
    }
}
