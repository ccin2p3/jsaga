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


/**
 * Interface that should be implemented by parties interested in changes in
 * topics and the list of root topics. It is currently used to send
 * notifications when the situation represented by a topic changes and for
 * tracking the list of currently supported topics (exposed as a resource
 * property)
 */
public interface TopicListener
{
    /**
     * Called when the value of the topic changes
     *
     * @param topic The topic that changed
     */
    void topicChanged(Topic topic);

    /**
     * Called when a topic is added
     *
     * @param topic The topic being added
     */
    void topicAdded(Topic topic);

    /**
     * Called when a topic is removed
     *
     * @param topic The topic being removed
     */
    void topicRemoved(Topic topic);
}
