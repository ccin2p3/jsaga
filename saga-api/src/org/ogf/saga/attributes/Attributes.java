package org.ogf.saga.attributes;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

/**
 * Provides a uniform paradigm to set and query parameters and properties of
 * SAGA objects. Attributes map a key to a value.
 */
public interface Attributes {

    // Attribute types
    public static final String STRING = "String";

    public static final String INT = "Int";

    public static final String ENUM = "Enum";

    public static final String FLOAT = "Float";

    public static final String BOOL = "Bool";

    public static final String TIME = "Time";

    // For "trigger" metrics:
    public static final String TRIGGER = "Trigger";

    // Boolean values:
    public static final String TRUE = "True";

    public static final String FALSE = "False";

    /**
     * Sets an attribute to a value.
     * 
     * @param key
     *            the attribute key.
     * @param value
     *            value to set the attribute to.
     */
    public void setAttribute(String key, String value)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            IncorrectStateException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Gets the value of an attribute.
     * 
     * @param key
     *            the attribute key.
     * @return the value of this attribute.
     */
    public String getAttribute(String key) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Sets an attribute to an array of values.
     * 
     * @param key
     *            the attribute key.
     * @param values
     *            values to set the attribute to.
     */
    public void setVectorAttribute(String key, String[] values)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            IncorrectStateException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Gets the array of values associated with an attribute.
     * 
     * @param key
     *            the attribute key.
     * @return the values of this attribute, or <code>null</code>.
     */
    public String[] getVectorAttribute(String key)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            IncorrectStateException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Removes an attribute.
     * 
     * @param key
     *            the attribute key.
     */
    public void removeAttribute(String key) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Gets the list of attribute keys.
     * 
     * @return the list of attribute keys.
     */
    public String[] listAttributes() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * Finds matching attributes.
     * 
     * @param patterns
     *            the search patterns.
     * @return the list of matching attribute keys.
     */
    public String[] findAttributes(String... patterns)
            throws NotImplementedException, BadParameterException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * Checks the attribute for being read-only.
     * 
     * @param key
     *            the attribute key.
     * @return <code>true</code> if the attribute exists and is read-only.
     */
    public boolean isReadOnlyAttribute(String key)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Checks the attribute for being writable.
     * 
     * @param key
     *            the attribute key.
     * @return <code>true</code> if the attribute exists and is writable.
     */
    public boolean isWritableAttribute(String key)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Checks the attribute for being removable.
     * 
     * @param key
     *            the attribute key.
     * @return <code>true</code> if the attribute exists and is removable.
     */
    public boolean isRemovableAttribute(String key)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Checks the attribute for being a vector.
     * 
     * @param key
     *            the attribute key.
     * @return <code>true</code> if the attribute is a vector attribute.
     */
    public boolean isVectorAttribute(String key)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            DoesNotExistException, TimeoutException, NoSuccessException;
}
