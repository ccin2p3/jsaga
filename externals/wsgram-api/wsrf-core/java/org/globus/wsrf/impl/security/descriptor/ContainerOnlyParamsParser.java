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

import org.globus.wsrf.impl.security.descriptor.util.ElementHandler;
import org.globus.wsrf.impl.security.descriptor.util.ElementParserException;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * Handles elements that are configured only in contianer security
 * descritpor namely &lt;replay-timer-interval&gt; and 
 * &lt;context-timer-interval&gt;
 */
public class ContainerOnlyParamsParser implements ElementHandler {

    public static final String REPLAY_TIMER_INTERVAL = "replay-timer-interval";
    public static final String CONTEXT_TIMER_INTERVAL = 
        "context-timer-interval";
    public static final String ATTRIB = "value";

    public static final QName CONTEXT_TIMER_INTERVAL_QNAME =
        new QName(SecurityDescriptor.NS, CONTEXT_TIMER_INTERVAL);
    public static final QName REPLAY_TIMER_INTERVAL_QNAME =
        new QName(SecurityDescriptor.NS, REPLAY_TIMER_INTERVAL);

    protected ContainerOnlyParamsParserCallback callback;

    public ContainerOnlyParamsParser(ContainerOnlyParamsParserCallback 
                                     callback) {
        super();
        this.callback = callback;
    }

    public void parse(Element elem) throws ElementParserException {

        String name = elem.getLocalName();
        
        if (name.equalsIgnoreCase(CONTEXT_TIMER_INTERVAL)) {
            callback.setContextTimerInterval(elem.getAttribute(ATTRIB));
        } 
            
        if (name.equalsIgnoreCase(REPLAY_TIMER_INTERVAL)) {
            callback.setReplayTimerInterval(elem.getAttribute(ATTRIB));
        } 
    }
}
