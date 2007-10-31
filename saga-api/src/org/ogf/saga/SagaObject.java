package org.ogf.saga;

import org.ogf.saga.session.Session;
import org.ogf.saga.error.DoesNotExist;

/**
 * This is the base for all SAGA objects.
 * Deviation from the SAGA specs: we don't want to call this
 * "Object" because that might cause some confusion in Java.
 * All SAGA objects must support the clone() method, so this interface
 * extends Cloneable.
 */
public interface SagaObject extends Cloneable {

    /**
     * Returns a shallow copy of the session from which this object was
     * created.
     * @return the session.
     * @exception DoesNotExist is thrown when this method is called on objects
     *     that do not have a session attached.
     */
    public Session getSession() throws DoesNotExist;

    /**
     * Returns the object id of this SAGA object.
     * Note: java.util.UUID could be used for this.
     * See Sun's comments on UUID generation.
     * @return the object id.
     */
    public String getId();
    
    /**
     * Copies the Saga object.
     * @return the clone.
     * @throws CloneNotSupportedException when the clone method is not supported.
     */
    public Object clone() throws CloneNotSupportedException;
    
    /**
     * Returns the object type. The SAGA application could use
     * introspection, but would then get class names that are
     * specific for a particular SAGA implementation.
     */
    public ObjectType getType();
}
