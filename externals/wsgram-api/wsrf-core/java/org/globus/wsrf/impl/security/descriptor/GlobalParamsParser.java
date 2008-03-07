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
 * Handles elements specified in the security descriptor that are not
 * method specific
 */
public class GlobalParamsParser implements ElementHandler {

    public static final String REPLAY_ATTACK_WINDOW = "replay-attack-window";
    public static final String REPLAY_ATTACK_FILTER = "replay-attack-filter";
    public static final String ATTRIB = "value";
    public static final String REJECT_LIMITED_PROXY = "reject-limited-proxy";
    public static final String GRID_MAP = "gridmap";
    public static final String CONTEXT_LIFETIME = "context-lifetime";

    public static final QName CONTEXT_LIFETIME_QNAME =
        new QName(SecurityDescriptor.NS, CONTEXT_LIFETIME);
    public static final QName REJECT_LIMITED_PROXY_QNAME =
        new QName(SecurityDescriptor.NS, REJECT_LIMITED_PROXY);
    public static final QName GRID_MAP_QNAME =
        new QName(SecurityDescriptor.NS, GRID_MAP);
    public static final QName REPLAY_ATTACK_FILTER_QNAME =
        new QName(SecurityDescriptor.NS, REPLAY_ATTACK_FILTER);
    public static final QName REPLAY_ATTACK_WINDOW_QNAME =
        new QName(SecurityDescriptor.NS, REPLAY_ATTACK_WINDOW);

    protected GlobalParamsParserCallback callback;

    public GlobalParamsParser(GlobalParamsParserCallback callback) {
        super();
        this.callback = callback;
    }

    public void parse(Element elem) throws ElementParserException {

        String name = elem.getLocalName();
        
        if (name.equalsIgnoreCase(REJECT_LIMITED_PROXY)) {
            callback.setRejectLimitedProxy(elem.getAttribute(ATTRIB));
        } 

        if (name.equalsIgnoreCase(GRID_MAP)) {
            callback.setGridMapFile(elem.getAttribute(ATTRIB));
        } 

        if (name.equalsIgnoreCase(REPLAY_ATTACK_FILTER)) {
            callback.setReplayAttackFilter(elem.getAttribute(ATTRIB));
        }

        if (name.equalsIgnoreCase(REPLAY_ATTACK_WINDOW)) {
            callback.setReplayAttackWindow(elem.getAttribute(ATTRIB));
        } 

        if (name.equalsIgnoreCase(CONTEXT_LIFETIME)) {
            Integer lifetime = 
                new Integer(elem.getAttribute(ATTRIB));
            callback.setContextLifetime(lifetime);
        } 
    }
}
