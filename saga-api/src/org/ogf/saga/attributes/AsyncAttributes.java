package org.ogf.saga.attributes;

import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.RVTask;

/**
 * Task versions of all methods from the <code>Attributes</code> interface.
 */
public interface AsyncAttributes extends Attributes {

    /**
     * Creates a task that sets an attribute to a value.
     * @param mode determines the initial state of the task.
     * @param key the attribute key.
     * @param value value to set the attribute to.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<String> setAttribute(TaskMode mode, String key, String value)
        throws NotImplemented;

    /**
     * Creates a task that obtains the value of an attribute.
     * @param mode determines the initial state of the task.
     * @param key the attribute key.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<String> getAttribute(TaskMode mode, String key)
        throws NotImplemented;

    /**
     * Creates a task that sets an attribute to an array of values.
     * @param mode determines the initial state of the task.
     * @param key the attribute key.
     * @param values values to set the attribute to.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<String[]> setVectorAttribute(TaskMode mode, String key,
            String[] values)
        throws NotImplemented;

    /**
     * Creates a task that obtains the array of values associated with an
     * attribute.
     * @param mode determines the initial state of the task.
     * @param key the attribute key.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<String[]> getVectorAttribute(TaskMode mode, String key)
        throws NotImplemented;

    /**
     * Creates a task that removes an attribute.
     * @param mode determines the initial state of the task.
     * @param key the attribute key.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task removeAttribute(TaskMode mode, String key)
        throws NotImplemented;
    
    /**
     * Creates a task that obtains the list of attribute keys.
     * @param mode determines the initial state of the task.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<String[]> listAttributes(TaskMode mode)
        throws NotImplemented;
    
    /**
     * Creates a task that finds matching attributes.
     * @param mode determines the initial state of the task.
     * @param keyPattern key search pattern.
     * @param valuePattern value search pattern.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<String[]> findAttributes(TaskMode mode, String keyPattern,
            String valuePattern)
        throws NotImplemented;

    /**
     * Creates a task that checks the attribute mode for being read-only.
     * @param mode determines the initial state of the task.
     * @param key the attribute key.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<Boolean> isReadOnlyAttribute(TaskMode mode, String key)
        throws NotImplemented;

    /**
     * Creates a task that checks the attribute mode for being writable.
     * @param mode determines the initial state of the task.
     * @param key the attribute key.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<Boolean> isWritableAttribute(TaskMode mode, String key)
        throws NotImplemented;

    /**
     * Creates a task that checks the attribute mode for being removeable.
     * @param mode determines the initial state of the task.
     * @param key the attribute key.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<Boolean> isRemoveableAttribute(TaskMode mode, String key)
        throws NotImplemented;

    /**
     * Creates a task that checks the attribute mode for being a vector.
     * @param mode determines the initial state of the task.
     * @param key the attribute key.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public RVTask<Boolean> isVectorAttribute(TaskMode mode, String key)
        throws NotImplemented;
}
