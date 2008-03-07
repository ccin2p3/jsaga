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

import org.globus.util.I18n;
import org.globus.wsrf.Topic;
import org.globus.wsrf.TopicListener;
import org.globus.wsrf.utils.Resources;

/**
 * Simple in-memory implementation of the Topic interface
 */
public class SimpleTopic implements Topic, TopicListener
{
    private static I18n i18n = I18n.getI18n(Resources.class.getName());
    protected Map subTopics;
    protected TopicExpressionType reference;
    protected Collection listeners;
    protected QName name;
    protected Object current;
    protected List topicPath; // ordered set of QNames
    static Log logger =
        LogFactory.getLog(SimpleTopic.class.getName());

    public void addTopic(Topic topic) throws Exception
    {
        if(this.reference != null)
        {
            throw new Exception(i18n.getMessage("addingSubTopicToReference"));
        }

        this.subTopics.put(topic.getName(), topic);

        List topicPath = new LinkedList();

        topicPath.addAll(this.topicPath);
        topicPath.add(topic.getName());

        topic.setTopicPath(topicPath);
        topic.addTopicListener(this);
        this.topicAdded(topic);
    }

    public synchronized void addTopicListener(TopicListener listener)
    {
        this.listeners.add(listener);
    }

    public QName getName()
    {
        return this.name;
    }

    public Topic getTopic(QName topicName)
    {
        // Maybe should be called getSubTopic ?
        return (Topic) this.subTopics.get(topicName);
    }

    public TopicExpressionType getTopicReference()
    {
        return this.reference;
    }

    public boolean isReference()
    {
        return (this.reference != null);
    }

    public Iterator topicIterator()
    {
        return this.subTopics.values().iterator();
    }

    public void notify(Object obj) throws Exception
    {
        this.current = obj;
        if(logger.isDebugEnabled())
        {
            logger.debug("Notify called on topic " +
                         this.name + " with message " + obj.toString());
        }
        this.topicChanged(this);
    }

    public Object getCurrentMessage()
    {
        return this.current;
    }

    public void removeTopic(Topic topic)
    {
        this.subTopics.remove(topic.getName());
        topic.removeTopicListener(this);
        this.topicRemoved(topic);
    }

    public synchronized void removeTopicListener(TopicListener listener)
    {
        this.listeners.remove(listener);
    }

    public synchronized Iterator topicListenerIterator()
    {
        return this.listeners.iterator();
    }

    public void setTopicReference(TopicExpressionType topicPath)
    {
        this.reference = topicPath;
    }

    public void setTopicPath(List topicPath)
    {
        this.topicPath = topicPath;
    }

    public List getTopicPath()
    {
        return this.topicPath;
    }

    /**
     * Create a topic with the given name
     *
     * @param name The name of the created topic
     */
    public SimpleTopic(QName name)
    {
        this(new HashMap(), null, new Vector(), name, null, null);
    }

    /**
     * Create a topic with the given parameters
     *
     * @param subTopics     A map of child topics
     * @param reference     A topic expression (only used if this is a topic
     *                      alias)
     * @param listeners     A collection of topic listeners
     * @param name          The name of this topic
     * @param current       The current value of this topic
     * @param topicPath     The concrete topic path of this topic
     */
    public SimpleTopic(
        Map subTopics, TopicExpressionType reference,
        Collection listeners, QName name,
        Object current, List topicPath)
    {
        this.subTopics = subTopics;
        this.reference = reference;
        this.listeners = listeners;
        this.name = name;
        this.current = current;
        this.topicPath = topicPath;
    }

    public synchronized void topicAdded(Topic topic)
    {
        Iterator listenerIterator = this.listeners.iterator();
        TopicListener listener;

        while(listenerIterator.hasNext())
        {
            listener = (TopicListener) listenerIterator.next();
            listener.topicAdded(topic);
        }
    }

    public synchronized void topicChanged(Topic topic)
    {
        Iterator listenerIterator = this.listeners.iterator();
        TopicListener listener;
        while(listenerIterator.hasNext())
        {
            listener = (TopicListener) listenerIterator.next();
            listener.topicChanged(this);
        }
    }

    public synchronized void topicRemoved(Topic topic)
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
