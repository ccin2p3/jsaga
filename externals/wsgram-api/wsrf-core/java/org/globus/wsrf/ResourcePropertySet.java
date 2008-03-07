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

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;

import org.w3c.dom.Element;

/**
 * This interface defines the API used to access a collection of resource 
 * property contained in a resource.
 */
public interface ResourcePropertySet  {
    
    /**
     * Adds a resource property to the set and makes it available to 
     * queries and subscriptions, etc.
     *
     * @param property the <code>ResourceProperty</code> to add.
     * @return true if property was successfully added, false otherwise.
     */
    boolean add(ResourceProperty property);
    
    /**
     * Removes a resource property.
     *
     * @param propName the name of the <code>ResourceProperty</code> to remove.
     * @return true if property was successfully removed, false otherwise.
     */
    boolean remove(QName propName);
    
    /**
     * Retrieves a resource property.
     *
     * @param propName the name of the <code>ResourceProperty</code> to 
     *         retrieve.
     * @return the <code>ResourceProperty</code> that was retrieved. Null if 
     *         the property does not exist.
     */
    ResourceProperty get(QName propName);

    /**
     * Creates a resource property entry with the specified meta data. Note the
     * resource property is not added to the set (and thus not target to 
     * queries or subscriptions)until the {@link #add add} method is called.
     */
    ResourceProperty create(ResourcePropertyMetaData rpMetaData);

    /**
     * Returns an iterator over <code>ResourceProperty</code> entries.
     *
     * @return an iterator over <code>ResourceProperty</code> entries.
     */
    Iterator iterator();

    /**
     * Removes all resource properties.
     */
    void clear();

    /**
     * Returns the number of resource properties this set contains.
     *
     * @return the number of resource properties contained.
     */
    int size();

    /**
     * Returns true if the set has at least one resource property.
     *
     * @return true if the set has at least one resource property. 
     *         False, otherwise.
     */
    boolean isEmpty();

    /**
     * Indicates if the resource property set allows adding of new arbitrary
     * resource property elements.
     *
     * @return true if adding new resource property elements is allowed. False
     *         otherwise. Please note that <code>isOpenContent</code> might
     *         return <code>true</code> but only certain subset of resource
     *         properties will be allowed to be added.
     */
    boolean isOpenContent();

    /**
     * Returns the element name of the entire resource properties document.
     *
     * @return the element name of the entire resource properties document.
     */
    QName getName();

    /**
     * Returns the entire Resource Property document as a SOAPElement.
     */
    SOAPElement toSOAPElement()
        throws SerializationException;

    /**
     * Returns the entire Resource Property document as a DOM Element.
     */
    Element toElement()
        throws SerializationException;

}
