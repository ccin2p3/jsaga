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

import org.apache.axis.Constants;
import org.apache.axis.encoding.ser.QNameDeserializerFactory;
import org.apache.axis.types.URI;

import org.globus.wsrf.WSNConstants;
import org.oasis.wsn.TopicExpressionType;

import javax.xml.namespace.QName;

public class TopicExpressionDeserializer extends DialectDependentDeserializer
{

    private static final QName ATTR = 
        new QName(TopicExpressionType.NAMESPACE, 
                  TopicExpressionType.DIALECT_ATTR);
    
    private static final URI SIMPLE_TOPIC_DIALECT;
    private static final URI CONCRETE_TOPIC_DIALECT;
    private static final URI FULL_TOPIC_DIALECT;
    
    static
    {
        try
        {
            SIMPLE_TOPIC_DIALECT = 
                new URI(WSNConstants.SIMPLE_TOPIC_DIALECT);
            CONCRETE_TOPIC_DIALECT = 
                new URI(WSNConstants.CONCRETE_TOPIC_DIALECT);
            FULL_TOPIC_DIALECT =
                new URI(WSNConstants.FULL_TOPIC_DIALECT);
        }
        catch(Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    public TopicExpressionDeserializer()
    {
        this.attributeName = ATTR;
        this.typeMappingName = "TopicExpressionDialect";
        this.javaType = TopicExpressionType.class;
        registerDeserializerFactory(SIMPLE_TOPIC_DIALECT,
                                    new QNameDeserializerFactory(
                                           QName.class,
                                           Constants.XSD_QNAME));
        registerDeserializerFactory(CONCRETE_TOPIC_DIALECT,
                                    new QNameDeserializerFactory(
                                           QName.class,
                                           Constants.XSD_QNAME));
        registerDeserializerFactory(FULL_TOPIC_DIALECT,
                                    new QNameDeserializerFactory(
                                           QName.class,
                                           Constants.XSD_QNAME));
    }
}
