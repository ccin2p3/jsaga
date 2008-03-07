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

import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Element;

import org.globus.wsrf.ResourceProperty;
import org.globus.wsrf.Topic;
import org.globus.wsrf.TopicListener;
import org.globus.wsrf.ResourcePropertyMetaData;
import org.globus.wsrf.utils.Resources;
import org.globus.wsrf.utils.AnyHelper;
import org.globus.wsrf.encoding.SerializationException;
import org.globus.wsrf.core.notification.ResourcePropertyValueChangeNotificationElementType;
import org.globus.util.I18n;

import org.oasis.wsn.TopicExpressionType;
import org.oasis.wsrf.properties.ResourcePropertyValueChangeNotificationType;
import org.oasis.wsrf.properties.ResourcePropertyValueChangeNotificationTypeNewValue;
import org.oasis.wsrf.properties.ResourcePropertyValueChangeNotificationTypeOldValue;

/**
 * This class can be used to expose arbitrary ResourceProperty as a Topic.
 */
public class ResourcePropertyTopic implements ResourceProperty, Topic
{
    private static Log logger =
        LogFactory.getLog(ResourcePropertyTopic.class.getName());
    private static I18n i18n = I18n.getI18n(Resources.class.getName());
    protected ResourceProperty rp;
    protected Topic topic;
    //TODO: might be safer to default to false
    protected boolean autoNotify = true;
    protected boolean sendOldValue = false;

    protected ResourcePropertyTopic()
    {
    }

    /**
     * Construct a new ResourcePropertyTopic
     *
     * @param rp    A ResourceProperty object.
     * @param qname A name of the topic.
     */
    public ResourcePropertyTopic(ResourceProperty rp, QName qname)
    {
        if(rp == null)
        {
            throw new IllegalArgumentException(
                i18n.getMessage("nullArgument", "rp"));
        }
        if(qname == null)
        {
            throw new IllegalArgumentException(
                i18n.getMessage("nullArgument", "qname"));
        }
        this.rp = rp;
        this.topic = new SimpleTopic(qname);
    }

    /**
     * Construct a new ResourcePropertyTopic. The topic name is the name of the
     * ResourceProperty.
     *
     * @param rp A ResourceProperty object.
     */
    public ResourcePropertyTopic(ResourceProperty rp)
    {
        if(rp == null)
        {
            throw new IllegalArgumentException(
                i18n.getMessage("nullArgument", "rp"));
        }
        this.rp = rp;
        this.topic = new SimpleTopic(this.rp.getMetaData().getName());
    }

    /**
     * Get the resource property associated with this topic
     *
     * @return The resource property
     */
    public ResourceProperty getResourceProperty()
    {
        return this.rp;
    }

    public QName getName()
    {
        return this.topic.getName();
    }

    public void addTopic(Topic topic) throws Exception
    {
        this.topic.addTopic(topic);
    }

    public void addTopicListener(TopicListener listener)
    {
        this.topic.addTopicListener(listener);
    }

    public Object getCurrentMessage()
    {
        return this.topic.getCurrentMessage();
    }

    public Topic getTopic(QName topicName)
    {
        return this.topic.getTopic(topicName);
    }

    public List getTopicPath()
    {
        return this.topic.getTopicPath();
    }

    public TopicExpressionType getTopicReference()
    {
        return this.topic.getTopicReference();
    }

    public boolean isReference()
    {
        return this.topic.isReference();
    }

    public Iterator topicIterator()
    {
        return this.topic.topicIterator();
    }

    public void notify(Object obj) throws Exception
    {
        ResourcePropertyValueChangeNotificationElementType message =
            this.createValueChangeMessage(false);
        //TODO: Ignoring parameter in this case
        this.setNewValue(message);
        this.topic.notify(message);
    }

    public void removeTopic(Topic topic)
    {
        this.topic.removeTopic(topic);
    }

    public void removeTopicListener(TopicListener listener)
    {
        this.topic.removeTopicListener(listener);
    }

    public Iterator topicListenerIterator()
    {
        return this.topic.topicListenerIterator();
    }

    public void setTopicPath(List topicPath)
    {
        this.topic.setTopicPath(topicPath);
    }

    public void setTopicReference(TopicExpressionType topicPath)
    {
        this.topic.setTopicReference(topicPath);
    }

    /**
     * @return Returns the auto notify setting.
     */
    public boolean autoNotify()
    {
        return this.autoNotify;
    }

    /**
     * @param autoNotify The autoNotify to set.
     */
    public void setAutoNotify(boolean autoNotify)
    {
        this.autoNotify = autoNotify;
    }

    protected void fireNotification(
        ResourcePropertyValueChangeNotificationElementType message)
    {
        if(this.autoNotify)
        {
            try
            {
                this.topic.notify(message);
            }
            catch(Exception e)
            {
                logger.error(i18n.getMessage(
                    "nodeliver", message), e);
                // TODO: I think these methods need to be allowed to throw exceptions
            }
        }
    }

    // delegated calls

    public void add(Object value)
    {
        ResourcePropertyValueChangeNotificationElementType message =
            createValueChangeMessage(this.sendOldValue);
        this.rp.add(value);
        setNewValue(message);
        fireNotification(message);
    }

    public void set(int index, Object value)
    {
        ResourcePropertyValueChangeNotificationElementType message =
            createValueChangeMessage(this.sendOldValue);
        this.rp.set(index, value);
        setNewValue(message);
        fireNotification(message);
    }

    public boolean remove(Object value)
    {
        ResourcePropertyValueChangeNotificationElementType message =
            createValueChangeMessage(this.sendOldValue);
        boolean rs = this.rp.remove(value);
        if(rs)
        {
            setNewValue(message);
            fireNotification(message);
        }
        return rs;
    }

    public Object get(int index)
    {
        return this.rp.get(index);
    }

    public void clear()
    {
        this.rp.clear();
    }

    public int size()
    {
        return this.rp.size();
    }

    public boolean isEmpty()
    {
        return this.rp.isEmpty();
    }

    public Iterator iterator()
    {
        return this.rp.iterator();
    }

    public ResourcePropertyMetaData getMetaData()
    {
        return rp.getMetaData();
    }

    public SOAPElement[] toSOAPElements()
        throws SerializationException
    {
        return this.rp.toSOAPElements();
    }

    public Element[] toElements()
        throws SerializationException
    {
        return this.rp.toElements();
    }

    /**
     * Determine whether notifications send the old resource property value as
     * well as the new value. This setting defaults to only sending the new
     * value.
     *
     * @return The setting
     */
    public boolean getSendOldValue()
    {
        return sendOldValue;
    }

    /**
     * Set the "send old value" behavior. This setting determines if
     * notifications include the old as well as the new value of the resource
     * property.
     *
     * @param sendOldValue If true the old value will be sent, if false
     *                     (default) only the new value will be sent.
     */
    public void setSendOldValue(boolean sendOldValue)
    {
        this.sendOldValue = sendOldValue;
    }

    private void setNewValue(
        ResourcePropertyValueChangeNotificationElementType message)
    {
        ResourcePropertyValueChangeNotificationTypeNewValue newValue =
            new ResourcePropertyValueChangeNotificationTypeNewValue();

        if(this.rp.size() != 0)
        {
            try
            {
                AnyHelper.setAny(newValue, this.rp.toSOAPElements());
            }
            catch(Exception e)
            {
                logger.error(
                    i18n.getMessage("rpSerializationError",
                                    this.rp.getMetaData().getName()), e);
                // TODO: I think these methods need to be allowed to throw exceptions
            }
        }
        message.getResourcePropertyValueChangeNotification().setNewValue(
            newValue);
    }

    private ResourcePropertyValueChangeNotificationElementType
        createValueChangeMessage(boolean sendOld)
    {
        ResourcePropertyValueChangeNotificationElementType changeMessage =
            new ResourcePropertyValueChangeNotificationElementType();
        ResourcePropertyValueChangeNotificationType message =
            new ResourcePropertyValueChangeNotificationType();
        changeMessage.setResourcePropertyValueChangeNotification(message);

        if(sendOld == true)
        {
            ResourcePropertyValueChangeNotificationTypeOldValue oldValue =
                new ResourcePropertyValueChangeNotificationTypeOldValue();

            if(this.rp.size() != 0)
            {
                try
                {
                    AnyHelper.setAny(oldValue, this.rp.toSOAPElements());
                }
                catch(Exception e)
                {
                    logger.error(
                        i18n.getMessage("rpSerializationError",
                                        this.rp.getMetaData().getName()), e);
                    // TODO: I think these methods need to be allowed to throw exceptions
                }
            }
            message.setOldValue(oldValue);
        }
        return changeMessage;
    }
}
