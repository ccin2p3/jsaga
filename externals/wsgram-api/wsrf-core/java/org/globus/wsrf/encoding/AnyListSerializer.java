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

import java.io.IOException;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.Constants;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.utils.Messages;
import org.apache.axis.wsdl.fromJava.Types;
import org.apache.axis.description.TypeDesc;

import org.w3c.dom.Element;
import org.xml.sax.Attributes;

public class AnyListSerializer implements Serializer {

    static Log logger =
        LogFactory.getLog(AnyListSerializer.class.getName());

    public AnyListSerializer() {}

    public void serialize(QName name, 
                          Attributes attributes,
                          Object obj,
                          SerializationContext context) 
        throws IOException {
        if (!(obj instanceof AnyListType)) {
            throw new IOException(Messages.getMessage("cantSerialize01"));
        }
        
        AnyListType any = (AnyListType)obj;
        
        context.startElement(name, attributes);
        
        Object value = null;
        QName xmlName = null;
        QName xmlType = null;
        TypeDesc typeDesc = null;
        Class clazz = null;

        Iterator iter = any.iterator();
        while(iter.hasNext()) {
            value = iter.next();

            clazz = value.getClass();
            xmlName = any.getElementName(clazz);

            // XXX: not really helps anything now
            typeDesc = TypeDesc.getTypeDescForClass(clazz);
            xmlType = (typeDesc != null) ? typeDesc.getXmlType() : null;
            
            context.serialize(xmlName, null, value, 
                              xmlType, Boolean.TRUE, null);
        }
        
        context.endElement();
    }
    
    public Element writeSchema(Class aClass, Types types) throws Exception {
        return null;
    }

    public String getMechanismType() {
        return Constants.AXIS_SAX;
    }

}
