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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.Constants;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.encoding.SerializerFactory;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.types.URI;
import org.apache.axis.wsdl.fromJava.Types;

import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import org.globus.wsrf.utils.Resources;
import org.globus.util.I18n;

import org.oasis.DialectDependentType;

public abstract class DialectDependentSerializer implements Serializer
{
    static Log logger =
        LogFactory.getLog(DialectDependentSerializer.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    protected Map serializerFactories = new HashMap();
    protected QName attributeName;
    protected String typeMappingName;

    public void registerSerializerFactory(
        URI dialect, SerializerFactory serializerFactory)
    {
        this.serializerFactories.put(dialect, serializerFactory);
    }

    public void unregisterSerializerFactory(URI dialect)
    {
        this.serializerFactories.remove(dialect);
    }

    public void serialize(
        QName name, Attributes attributes, Object obj,
        SerializationContext context) throws IOException
    {
        DialectDependentType dialectExpression;
        if(!(obj instanceof DialectDependentType))
        {
            throw new IOException(
                i18n.getMessage("expectedType",
                                DialectDependentType.class.toString()));
        }
        else
        {
            dialectExpression = (DialectDependentType) obj;
        }

        if(attributes == null)
        {
            attributes = new AttributesImpl();
        }
        else if(!(attributes instanceof AttributesImpl))
        {
            attributes = new AttributesImpl(attributes);
        }

        URI dialectURI = dialectExpression.getDialect();

        if(dialectURI != null)
        {
            ((AttributesImpl) attributes).addAttribute(attributeName.getNamespaceURI(),
                                                       attributeName.getLocalPart(),
                                                       "",
                                                       "CDATA",
                                                       dialectURI.toString());
        }
        else
        {
            throw new IOException(i18n.getMessage("nullArgument", "dialect"));
        }

        SerializerFactory serializerFactory = (SerializerFactory)
            serializerFactories.get(dialectURI);

        if (serializerFactory == null)
        {
            TypeMapping tm = context.getTypeMapping();

            QName xmlType = new QName(dialectURI.toString(), typeMappingName);
            Class javaType = tm.getClassForQName(xmlType);
            if (javaType != null) 
            {
                serializerFactory = 
                    (SerializerFactory)tm.getSerializer(javaType, xmlType);
            }

            if (serializerFactory == null)
            {
                throw new IOException(i18n.getMessage("noDialectSerializer", 
                                                      dialectURI));
            }
        }

        Serializer serializer =
            (Serializer) serializerFactory.getSerializerAs(Constants.AXIS_SAX);
        serializer.serialize(name, attributes,
                             dialectExpression.getValue(),
                             context);
    }

    public Element writeSchema(Class aClass, Types types) throws Exception
    {
        return null;
    }

    public String getMechanismType()
    {
        return Constants.AXIS_SAX;
    }
}
