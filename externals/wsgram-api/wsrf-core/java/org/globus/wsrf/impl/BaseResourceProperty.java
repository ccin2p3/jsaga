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

import org.globus.wsrf.ResourceProperty;
import org.globus.wsrf.ResourcePropertyMetaData;
import org.globus.wsrf.utils.Resources;
import org.globus.util.I18n;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;

import org.w3c.dom.Element;

import org.globus.wsrf.encoding.ObjectDeserializer;
import org.globus.wsrf.encoding.ObjectSerializer;

import org.apache.axis.message.MessageElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BaseResourceProperty implements ResourceProperty {

    private static Log logger =
        LogFactory.getLog(BaseResourceProperty.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    protected ResourcePropertyMetaData metaData;

    public BaseResourceProperty(QName name) {
        this(new SimpleResourcePropertyMetaData(name));
    }

    public BaseResourceProperty(ResourcePropertyMetaData metaData) {
        if (metaData == null) {
            throw new IllegalArgumentException(
                i18n.getMessage("nullArgument", "metaData"));
        }
        this.metaData = metaData;
    }

    protected void setMetaData(ResourcePropertyMetaData metaData) {
        this.metaData = metaData;
    }

    public ResourcePropertyMetaData getMetaData() {
        return this.metaData;
    }

    protected Object convert(Object value) {
        if (value == null) {
            return null;
        }
        Class type = getMetaData().getType();
        if (type == null || type == Object.class) {
            return value;
        }
        try {
            if (type.isPrimitive()) {
                return convertPrimitive(type, value);
            } else {
                return convertObject(type, value);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            String msg = i18n.getMessage("failedToConvert", e.getMessage());
            logger.debug(msg, e);
            throw new IllegalArgumentException(msg);
        }
    }

    protected Object convertObject(Class type, Object value)
        throws Exception {
        QName name = getMetaData().getName();
        return convert(value, type, name);
    }

    protected Object convertPrimitive(Class type, Object value)
        throws Exception {
        QName name = getMetaData().getName();
        if (type == Boolean.TYPE) {
            return convert(value, Boolean.class, name);
        } else if (type == Character.TYPE) {
            return convert(value, Character.class, name);
        } else if (type == Byte.TYPE) {
            return convert(value, Byte.class, name);
        } else if (type == Short.TYPE) {
            return convert(value, Short.class, name);
        } else  if (type == Integer.TYPE) {
            return convert(value, Integer.class, name);
        } else if (type == Long.TYPE) {
            return convert(value, Long.class, name);
        } else if (type == Float.TYPE) {
            return convert(value, Float.class, name);
        } else if (type == Double.TYPE) {
            return convert(value, Double.class, name);
        } else {
            throw new IllegalArgumentException(
                i18n.getMessage(
                    "cantConvertType",
                    new Object[] {value.getClass(), type}));
        }
    }

    private static Object convert(Object value, Class type, QName name)
        throws Exception {
        if (type.isAssignableFrom(value.getClass())) {
            // special case
            if (value instanceof MessageElement &&
                Element.class.equals(type)) {
                return ((MessageElement)value).getAsDOM();
            }
            return value;
        } else if (value instanceof SOAPElement) {
            return ObjectDeserializer.toObject((SOAPElement)value,
                                               type);
        } else if (value instanceof Element) {
            if (SOAPElement.class.equals(type)) {
                return new MessageElement((Element)value);
            } else {
                return ObjectDeserializer.toObject((Element)value, type);
            }
        } else if (type.equals(SOAPElement.class)) {
            return ObjectSerializer.toSOAPElement(value, name);
        } else if (type.equals(Element.class)) {
            return ObjectSerializer.toElement(value, name);
        } else {
            throw new IllegalArgumentException(
                i18n.getMessage(
                    "cantConvertType",
                    new Object[] {value.getClass(), type}));
        }
    }

}
