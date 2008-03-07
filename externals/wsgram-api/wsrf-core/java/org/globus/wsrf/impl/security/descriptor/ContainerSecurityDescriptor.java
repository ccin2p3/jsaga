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
package org.globus.wsrf.impl.security.descriptor;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.globus.wsrf.impl.security.util.FixedObjectInputStream;

/**
 * Represents a container's security descriptor.
 */
public class ContainerSecurityDescriptor extends SecurityDescriptor
    implements ContainerOnlyParamsParserCallback {

    private String contextInterval = null;
    private String replayInterval = null;

    public ContainerSecurityDescriptor() {
        super();

        register(ContainerOnlyParamsParser.CONTEXT_TIMER_INTERVAL_QNAME,
                 new ContainerOnlyParamsParser(this));
        register(ContainerOnlyParamsParser.REPLAY_TIMER_INTERVAL_QNAME,
                 new ContainerOnlyParamsParser(this));
    }

    /**
     * Sets the interval at which the timer that collects up expired
     * message id used in replay attack prevention needs to be run.
     * Should be in milliseconds.
     */
    public void setReplayTimerInterval(String value) {
        this.replayInterval = value;
    }

    /**
     * Returns the replay attack prevention timer interval
     */
    public String getReplayTimerInterval() {
        return this.replayInterval;
    }

    /**
     * Sets the interval at which the timer that collects up expired
     * contexts, created when secure conversation is used, needs to be run.
     */
    public void setContextTimerInterval(String value) {
        this.contextInterval = value;
    }

    /**
     * Returns the interval at which the timer that collects up expired
     * contexts, created when secure conversation is used, is run.
     */
    public String getContextTimerInterval() {
        return this.contextInterval;
    }

    protected void writeObject(ObjectOutputStream oos) throws IOException {
        super.writeObject(oos);
        oos.writeObject(this.contextInterval);
        oos.writeObject(this.replayInterval);
    }

    protected void readObject(FixedObjectInputStream ois)
        throws IOException, ClassNotFoundException {
        super.readObject(ois);
        this.contextInterval = (String)ois.readObject();
        this.replayInterval = (String)ois.readObject();
    }
}
