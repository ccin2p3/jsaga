package org.ogf.saga.bootstrap;

import java.lang.reflect.InvocationTargetException;

import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.SagaError;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.job.JobFactory;
import org.ogf.saga.logicalfile.LogicalFileFactory;
import org.ogf.saga.monitoring.MonitoringFactory;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.rpc.RPCFactory;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.stream.StreamFactory;
import org.ogf.saga.task.TaskFactory;

/**
 * The idea of this class is that the SAGA user sets the environment variable
 * "saga.factory" to the classname of an implementation of the
 * {@link SagaFactory} interface.
 * The ImplementationBootstrapLoader instantiates exactly one instance of
 * this class, which must have a public parameter-less constructor.
 */
public class ImplementationBootstrapLoader {

    private static final String PROPERTY_NAME = "saga.factory";

    private static SagaFactory factory;

    private static synchronized void initFactory() {

        if (factory == null) {
            // Obtain the name of the SAGA factory.
            String factoryName = System.getProperty(PROPERTY_NAME);
            if (factoryName == null) {
                throw new SagaError("No SAGA factory name specified");
            }
    
            // Try to obtain a class instance of it, using the current
            // class loader, and running the static initializers.
            Class factoryClass;
            try {
                factoryClass = Class.forName(factoryName);
            } catch(ClassNotFoundException e) {
                throw new SagaError("Could not load class " + factoryName, e);
            }

            // Now try to obtain an instance of this class, using a
            // parameter-less constructor.
            try {
                factory = (SagaFactory) factoryClass.getConstructor()
                    .newInstance();
            } catch(NoSuchMethodException e) {
                throw new SagaError("Factory " + factoryName
                        + " has no public noargs constructor", e);
            } catch(InvocationTargetException e1) {
                throw new SagaError("Constructor of " + factoryName
                        + " threw an exception", e1.getCause());
            } catch(Throwable e2) {
                throw new SagaError("Instantiation of " + factoryName
                        + " failed", e2);
            }
        }
    }

    /**
     * Creates a buffer factory.
     * @return a buffer factory.
     * @throws NotImplemented is thrown when buffers are not implemented.
     */
    public static BufferFactory createBufferFactory()
        throws NotImplemented {
        initFactory();
        return factory.createBufferFactory();
    }

    /**
     * Creates a context factory. Cannot throw NotImplemented.
     * @return a context factory.
     */
    public static ContextFactory createContextFactory() {
        initFactory();
        return factory.createContextFactory();
    }

    /**
     * Creates a file factory. Cannot throw NotImplemented, because the
     * IOVec constructor cannot (according to the SAGA specs).
     * @return a file factory.
     */
    public static FileFactory createFileFactory() {
        initFactory();
        return factory.createFileFactory();
    }

    /**
     * Creates a job factory.
     * @return a job factory.
     * @throws NotImplemented is thrown when jobs are not implemented.
     */
    public static JobFactory createJobFactory()
        throws NotImplemented {
        initFactory();
        return factory.createJobFactory();
    }

    /**
     * Creates a logical file factory.
     * @return a logical file factory.
     * @throws NotImplemented is thrown when logical files are not implemented.
     */
    public static LogicalFileFactory createLogicalFileFactory()
        throws NotImplemented {
        initFactory();
        return factory.createLogicalFileFactory();
    }
    
    /**
     * Creates a monitoring factory.
     * @return a monitoring factory.
     * @throws NotImplemented is thrown when monitoring is not implemented.
     */
    public static MonitoringFactory createMonitoringFactory()
        throws NotImplemented {
        initFactory();
        return factory.createMonitoringFactory();
    }

    /**
     * Creates a namespace factory.
     * @return a namespace factory.
     * @throws NotImplemented is thrown when namespace is not implemented.
     */
    public static NSFactory createNamespaceFactory()
        throws NotImplemented {
        initFactory();
        return factory.createNamespaceFactory();
    }

    /**
     * Creates an RPC factory. Cannot throw NotImplemented,
     * because the Parameter constructor cannot throw NotImplemented.
     * @return an RPC factory.
     */
    public static RPCFactory createRPCFactory() {
        initFactory();
        return factory.createRPCFactory();
    }

    /**
     * Creates a Session factory.
     * @return a Session factory.
     */
    public static SessionFactory createSessionFactory() {
        initFactory();
        return factory.createSessionFactory();
    }

    /**
     * Creates a stream factory.
     * @return a stream factory.
     * @throws NotImplemented is thrown when streams are not implemented.
     */
    public static StreamFactory createStreamFactory()
        throws NotImplemented {
        initFactory();
        return factory.createStreamFactory();
    }

    /**
     * Creates a task factory.
     * @return a task factory.
     * @throws NotImplemented is thrown when tasks are not implemented.
     */
    public static TaskFactory createTaskFactory()
        throws NotImplemented {
        initFactory();
        return factory.createTaskFactory();
    }
}
