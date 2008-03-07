/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package org.globus.wsrf;

import org.globus.wsrf.encoding.SerializationException;

import java.io.Serializable;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;

/**
 * A basic representation of a resource key. A resource key is composed of
 * name and the actual resource key value.
 */
public interface ResourceKey extends Serializable {

    /**
     * The actual key value.
     *
     * @return the key value. Cannot be null.
     */
    Object getValue();

    /**
     * The name of the key.
     *
     * @return the name of the key. Cannot be null.
     */
    QName getName();

    /**
     * Converts the resource key into a {@link SOAPElement SOAPElement}.
     * The element name and namespace must match the name of the key.
     *
     * @return the resource key as a <code>SOAPElement</code>
     * @throws SerializationException in case the conversion fails.
     */
    SOAPElement toSOAPElement()
        throws SerializationException;

}
