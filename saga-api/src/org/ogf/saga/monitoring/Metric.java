package org.ogf.saga.monitoring;

import org.ogf.saga.SagaObject;
import org.ogf.saga.attributes.Attributes;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;

/**
 * Metrics represent monitorable entities.
 */
public interface Metric extends SagaObject, Attributes {

    /** Attribute name: name of the metric (ReadOnly). */
    public static final String NAME = "Name";

    /** Attribute name: description of the metric (ReadOnly). */
    public static final String DESCRIPTION = "Description";

    /**
     * Attribute name: access mode of the metric (ReadOnly).
     * Possible values: "ReadOnly", "ReadWrite", or "Final".
     * This determines what can be done with the VALUE attribute.
     */
    public static final String MODE = "Mode";

    /** Attribute name: unit of the metric (ReadOnly). */
    public static final String UNIT = "Unit";

    /**
     * Attribute name: value type of the metric (ReadOnly).
     * Possible values: "String", "Int", "Enum", "Float", "Bool",
     * "Time", "Trigger".
     */
    public static final String TYPE = "Type";

    /** Attribute name: value of the metric (See {@link #MODE}). */
    public static final String VALUE = "Value";

    /**
     * Adds the specified callback to the metric.
     * @return the cookie that identifies the callback in the metric.
     */
    public int addCallback(Callback cb)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, Timeout, NoSuccess;

    /**
     * Removes a callback from the metric.
     * @param cookie the cookie that identifies the metric.
     */
    public void removeCallback(int cookie)
        throws NotImplemented, BadParameter, AuthenticationFailed,
            AuthorizationFailed,PermissionDenied, IncorrectState, Timeout,
            NoSuccess;

    /**
     * Pushes the metric value to the backend.
     */
    public void fire()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, Timeout, NoSuccess;
}
