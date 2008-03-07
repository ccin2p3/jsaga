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
package org.globus.axis.handlers;

import java.util.Hashtable;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.transport.http.HTTPConstants;

public class HTTP11Handler extends BasicHandler {

    private static final Hashtable ENCODING_CHUNKED_HEADERS;

    static {
        ENCODING_CHUNKED_HEADERS = new Hashtable();
        ENCODING_CHUNKED_HEADERS.put(
                   HTTPConstants.HEADER_TRANSFER_ENCODING,
                   HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED);
    }

    public void invoke(MessageContext msgCtx) throws AxisFault {
        Object value =
            msgCtx.getProperty(MessageContext.HTTP_TRANSPORT_VERSION);
        if (value == null) {
            msgCtx.setProperty(MessageContext.HTTP_TRANSPORT_VERSION,
                               HTTPConstants.HEADER_PROTOCOL_V11);
            Hashtable headers = 
                (Hashtable)msgCtx.getProperty(HTTPConstants.REQUEST_HEADERS);
            if (headers == null) {
                msgCtx.setProperty(HTTPConstants.REQUEST_HEADERS,
                                   ENCODING_CHUNKED_HEADERS);
            }
        }
    }
    
}

