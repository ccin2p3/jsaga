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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.util.I18n;
import org.globus.wsrf.Topic;
import org.globus.wsrf.TopicList;
import org.globus.wsrf.WSNConstants;
import org.globus.wsrf.topicexpression.InvalidTopicExpressionException;
import org.globus.wsrf.topicexpression.TopicExpressionResolutionException;
import org.globus.wsrf.topicexpression.TopicExpressionEvaluator;
import org.globus.wsrf.topicexpression.TopicExpressionException;
import org.globus.wsrf.topicexpression.UnsupportedTopicExpressionDialectException;
import org.globus.wsrf.utils.Resources;
import org.oasis.wsn.TopicExpressionType;

/**
 * Topic expression evalutor for the simple topic dialect.
 *
 * @see org.globus.wsrf.topicexpression.TopicExpressionEvaluator
 */
public class SimpleTopicExpressionEvaluator implements TopicExpressionEvaluator
{
    static Log logger =
        LogFactory.getLog(SimpleTopicExpressionEvaluator.class.getName());
    private static I18n i18n = I18n.getI18n(Resources.class.getName());
    private static String[] dialects = {WSNConstants.SIMPLE_TOPIC_DIALECT};

    public Collection resolve(TopicExpressionType expression,
                              TopicList topicList) 
        throws UnsupportedTopicExpressionDialectException,
               TopicExpressionResolutionException,
               InvalidTopicExpressionException,
               TopicExpressionException
    {
        QName topicName = (QName) ((TopicExpressionType) expression).getValue();

        logger.debug("Looking for topic with namespace: " +
                     topicName.getNamespaceURI() + " and local part " +
                     topicName.getLocalPart());

        Collection result = new Vector();
        List topicPath = new LinkedList();
        topicPath.add(topicName);

        Topic topic = topicList.getTopic(topicPath);

        if(topic != null)
        {
            result.add(topic);
        }

        return result;
    }

    public String[] getDialects()
    {
        return dialects;
    }

    public List getConcreteTopicPath(TopicExpressionType expression)
        throws UnsupportedTopicExpressionDialectException,
               InvalidTopicExpressionException,
               TopicExpressionException
    {
        List result = new LinkedList();
        result.add(((TopicExpressionType) expression).getValue());
        return result;
    }

    public TopicExpressionType toTopicExpression(List topicPath)
        throws InvalidTopicExpressionException,
               TopicExpressionException
    {
        if(topicPath == null || topicPath.size() != 1)
        {
            throw new InvalidTopicExpressionException(
                i18n.getMessage("invalidSimpleTopicPath"));
        }

        TopicExpressionType result = null;
        try {
             result = new TopicExpressionType(
                            WSNConstants.SIMPLE_TOPIC_DIALECT, 
                            (QName) topicPath.get(0));
        } catch (IOException e) {
            throw new TopicExpressionException("", e);
        }
        return result;
    }
}
