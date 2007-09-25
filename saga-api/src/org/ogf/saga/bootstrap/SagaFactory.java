package org.ogf.saga.bootstrap;

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

/**
 * This interface must be implemented by a SAGA implementation that uses
 * these language bindings. It creates factories for all packages in SAGA.
 * See the {@link ImplementationBootstrapLoader} description.
 */
public interface SagaFactory {

    /**
     * Creates a factory for the Saga buffer package.
     * @return the buffer factory.
     * @exception NotImplemented is thrown when buffers are not implemented.
     */
    BufferFactory createBufferFactory() throws NotImplemented;

    /**
     * Creates a factory for the Saga context package.
     * @return the context factory.
     */
    ContextFactory createContextFactory();

    /**
     * Creates a factory for the Saga file package.
     * Note: this method cannot throw NotImplemented, because the
     * IOVec constructor from the SAGA specs does not throw NotImplemented.
     * @return the File factory.
     */
    FileFactory createFileFactory();

    /**
     * Creates a factory for the Saga jobs package.
     * @return the jobs factory.
     * @exception NotImplemented is thrown when jobs are not implemented.
     */
    JobFactory createJobFactory() throws NotImplemented;

    /**
     * Creates a factory for the Saga logical file package.
     * @return the logical file factory.
     * @exception NotImplemented is thrown when logical file is not implemented.
     */
    LogicalFileFactory createLogicalFileFactory() throws NotImplemented;

    /**
     * Creates a factory for the Saga monitoring package.
     * @return the monitoring factory.
     * @exception NotImplemented is thrown when monitoring is not implemented.
     */
    MonitoringFactory createMonitoringFactory() throws NotImplemented;

    /**
     * Creates a factory for the Saga namespace package.
     * @return the namespace factory.
     * @exception NotImplemented is thrown when namespaces are not implemented.
     */
    NamespaceFactory createNamespaceFactory() throws NotImplemented;

    /**
     * Creates a factory for the Saga RPC package.
     * Note: this method cannot throw NotImplemented, because the
     * Parameter constructor from the SAGA specs does not throw NotImplemented.
     * @return the RPC factory.
     */
    RPCFactory createRPCFactory();

    /**
     * Creates a factory for the Saga session package.
     * @return the session factory.
     */
    SessionFactory createSessionFactory();

    /**
     * Creates a factory for the Saga stream package.
     * @return the stream factory.
     * @exception NotImplemented is thrown when streams are not implemented.
     */
    StreamFactory createStreamFactory() throws NotImplemented;

    /**
     * Creates a factory for the Saga task package.
     * @return the task factory.
     * @exception NotImplemented is thrown when tasks are not implemented.
     */
    TaskFactory createTaskFactory() throws NotImplemented;
}
