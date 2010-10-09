package org.ogf.saga.attributes;

import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

/**
 * Task versions of all methods from the <code>Attributes</code> interface.
 * The generic type <code>T</code> specifies the object type that implements
 * this interface.
 */
public interface AsyncAttributes<T> extends Attributes {

    /**
     * Creates a task that sets an attribute to a value.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param key
     *            the attribute key.
     * @param value
     *            value to set the attribute to.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<T, Void> setAttribute(TaskMode mode, String key, String value)
            throws NotImplementedException;

    /**
     * Creates a task that obtains the value of an attribute.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param key
     *            the attribute key.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<T, String> getAttribute(TaskMode mode, String key)
            throws NotImplementedException;

    /**
     * Creates a task that sets an attribute to an array of values.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param key
     *            the attribute key.
     * @param values
     *            values to set the attribute to.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<T, Void> setVectorAttribute(TaskMode mode, String key,
            String[] values) throws NotImplementedException;

    /**
     * Creates a task that obtains the array of values associated with an
     * attribute.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param key
     *            the attribute key.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<T, String[]> getVectorAttribute(TaskMode mode, String key)
            throws NotImplementedException;

    /**
     * Creates a task that removes an attribute.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param key
     *            the attribute key.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<T, Void> removeAttribute(TaskMode mode, String key)
            throws NotImplementedException;

    /**
     * Creates a task that obtains the list of attribute keys.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<T, String[]> listAttributes(TaskMode mode)
            throws NotImplementedException;

    /**
     * Creates a task that finds matching attributes.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param patterns
     *            the search patterns.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<T, String[]> findAttributes(TaskMode mode, String... patterns)
            throws NotImplementedException;

    /**
     * Creates a task that tests for the existence of an attribute.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param key
     *            the attribute key.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<T, Boolean> existsAttribute(TaskMode mode, String key)
            throws NotImplementedException;

    /**
     * Creates a task that checks the attribute mode for being read-only.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param key
     *            the attribute key.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<T, Boolean> isReadOnlyAttribute(TaskMode mode, String key)
            throws NotImplementedException;

    /**
     * Creates a task that checks the attribute mode for being writable.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param key
     *            the attribute key.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<T, Boolean> isWritableAttribute(TaskMode mode, String key)
            throws NotImplementedException;

    /**
     * Creates a task that checks the attribute mode for being removable.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param key
     *            the attribute key.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<T, Boolean> isRemovableAttribute(TaskMode mode, String key)
            throws NotImplementedException;

    /**
     * Creates a task that checks the attribute mode for being a vector.
     * 
     * @param mode
     *            determines the initial state of the task.
     * @param key
     *            the attribute key.
     * @return the task.
     * @exception NotImplementedException
     *                is thrown when the task version of this method is not
     *                implemented.
     */
    public Task<T, Boolean> isVectorAttribute(TaskMode mode, String key)
            throws NotImplementedException;
}
