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

import java.util.Iterator;

/**
 * Interface used for managing a list of topic listeners. Currently inherited
 * by the <code>TopicList</code> and <code>Topic</code> interfaces.
 *
 * @see TopicListener
 * @see TopicList
 * @see Topic
 */
public interface TopicListenerList
{
    /**
     * Add a topic listener
     *
     * @param listener The topic listener to add
     * @see TopicListener
     */
    void addTopicListener(TopicListener listener);

    /**
     * Remove a topic listener
     *
     * @param listener The topic listener to remove.
     * @see TopicListener
     */
    void removeTopicListener(TopicListener listener);

    /**
     * Get a iterator for the list of TopicListeners
     *
     * @return The iterator
     * @see TopicListener
     */
    Iterator topicListenerIterator();
}
