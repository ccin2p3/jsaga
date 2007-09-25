package org.ogf.saga.attributes;

import org.ogf.saga.SagaBase;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.ReadOnly;
import org.ogf.saga.error.Timeout;

/**
 * Provides a uniform paradigm to set and query parameters and properties
 * of SAGA objects.
 * Attributes map a key to a value.
 */
public interface Attributes extends SagaBase {

    /**
     * Sets an attribute to a value.
     * @param key the attribute key.
     * @param value value to set the attribute to.
     * @return the old value of this attribute, or <code>null</code>.
     */
    public String setAttribute(String key, String value)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, BadParameter,
            ReadOnly, DoesNotExist, Timeout, NoSuccess;

    /**
     * Gets the value of an attribute.
     * @param key the attribute key.
     * @return the value of this attribute.
     */
    public String getAttribute(String key)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, ReadOnly,
            DoesNotExist, Timeout, NoSuccess;

    /**
     * Sets an attribute to an array of values.
     * @param key the attribute key.
     * @param values values to set the attribute to.
     * @return the old values of this attribute, or <code>null</code>.
     */
    public String[] setVectorAttribute(String key, String[] values)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, BadParameter,
            ReadOnly, DoesNotExist, Timeout, NoSuccess;

    /**
     * Gets the array of values associated with an attribute.
     * @param key the attribute key.
     * @return the values of this attribute, or <code>null</code>.
     */
    public String[] getVectorAttribute(String key)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, ReadOnly,
            DoesNotExist, Timeout, NoSuccess;

    /**
     * Removes an attribute.
     * @param key the attribute key.
     */
    public void removeAttribute(String key)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, DoesNotExist, ReadOnly, Timeout, NoSuccess;
    
    /**
     * Gets the list of attribute keys.
     * @return the list of attribute keys.
     */
    public String[] listAttributes()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, Timeout, NoSuccess;
    
    /**
     * Finds matching attributes.
     * @param keyPattern key search pattern.
     * @param valuePattern value search pattern.
     * @return the list of matching attribute keys.
     */
    public String[] findAttributes(String keyPattern, String valuePattern)
        throws NotImplemented, BadParameter, AuthenticationFailed,
            AuthorizationFailed, PermissionDenied, Timeout, NoSuccess;

    /**
     * Checks the attribute for being read-only.
     * @param key the attribute key.
     * @return <code>true</code> if the attribute exists and is read-only.
     */
    public boolean isReadOnlyAttribute(String key)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, DoesNotExist, Timeout, NoSuccess;

    /**
     * Checks the attribute for being writable.
     * @param key the attribute key.
     * @return <code>true</code> if the attribute exists and is writable.
     */
    public boolean isWritableAttribute(String key)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, DoesNotExist, Timeout, NoSuccess;

    /**
     * Checks the attribute for being removeable.
     * @param key the attribute key.
     * @return <code>true</code> if the attribute exists and is removeable.
     */
    public boolean isRemoveableAttribute(String key)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, DoesNotExist, Timeout, NoSuccess;

    /**
     * Checks the attribute for being a vector.
     * @param key the attribute key.
     * @return <code>true</code> if the attribute is a vector attribute.
     */
    public boolean isVectorAttribute(String key)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, DoesNotExist, Timeout, NoSuccess;
}
