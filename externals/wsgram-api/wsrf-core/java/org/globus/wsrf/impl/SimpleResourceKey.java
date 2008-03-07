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
package org.globus.wsrf.impl;

import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.InvalidResourceKeyException;
import org.globus.wsrf.encoding.SerializationException;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.encoding.ObjectDeserializer;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A basic implementation of <code>ResourceKey</code>. 
 */
public class SimpleResourceKey implements ResourceKey {

    private static Log logger =
        LogFactory.getLog(SimpleResourceKey.class.getName());

    private QName name;
    private Object value;
    
    /**
     * Creates a new SimpleResourceKey.
     *
     * @param header the SOAPElement containing the key. The element's name
     *        and namespace are used as key name.
     * @param type the type of the key. The SOAPElement will be deserialized
     *        into this type using 
     *        {@link ObjectDeserializer#toObject(SOAPElement, Class) 
     *        ObjectDeserializer.toObject()}. 
     * @throws InvalidResourceKeyException if deserialization of the key fails.
     */
    public SimpleResourceKey(SOAPElement header, Class type)
        throws InvalidResourceKeyException {
        try {
            this.value = ObjectDeserializer.toObject(header, type);
        } catch (Exception e) {
            throw new InvalidResourceKeyException(e);
        }
        this.name = new QName(header.getElementName().getURI(),
                              header.getElementName().getLocalName());
    }
    
    /**
     * Creates a new SimpleResourceKey.
     *
     * @param name the name of the key
     * @param value the value of the key. The value of the key can be any 
     *        simple/primitive type or any WSDL2Java generated type or any
     *        other type with proper type mappings.
     */
    public SimpleResourceKey(QName name, Object value) {
        this.name = name;
        this.value = value;
    }
    
    public QName getName() {
        return this.name;
    }
    
    public Object getValue() {
        return this.value;
    }
    
    public SOAPElement toSOAPElement()
        throws SerializationException {
        return ObjectSerializer.toSOAPElement(this.value, this.name);
    }

    public String toString() {
        return this.name  + "=" + this.value;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof SimpleResourceKey)) {
            return false;
        }
        
        SimpleResourceKey otherKey = (SimpleResourceKey) obj;
        
        if (!this.name.equals(otherKey.name)) {
            return false;
        }
        
        if (!this.value.equals(otherKey.value)) {
            return false;
        }
        
        return true;
    }
    
    public int hashCode() {
        return this.name.hashCode() + this.value.hashCode();
    }

}
