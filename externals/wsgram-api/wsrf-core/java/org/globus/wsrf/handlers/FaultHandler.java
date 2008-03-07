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
package org.globus.wsrf.handlers;

import java.util.Calendar;

import javax.xml.soap.SOAPElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.SOAPPart;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.message.addressing.To;
import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.message.addressing.Constants;
import org.apache.axis.message.addressing.EndpointReference;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.message.addressing.ReferencePropertiesType;

import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.impl.ResourceContextImpl;
import org.globus.wsrf.utils.Resources;
import org.globus.util.I18n;

import org.oasis.wsrf.faults.BaseFaultType;

// must be after the AddressingHandler
public class FaultHandler extends BasicHandler {

    private static Log logger =
        LogFactory.getLog(FaultHandler.class.getName());

    private static I18n i18n = 
        I18n.getI18n(Resources.class.getName());

    public void invoke(MessageContext msgContext)
        throws AxisFault {
        // set address
        AddressingHeaders headers =
            (AddressingHeaders)msgContext.getProperty(
                  Constants.ENV_ADDRESSING_RESPONSE_HEADERS
            );
        if (headers == null) {
            headers = new AddressingHeaders();
            msgContext.setProperty(Constants.ENV_ADDRESSING_RESPONSE_HEADERS,
                                   headers);
        }
        if (headers.getFrom() != null) {
            return;
        }
        EndpointReference from = getFrom(msgContext);
        if (from != null) {
            headers.setFrom(from);
        }
    }

    public void onFault(MessageContext msgContext) {
        Message msg = msgContext.getCurrentMessage();
        if (msg == null) {
            return;
        }
        SOAPPart part =(SOAPPart) msg.getSOAPPart();
        if (part == null) {
            return;
        }
        Object obj = part.getCurrentMessage();
        if (!(obj instanceof BaseFaultType)) {
            return;
        }
        BaseFaultType fault = (BaseFaultType)obj;

        // check timestamp
        Calendar timestamp = fault.getTimestamp();
        if (timestamp == null) {
            fault.setTimestamp(Calendar.getInstance());
        }

        // check originator
        EndpointReferenceType originator = fault.getOriginator();
        if (originator == null) {
            originator = getFrom(msgContext);
            fault.setOriginator(originator);
        }
    }

    private EndpointReference getFrom(MessageContext msgContext) {
        // check if service set
        if (msgContext.getService() == null) {
            return null;
        }
        AddressingHeaders headers =
            (AddressingHeaders)msgContext.getProperty(
                  Constants.ENV_ADDRESSING_REQUEST_HEADERS
            );
        if (headers == null) {
            return null;
        }

        To to = headers.getTo();
        if (to == null) {
            logger.warn(i18n.getMessage("noToHeader"));
            return null;
        }

        Address address = new Address(to);
        EndpointReference fromEPR = new EndpointReference(address);

        // set resource properties
        Message requestMsg = msgContext.getRequestMessage();
        try {
            ReferencePropertiesType props =
                getReferenceProperties(msgContext, requestMsg);
            if (props != null) {
                fromEPR.setProperties(props);
            }
        } catch (Exception e) {
            logger.debug("", e);
        }

        return fromEPR;
    }

    private ReferencePropertiesType getReferenceProperties(MessageContext ctx,
                                                           Message msg)
        throws Exception {
        ReferencePropertiesType props = null;
        ResourceContext resourceCtx =
            new ResourceContextImpl(ctx, msg);
        // XXX: it extracts the header element directly
        // and inserts into the reference properties
        // since it is a header it might have mustUnderstand
        // actor attributes - these should be removed
        // this is not done now. propably a clone of the
        // header should be added
        SOAPElement element = resourceCtx.getResourceKeyHeader();
        if (element != null) {
            props = new ReferencePropertiesType();
            props.add(element);
        }
        return props;
    }
}
