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
 * Interface implemented by entities (e.g. resources) that have associated 
 * topic spaces. Primarly exists to abstract different <code>TopicList</code>
 * implementations.
 *
 * @see TopicList
 */
public interface TopicListAccessor
{
    /**
     * Get the topic list
     *
     * @return The topic list
     * @see TopicList
     */
    TopicList getTopicList();
}
