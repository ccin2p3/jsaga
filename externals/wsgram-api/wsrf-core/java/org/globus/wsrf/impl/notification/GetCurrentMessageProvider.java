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
package org.globus.wsrf.impl.notification;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.Topic;
import org.globus.wsrf.TopicList;
import org.globus.wsrf.TopicListAccessor;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.Constants;
import org.globus.wsrf.topicexpression.UnsupportedTopicExpressionDialectException;
import org.globus.wsrf.utils.AnyHelper;
import org.globus.wsrf.utils.Resources;
import org.globus.wsrf.utils.FaultHelper;
import org.globus.wsrf.jndi.JNDIUtils;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.encoding.ObjectConverter;
import org.globus.util.I18n;

import org.oasis.wsn.InvalidTopicExpressionFaultType;
import org.oasis.wsn.NoCurrentMessageOnTopicFaultType;
import org.oasis.wsn.ResourceUnknownFaultType;
import org.oasis.wsn.TopicNotSupportedFaultType;
import org.oasis.wsn.GetCurrentMessage;
import org.oasis.wsn.GetCurrentMessageResponse;
import org.oasis.wsn.TopicPathDialectUnknownFaultType;

import javax.xml.soap.SOAPElement;

public class GetCurrentMessageProvider
{
    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    private static Log logger =
        LogFactory.getLog(GetCurrentMessageProvider.class.getName());

    private static String GET_CURRENT_MESSAGE_CONTEXT =
        Constants.JNDI_BASE_NAME + "/providers/GetCurrentMessageProvider/";
    
    /**
     * Provider for the get current message operation
     *
     * @param request
     * @return The current message associated with the Topic
     * @throws RemoteException
     * @throws ResourceUnknownFaultType
     * @throws InvalidTopicExpressionFaultType
     *
     * @throws TopicNotSupportedFaultType
     * @throws NoCurrentMessageOnTopicFaultType
     *
     */
    public GetCurrentMessageResponse
        getCurrentMessage(GetCurrentMessage request)
        throws RemoteException,
               ResourceUnknownFaultType,
               InvalidTopicExpressionFaultType,
               TopicNotSupportedFaultType,
               NoCurrentMessageOnTopicFaultType
    {
        Object resource = null;
        try
        {
            resource = ResourceContext.getResourceContext().getResource();
        }
        catch (NoSuchResourceException e)
        {
            ResourceUnknownFaultType fault = new ResourceUnknownFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(
                          i18n.getMessage("resourceDisoveryFailed")
            );
            faultHelper.addFaultCause(e);
            throw fault;
        }
        catch(Exception e)
        {
            throw new RemoteException(
                i18n.getMessage("resourceDisoveryFailed"), e);
        }

        if(!(resource instanceof TopicListAccessor))
        {
            throw new TopicNotSupportedFaultType();
        }

        TopicList topicList;
        Collection topics;
        Object message;
        topicList = ((TopicListAccessor) resource).getTopicList();

        try
        {
            topics = topicList.getTopics(request.getTopic());
        }
        catch(UnsupportedTopicExpressionDialectException e)
        {
            logger.debug("", e);
            TopicPathDialectUnknownFaultType fault =
                new TopicPathDialectUnknownFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            throw fault;
        }
        catch(Exception e)
        {
            logger.debug("", e);
            InvalidTopicExpressionFaultType fault =
                new InvalidTopicExpressionFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            faultHelper.setDescription(
                          i18n.getMessage("topicExpressionResolutionFailed")
            );
            throw fault;
        }

        if(topics.isEmpty())
        {
            throw new TopicNotSupportedFaultType();
        }

        message = ((Topic) (topics.toArray())[0]).getCurrentMessage();

        if(message == null)
        {
            throw new NoCurrentMessageOnTopicFaultType();
        }

        ObjectConverter converter = null;
        try 
        {
            Context initialContext = new InitialContext();
            converter = (ObjectConverter)JNDIUtils.lookup(
                   initialContext, 
                   GET_CURRENT_MESSAGE_CONTEXT + message.getClass().getName(),
                   ObjectConverter.class);
        } 
        catch (NameNotFoundException e) 
        {
            // that's ok. maybe converter is not registered
        } 
        catch (Exception e)
        {
            logger.debug("Converter lookup failed", e);
        }

        GetCurrentMessageResponse response = new GetCurrentMessageResponse();

        try
        {
            SOAPElement elem = (converter != null) ?
                converter.toSOAPElement(message) :
                ObjectSerializer.toSOAPElement(message);

            AnyHelper.setAny(response, elem);
        }
        catch(Exception e)
        {
            throw new RemoteException(
                i18n.getMessage("notificationSerializationError"), e);
        }

        return response;
    }
}
