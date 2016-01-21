package org.ogf.saga.bootstrap;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.isn.ISNFactory;
import org.ogf.saga.job.JobFactory;
import org.ogf.saga.logicalfile.LogicalFileFactory;
import org.ogf.saga.monitoring.MonitoringFactory;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.resource.ResourceFactory;
import org.ogf.saga.rpc.RPCFactory;
import org.ogf.saga.sd.SDFactory;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.stream.StreamFactory;
import org.ogf.saga.task.TaskFactory;
import org.ogf.saga.url.URLFactory;

/**
 * This class allows the user to have one or more {@link SagaFactory SagaFactories},
 * one for each Java Saga implementation that is to be used. A {@link SagaFactory}
 * must be used to actually create Saga objects. All factory creation methods have an optional
 * parameter, the classname of the Saga factory. When this parameter is not specified,
 * a default Saga factory is used.
 * <p>
 * The classname of the default Saga factory is to be provided by the
 * the environment variable (Java property) <code>saga.factory</code>.
 * <p>
 * The
 * <code>ImplementationBootstrapLoader</code> instantiates
 * exactly one instance of each Saga factory classname. A Saga factory must have a public parameter-less
 * constructor.
 */
public class ImplementationBootstrapLoader {

    /** One ImplementationBootstrapLoader for each SagaFactory class. */
    private static Map<String, ImplementationBootstrapLoader> s_loaders = new HashMap<String, ImplementationBootstrapLoader>();

    /** The saga factory instance, specific for a Saga implementation. */
    private SagaFactory sagaFactory;

    private BufferFactory bufferFactory;
    
    private ContextFactory contextFactory;
    
    private FileFactory fileFactory;
    
    private ISNFactory ISNFactory;
    
    private JobFactory jobFactory;
    
    private LogicalFileFactory logicalFileFactory;
    
    private MonitoringFactory monitoringFactory;
    
    private NSFactory NSFactory;
    
    private RPCFactory RPCFactory;
    
    private SDFactory SDFactory;
    
    private SessionFactory sessionFactory;
    
    private StreamFactory streamFactory;
    
    private TaskFactory taskFactory;
    
    private URLFactory URLFactory;

    private ResourceFactory resourceFactory;

    private static synchronized ImplementationBootstrapLoader getLoader(String factoryName) throws NoSuccessException {
        
	// Deterimine SagaFactory classname if not specified.
        if (factoryName == null) {
            Properties sagaProperties = SagaProperties.getDefaultProperties();
            // Obtain the name of the SAGA factory.
            factoryName = sagaProperties.getProperty(SagaProperties.FACTORY);
            if (factoryName == null) {
                throw new NoSuccessException("No SAGA factory name specified");
            }
        }

        // See if this factory is already available.
        ImplementationBootstrapLoader loader = s_loaders.get(factoryName);
        if (loader == null) {
            // Nope. Create it.
            loader = new ImplementationBootstrapLoader(factoryName);

            // Put instance in map
            s_loaders.put(factoryName, loader);
        }
        
        return loader;
    }
    
    private ImplementationBootstrapLoader(String factoryName) throws NoSuccessException {
	
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
        // parameter-less constructor. All SagaFactory implementations should have that,
        // and it should be public.
        try {
            sagaFactory = (SagaFactory) factoryClass.getConstructor()
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

    /**
     * Creates a buffer factory, using the specified SagaFactory.
     * 
     * @param factoryName classname of the Saga factory to be used, or null.
     * @return a buffer factory.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static BufferFactory getBufferFactory(String factoryName)
            throws NoSuccessException {
	ImplementationBootstrapLoader l = getLoader(factoryName);
	synchronized(l) {
	    if (l.bufferFactory == null) {
		l.bufferFactory = l.sagaFactory.createBufferFactory();
	    }
	    return l.bufferFactory;
	}
    }
    
    /**
     * Creates a context factory, using the specified SagaFactory.
     * 
     * @param factoryName classname of the Saga factory to be used, or null.
     * @return a context factory.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static ContextFactory getContextFactory(String factoryName)
            throws NoSuccessException {
	ImplementationBootstrapLoader l = getLoader(factoryName);
	synchronized(l) {
	    if (l.contextFactory == null) {
		l.contextFactory = l.sagaFactory.createContextFactory();
	    }
	    return l.contextFactory;
	}
    }
    
    /**
     * Creates a session factory, using the specified SagaFactory.
     * 
     * @param factoryName classname of the Saga factory to be used, or null.
     * @return a session factory.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static SessionFactory getSessionFactory(String factoryName)
            throws NoSuccessException {
	ImplementationBootstrapLoader l = getLoader(factoryName);
	synchronized(l) {
	    if (l.sessionFactory == null) {
		l.sessionFactory = l.sagaFactory.createSessionFactory();
	    }
	    return l.sessionFactory;
	}
    }
        
    /**
     * Creates a file factory, using the specified SagaFactory.
     * This method annot throw NotImplemented, because the IOVec
     * constructor cannot (according to the SAGA specs).
     * 
     * @param factoryName classname of the Saga factory to be used, or null.
     * @return a file factory.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static FileFactory getFileFactory(String factoryName)
            throws NoSuccessException {
	ImplementationBootstrapLoader l = getLoader(factoryName);
	synchronized(l) {
	    if (l.fileFactory == null) {
		l.fileFactory = l.sagaFactory.createFileFactory();
	    }
	    return l.fileFactory;
	}
    }
    
    /**
     * Creates a job factory, using the specified SagaFactory.
     * 
     * @param factoryName classname of the Saga factory to be used, or null.
     * @return a job factory.
     * @throws NotImplementedException
     *             is thrown when jobs are not implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static JobFactory getJobFactory(String factoryName) throws NotImplementedException,
            NoSuccessException {
	ImplementationBootstrapLoader l = getLoader(factoryName);
	synchronized(l) {
	    if (l.jobFactory == null) {
		l.jobFactory = l.sagaFactory.createJobFactory();
	    }
	    return l.jobFactory;
	}
    }

    /**
     * Creates a logical file factory, using the specified SagaFactory.
     * 
     * @param factoryName classname of the Saga factory to be used, or null.
     * @return a logical file factory.
     * @throws NotImplementedException
     *             is thrown when logical files are not implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static LogicalFileFactory getLogicalFileFactory(String factoryName)
            throws NotImplementedException, NoSuccessException {
	ImplementationBootstrapLoader l = getLoader(factoryName);
	synchronized(l) {
	    if (l.logicalFileFactory == null) {
		l.logicalFileFactory = l.sagaFactory.createLogicalFileFactory();
	    }
	    return l.logicalFileFactory;
	}
    }

    /**
     * Creates a monitoring factory, using the specified SagaFactory.
     * 
     * @param factoryName classname of the Saga factory to be used, or null.
     * @return a monitoring factory.
     * @throws NotImplementedException
     *             is thrown when monitoring is not implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static MonitoringFactory getMonitoringFactory(String factoryName)
            throws NotImplementedException, NoSuccessException {
	ImplementationBootstrapLoader l = getLoader(factoryName);
	synchronized(l) {
	    if (l.monitoringFactory == null) {
		l.monitoringFactory = l.sagaFactory.createMonitoringFactory();
	    }
	    return l.monitoringFactory;
	}
    }

    /**
     * Creates a namespace factory, using the specified SagaFactory.
     * 
     * @param factoryName classname of the Saga factory to be used, or null.
     * @return a namespace factory.
     * @throws NotImplementedException
     *             is thrown when namespace is not implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static NSFactory getNamespaceFactory(String factoryName)
            throws NotImplementedException, NoSuccessException {
	ImplementationBootstrapLoader l = getLoader(factoryName);
	synchronized(l) {
	    if (l.NSFactory == null) {
		l.NSFactory = l.sagaFactory.createNamespaceFactory();
	    }
	    return l.NSFactory;
	}
    }

    /**
     * Creates an RPC factory, using the specified SagaFactory.
     * 
     * @param factoryName classname of the Saga factory to be used, or null.
     * @return an RPC factory.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     * @throws NotImplementedException 
     * @throws NotImplementedException
     *             is thrown when RPC is not implemented.            
     */
    public static RPCFactory getRPCFactory(String factoryName) throws NoSuccessException,
            NotImplementedException {
	ImplementationBootstrapLoader l = getLoader(factoryName);
	synchronized(l) {
	    if (l.RPCFactory == null) {
		l.RPCFactory = l.sagaFactory.createRPCFactory();
	    }
	    return l.RPCFactory;
	}
    }

    /**
     * Creates a stream factory, using the specified SagaFactory.
     * 
     * @param factoryName classname of the Saga factory to be used, or null.
     * @return a stream factory.
     * @throws NotImplementedException
     *             is thrown when streams are not implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static StreamFactory getStreamFactory(String factoryName)
            throws NotImplementedException, NoSuccessException {
	ImplementationBootstrapLoader l = getLoader(factoryName);
	synchronized(l) {
	    if (l.streamFactory == null) {
		l.streamFactory = l.sagaFactory.createStreamFactory();
	    }
	    return l.streamFactory;
	}
    }

    /**
     * Creates a task factory, using the specified SagaFactory.
     * 
     * @param factoryName classname of the Saga factory to be used, or null.
     * @return a task factory.
     * @throws NotImplementedException
     *             is thrown when tasks are not implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static TaskFactory getTaskFactory(String factoryName)
            throws NotImplementedException, NoSuccessException {
	ImplementationBootstrapLoader l = getLoader(factoryName);
	synchronized(l) {
	    if (l.taskFactory == null) {
		l.taskFactory = l.sagaFactory.createTaskFactory();
	    }
	    return l.taskFactory;
	}
    }
    
    /**
     * Creates an URL factory, using the specified SagaFactory.
     * 
     * @param factoryName classname of the Saga factory to be used, or null.
     * @return an URL factory.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static URLFactory getURLFactory(String factoryName)
            throws NoSuccessException {
	ImplementationBootstrapLoader l = getLoader(factoryName);
	synchronized(l) {
	    if (l.URLFactory == null) {
		l.URLFactory = l.sagaFactory.createURLFactory();
	    }
	    return l.URLFactory;
	}
    }
    
    // REMOVE FROM GFD DOC
    /**
     * Creates a SD factory for Service Discovery, using the specified SagaFactory.
     * 
     * @param factoryName classname of the Saga factory to be used, or null.
     * @return a SD factory.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     * @throws NotImplementedException
     *             is thrown when SD is not implemented.   
     */
    public static SDFactory getSDFactory(String factoryName)
            throws NotImplementedException, NoSuccessException {
	ImplementationBootstrapLoader l = getLoader(factoryName);
	synchronized(l) {
	    if (l.SDFactory == null) {
		l.SDFactory = l.sagaFactory.createSDFactory();
	    }
	    return l.SDFactory;
	}
    }
    
    /**
     * Creates a ISN factory for the Information System Navigator, using the specified SagaFactory.
     * 
     * @param factoryName classname of the Saga factory to be used, or null.
     * @return an ISN factory.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     * @throws NotImplementedException
     *             is thrown when ISN is not implemented.   
     */
    public static ISNFactory getISNFactory(String factoryName)
            throws NotImplementedException, NoSuccessException {
	ImplementationBootstrapLoader l = getLoader(factoryName);
	synchronized(l) {
	    if (l.ISNFactory == null) {
		l.ISNFactory = l.sagaFactory.createISNFactory();
	    }
	    return l.ISNFactory;
	}
    } 
    // END REMOVE FROM GFD DOC

    /**
     * Creates a manager factory, using the specified SagaFactory.
     *
     * @param factoryName classname of the Saga factory to be used, or null.
     * @return an URL factory.
     * @throws NotImplementedException
     *             is thrown when resource management is not implemented.
     * @throws NoSuccessException
     *             is thrown when the Saga factory could not be created.
     */
    public static ResourceFactory getManagerFactory(String factoryName)
            throws NotImplementedException, NoSuccessException {
        ImplementationBootstrapLoader l = getLoader(factoryName);
        synchronized(l) {
            if (l.resourceFactory == null) {
                l.resourceFactory = l.sagaFactory.createManagerFactory();
            }
            return l.resourceFactory;
        }
    }
}
