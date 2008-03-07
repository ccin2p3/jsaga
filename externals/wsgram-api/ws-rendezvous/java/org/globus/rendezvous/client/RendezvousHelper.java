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
package org.globus.rendezvous.client;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import javax.xml.rpc.Stub;
import javax.xml.namespace.QName;

import org.apache.axis.Constants;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.oasis.wsn.Subscribe;
import org.oasis.wsn.TopicExpressionType;
import org.oasis.wsrf.properties.GetResourcePropertyResponse;

import org.globus.util.I18n;

import org.globus.rendezvous.client.RendezvousConstants;
import org.globus.rendezvous.client.ClientSecurityStrategy;
import org.globus.rendezvous.generated.RendezvousPortType;
import org.globus.rendezvous.generated.service.RendezvousServiceAddressingLocator;
import org.globus.rendezvous.generated.RegisterInput;
import org.globus.rendezvous.generated.RegisterResponse;
import org.globus.rendezvous.generated.RankTakenFaultType;
import org.globus.rendezvous.generated.CapacityReachedFaultType;

import org.globus.wsrf.NotifyCallback;
import org.globus.wsrf.WSNConstants;
import org.globus.wsrf.NotificationConsumerManager;
import org.globus.wsrf.Topic;
import org.globus.wsrf.TopicList;
import org.globus.wsrf.TopicListAccessor;
import org.globus.wsrf.container.ServiceContainer;
import org.globus.wsrf.impl.ResourcePropertyTopic;
import org.globus.wsrf.impl.SimpleTopicList;
import org.globus.wsrf.impl.SimpleTopic;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;
import org.globus.wsrf.encoding.ObjectDeserializer;

/**
 * Helper class to invoke operations on RendezvousPortType
 * in a potentially secure manner.
 */
public class RendezvousHelper {

    public static final int NO_DESIRED_RANK = -1;

    public RendezvousHelper() {
        this(null);
    }

    public RendezvousHelper(ClientSecurityStrategy security) {
        this.security = security;
    }

    private static byte[] makeHeader(int count) {
        String stringHeader = Integer.toString(count) + " ";
        byte[] bytesHeader = stringHeader.getBytes();
        return bytesHeader;
    }

    /**
     * Prepend a Rendezvous binary data encoding header to a byte array.
     * The header is an encoding of a count and is separated from the rest
     * of the data by a 'space' ASCII character (byte 32).<p>
     * This is used by:
     * <ul>
     * <li>
     * The Rendezvous resource when aggregating its registrant data elements.
     * The number of registrants is prepended as a header.</li>
     * <li>
     * A client which wishes to register application binary data with a
     * bottom-level Rendezvous and needs to prepend metadata about the
     * binary data that will enable parsing later on, for instance the size
     * of the binary data, i.e. the number of bytes.</li>
     * </ul>
     * @param data byte[] the data to prepend a header to
     * @param count int the positive number to encode as the header
     * @return byte[]
     */
    public static byte[] prependHeader(byte[] data, int count) {
        if (count < 0) {
            throw new RuntimeException();
        }
        byte[] header = makeHeader(count);
        if (count == 0) {
            return header;
        }
        byte[] finalData = new byte[header.length + data.length];
        System.arraycopy(header, 0, finalData, 0, header.length);
        System.arraycopy(data, 0, finalData, header.length, data.length);
        return finalData;
    }

    /**
     * Extract byte array representing the binary array
     * which is the value of the RegistrantData RP. The byte array is
     * an encoding of the number of registrants. The header is terminated by
     * a 'space' ASCII character (byte 32) which is not included in the
     * result of this function.
     * @param data byte[] the data which contains the header to extract
     * @param beginIndex int the index at which the header starts in the
     *                       input array
     * @return byte[]
     */
    public static byte[] extractHeader(byte[] data, int beginIndex) {
        List byteList = new java.util.ArrayList(0);
        for (int i = beginIndex; i < data.length; i++) {
            byte readByte = data[i];
            if (readByte == 32) {
                break;
            }
            byteList.add(new Byte(readByte));
        }
        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            byteArray[i] = ( (Byte) byteList.get(i)).byteValue();
            //System.out.println("adding byte value to decoded header" + bytes[i]);
        }
        return byteArray;
    }

    public static int readCount(byte[] bytes) {
        String header = new String(bytes);
        System.out.println("Parsing header string " + header);
        int count = 0;
        try {
            count = Integer.parseInt(header);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return count;
    }

    /**
     * Pull status from the Rendezvous service/resource.
     * @param rendezvousEndpoint EndpointReferenceType endpoint to query
     * @throws Exception
     * @return boolean
     */
    public boolean isRendezvousDone(EndpointReferenceType rendezvousEndpoint)
        throws Exception
    {
        //pull status
        RendezvousServiceAddressingLocator locator =
            new RendezvousServiceAddressingLocator();
        RendezvousPortType port =
            locator.getRendezvousPortTypePort(rendezvousEndpoint);

        //security
        if (this.security != null) {
            this.security.setStubPropertiesForGetResourceProperty( (Stub) port);
        }

        GetResourcePropertyResponse getRPResponse =
            port.getResourceProperty(
            RendezvousConstants.RP_COMPLETED);
        Boolean done = (Boolean) ObjectDeserializer.toObject(
            getRPResponse.get_any()[0],
            Boolean.class);
        return done.booleanValue();
    }

    /**
     * will be null/empty if rdv not complete/done
     */
    public byte[] getRegistrantData(EndpointReferenceType rendezvousEndpoint)
        throws Exception
    {
        RendezvousServiceAddressingLocator locator =
            new RendezvousServiceAddressingLocator();
        RendezvousPortType port =
            locator.getRendezvousPortTypePort(rendezvousEndpoint);

        //security
        if (this.security != null) {
            this.security.setStubPropertiesForGetResourceProperty( (Stub) port);
        }

        GetResourcePropertyResponse getRPResponse =
            port.getResourceProperty(
            RendezvousConstants.RP_DATA);
        getRPResponse.get_any()[0].setType(Constants.XSD_BASE64);
        byte[] data = (byte[]) ObjectDeserializer.toObject(
            getRPResponse.get_any()[0]);
        return data;
    }

    /**
     * Susbcribe for notifications when set of registered data changes,
     * i.e. when new data gets registered.
     * @param rendezvousEndpoint EndpointReferenceType
     * @param notificationCallback NotifyCallback
     * @throws Exception
     *
    public void subscribeForDataNotifications(
        EndpointReferenceType rendezvousEndpoint,
        NotifyCallback        notificationCallback)
        throws Exception
    {
        this.subscribeForNotifications(rendezvousEndpoint,
                               notificationCallback,
                               RendezvousConstants.RP_DATA);

    }*/

    /**
     * Susbcribe for notifications on completion of rendezvous.
     * It is possible to use CompletionNotifyCallbackImpl in order
     * to easily implement the notification callback to pass to this method.
     * @param rendezvousEndpoint EndpointReferenceType
     * @param notificationCallback NotifyCallback
     * @throws Exception
     */
    public void subscribeForCompletionNotifications(
        EndpointReferenceType rendezvousEndpoint,
        NotifyCallback        notificationCallback)
        throws Exception
    {
        this.subscribeForNotifications(rendezvousEndpoint,
                                       notificationCallback,
                                       RendezvousConstants.RP_COMPLETED);
    }

    public void subscribeForNotifications(
        EndpointReferenceType rendezvousEndpoint,
        NotifyCallback        notificationCallback,
        QName                 topicQName)
        throws Exception
    {
        Subscribe request = new Subscribe();
        request.setUseNotify(Boolean.TRUE);
        NotificationConsumerManager notificationConsumerManager = null;

        //if GSI required
        if (true) { //TODO: GSI or not should be encapsulated in security
            //strategy (implies interface change)
            Map properties = new HashMap();
            properties.put(
                ServiceContainer.CLASS,
                "org.globus.wsrf.container.GSIServiceContainer");
            notificationConsumerManager
                = NotificationConsumerManager.getInstance(properties);
        }
        else {
            notificationConsumerManager = NotificationConsumerManager.
                getInstance();
        }
        notificationConsumerManager.startListening();
        EndpointReferenceType notificationConsumerEPR = null;

        //security
        ResourceSecurityDescriptor securityDescriptor = null;
        if (this.security != null) {
            securityDescriptor = this.security.getSecurityDescriptor();
        }

        List topicPath = new LinkedList();
        topicPath.add(topicQName);

        notificationConsumerEPR =
            notificationConsumerManager.createNotificationConsumer(
            topicPath,
            notificationCallback,
            securityDescriptor);

        request.setConsumerReference(notificationConsumerEPR);
        TopicExpressionType topicExpression = new TopicExpressionType();
        topicExpression.setDialect(WSNConstants.SIMPLE_TOPIC_DIALECT);
        topicExpression.setValue(topicQName);
        request.setTopicExpression(topicExpression);

        RendezvousServiceAddressingLocator locator =
            new RendezvousServiceAddressingLocator();
        RendezvousPortType port =
            locator.getRendezvousPortTypePort(rendezvousEndpoint);

        //security
        if (this.security != null) {
            this.security.setStubPropertiesForSubscribe( (Stub) port);
        }

        port.subscribe(request);
    }


    /**
     * Register application binary data with a rendezvous without specifying
     * any desired rank.
     * @param rendezvousEndpoint EndpointReferenceType the endpoint to register with
     * @param data byte[] the input binary data to register
     * @throws Exception
     * @return int the rank, positive.
     */
    public int registerWithRendezvous(
        EndpointReferenceType rendezvousEndpoint,
        byte[] data)
    throws Exception
    {
        int rank = -1;
        try {
            rank = registerWithRendezvous(rendezvousEndpoint, data,
                                          RendezvousHelper.NO_DESIRED_RANK);
        }
        catch (RankTakenFaultType e) {
            String errorMessage =
                i18n.getMessage(Resources.INVALID_RANK_TAKEN_ERROR);
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage);
        }
        return rank;
    }


    /**
     * @param rendezvousEndpoint EndpointReferenceType the endpoint to register with
     * @param data byte[] the input binary data to register
     * @param desiredRank int Can be NO_DESIRED_RANK to indicate no desired rank
     * @throws Exception
     * @return int the rank. Is >= 0 if the registration succeeded, -1 otherwise.
     */
    public int registerWithRendezvous(
        EndpointReferenceType rendezvousEndpoint,
        byte[] data, int desiredRank)
    throws Exception
    {
        RendezvousServiceAddressingLocator locator =
            new RendezvousServiceAddressingLocator();
        RendezvousPortType parentPort =
            locator.getRendezvousPortTypePort(rendezvousEndpoint);

        if (this.security != null) {
            this.security.setStubPropertiesForRegister( (Stub) parentPort);
        }

        RegisterResponse response = parentPort.register(
            new RegisterInput(data, desiredRank));
        int rank = response.getRank();
        if (logger.isDebugEnabled()) {
            logger.debug("Obtained rank: " + rank);
        }
        return rank; //assuming it is within Java int limits

    }

    private ClientSecurityStrategy security;

    private static final Log logger =
        LogFactory.getLog(RendezvousHelper.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());
}
