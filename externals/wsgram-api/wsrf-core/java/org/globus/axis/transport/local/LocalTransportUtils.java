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
package org.globus.axis.transport.local;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.axis.Constants;
import org.apache.axis.AxisEngine;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.transport.local.LocalTransport;
import org.apache.axis.session.Session;

import javax.xml.rpc.Stub;

/**
 * Utility methods for local invocations.
 */
public class LocalTransportUtils {
    
    private static final String PROPERTIES = 
        "contextProperties";
    
    private static final String [] PROPS = 
    {Constants.MC_CONFIGPATH, Constants.MC_HOME_DIR};
    
    static {
        Call.addTransportPackage("org.apache.axis.transport");
    }
    
    /**
     * Initializes the local transport support.
     */
    public static void init() {}

    /**
     * Configures the stub for proper local invocation.
     * A MessageContext must be associated with the current
     * thread.
     *
     * @param stub the stub to configure for local invocation.
     * @return true if stub was successfully configured for
     *         local invocation. False otherwise.
     */
    public static boolean enableLocalTransport(Stub stub) {
        MessageContext ctx = MessageContext.getCurrentContext();
        if (ctx == null) {
            return false;
        }
        if (!storeProperties(ctx)) {
            return false;
        }
        AxisEngine engine = ctx.getAxisEngine();
        stub._setProperty(LocalTransport.LOCAL_SERVER,
                          engine);
        return true;
    }
    
    static boolean storeProperties(MessageContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException();
        }
        Map savedProps = new HashMap();
        Object value = null;
        for (int i=0;i<PROPS.length;i++) {
            value = ctx.getProperty(PROPS[i]);
            if (value != null) {
                savedProps.put(PROPS[i], value);
            }
        }
        AxisEngine engine = ctx.getAxisEngine();
        Session session = engine.getApplicationSession();
        if (session == null) {
            return false;
        } 
        session.set(PROPERTIES, savedProps);
        return true;
    }

    static boolean restoreProperties(MessageContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException();
        }
        AxisEngine engine = ctx.getAxisEngine();
        Session session = engine.getApplicationSession();
        if (session == null) {
            return false;
        }
        Map savedProps = (Map)session.get(PROPERTIES);
        if (savedProps == null) {
            return false;
        }
        Iterator iter = savedProps.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            ctx.setProperty((String)entry.getKey(),
                            entry.getValue());
        }
        session.remove(PROPERTIES);
        return true;
    }
    
}

