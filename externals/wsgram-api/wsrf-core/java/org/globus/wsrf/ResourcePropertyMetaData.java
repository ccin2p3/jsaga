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

import javax.xml.namespace.QName;

/**
 * Defines a metadata information about a resource property.
 */
public interface ResourcePropertyMetaData {
    
    /**
     * Returns ResourceProperty name.
     *
     * @return the resource property name.
     */
    QName getName();
    
    /**
     * Returns if this resource property can be nillable.
     *
     * @return true if the resource property can be nillable. False otherwise.
     */
    boolean isNillable();
 
    /**
     * Returns the minimum number of values that this resource property can 
     * have.
     * 
     * @return the minimum number of values that this resource property can 
     *         have. 
     */
    int getMinOccurs();

    /**
     * Returns the maximum number of values that this resource property can 
     * have.
     * 
     * @return the maximum number of values that this resource property can 
     *         have. Returns {@link Integer#MAX_VALUE Integer.MAX_VALUE} if
     *        unlimited.
     */
    int getMaxOccurs();

    /**
     * Returns the Java element type of this property.
     * When adding or setting element values the input value
     * will be automatically converted into this type.
     * If set to <code>Object.class</code> then no conversion
     * will be done.
     *
     * @return Element type of this property.
     */
    Class getType();

    /**
     * Returns whether this property is read only.
     * 
     * @return Returns true if this property is read only. False, otherwise.
     */
    public boolean isReadOnly();
    
}
