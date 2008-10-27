package org.ogf.saga;

import org.ogf.saga.session.Session;
import org.ogf.saga.error.DoesNotExistException;

/**
 * This is the base for all SAGA objects. Deviation from the SAGA specs: we
 * don't want to call this "Object" because that might cause some confusion in
 * Java. All SAGA objects must support the clone() method, so this interface
 * extends Cloneable.
 */
public interface SagaObject extends Cloneable {

    /** Timeout constant: wait forever. */
    public static final float WAIT_FOREVER = -1.0F;

    /** Timeout constant: don't wait. */
    public static final float NO_WAIT = 0.0F;

    /**
     * Returns a shallow copy of the session from which this object was created.
     * 
     * @return the session.
     * @exception DoesNotExistException
     *                is thrown when this method is called on objects that do
     *                not have a session attached.
     */
    public Session getSession() throws DoesNotExistException;

    /**
     * Returns the object id of this SAGA object. Note: java.util.UUID could be
     * used for this. See Sun's comments on UUID generation.
     * 
     * @return the object id.
     */
    public String getId();

    /**
     * Copies the Saga object.
     * 
     * @return the clone.
     * @throws CloneNotSupportedException
     *             when the clone method is not supported.
     */
    public Object clone() throws CloneNotSupportedException;

}
