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
package org.globus.wsrf.encoding;

import org.oasis.AnyListType;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.encoding.DeserializationContext;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.DeserializerImpl;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.encoding.Target;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.message.SOAPHandler;
import org.apache.axis.utils.Messages;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class AnyListDeserializer extends DeserializerImpl {

    static Log logger =
        LogFactory.getLog(AnyListDeserializer.class.getName());

    private QName xmlType;
    private Class javaType;

    public AnyListDeserializer(Class javaType, QName xmlType) {
        this(javaType, xmlType, null);
    }

    public AnyListDeserializer(Class javaType, QName xmlType, TypeDesc desc) {
        this.xmlType = xmlType;
        this.javaType = javaType;
    }

    public void onStartElement(String namespace, String localName,
                               String prefix, Attributes attributes,
                               DeserializationContext context)
        throws SAXException {
        try {
            this.value = (AnyListType)javaType.newInstance();
        } catch (Throwable e) {
            throw new SAXException(Messages.getMessage("cantCreateBean00", 
                                                       javaType.getName(), 
                                                       e.toString()));
        }
    }

    public SOAPHandler onStartChild(String namespace,
                                    String localName,
                                    String prefix,
                                    Attributes attributes,
                                    DeserializationContext context)
        throws SAXException {
     
        QName elemQName = new QName(namespace, localName);

        Class childClass = ((AnyListType)value).getType(elemQName);
        if (childClass == null) {
            // TODO: fix exception msg
            throw new SAXException("");
        }
        
        QName childXMLType = context.getTypeFromAttributes(namespace, 
                                                           localName,
                                                           attributes);

        if (childXMLType == null) {
            TypeDesc desc =  TypeDesc.getTypeDescForClass(childClass);
            childXMLType = (desc != null) ? desc.getXmlType() : null;
        }

        Deserializer dSer = null;
        
        if (childXMLType == null) {
            TypeMapping tm = context.getTypeMapping();
            QName defaultXMLType = tm.getTypeQName(childClass);
            dSer = context.getDeserializer(childClass, defaultXMLType);
        } else {
            dSer = context.getDeserializerForType(childXMLType);
        }

        if (dSer == null) {
            dSer = context.getDeserializerForClass(childClass);
        }   

        if (dSer == null) {
            throw new SAXException(Messages.getMessage("noDeser00",
                                                       childXMLType.toString()));
        }

        dSer.registerValueTarget(new AnyListTarget());
        addChildDeserializer(dSer);
        
        return (SOAPHandler)dSer;
    }

    private class AnyListTarget implements Target {
        public void set(Object obj) throws SAXException {
            ((AnyListType)value).add(obj);
        }
    }
    
}
