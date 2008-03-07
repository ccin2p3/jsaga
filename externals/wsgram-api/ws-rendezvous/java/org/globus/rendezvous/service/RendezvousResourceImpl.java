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
package org.globus.rendezvous.service;

import java.math.BigInteger;
import java.util.List;
import java.util.LinkedList;
import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;

import org.apache.axis.Constants;

import org.globus.rendezvous.generated.RankTakenFaultType;
import org.globus.rendezvous.service.utils.FaultUtils;
import org.globus.rendezvous.service.utils.Resources;

import org.globus.rendezvous.client.RendezvousConstants;
import org.globus.rendezvous.client.RendezvousHelper;
import org.globus.rendezvous.generated.RendezvousPortType;
import org.globus.rendezvous.generated.RendezvousResourceProperties;
import org.globus.rendezvous.generated.StateChangeNotificationMessageType;
import org.globus.rendezvous.generated.StateChangeNotificationMessageWrapperType;
import org.globus.rendezvous.generated.RendezvousDataType;

import org.globus.wsrf.ResourceProperty;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceProperties;
import org.globus.wsrf.ResourcePropertySet;
import org.globus.wsrf.Topic;
import org.globus.wsrf.TopicList;
import org.globus.wsrf.TopicListAccessor;
import org.globus.wsrf.ResourcePropertyMetaData;
import org.globus.wsrf.impl.SimpleResourcePropertyMetaData;

import org.globus.wsrf.impl.ReflectionResource;
import org.globus.wsrf.impl.ReflectionResourceProperty;
import org.globus.wsrf.impl.ResourcePropertyTopic;
import org.globus.wsrf.impl.SimpleResourcePropertySet;
import org.globus.wsrf.impl.SimpleTopicList;
import org.globus.wsrf.impl.SimpleTopic;
import org.globus.wsrf.impl.SimpleResourceProperty;

import org.globus.util.I18n;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.wsrf.utils.PerformanceLog;
import org.globus.util.I18n;

public class RendezvousResourceImpl extends    ReflectionResource
                                    implements RendezvousResource,
                                               TopicListAccessor
{

    public void initialize(
            Object                          resourceBean,
            QName                           resourceElementQName,
            Object                          key)
            throws                          ResourceException
    {
        if (logger.isDebugEnabled()) {
            logger.debug("initialize called");
        }

        super.initialize(resourceBean, resourceElementQName, key);

        int capacity = this.getCapacity();

        this.initializeTopics();
    }

    protected ResourceProperty createNewResourceProperty(
                                       ResourcePropertyMetaData metaData,
                                       Object resourceBean)
        throws Exception
    {
        QName rpQName = metaData.getName();
        if (rpQName.equals(RendezvousConstants.RP_DATA)) {
            return super.createNewResourceProperty(
                RP_DATA_METADATA,
                resourceBean);
            //not great another MetaData object was created for nothing...
            //todo: modif base class so we can override instead:
            //      createNewResourceProperty(qName, resourceBean)
        } else {
            return super.createNewResourceProperty(metaData, resourceBean);
        }
    }

    //used to handle special case of (byte[] - xsd:base64Binary) mapping
    private static final SimpleResourcePropertyMetaData RP_DATA_METADATA =
        new SimpleResourcePropertyMetaData(RendezvousConstants.RP_DATA,
                                           1, 1, false, Object.class,
                                           false, //not read-only -> for setter
                                           Constants.XSD_BASE64);



    /**
     * Called by initialize()
     */
    protected void initializeTopics() {

        logger.debug("Initializing Rendezvous topics");

        this.topicList = new SimpleTopicList(this); //this overrides existing
        //resource properties:
        // wsnt:Topic, wsnt:FixedTopicSet, wsnt:TopicExpressionDialect
        // => resource bean JavaBean properties
        //    are not linked to the RPs anymore. Their values must be
        // accessed through this object instead, returned by getTopicList();

        //====== RP RendezvousCompleted topic
        Topic rendezvousCompletedChangeTopic;
        rendezvousCompletedChangeTopic = new SimpleTopic(
            RendezvousConstants.RP_COMPLETED); //TODO define specific topic
        //we will do manual, custom notifications
        this.topicList.addTopic(rendezvousCompletedChangeTopic);
        if (logger.isDebugEnabled()) {
            logger.debug("Added topic for rendezvous completion:" +
                         rendezvousCompletedChangeTopic.getName() +
                         " to resource " + this);
        }
    }

    /**
     * Used by derived classes to initialize the implementation state of the
     * rendezvous resource. For instance in case of a persistent resource,
     * this function would be used in order to set the rendezvous internal
     * state to what was saved and recovered.
     */
    protected void setInternalRendezvousState(RendezvousDataType internalState)
    {
        if (internalState != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Setting internal rendezvous state");
            }
            this.tally = internalState.getTally();
            this.internalBinaryData = internalState.getInternalRegistrantData();
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("No internal rendezvous state to set");
            }
        }
    }

    /**
     * Used by derived types to obtain an XML-serializable form of the internal
     * state of the rendezvous resource, for instance in order to passivate
     * it to disk.
     * @return RendezvousDataType
     */
    protected RendezvousDataType getInternalRendezvousState() {
        RendezvousDataType internalState = new RendezvousDataType();
        internalState.setTally(this.tally);
        internalState.setInternalRegistrantData(this.internalBinaryData);
        return internalState;
    }

    /**
     * Create external, aggregated view of the data when all have registered
     * @return byte[]
     */
    private byte[] getMungedData() {
        int capacity = this.getCapacity();
        if (this.tally != capacity) {
            throw new RuntimeException("Tally != capacity");
        }
        int totalDataLength = 0;
        byte[][] dataElements = this.getInternalBinaryData();
        //check
        if (capacity != dataElements.length) {throw new RuntimeException();}

        totalDataLength = 0;
        for (int i = 0; i < capacity; i++) {
            int dataLength = dataElements[i].length;
            if (logger.isDebugEnabled()) {
                logger.debug("length of data element " + i + ": " + dataLength);
            }
            totalDataLength += dataLength;
        }

        // One entry per prefix, plus original total length
        byte[] mungedData = new byte[totalDataLength];
        int idxMunged = 0;
        for (int i = 0; i < capacity; i++) {
            for (int j = 0; j < dataElements[i].length; j++) {
                mungedData[idxMunged++] = dataElements[i][j];
            }
        }
        //check idxMunged == totalLength
        if (idxMunged != totalDataLength) {
            throw new RuntimeException("idxMunged != totalLength");
        }

        //prepend header
        byte[] finalData = RendezvousHelper.prependHeader(mungedData,
            this.tally);

        return finalData;
    }

    public synchronized int register(byte[] data, int desiredRank)
        throws RankTakenFaultType
    {

        if (isFull()) {
            throw new RuntimeException(
                "Precondition violation: rendezvous resource full");
        }
        //in this implementation we assume no rank will need more
        //than 4 bytes (Java int) to be represented.
        int rank = desiredRank;
        if (desiredRank == -1) {
            rank = determineRank();
        }
        else {
            if (this.getInternalBinaryData()[desiredRank] != null) {
                String errorMessage = i18n.getMessage(
                    Resources.RANK_TAKEN_ERROR, Integer.toString(rank));
                logger.error(errorMessage);
                RankTakenFaultType fault = (RankTakenFaultType)
                    FaultUtils.makeFault(RankTakenFaultType.class,
                                         errorMessage, null);
                throw fault;
            }
        }

        this.addData(rank, data);

        if (this.isFull()) {
            this.onBecomingFull();
        }
        return rank;
    }

    private int determineRank() {
        byte[][] internalData = this.getInternalBinaryData();
        //in most cases
        int rank = this.tally; //no if 1,2, .. , 4, 5 => tally == 4, rank cannot be 4

        if (internalData[rank] != null) { //slot taken: internal data not contiguous
            int max = this.getCapacity();
            for (int i = 0; i < max; i++) {
                if (internalData[i] == null) {
                    rank = i;
                    break;
                }
            }

        }
        //TODO more efficient code to keep track of/lookup available slots
        //(if 3,000 processes, algo of complexity n is not going to fly!
        //example: ordered stack to keep track of available slots
        return rank;
    }

    private void addData(int rank, byte[] data) {
        byte[][] contactData = this.getInternalBinaryData();
        contactData[rank] = data;
        tally++;
        if (logger.isDebugEnabled()) {
            logger.debug("number of registrants is " + tally);
            String dataString = "";
            for (int i = 0; i < data.length; i++) {
                dataString = dataString + contactData[rank][i] + " ";
            }
            logger.debug("Registered the following data: " + dataString);
        }
    }

    private synchronized void onBecomingFull() {

        if (logger.isDebugEnabled()) {
            logger.debug("Rendezvous completed.");
        }
        this.setRegistrantData(this.getMungedData());
        this.setRendezvousCompletedAndNotify(true);
    }

    public synchronized boolean isFull() {
        return (this.tally == this.getCapacity());
    }

    private synchronized byte[][] getInternalBinaryData() {
        int capacity = this.getCapacity();
        if (this.internalBinaryData == null) {
            this.internalBinaryData = new byte[capacity][];
        }
        return this.internalBinaryData;
    }

    private int tally = 0;
    private byte[][] internalBinaryData;

    //==========================================================================
    // WSRF resource features:

    //========= WS-BaseN features ==========

    public TopicList getTopicList() {
        return this.topicList;
    }

    private TopicList topicList;
    /*
    //========= Resource properties: dynamic implementationsb ===

    public byte[] getRegistrantData() {
        return this.getMungedData();
    }*/

    //========= Resource properties: helper accessors ===

    private int getCapacity() {
        return ((Integer)this.getResourcePropertySet().get(
            RendezvousConstants.RP_CAPACITY).get(0)).intValue();
                                               //assume no loss in conversion
    }

    private void setRendezvousCompleted(boolean value) {
        this.getResourcePropertySet().get(
            RendezvousConstants.RP_COMPLETED).set(0, new Boolean(value));
    }

    private void setRendezvousCompletedAndNotify(boolean isCompleted) {
        if (!isCompleted) {
            throw new RuntimeException("rendezvous set as not done? weird");
        }
        setRendezvousCompleted(isCompleted);
        //create notification message
        StateChangeNotificationMessageType message
            = new StateChangeNotificationMessageType();
        message.setRendezvousCompleted(isCompleted);
        message.setRegistrantData(this.getMungedData());

        StateChangeNotificationMessageWrapperType messageWrapper
            = new StateChangeNotificationMessageWrapperType();
        messageWrapper.setStateChangeNotificationMessage(message);

        //notify on the topic using the state change message
        LinkedList topicPath = new LinkedList();
        QName topicQName = RendezvousConstants.RP_COMPLETED;
        topicPath.add(topicQName);
        //TODO define a specific topic for this event ("data when completed")
        //right now it is "completed(/not)" in topic with payload adds data
        Topic completionTopic = this.getTopicList().getTopic(topicPath);
        logger.debug("Notifying of rendezvous completion to topic listeners");
        try {
            completionTopic.notify(messageWrapper);
        }
        catch (Exception e) {
            String errorMessage = "Could not notify " + topicQName +
                " listeners";
            throw new RuntimeException(errorMessage, e);
        }
    }

    private void setRegistrantData(byte[] value) {
        this.getResourcePropertySet().get(
            RendezvousConstants.RP_DATA).set(0, value);

        //TODO must do manual fire notif here if this RP can be a topic
        //(in future);
    }

    //==========================================================================
    // Misc:.

    private static final Log logger =
        LogFactory.getLog(RendezvousResourceImpl.class.getName());

    private static I18n i18n = I18n.getI18n(
        org.globus.rendezvous.service.utils.Resources.class.getName());

    private static PerformanceLog performanceLogger = new PerformanceLog(
        RendezvousResourceImpl.class.getName() + ".performance");

}
