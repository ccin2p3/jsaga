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

import javax.xml.namespace.QName;

import org.globus.wsrf.WSRFConstants;
import org.globus.wsrf.ResourcePropertyMetaData;
import org.globus.wsrf.utils.Resources;
import org.globus.util.I18n;

public class SimpleResourcePropertyMetaData 
    implements ResourcePropertyMetaData {

    public static final SimpleResourcePropertyMetaData TERMINATION_TIME = 
        new SimpleResourcePropertyMetaData(WSRFConstants.TERMINATION_TIME, 
                                           1, 1, true, true);
    
    public static final SimpleResourcePropertyMetaData CURRENT_TIME = 
        new SimpleResourcePropertyMetaData(WSRFConstants.CURRENT_TIME, 
                                           1, 1, false, true);

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    protected QName name;
    protected boolean nillable;
    protected int minOccurs;
    protected int maxOccurs;
    protected Class type;
    protected QName xmlType;
    protected boolean readOnly;

    public SimpleResourcePropertyMetaData(QName name) {
        this(name, 1, 1, false, Object.class, false, null);
    }

    public SimpleResourcePropertyMetaData(QName name,
                                          int minOccurs,
                                          int maxOccurs,
                                          boolean nillable,
                                          boolean readOnly) {
        this(name, minOccurs, maxOccurs, 
             nillable, Object.class, readOnly, null);
    }

    public SimpleResourcePropertyMetaData(QName name,
                                          int minOccurs,
                                          int maxOccurs,
                                          boolean nillable,
                                          Class type,
                                          boolean readOnly) {
        this(name, minOccurs, maxOccurs, 
             nillable, type, readOnly, null);
    }

    public SimpleResourcePropertyMetaData(QName name,
                                          int minOccurs,
                                          int maxOccurs,
                                          boolean nillable,
                                          Class type,
                                          boolean readOnly,
                                          QName xmlType) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        this.name = name;
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
        this.nillable = nillable;
        this.readOnly = readOnly;
        this.xmlType = xmlType;
        setType(type);
    }

    protected void setXmlType(QName type) {
        this.xmlType = type;
    }
    
    public QName getXmlType() {
        return this.xmlType;
    }

    public QName getName() {
        return this.name;
    }

    public boolean isNillable() {
        return this.nillable;
    }

    /**
     * Sets if this resource property can be nillable.
     *
     * @param nillable the nillable property.
     */
    protected void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public int getMinOccurs() {
        return this.minOccurs;
    }

    /**
     * Sets the minimum number of values that this resource property can ever
     * have.
     *
     * @param min the minimum number of values allowed in this resource 
     *        property.
     */
    protected void setMinOccurs(int min) {
        this.minOccurs = min;
    }

    public int getMaxOccurs() {
        return this.maxOccurs;
    }

    /**
     * Sets the maximum number of values that this resource property can ever
     * have.
     *
     * @param max the maximum number of values allowed in this resource 
     *        property.
     */
    protected void setMaxOccurs(int max) {
        this.maxOccurs = max;
    }

    /**
     * Sets the Java element type of this property.
     * When adding or setting element values the input value
     * will be automatically converted into this type.
     * If set to <code>Object.class</code> then no conversion
     * will be done. 
     * This operation is optional. If setting the element type is not
     * supported it should throw 
     * {@link java.lang.UnsupportedOperationException
     * UnsupportedOperationException}.
     *
     * @param type Element type of this property.
     */
    protected void setType(Class type) {
        if (type == Void.TYPE) {
            throw new IllegalArgumentException();
        }
        this.type = type;
    }

    public Class getType() {
        return this.type;
    }

    protected void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }


}
