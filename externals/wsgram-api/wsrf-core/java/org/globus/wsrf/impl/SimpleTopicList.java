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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.oasis.wsn.TopicExpressionType;

import org.globus.wsrf.ResourceProperties;
import org.globus.wsrf.ResourceProperty;
import org.globus.wsrf.ResourcePropertySet;
import org.globus.wsrf.Topic;
import org.globus.wsrf.TopicList;
import org.globus.wsrf.TopicListMetaData;
import org.globus.wsrf.TopicListener;
import org.globus.wsrf.WSNConstants;
import org.globus.wsrf.topicexpression.InvalidTopicExpressionException;
import org.globus.wsrf.topicexpression.TopicExpressionEngine;
import org.globus.wsrf.topicexpression.TopicExpressionEvaluator;
import org.globus.wsrf.topicexpression.TopicExpressionException;
import org.globus.wsrf.topicexpression.TopicExpressionResolutionException;
import org.globus.wsrf.topicexpression.UnsupportedTopicExpressionDialectException;

/**
 * Simplistic in memory topic list.
 */
public class SimpleTopicList implements TopicList, TopicListener
{
    protected Map rootTopics = new HashMap();
    protected Collection listeners = new Vector();

    private ResourceProperty supportedTopics =
        new SimpleResourceProperty(WSNConstants.TOPIC);
    private ResourceProperty fixedTopicSet =
        new SimpleResourceProperty(WSNConstants.FIXED_TOPIC_SET);

    private static TopicExpressionEngine topicExpressionEngine =
        TopicExpressionEngineImpl.getInstance();

    private TopicListMetaData metaData;

    static Log logger =
        LogFactory.getLog(SimpleTopicList.class.getName());

    /**
     * This constructor will add the resource properties associated with the
     * notification producer porttype to the supplied resource object
     *
     * @param resource Resource object, must implement the ResourceProperties
     *                 interface
     */
    public SimpleTopicList(ResourceProperties resource)
    {
        this(resource, new SimpleTopicListMetaData(false));
    }

    /**
     * This constructor will add the resource properties associated with the
     * notification producer porttype to the supplied resource object
     *
     * @param resource Resource object, must implement the ResourceProperties
     *                 interface
     */
    public SimpleTopicList(ResourceProperties resource,
                           TopicListMetaData metaData)
    {
        this.metaData = metaData;

        ResourcePropertySet propertySet = resource.getResourcePropertySet();

        // init and add fixedTopicSet RP
        this.fixedTopicSet.add(new Boolean(metaData.isTopicSetFixed()));
        propertySet.add(this.fixedTopicSet);

        // add supportedTopics RP
        propertySet.add(this.supportedTopics);

        try
        {
            propertySet.add(new ReflectionResourceProperty(
                WSNConstants.TOPIC_EXPRESSION_DIALECTS,
                "SupportedDialects",
                topicExpressionEngine));
        }
        catch(Exception e)
        {
            logger.debug("Failed to set up SupportedDialects RP", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public void addTopic(Topic topic)
    {
        this.rootTopics.put(topic.getName(), topic);

        List topicPath = new LinkedList();
        topicPath.add(topic.getName());
        topic.setTopicPath(topicPath);

        topic.addTopicListener(this);
        this.topicAdded(topic);
    }

    public synchronized void addTopicListener(TopicListener listener)
    {
        this.listeners.add(listener);
    }

    public Collection getTopics(TopicExpressionType topicExpression)
        throws UnsupportedTopicExpressionDialectException,
               TopicExpressionResolutionException,
               InvalidTopicExpressionException,
               TopicExpressionException
    {
        return topicExpressionEngine.resolveTopicExpression(topicExpression,
                                                            this);
    }

    public void removeTopic(Topic topic)
    {
        this.rootTopics.remove(topic.getName());
        topic.removeTopicListener(this);
        this.topicRemoved(topic);
    }

    public synchronized void removeTopicListener(TopicListener listener)
    {
        listeners.remove(listener);
    }

    public synchronized Iterator topicListenerIterator()
    {
        return listeners.iterator();
    }

    public Topic getTopic(List topicPath)
    {
        Iterator pathIterator = topicPath.iterator();
        QName topicName;
        Topic topic = null;
        while(pathIterator.hasNext())
        {
            topicName = (QName) pathIterator.next();
            if(topic == null)
            {
                topic = (Topic) this.rootTopics.get(topicName);
            }
            else
            {
                topic = topic.getTopic(topicName);
            }

            if(topic == null)
            {
                return null;
            }

        }

        return topic;
    }

    public TopicListMetaData getTopicListMetaData()
    {
        return this.metaData;
    }

    public Iterator topicIterator()
    {
        return this.rootTopics.values().iterator();
    }

    public void topicAdded(Topic topic)
    {
        TopicExpressionEvaluator evaluator =
            topicExpressionEngine.getEvaluator(
                WSNConstants.SIMPLE_TOPIC_DIALECT);
        TopicExpressionType topicExpression = null;
        try
        {
            topicExpression =
                evaluator.toTopicExpression(topic.getTopicPath());
        }
        catch(Exception e)
        {
            logger.debug("Failed to create topic expression from topic path",
                         e);
            throw new RuntimeException(e.getMessage());
        }

        this.supportedTopics.add(topicExpression);

        synchronized(this)
        {
            Iterator listenerIterator = this.listeners.iterator();
            TopicListener listener;
            while(listenerIterator.hasNext())
            {
                listener = (TopicListener) listenerIterator.next();
                listener.topicAdded(topic);
            }
        }
    }

    public synchronized void topicChanged(Topic topic)
    {
        Iterator listenerIterator = this.listeners.iterator();
        TopicListener listener;
        if (logger.isDebugEnabled()) {
            logger.debug("being notified that a topic has changed");
        }
        while(listenerIterator.hasNext())
        {
            listener = (TopicListener) listenerIterator.next();
            if (logger.isDebugEnabled()) {
                logger.debug("notifying listener " + listener);
            }
            listener.topicChanged(topic);
        }
    }

    public void topicRemoved(Topic topic)
    {
        TopicExpressionEvaluator evaluator =
            topicExpressionEngine.getEvaluator(
                WSNConstants.SIMPLE_TOPIC_DIALECT);
        TopicExpressionType topicExpression = null;
        try
        {
            topicExpression =
                evaluator.toTopicExpression(topic.getTopicPath());
        }
        catch(Exception e)
        {
            logger.debug("Failed to create topic expression from topic path",
                         e);
            throw new RuntimeException(e.getMessage());
        }

        this.supportedTopics.remove(topicExpression);

        synchronized(this)
        {
            Iterator listenerIterator = this.listeners.iterator();
            TopicListener listener;
            while(listenerIterator.hasNext())
            {
                listener = (TopicListener) listenerIterator.next();
                listener.topicRemoved(topic);
            }
        }
    }
}
