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

import java.util.Map;
import java.util.HashMap;

import org.globus.wsrf.config.ContainerConfig;

import org.apache.axis.AxisEngine;
import org.apache.axis.MessageContext;
import org.apache.axis.AxisFault;
import org.apache.axis.message.MessageElement;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ServiceDesc;
import org.apache.axis.handlers.soap.SOAPService;

import org.apache.axis.message.addressing.Constants;
import org.apache.axis.message.addressing.To;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.message.addressing.Action;
import org.apache.axis.message.addressing.ReferencePropertiesType;
import org.apache.axis.message.addressing.util.AddressingUtils;
import org.apache.axis.types.URI;

import org.globus.axis.description.ServiceDescUtil;

import javax.xml.namespace.QName;

/**
 * Extends the Apache WS-Addressing AddressingHandler to customize how
 * the target service is set.
 */
public class AddressingHandler
    extends org.apache.axis.message.addressing.handler.AddressingHandler {
    
    private static final QName MESSAGE_ID_QNAME = 
        new QName(Constants.NS_URI_ADDRESSING, Constants.MESSAGE_ID);

    private static final QName TO_QNAME = 
        new QName(Constants.NS_URI_ADDRESSING, Constants.TO);

    private static final QName FROM_QNAME = 
        new QName(Constants.NS_URI_ADDRESSING, Constants.FROM);
    
    private static final QName ACTION_QNAME = 
        new QName(Constants.NS_URI_ADDRESSING, Constants.ACTION);

    private static final QName REPLY_TO_QNAME = 
        new QName(Constants.NS_URI_ADDRESSING, Constants.REPLY_TO);

    private static final QName FAULT_TO_QNAME = 
        new QName(Constants.NS_URI_ADDRESSING, Constants.FAULT_TO);

    private static final QName RELATES_TO_QNAME = 
        new QName(Constants.NS_URI_ADDRESSING, Constants.RELATES_TO);

    protected void processClientRequest(MessageContext msgContext,
                                        boolean setMustUnderstand)
        throws Exception {
        super.processClientRequest(msgContext, setMustUnderstand);
        // pass wsa header list
        setSignedHeaders(msgContext,
                         Constants.ENV_ADDRESSING_REQUEST_HEADERS);
    }

    protected void processServerRequest(MessageContext msgContext)
        throws Exception {
        super.processServerRequest(msgContext);
        setVerifiedHeaders(msgContext,
                           Constants.ENV_ADDRESSING_REQUEST_HEADERS);
    }

    protected void processServerResponse(MessageContext msgContext,
                                         boolean setMustUnderstand)
        throws Exception {
        OperationDesc operation = msgContext.getOperation();
        if (operation != null) {
            SOAPService service = msgContext.getService();
            if (service != null) {
                ServiceDesc serviceDesc = service.getServiceDescription();
                if (serviceDesc != null) {
                    Map actionMap = (Map)serviceDesc.getProperty(
                                              ServiceDescUtil.ACTION_MAP);
                    if (actionMap != null) {
                        String action = (String)actionMap.get(operation);
                        if (action != null) {
                            setAction(msgContext, action);
                        }
                    }
                }
            }
        }
        super.processServerResponse(msgContext, setMustUnderstand);
        // pass wsa header list
        setSignedHeaders(msgContext,
                         Constants.ENV_ADDRESSING_RESPONSE_HEADERS);
    }

    private void setAction(MessageContext msgContext, String newAction) 
        throws Exception {
        AddressingHeaders resHeaders = 
            AddressingUtils.getResponseHeaders(msgContext);
        Action action = resHeaders.getAction();
        if (action == null) {
            resHeaders.setAction(new Action(new URI(newAction)));
        }
    }

    protected void setTargetService(MessageContext msgContext,
                                    AddressingHeaders headers)
        throws Exception {
        To toURI = headers.getTo();
        if (toURI == null) {
            return;
        }
        String path = toURI.getPath();
        if (path == null) {
            return;
        }

        AxisEngine engine = msgContext.getAxisEngine();
        ContainerConfig config = ContainerConfig.getConfig(engine);
        String base = config.getWSRFLocation();
        int len = base.length() + 1;

        if (path.startsWith("/" + base) && path.length() > len) {
            String service = path.substring(len);
            msgContext.setTargetService(service);
        }
    }
    
    private void setVerifiedHeaders(MessageContext msgCtx,
                                    String headerProperty) {
        AddressingHeaders headers =
            (AddressingHeaders)msgCtx.getProperty(headerProperty);
        if (headers == null) {
            return;
        }
        Map currentHeaders = (Map)msgCtx.getProperty(
         org.globus.wsrf.impl.security.authentication.Constants.ENFORCED_SECURE_HEADERS);

        Map verifiedHeaders = updateVerifiedHeaders(currentHeaders, headers);

        msgCtx.setProperty(
          org.globus.wsrf.impl.security.authentication.Constants.ENFORCED_SECURE_HEADERS,
          verifiedHeaders);
    }

    /**
     * This does not include the header that contains the key 
     */
    private Map updateVerifiedHeaders(Map map, AddressingHeaders headers) {
        if (map == null) {
            map = new HashMap();
        }

        // must be checked as we do dispatched on that
        if (headers.getTo() != null) {
            map.put(TO_QNAME, "");
        }
        // both must be checked as fault or reply
        // can be returned to these if specified
        if (headers.getReplyTo() != null) {
            map.put(REPLY_TO_QNAME, "");
        }
        if (headers.getFaultTo() != null) {
            map.put(FAULT_TO_QNAME, "");
        }

        return map;
    }

    private void setSignedHeaders(MessageContext msgCtx,
                                  String headerProperty) {
        AddressingHeaders headers =
            (AddressingHeaders)msgCtx.getProperty(headerProperty);
        if (headers == null) {
            return;
        }
        Map currentHeaders = (Map)msgCtx.getProperty(
         org.globus.wsrf.impl.security.authentication.Constants.SECURE_HEADERS);
        
        Map secureHeaders = updateSignedHeaders(currentHeaders, headers);
        
        msgCtx.setProperty(
         org.globus.wsrf.impl.security.authentication.Constants.SECURE_HEADERS,
         secureHeaders);
    }

    private Map updateSignedHeaders(Map map, AddressingHeaders headers) {
        if (map == null) {
            map = new HashMap();
        }

        if (headers.getMessageID() != null) {
            map.put(MESSAGE_ID_QNAME, "");
        }
        if (headers.getTo() != null) {
            map.put(TO_QNAME, "");
        }
        if (headers.getAction() != null) {
            map.put(ACTION_QNAME, "");
        }
        if (headers.getFrom() != null) {
            map.put(FROM_QNAME, "");
        }
        if (headers.getReplyTo() != null) {
            map.put(REPLY_TO_QNAME, "");
        }
        if (headers.getFaultTo() != null) {
            map.put(FAULT_TO_QNAME, "");
        }
        if ((headers.getRelatesTo() != null) && 
            !headers.getRelatesTo().isEmpty()) {
            map.put(RELATES_TO_QNAME, "");
        }
        ReferencePropertiesType props = headers.getReferenceProperties();
        if (props != null) {
            MessageElement [] elements = props.get_any();
            if (elements != null) {
                for (int i=0;i<elements.length;i++) {
                    map.put(elements[i].getQName(), "");
                }
            }
        }
        
        return map;
    }

    protected void resetOperations(MessageContext ctx) 
        throws AxisFault {
        ServiceDescUtil.resetOperations(ctx);
    }
    
}
