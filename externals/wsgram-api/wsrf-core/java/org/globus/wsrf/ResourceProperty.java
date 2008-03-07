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

import java.util.Iterator;

import javax.xml.soap.SOAPElement;

import org.w3c.dom.Element;

import org.globus.wsrf.encoding.SerializationException;

/**
 * Represents a single resource property. A resource property can have multiple
 * values and be converted into a DOM <code>Element</code> or
 * <code>SOAPElement</code> array.
 */
public interface ResourceProperty {

    /**
     * Adds a value.
     *
     * @param value the value to add.
     */
    void add(Object value);

    /**
     * Removes a specific value. If the resource property contains
     * multiple of the same value, only the first one is removed. 
     *
     * @param value value to remove.
     * @return true if the value was removed. False otherwise.
     */
    boolean remove(Object value);

    /**
     * Retrieves a value at a specific index.
     *
     * @param index the index of value to retrieve.
     * @return the value at the given index. This operation might fail
     *         if the index is out of bounds.
     */
    Object get(int index);

    /**
     * Sets a value at a specific index.
     *
     * @param index the index to set value at.
     * @param value the new value
     */
    void set(int index, Object value);

    /**
     * Removes all values.
     */
    void clear();

    /**
     * Returns the number of values in the resource property.
     *
     * @return the number of values.
     */
    int size();

    /**
     * Returns true if the resource property has any values.
     *
     * @return  true if the resource property has any values. False, otherwise.
     */
    boolean isEmpty();


    /**
     * Returns iterator over the values of this resource property.
     *
     * @return iterator over the values of this resource property.
     */
    Iterator iterator();

    /**
     * Converts the resource property value into a SOAPElement array.
     * Each value is wrapped into an element named after the resource property.
     * If the RP has no values (is null), and RP element was defined as:
     * <ul>
     * <li>minOccurs >= 0 - the function returns null.</li>
     * <li>nillable == true - the function returns a single element with
     * <i>xsi:nil="true"</i> attribute set.</li>
     * </ul>
     *
     * @return the resource property as a SOAPElement array.
     * @throws SerializationException if conversion fails.
     */
    SOAPElement[] toSOAPElements()
        throws SerializationException;

    /**
     * Converts the resource property value into a DOM Element array.
     * Each value is wrapped into an element named after the resource property.
     * If the RP has no values (is null), and RP element was defined as:
     * <ul>
     * <li>minOccurs >= 0 - the function returns null.</li>
     * <li>nillable == true - the function returns a single element with
     * <i>xsi:nil="true"</i> attribute set.</li>
     * </ul>
     *
     * @return the resource property as a DOM Element array.
     * @throws SerializationException if conversion fails.
     */
    Element[] toElements()
        throws SerializationException;

    /**
     * Gets meta data of this resource property.
     *
     * @return meta data of this resource property. Never null.
     */
    ResourcePropertyMetaData getMetaData();

}
