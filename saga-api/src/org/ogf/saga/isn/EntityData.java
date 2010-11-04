package org.ogf.saga.isn;

import org.ogf.saga.SagaObject;
import org.ogf.saga.attributes.Attributes;

/**
 * Provides read access to the data of an entity. This class implements the
 * {@link org.ogf.saga.attributes.Attributes org.ogf.saga.attributes.Attributes}
 * interface and offers getter methods for the user to read key/value pairs.
 * Access to the keys and values is through the
 * <code>org.ogf.saga.attributes.Attributes</code> interface. The class
 * provides no other methods. This class has no CONSTRUCTOR, it can only be
 * accessed via an {@link EntityDataSet} object.
 * 
 */
public interface EntityData extends SagaObject, Attributes {

}
