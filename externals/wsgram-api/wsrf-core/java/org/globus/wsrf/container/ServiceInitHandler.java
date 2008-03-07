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
package org.globus.wsrf.container;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;

public class ServiceInitHandler extends BasicHandler {

    public void invoke(MessageContext messageContext) throws AxisFault {
        try {
            ServiceManager.initializeService(messageContext);
        } catch (AxisFault e) {
            throw e;
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }
    
}

