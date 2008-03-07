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

/**
 * TopicExpressionType.java
 *
 */

package org.oasis.wsn;

import java.io.Serializable;

import org.apache.axis.encoding.SimpleType;
import org.apache.axis.types.URI;

import org.oasis.DialectDependentType;

public class TopicExpressionType implements SimpleType, 
                                            DialectDependentType,
                                            Serializable
{
    
    public static final String NAMESPACE =
        "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd";
    
    public static final String DIALECT_ATTR = 
        "Dialect";

    private URI dialect;  // attribute
    private Object value;

    public TopicExpressionType()
    {
        this((URI)null, null);
    }

    public TopicExpressionType(String dialect, Object value) 
        throws URI.MalformedURIException
    {
        this(new URI(dialect), value);
    }

    public TopicExpressionType(URI dialect, Object value)
    {
        this.value = value;
        this.dialect = dialect;
    }

    /**
     * Gets the dialect value for this TopicExpressionType.
     *
     * @return dialect
     */
    public URI getDialect()
    {
        return dialect;
    }

    /**
     * Sets the dialect value for this TopicExpressionType.
     *
     * @param dialect
     */
    public void setDialect(String dialect)
        throws URI.MalformedURIException
    {
        setDialect(new URI(dialect));
    }

    /**
     * Sets the dialect value for this TopicExpressionType.
     *
     * @param dialect
     */
    public void setDialect(URI dialect)
    {
        this.dialect = dialect;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(TopicExpressionType.class,
                                                     true);

    static
    {
        typeDesc.setXmlType(new javax.xml.namespace.QName(
                                     NAMESPACE,
                                     "TopicExpressionType"));
        org.apache.axis.description.AttributeDesc attrField =
                new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("dialect");
        attrField.setXmlName(new javax.xml.namespace.QName("", DIALECT_ATTR));
        attrField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "anyURI"));
        typeDesc.addFieldDesc(attrField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc()
    {
        return typeDesc;
    }
}
