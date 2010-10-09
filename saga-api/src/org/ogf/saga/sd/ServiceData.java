package org.ogf.saga.sd;

import org.ogf.saga.SagaObject;
import org.ogf.saga.attributes.Attributes;

/**
 * Read access to the key/value pairs of a {@link ServiceDescription} instance.
 * This class implements the
 * {@link org.ogf.saga.attributes.Attributes org.ogf.saga.attributes.Attributes}
 * interface and offers getter methods for the user to read key/value pairs
 * defined by the service publisher. Service publishers are completely free to
 * define their own key names. Access to the keys and values is through the
 * <code>org.ogf.saga.attributes.Attributes</code> interface. The class
 * provides no other methods. This class has no CONSTRUCTOR, as it can only be
 * created by calling {@link ServiceDescription#getData getData} method on a
 * <code>ServiceDescription</code> instance.
 */
public interface ServiceData extends SagaObject, Attributes {

}
