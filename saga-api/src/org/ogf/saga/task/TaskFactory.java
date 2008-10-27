package org.ogf.saga.task;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

/**
 * Factory for objects in the task package.
 */
public abstract class TaskFactory {

    private static TaskFactory factory;

    private synchronized static void initializeFactory()
            throws NotImplementedException, NoSuccessException {
        if (factory == null) {
            factory = ImplementationBootstrapLoader.createTaskFactory();
        }
    }

    /**
     * Constructs a <code>TaskContainer</code> object. This method is to be
     * provided by the factory.
     * 
     * @return the task container.
     */
    protected abstract TaskContainer doCreateTaskContainer()
            throws NotImplementedException, TimeoutException,
            NoSuccessException;

    /**
     * Constructs a <code>TaskContainer</code> object. This method is to be
     * provided by the factory.
     * 
     * @return the task container.
     */
    public synchronized static TaskContainer createTaskContainer()
            throws NotImplementedException, TimeoutException,
            NoSuccessException {
        initializeFactory();
        return factory.doCreateTaskContainer();
    }
}
