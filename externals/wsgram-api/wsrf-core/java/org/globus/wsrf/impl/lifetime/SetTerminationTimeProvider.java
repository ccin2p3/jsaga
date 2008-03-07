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
package org.globus.wsrf.impl.lifetime;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.util.I18n;
import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceHome;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.ResourceLifetime;
import org.globus.wsrf.Topic;
import org.globus.wsrf.TopicList;
import org.globus.wsrf.TopicListAccessor;
import org.globus.wsrf.WSRFConstants;
import org.globus.wsrf.utils.FaultHelper;
import org.globus.wsrf.utils.Resources;

import org.oasis.wsrf.lifetime.ResourceUnknownFaultType;
import org.oasis.wsrf.lifetime.TerminationTimeChangeRejectedFaultType;
import org.oasis.wsrf.lifetime.UnableToSetTerminationTimeFaultType;
import org.oasis.wsrf.lifetime.SetTerminationTime;
import org.oasis.wsrf.lifetime.SetTerminationTimeResponse;
import org.oasis.wsrf.lifetime.TerminationNotification;

/**
 * SetTerminationTime operation implementation. The resource must provide
 * TerminationTime resource property to use this provider.
 */
public class SetTerminationTimeProvider {

    private static I18n i18n = I18n.getI18n(Resources.class.getName());
    private static Log logger =
        LogFactory.getLog(SetTerminationTimeProvider.class.getName());

    private static final List TERMINATION_TOPIC_PATH;

    static {
        TERMINATION_TOPIC_PATH = new Vector(1);
        TERMINATION_TOPIC_PATH.add(WSRFConstants.TERMINATION_TOPIC);
    }

    public SetTerminationTimeResponse setTerminationTime(SetTerminationTime request)
        throws RemoteException,
               UnableToSetTerminationTimeFaultType,
               ResourceUnknownFaultType,
               TerminationTimeChangeRejectedFaultType {

        if (request == null) {
            UnableToSetTerminationTimeFaultType fault =
                new UnableToSetTerminationTimeFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(
                          i18n.getMessage("nullArgument", "request")
            );
            throw fault;
        }

        Object resource = null;
        ResourceHome home = null;
        ResourceKey key = null;
        try {
            ResourceContext ctx = ResourceContext.getResourceContext();
            home = ctx.getResourceHome();
            key = ctx.getResourceKey();
            resource = home.find(key);
        } catch (NoSuchResourceException e) {
            ResourceUnknownFaultType fault = new ResourceUnknownFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(
                          i18n.getMessage("resourceDisoveryFailed")
            );
            faultHelper.addFaultCause(e);
            throw fault;
        } catch (Exception e) {
            throw new RemoteException(
                i18n.getMessage("resourceDisoveryFailed"), e);
        }

        if (!(resource instanceof ResourceLifetime)) {
            UnableToSetTerminationTimeFaultType fault = 
                new UnableToSetTerminationTimeFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(i18n.getMessage("rltNotSupported"));
            throw fault;
        }

        Calendar termTime = request.getRequestedTerminationTime();

        Calendar currentTime = Calendar.getInstance();
        Calendar newTermTime = null;

        // if termTime in the past - immediate destruction
        if (termTime != null &&
            termTime.getTime().before(currentTime.getTime())) {
            try {
                home.remove(key);
            } catch (NoSuchResourceException e) {
                ResourceUnknownFaultType fault =
                    new ResourceUnknownFaultType();
                FaultHelper faultHelper = new FaultHelper(fault);
                faultHelper.addFaultCause(e);
                throw fault;
            } catch (Exception e) {
                UnableToSetTerminationTimeFaultType fault =
                    new UnableToSetTerminationTimeFaultType();
                FaultHelper faultHelper = new FaultHelper(fault);
                faultHelper.setDescription(
                              i18n.getMessage("resourceRemoveFailed")
                );
                faultHelper.addFaultCause(e);
                throw fault;
            }
            newTermTime = currentTime;
        } else {
            ((ResourceLifetime) resource).setTerminationTime(termTime);
            newTermTime = termTime;
        }

        SetTerminationTimeResponse response =
            new SetTerminationTimeResponse();

        response.setCurrentTime(currentTime);
        response.setNewTerminationTime(newTermTime);

        return response;
    }

    // TODO: move the below somewhere else?
    public static void sendTerminationNotification(Object resource) {
        sendTerminationNotification(resource, null);
    }

    public static void sendTerminationNotification(Object resource,
                                                   Calendar currentTime) {
        if (resource instanceof TopicListAccessor) {
            TopicList topicList = 
                ((TopicListAccessor) resource).getTopicList();
            Topic terminationTopic = 
                topicList.getTopic(TERMINATION_TOPIC_PATH);
            if (terminationTopic != null) {
                TerminationNotification terminationNotification =
                    new TerminationNotification();
                if (currentTime == null) {
                    currentTime = Calendar.getInstance();
                }
                // TODO: what do we put in the reason field?
                terminationNotification.setTerminationTime(currentTime);
                try {
                    terminationTopic.notify(terminationNotification);
                } catch(Exception e) {
                    logger.error("", e);
                }
            }
        }
    }

}
