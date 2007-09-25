package org.ogf.saga.context;

import org.ogf.saga.SagaBase;
import org.ogf.saga.attributes.Attributes;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;

/**
 * A <code>Context</code> provides the functionality of a security information
 * container.
 */
public interface Context extends SagaBase, Attributes {
    /**
     * Sets default attribute values for this context type, based on all
     * non-empty attributes.
     */
    public void setDefaults()
        throws NotImplemented, IncorrectState, NoSuccess;
}
