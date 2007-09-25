package org.ogf.saga.task;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.Timeout;

/**
 * Factory for objects in the task package.
 */
public abstract class TaskFactory {
    
    private static TaskFactory factory;

    private synchronized static void initializeFactory()
        throws NotImplemented {
        if (factory == null) {
            factory = ImplementationBootstrapLoader.createTaskFactory();
        }
    }
    
    /**
     * Constructs a <code>TaskContainer</code> object.
     * This method is to be provided by the factory.
     * @return the task container.
     */
    protected abstract TaskContainer doCreateTaskContainer()
        throws NotImplemented, Timeout, NoSuccess;
    
    /**
     * Constructs a <code>TaskContainer</code> object.
     * This method is to be provided by the factory.
     * @return the task container.
     */
    public synchronized static TaskContainer createTaskContainer()
        throws NotImplemented, Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateTaskContainer();
    }
}
