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

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;

/**
 * This handler is used only with "local" transport. 
 * It resets the target service set by the LocalSender
 * so that AddressingHandler and URLHandler can set the 
 * service appropriately.
 */
public class LocalRequester extends BasicHandler {
    
    public void invoke(MessageContext msgCtx) throws AxisFault {
        // reset it becuase LocalSender will set it no matter what
        msgCtx.setTargetService(null);
        LocalTransportUtils.restoreProperties(msgCtx);
    }
    
}

