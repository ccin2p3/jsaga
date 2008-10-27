package org.ogf.saga.monitoring;

import org.ogf.saga.SagaObject;
import org.ogf.saga.attributes.Attributes;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

/**
 * Metrics represent monitorable entities.
 */
public interface Metric extends SagaObject, Attributes {

    /** Attribute name: name of the metric (ReadOnly). */
    public static final String NAME = "Name";

    /** Attribute name: description of the metric (ReadOnly). */
    public static final String DESCRIPTION = "Description";

    /**
     * Attribute name: access mode of the metric (ReadOnly). Possible values:
     * "ReadOnly", "ReadWrite", or "Final". This determines what can be done
     * with the VALUE attribute.
     */
    public static final String MODE = "Mode";

    /** Attribute name: unit of the metric (ReadOnly). */
    public static final String UNIT = "Unit";

    /**
     * Attribute name: value type of the metric (ReadOnly). Possible values:
     * "String", "Int", "Enum", "Float", "Bool", "Time", "Trigger".
     */
    public static final String TYPE = "Type";

    /** Attribute name: value of the metric (See {@link #MODE}). */
    public static final String VALUE = "Value";

    /**
     * Adds the specified callback to the metric.
     * 
     * @return the cookie that identifies the callback in the metric.
     */
    public int addCallback(Callback cb) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Removes a callback from the metric.
     * 
     * @param cookie
     *            the cookie that identifies the metric.
     */
    public void removeCallback(int cookie) throws NotImplementedException,
            BadParameterException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            TimeoutException, NoSuccessException;

    /**
     * Pushes the metric value to the backend.
     */
    public void fire() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            TimeoutException, NoSuccessException;
}
