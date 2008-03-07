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

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.Constants;
import org.apache.axis.encoding.DeserializationContext;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.DeserializerFactory;
import org.apache.axis.encoding.DeserializerImpl;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.encoding.Target;
import org.apache.axis.message.SOAPHandler;
import org.apache.axis.types.URI;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.globus.wsrf.utils.Resources;
import org.globus.util.I18n;

import org.oasis.DialectDependentType;

public abstract class DialectDependentDeserializer extends DeserializerImpl
{
    static Log logger =
        LogFactory.getLog(DialectDependentDeserializer.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    protected Map deserializerFactories = new HashMap();
    protected QName attributeName;
    protected String typeMappingName;
    protected Class javaType;
    protected boolean failOnError = true;

    public void registerDeserializerFactory(
        URI dialect, DeserializerFactory deserializerFactory)
    {
        this.deserializerFactories.put(dialect, deserializerFactory);
    }

    public void unregisterDeserializerFactory(URI dialect)
    {
        this.deserializerFactories.remove(dialect);
    }

    public void onStartElement(
        String namespace, String localName, String prefix,
        Attributes attributes, DeserializationContext context)
        throws SAXException
    {
        URI dialect = null;
        try
        {
            this.value = javaType.newInstance();
            for(int i = 0; i < attributes.getLength(); i++)
            {
                if(attributes.getLocalName(i).equals(
                    attributeName.getLocalPart()))
                {
                    //TODO: Fix this in axis
                    if(attributes.getURI(i).equals("") ||
                       attributes.getURI(i).equals(
                           attributeName.getNamespaceURI()))
                    {
                        dialect = new URI(attributes.getValue(i));
                        ((DialectDependentType) this.value).setDialect(dialect);
                        break;
                    }
                }
            }
        }
        catch(Exception e)
        {
            throw new SAXException(e);
        }
        DeserializerFactory factory = (DeserializerFactory)
            this.deserializerFactories.get(dialect);

        if (factory == null)
        {
            TypeMapping tm = context.getTypeMapping();
            QName xmlType = new QName(dialect.toString(), typeMappingName);

            factory = (DeserializerFactory)tm.getDeserializer(xmlType);

            if (factory == null)
            {
                if (failOnError) 
                {
                    throw new SAXException(
                               i18n.getMessage("noDialectDeserializer", 
                                               dialect));
                }
                else 
                {
                    return;
                }
            } 
        } 

        SOAPHandler dialectDeserializer =
            (SOAPHandler) factory.getDeserializerAs(Constants.AXIS_SAX);
        ((Deserializer) dialectDeserializer).registerValueTarget(
                         new DialectTarget());
        this.isEnded = true;
        this.valueComplete();
        context.replaceElementHandler(dialectDeserializer);
        dialectDeserializer.startElement(namespace, localName, prefix,
                                         attributes, context);
    }

    private class DialectTarget implements Target
    {
        public void set(Object obj) throws SAXException
        {
            ((DialectDependentType) DialectDependentDeserializer.this.value).
                setValue(obj);
        }
    }
}
