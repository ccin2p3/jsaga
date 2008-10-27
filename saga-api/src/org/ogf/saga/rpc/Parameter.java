package org.ogf.saga.rpc;

/**
 * Parameters for RPC calls. Unlike the language-independent SAGA specifications,
 * this interface does not extend Buffer, because existing Java language
 * bindings for RPC usually just use {@link java.lang.Object}. See,
 * for instance, the Java bindings for Ninf-g and the Apache XML-RPC. 
 */
public interface Parameter {

    /**
     * Sets the io mode.
     * 
     * @param mode
     *            the value for io mode.
     */
    public void setIOMode(IOMode mode);

    /**
     * Retrieves the current value for io mode.
     * 
     * @return the value of io mode.
     */
    public IOMode getIOMode();
    
    /**
     * Sets the parameter object.
     * 
     * @param object
     *            the parameter value.
     */
    public void setData(Object object);
    
    /**
     * Retrieves the current value of the parameter object.
     * 
     * @return the current parameter object value.
     */
    public Object getData();
}
