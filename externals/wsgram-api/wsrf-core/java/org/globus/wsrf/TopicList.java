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

import java.util.Collection;
import java.util.List;
import java.util.Iterator;

import org.globus.wsrf.topicexpression.UnsupportedTopicExpressionDialectException;
import org.globus.wsrf.topicexpression.TopicExpressionResolutionException;
import org.globus.wsrf.topicexpression.InvalidTopicExpressionException;
import org.globus.wsrf.topicexpression.TopicExpressionException;
import org.oasis.wsn.TopicExpressionType;

/**
 * Interface for managing and performing queries on a set of root topics.
 */
public interface TopicList extends TopicListenerList
{
    /**
     * Add a root topic
     *
     * @param topic The topic to add
     */
    void addTopic(Topic topic);

    /**
     * Remove the root topic
     *
     * @param topic The topic to remove
     */
    void removeTopic(Topic topic);

    /**
     * Get the set of topics the given topic expression resolves to
     *
     * @param topicExpression The topic expression to resolve to a set of
     *                        topics
     * @return The resulting set of topics
     * @throws UnsupportedTopicExpressionDialectException
     *                                  if the topic expression dialect is not
     *                                  supported
     * @throws TopicExpressionResolutionException
     *                                  if the expression could not be
     *                                  evaluated
     * @throws InvalidTopicExpressionException
     *                                  if the topic expression is invalid
     * @throws TopicExpressionException if any other error occurs
     */
    Collection getTopics(TopicExpressionType topicExpression)
        throws UnsupportedTopicExpressionDialectException,
               TopicExpressionResolutionException,
               InvalidTopicExpressionException,
               TopicExpressionException;

    /**
     * Get the topic for the given topic path.
     *
     * @param topicPath The topic path (list of topic names)
     * @return The topic corresponding to the given topic path
     */
    Topic getTopic(List topicPath);

    /**
     * Returns meta data associated with this topic list.
     *
     * @return meta data of this topic list.
     */
    TopicListMetaData getTopicListMetaData();

    /**
     * Iterator for the set of root topics
     *
     * @return The iterator
     */
    Iterator topicIterator();
}
