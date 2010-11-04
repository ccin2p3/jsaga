package org.ogf.saga.bootstrap;

import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.context.ContextFactory;
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
 * This interface must be implemented by a SAGA implementation that uses these
 * language bindings. It creates factories for all packages in SAGA. See the
 * {@link ImplementationBootstrapLoader} description.
 */
public interface SagaFactory {

    /**
     * Creates a factory for the Saga buffer package.
     * 
     * @return the buffer factory.
     * @exception NotImplementedException
     *                is thrown when buffers are not implemented.
     */
    BufferFactory createBufferFactory() throws NotImplementedException;

    /**
     * Creates a factory for the Saga context package.
     * 
     * @return the context factory.
     */
    ContextFactory createContextFactory();

    /**
     * Creates a factory for the Saga file package. Note: this method cannot
     * throw NotImplemented, because the IOVec constructor from the SAGA specs
     * does not throw NotImplemented.
     * 
     * @return the File factory.
     */
    FileFactory createFileFactory();

    /**
     * Creates a factory for the Saga jobs package.
     * 
     * @return the jobs factory.
     * @exception NotImplementedException
     *                is thrown when jobs are not implemented.
     */
    JobFactory createJobFactory() throws NotImplementedException;

    /**
     * Creates a factory for the Saga logical file package.
     * 
     * @return the logical file factory.
     * @exception NotImplementedException
     *                is thrown when logical file is not implemented.
     */
    LogicalFileFactory createLogicalFileFactory()
            throws NotImplementedException;

    /**
     * Creates a factory for the Saga monitoring package.
     * 
     * @return the monitoring factory.
     * @exception NotImplementedException
     *                is thrown when monitoring is not implemented.
     */
    MonitoringFactory createMonitoringFactory() throws NotImplementedException;

    /**
     * Creates a factory for the Saga namespace package.
     * 
     * @return the namespace factory.
     * @exception NotImplementedException
     *                is thrown when namespaces are not implemented.
     */
    NSFactory createNamespaceFactory() throws NotImplementedException;

    /**
     * Creates a factory for the Saga RPC package.
     * 
     * @return the RPC factory.
     * @exception NotImplementedException
     *                is thrown when RPC is not implemented.
     */
    RPCFactory createRPCFactory() throws NotImplementedException;

    /**
     * Creates a factory for the Saga Service Discovery package.
     * 
     * @return the SD factory.
     * @exception NotImplementedException
     *                is thrown when SD is not implemented.
     */
    SDFactory createSDFactory() throws NotImplementedException;

    /**
     * Creates a factory for the Saga session package.
     * 
     * @return the session factory.
     */
    SessionFactory createSessionFactory();

    /**
     * Creates a factory for the Saga stream package.
     * 
     * @return the stream factory.
     * @exception NotImplementedException
     *                is thrown when streams are not implemented.
     */
    StreamFactory createStreamFactory() throws NotImplementedException;

    /**
     * Creates a factory for the Saga task package.
     * 
     * @return the task factory.
     * @exception NotImplementedException
     *                is thrown when tasks are not implemented.
     */
    TaskFactory createTaskFactory() throws NotImplementedException;

    /**
     * Creates a factory for the Saga URL package.
     * 
     * @return the URL factory.
     * @exception NotImplementedException
     *                is thrown when URLs are not implemented.
     */
    URLFactory createURLFactory() throws NotImplementedException;
}
