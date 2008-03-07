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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import javax.xml.soap.SOAPElement;

import org.globus.util.I18n;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.encoding.SerializationException;
import org.globus.wsrf.ResourcePropertyMetaData;
import org.globus.wsrf.utils.Resources;

/**
 * A simple in-memory implementation of the <code>ResourceProperty</code>
 * interface. The resource property values are stored in a List.
 */
public class SimpleResourceProperty extends BaseResourceProperty {

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    private List list;
    private boolean validation = false;

    public SimpleResourceProperty(QName name) {
        this(new SimpleResourcePropertyMetaData(name));
    }

    public SimpleResourceProperty(ResourcePropertyMetaData metaData) {
        super(metaData);
        this.list = new ArrayList();
    }

    public void setEnableValidation(boolean validation) {
        this.validation = validation;
    }

    public boolean isValidationEnabled() {
        return this.validation;
    }

    public void add(Object value) {
        checkReadOnly();
        this.list.add(convert(value));
    }

    public boolean remove(Object value) {
        checkReadOnly();
        return this.list.remove(convert(value));
    }

    public Object get(int index) {
        return this.list.get(index);
    }

    public void set(int index, Object value) {
        checkReadOnly();
        this.list.set(index, convert(value));
    }

    public void clear() {
        checkReadOnly();
        this.list.clear();
    }

    public int size() {
        return this.list.size();
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public Iterator iterator() {
        return this.list.iterator();
    }

    private void checkReadOnly() {
        if (this.validation && this.metaData.isReadOnly()) {
            throw new IllegalStateException(i18n.getMessage("modifyReadOnly"));
        }
    }

    public SOAPElement[] toSOAPElements()
        throws SerializationException {
        SOAPElement [] values = null;
        boolean nillable = this.metaData.isNillable();
        QName name = getMetaData().getName();
        if (this.list.isEmpty()) {
            if (nillable) {
                values = new SOAPElement[1];
                values[0] = ObjectSerializer.toSOAPElement(null,
                                                           name,
                                                           nillable);
            }
        } else {
            values = new SOAPElement[this.list.size()];
            Iterator iter = this.list.iterator();
            int i = 0;
            while(iter.hasNext()) {
                Object value = iter.next();
                values[i++] = ObjectSerializer.toSOAPElement(value,
                                                             name,
                                                             nillable);
            }
        }
        return values;
    }

    public Element[] toElements()
        throws SerializationException {
        Element [] values = null;
        boolean nillable = this.metaData.isNillable();
        QName name = getMetaData().getName();
        if (this.list.isEmpty()) {
            if (nillable) {
                values = new Element[1];
                values[0] = ObjectSerializer.toElement(null,
                                                       name,
                                                       nillable);
            }
        } else {
            values = new Element[this.list.size()];
            Iterator iter = this.list.iterator();
            int i = 0;
            while(iter.hasNext()) {
                Object value = iter.next();
                values[i++] = ObjectSerializer.toElement(value,
                                                         name,
                                                         nillable);
            }
        }
        return values;
    }

}
