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
package org.globus.wsrf;


import java.util.List;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.oasis.wsn.TopicExpressionType;

/**
 * Interface for representing a topic. A topic is either a child of a parent
 * topic or in the case of root topics a member of a topic list. Non-root 
 * topics have names without a namespace component.
 *
 * @see TopicList
 */
public interface Topic extends TopicListenerList
{
    /**
     * Add a sub-topic.
     *
     * @param topic The sub-topic to add
     * @throws Exception
     */
    void addTopic(Topic topic) throws Exception;

    /**
     * Remove a sub-topic
     *
     * @param topic The sub-topic to remove
     */
    void removeTopic(Topic topic);

    /**
     * Get the sub-topic with the given topic name
     *
     * @param topicName The topic name of the sub topic
     * @return The sub-topic
     */
    Topic getTopic(QName topicName);

    /**
     * Set the topic expression that resolves to a set of topics that this topic
     * references. Only used for reference topics.
     *
     * @param topicPath The topic expression to set.
     */
    void setTopicReference(TopicExpressionType topicPath);

    /**
     * Get the topic expression for the topic(s) that this topic references.
     *
     * @return The topic expression that this topic reference or null if this
     *         topic is not a reference.
     */
    TopicExpressionType getTopicReference();

    /**
     * Set the topic path. The topic path is represented as a ordered list of
     * topic names
     *
     * @param topicPath The topic path to associate with this topic
     */
    void setTopicPath(List topicPath);

    /**
     * Get the topic path. The topic path is represented as a ordered list of
     * topic names
     *
     * @return The topic path of this topic
     */
    List getTopicPath();

    /**
     * Get the name of the topic. Only root topics should actually be qualified
     * names, other topic should be non-qualified
     *
     * @return The name of the topic
     */
    QName getName();

    /**
     * Get the current notification message if there is any
     *
     * @return A object containing the current message, may be null
     */
    Object getCurrentMessage();

    /**
     * Send out a notification on this topic
     *
     * @param obj Object representation of the message to send
     * @throws Exception
     */
    void notify(Object obj) throws Exception;

    /**
     * Is this a topic reference?
     *
     * @return true if this topic is a reference to another topic false if not
     */
    boolean isReference();

    /**
     * Iterator for the set of child topics
     *
     * @return The iterator
     */
    Iterator topicIterator();
}
