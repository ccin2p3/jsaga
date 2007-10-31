package org.ogf.saga.attributes;

import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

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
    public Task setAttribute(TaskMode mode, String key,
            String value) throws NotImplemented;

    /**
     * Creates a task that obtains the value of an attribute.
     * @param mode determines the initial state of the task.
     * @param key the attribute key.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<String> getAttribute(TaskMode mode, String key)
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
    public Task setVectorAttribute(TaskMode mode, String key,
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
    public Task<String[]> getVectorAttribute(TaskMode mode, String key)
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
    public Task<String[]> listAttributes(TaskMode mode)
        throws NotImplemented;
    
    /**
     * Creates a task that finds matching attributes.
     * @param mode determines the initial state of the task.
     * @param pattern the search pattern.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<String[]> findAttributes(TaskMode mode, String pattern)
        throws NotImplemented;

    /**
     * Creates a task that checks the attribute mode for being read-only.
     * @param mode determines the initial state of the task.
     * @param key the attribute key.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Boolean> isReadOnlyAttribute(TaskMode mode, String key)
        throws NotImplemented;

    /**
     * Creates a task that checks the attribute mode for being writable.
     * @param mode determines the initial state of the task.
     * @param key the attribute key.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Boolean> isWritableAttribute(TaskMode mode, String key)
        throws NotImplemented;

    /**
     * Creates a task that checks the attribute mode for being removable.
     * @param mode determines the initial state of the task.
     * @param key the attribute key.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Boolean> isRemovableAttribute(TaskMode mode, String key)
        throws NotImplemented;

    /**
     * Creates a task that checks the attribute mode for being a vector.
     * @param mode determines the initial state of the task.
     * @param key the attribute key.
     * @return the task.
     * @exception NotImplemented is thrown when the task version of this
     *     method is not implemented.
     */
    public Task<Boolean> isVectorAttribute(TaskMode mode, String key)
        throws NotImplemented;
}
