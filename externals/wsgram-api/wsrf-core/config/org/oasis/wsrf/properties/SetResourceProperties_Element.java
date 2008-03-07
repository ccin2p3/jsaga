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
package org.oasis.wsrf.properties;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.Serializable;

import org.oasis.AnyListType;

import javax.xml.namespace.QName;

public class SetResourceProperties_Element implements AnyListType, Serializable {
    
    private static final String PROPERTIES_NS =
        "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceProperties-1.2-draft-01.xsd";
    
    private QName INSERT_NAME = 
        new QName(PROPERTIES_NS, "Insert");

    private QName DELETE_NAME = 
        new QName(PROPERTIES_NS, "Delete");

    private QName UPDATE_NAME = 
        new QName(PROPERTIES_NS, "Update");

    private List values;
    
    public SetResourceProperties_Element() {
        this.values = new ArrayList();
    }
    
    public void add(Object value) {
        if (value instanceof DeleteType ||
            value instanceof InsertType ||
            value instanceof UpdateType) {
            values.add(value);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public int size() {
        return this.values.size();
    }

    public Object get(int index) {
        return this.values.get(index);
    }

    public Iterator iterator() {
        return values.iterator();
    } 
    
    public QName getElementName(Class clazz) {
        if (clazz == DeleteType.class) {
            return DELETE_NAME;
        } else if (clazz == InsertType.class) {
            return INSERT_NAME;
        } else if (clazz == UpdateType.class) {
            return UPDATE_NAME;
        } else {
            return null;
        }
    }

    public Class getType(QName elementName) {
        if (elementName.equals(DELETE_NAME)) {
            return DeleteType.class;
        } else if (elementName.equals(INSERT_NAME)) {
            return InsertType.class;
        } else if (elementName.equals(UPDATE_NAME)) {
            return UpdateType.class;
        } else {
            return null;
        }
    }

    public void setInsert(InsertType insert) {
        this.values.clear();
        this.values.add(insert);
    }

    public void setUpdate(UpdateType update) {
        this.values.clear();
        this.values.add(update);
    }

    public void setDelete(DeleteType delete) {
        this.values.clear();
        this.values.add(delete);
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SetResourceProperties_Element.class, true);

    static {
        typeDesc.setXmlType(new QName(PROPERTIES_NS, 
                                      ">SetResourceProperties"));
    }
    
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}

