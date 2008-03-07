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
package org.globus.rendezvous.service.utils;

import org.oasis.wsrf.faults.BaseFaultType;
import org.oasis.wsrf.faults.BaseFaultTypeDescription;
import org.oasis.wsrf.faults.BaseFaultTypeErrorCode;

import java.lang.reflect.Constructor;
import java.util.Calendar;

import org.oasis.wsrf.faults.BaseFaultType;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.Text;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.utils.JavaUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.util.I18n;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.container.ServiceHost;
import org.globus.wsrf.utils.AddressingUtils;
import org.globus.wsrf.utils.FaultHelper;

import org.w3c.dom.Element;

/**
 * Utility class containing static methods for constructing faults.
 */
public class FaultUtils {

    public static BaseFaultType makeFault(
            Class                                   faultClass,
            String                                  description,
            Exception                               cause) {
        Calendar timestamp = Calendar.getInstance();

        //originator
        EndpointReferenceType originator = getEndpointReference();

        //errorCode
        BaseFaultTypeErrorCode errorCode = new BaseFaultTypeErrorCode();
        if (cause != null) {
            MessageElement [] any = new MessageElement[] {
                new MessageElement(new Text(JavaUtils.stackToString(cause)))
            };
            errorCode.set_any(any);
        }
        errorCode.setDialect(FaultHelper.STACK_TRACE);

        BaseFaultTypeDescription[] faultDescription
            = new BaseFaultTypeDescription[] {
                new BaseFaultTypeDescription(description)
        };

        //faultCause
        BaseFaultType[] faultCause;
        if (cause != null) {
            faultCause = new BaseFaultType[] {
                FaultHelper.toBaseFault(cause)
            };
        } else {
            faultCause = new BaseFaultType[0];
        }

        BaseFaultType f = null;
        try {
            f = (BaseFaultType)faultClass.newInstance();
            f.setTimestamp(timestamp);
            f.setOriginator(originator);
            f.setErrorCode(errorCode);
            f.setDescription(faultDescription);
            f.setFaultCause(faultCause);
        } catch (Exception e) {
            String errorMessage =
                i18n.getMessage(Resources.FAULT_INSTANCIATION_ERROR);
            logger.error(errorMessage, e);
        }

        return f;
    }


    private static EndpointReferenceType getEndpointReference()
    {
        EndpointReferenceType epr = null;
        try
        {
            ResourceContext ctx = ResourceContext.getResourceContext();
            epr = AddressingUtils.createEndpointReference(
                ServiceHost.getBaseURL() + ctx.getService(),
                ctx.getResourceKey());
        }
        catch(Exception e)
        {
            String errorMessage = i18n.getMessage(Resources.EPR_CREATION_ERROR);
            logger.error(errorMessage, e);
        }

        return epr;
    }

    private static Log logger = LogFactory.getLog(FaultUtils.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

}
