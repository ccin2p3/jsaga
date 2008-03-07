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

import org.globus.wsrf.TopicListMetaData;

/**
 * Simple TopicList meta data.
 */
public class SimpleTopicListMetaData implements TopicListMetaData {

    protected boolean fixed = false;

    public SimpleTopicListMetaData() {
        this(false);
    }

    public SimpleTopicListMetaData(boolean fixed) {
        this.fixed = fixed;
    }

    /**
     * Indicate whether the topic set is fixed or not
     *
     * @param fixed Boolean indicating whether the topic set is fixed or
     *              not
     */
    protected void setFixedTopicSet(boolean fixed) {
        this.fixed = fixed;
    }

    public boolean isTopicSetFixed() {
        return this.fixed;
    }
    
}
