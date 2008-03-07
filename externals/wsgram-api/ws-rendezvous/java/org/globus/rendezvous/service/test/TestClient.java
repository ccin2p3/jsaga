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
package org.globus.rendezvous.service.test;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.oasis.wsn.Subscribe;
import org.oasis.wsn.TopicExpressionType;
import org.oasis.wsrf.properties.ResourcePropertyValueChangeNotificationType;
import org.oasis.wsrf.properties.GetResourcePropertyResponse;

import org.globus.axis.util.Util;
import org.globus.wsrf.impl.security.descriptor.GSITransportAuthMethod;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.globus.wsrf.impl.security.authorization.HostAuthorization;

import org.globus.wsrf.NotifyCallback;
import org.globus.wsrf.WSRFConstants;
import org.globus.wsrf.NotificationConsumerManager;
import org.globus.wsrf.core.notification.ResourcePropertyValueChangeNotificationElementType;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.utils.AddressingUtils;

import org.globus.rendezvous.client.RendezvousConstants;
import org.globus.rendezvous.client.RendezvousHelper;
import org.globus.rendezvous.client.CompletionNotifyCallbackImpl;
import org.globus.rendezvous.generated.CapacityReachedFaultType;
import org.globus.rendezvous.generated.RankTakenFaultType;
import org.globus.rendezvous.generated.RendezvousPortType;
import org.globus.rendezvous.generated.RendezvousFactoryPortType;
import org.globus.rendezvous.generated.CreateSync;
import org.globus.rendezvous.generated.CreateSyncResponse;
import org.globus.rendezvous.generated.RegisterInput;
import org.globus.rendezvous.generated.RegisterResponse;
import org.globus.rendezvous.generated.RendezvousResourceProperties;
import org.globus.rendezvous.generated.service.RendezvousServiceAddressingLocator;
import org.globus.rendezvous.generated.service.RendezvousFactoryServiceAddressingLocator;


public class TestClient {

    private RendezvousFactoryServiceAddressingLocator locator =
        new RendezvousFactoryServiceAddressingLocator();

    /**
     * Test data.
     */
    private interface TestApplicationData {
        static final byte[] BINARY_DATA_1 = {01, 02, 03};
        static final byte[] BINARY_DATA_EMPTY = new byte[0];
    }

    private static byte[] makeInputData(byte[] applicationData) {
        return RendezvousHelper.prependHeader(
        applicationData, applicationData.length);
    }

    /**
     *
     * @param applicationData byte[] the application-meaningful data to
     *        register with the bottom level rendezvous resource. It will
     *        be the same for all bottom-level registrant, which is not
     *        very realistic...
     *        (this is NOT the input data to the register() call!)
     */
    public TestClient(String hostName, byte[] applicationData) {
        this.hostName = hostName;
        this.applicationData = applicationData;
        this.inputData = makeInputData(this.applicationData);
    }

    private String getServiceURL() {
        return "https://" + this.hostName +
            ":8443/wsrf/services/RendezvousFactoryService";
        //TODO use ServiceURL object instead
    }

    public static void main(String[] args) throws Exception {
       String hostName = "localhost";
       if (args.length >= 1) {
           hostName = args[0];
       }
       executeTestSuite(hostName, TestApplicationData.BINARY_DATA_1);
       executeTestSuite(hostName, TestApplicationData.BINARY_DATA_EMPTY);
    }

    private static void executeTestSuite(String hostName,
                                         byte[] applicationData)
        throws Exception
    {

       new TestClient(hostName, applicationData).testRegistrationWhenCapacityIsZero();
       System.out.println("=================================================");

       new TestClient(hostName, applicationData).testRegistrationWithDesiredRank();
       System.out.println("=================================================");

       new TestClient(hostName, applicationData).testRegistrationWithTakenRank();
       System.out.println("=================================================");

       new TestClient(hostName, applicationData).testRegisterToCompletion();
       System.out.println("=================================================");
    }


    private EndpointReferenceType createRendezvous(int capacity) throws Exception {

        RendezvousFactoryPortType portFactory = getFactory();

        // Create rendezvous resource
        CreateSyncResponse createResponse = portFactory.createSync(
            new CreateSync(capacity));
        EndpointReferenceType epr = createResponse.getEndpointReference();
        return epr;
    }

    private RendezvousFactoryPortType getFactory() throws Exception {
        // endpoint + port = master service
        EndpointReferenceType endpoint = new EndpointReferenceType();
        String serviceURL = getServiceURL();
        System.out.println("Rendezvous(Factory) service: " + serviceURL);
        endpoint.setAddress(new Address(serviceURL));

        setSecurityTypeFromEndpoint(endpoint);

        RendezvousFactoryPortType portFactory =
            locator.getRendezvousFactoryPortTypePort(endpoint);

        setFactorySecurityProperties((Stub) portFactory);

        return portFactory;
    }


    public void testRegistrationWhenCapacityIsZero()
        throws Exception
    {
        System.out.println("Test registration when capacity is zero");

        EndpointReferenceType epr = createRendezvous(0);
        //test check: try to register

        boolean exceptionWasThrown = false;
        try {
            System.out.println(
                "Trying to register when capacity is zero. This should fail.");
            int rank = registerData(epr, makeInputData(this.applicationData));
        }
        catch (CapacityReachedFaultType e) {
            System.out.println(
                "Should see exception on server side for rendezvous full");
            System.out.println("Fault Description: " +
                               e.getDescription(0).get_value());
            exceptionWasThrown = true;
        }
        check(exceptionWasThrown);

    }

    public void testRegistrationWithDesiredRank() throws Exception {
        System.out.println("Test registration with desired rank");

        EndpointReferenceType epr = createRendezvous(10);

        int rank1 = registerData(epr, inputData, 0);
        check(rank1 == 0);
        int rank2 = registerData(epr, inputData, 1);
        check(rank2 == 1);
        //no 2
        int rank3 = registerData(epr, inputData, 3);
        check(rank3 == 3);
        int rank4 = registerData(epr, inputData, 5);
        //no 4
        check(rank4 == 5);
        int rank5 = registerData(epr, inputData); //no rank
        check(rank5 == 4, "rank was " + rank5 + " instead of 4"); //since rank count =4 was available
        int rank6 = registerData(epr, inputData); //no rank
        check(rank6 == 2); //since slot 2 was available
        int rank7 = registerData(epr, inputData); //no rank
        check(rank7 == 6); //since slot 6 must be next slot available
    }


    public void testRegistrationWithTakenRank() throws Exception {
        System.out.println("Test registration with already taken chosen rank");

        EndpointReferenceType epr = createRendezvous(10);

        final int RANK = 0;
        int rank1 = registerData(epr, inputData, RANK);
        check(rank1 == RANK);

        boolean exceptionWasThrown = false;
        try {
            int rank2 = registerData(epr, inputData, RANK);
        }
        catch (RankTakenFaultType e) {
            exceptionWasThrown = true;
            System.out.println(
                "Should see exception on server side for rank already taken");
            System.out.println("Fault Description: " +
                               e.getDescription(0).get_value());
        }
        check(exceptionWasThrown);
    }

    public void testRegisterToCompletion() throws Exception {
        System.out.println("TESTING OF register():");

        createRendezvousResources();

        int rank = 0;

        rank = registerData(eprSub1, inputData);
        check(rank == 0);
        rank = registerData(eprSub1, inputData);
        check(rank == 1);

        rank = registerData(eprSub2, inputData);
        check(rank == 0);
        rank = registerData(eprSub2, inputData);
        check(rank == 1);

        waitAndSee();
    }

    private static void check(boolean assertion) {
        check(assertion, null);
    }

    private static void check(boolean assertion, String messageIfFailure) {
        if (!assertion) {
            throw new RuntimeException(messageIfFailure);
        }
    }


    private void createRendezvousResources() throws Exception {

        RendezvousFactoryPortType factoryPort = getFactory();
        // Create top-level rendezvous resource
        CreateSyncResponse createResponse =
            factoryPort.createSync(new CreateSync(2));
        eprTop = createResponse.getEndpointReference();

        eprTop.getProperties().get_any()[0].getValue();
        RendezvousFactoryPortType portTop =
            locator.getRendezvousFactoryPortTypePort(eprTop);

        //susbscribe for notifications to top-level rendezvous
        invocationHelper.subscribeForCompletionNotifications(
            eprTop, new TopRendezvousCallback());

        // Create first sub-rendezvous resource
        createResponse = factoryPort.createSync(new CreateSync(2));
        eprSub1 = createResponse.getEndpointReference();
        //susbscribe for notifications to first sub-rendezvous
        invocationHelper.subscribeForCompletionNotifications(
            eprSub1, new SubRendezvousCallback("1"));

        // Create second sub-rendezvous resource
        createResponse = factoryPort.createSync(new CreateSync(2));
        eprSub2 = createResponse.getEndpointReference();
        //susbscribe for notifications to second sub-rendezvous
        invocationHelper.subscribeForCompletionNotifications(
            eprSub2, new SubRendezvousCallback("2"));

        GetResourcePropertyResponse getRPResponse =
            portTop.getResourceProperty(RendezvousConstants.RP_CAPACITY);
        System.out.println("I see a multijob capacity of: " +
                           getRPResponse.get_any()[0].getValue());
    }

    private int registerData(EndpointReferenceType endpoint,
                             byte[] data)
        throws Exception
    {
        return registerData(endpoint, data, RendezvousHelper.NO_DESIRED_RANK);
    }

    private int registerData(EndpointReferenceType endpoint,
                             byte[] data, int desiredRank)
        throws Exception
    {
        System.out.println("About to register data with rank " + desiredRank);

        int rank = this.invocationHelper.registerWithRendezvous(
            endpoint, data, desiredRank);

        System.out.println("Got rank " + rank);

        return rank;
    }

    private void waitAndSee() throws Exception {
        //Let the notification come in
        int timeout = 10000;
        if (!isDataReceived()) {
            //Let the notification come in
            System.out.println("Waiting for top-level notification for " +
                               timeout + " seconds...");
            Thread.sleep(timeout);
        }
        boolean dataReceived = isDataReceived();
        System.out.println("Has data for the whole rendezvous been received? "+
                           dataReceived);
        if (!dataReceived) {
            //let's pull completion state
            boolean allDone =
                this.invocationHelper.isRendezvousDone(this.eprTop);
            check(allDone,
                  "Test failed: global rendezvous has not completed after " +
                  timeout + " milliseconds of waiting time.");
        }

        //all done and data received

        //pull and test binary data:
        byte[] pulledData = this.invocationHelper.getRegistrantData(this.eprTop);

        if (dataReceived) {
            //compare pulled and notified data
            boolean sameDataInNotificationAndQuery = true;
            if (this.wholeData.length != pulledData.length) {
                sameDataInNotificationAndQuery = false;
            }
            else {
                for (int i = 0; i < wholeData.length; i++) {
                    if (wholeData[i] != pulledData[i]) {
                        sameDataInNotificationAndQuery = false;
                        break;
                    }
                }
            }
            check(sameDataInNotificationAndQuery,
                  "pulled registrant data is different than notified!");
        }

        //if this.wholeData != null then pulledData equals to this.wholeData

        //check all data is in correct format
        readWholeData2(pulledData);

    }

    private synchronized boolean isDataReceived() {
        return (this.wholeData != null);
    }

    private boolean isTopRendezvousDone() throws Exception {
        //pull status
        return this.invocationHelper.isRendezvousDone(this.eprTop);
    }

    private void readWholeData2(byte[] wholeData) {
        readItemData(0, wholeData, 2, new String[] {"process", "sub-job"});
    }

    private int readItemData(int offset,
                                    byte[] data,
                                    int depth, String[] itemNames) {

        System.out.println("depth = " + depth);
        byte[] header = RendezvousHelper.extractHeader(data, offset);
        offset += header.length + 1; //1 for SPACE
        int numberOfItems = RendezvousHelper.readCount(header);
        System.out.println("Number of items at this depth: " + numberOfItems);

        if (depth == 0) {
            int numberOfBytes = numberOfItems;
            System.out.print("\t" + itemNames[depth] + " data: ");

            for (int i = 0; i < numberOfBytes; i++) {
                byte dataByte = data[offset + i];
                System.out.print(dataByte + " ");
                check(dataByte == applicationData[i],
                      "Extracted and registered data bytes do not match");
            }
            System.out.println();
            offset += numberOfBytes;
        }
        else {
            int itemCount = 0;
            while (itemCount < numberOfItems) {
                System.out.println("reading " + itemNames[depth - 1] + " " +
                                   itemCount);
                offset = readItemData(offset, data, depth - 1, itemNames);
                itemCount++;
            }
        }

        return offset;

    }

    private void setFactorySecurityProperties(Stub stub)
    {
    }

    private void setSecurityTypeFromEndpoint(EndpointReferenceType epr)
      {
          if (logger.isDebugEnabled()) {
              logger.debug("Endpoint Address URL Scheme:\n"
                          + epr.getAddress().getScheme());
          }

          if (epr.getAddress().getScheme().equals("http"))
          {
              if (logger.isDebugEnabled()) {
                  logger.debug("using message-level security");
              }
          } else
          {
              if (logger.isDebugEnabled()) {
                  logger.debug("using transport-level security");
              }
              Util.registerTransport(); //this will be used to for rendezvous instances
          }
       }


    private class TopRendezvousCallback extends CompletionNotifyCallbackImpl
    {

        public TopRendezvousCallback() {
            super(null);
        }

        public void onRendezvousCompleted(byte[] bytes) {
            synchronized(System.out) {
                System.out.println(
                    "Notification: Top-level rendezvous completed");
                String byteString = "all bytes received: ";
                for (int i =0; i<bytes.length ;i++) {
                    byteString += bytes[i] + " ";
                }
                System.out.println(byteString);
                wholeData = bytes;
            }
        }

    } //end TopRendezvousCallback

    private class SubRendezvousCallback extends CompletionNotifyCallbackImpl
    {
        public SubRendezvousCallback(String id) {
            super(null);
            this.id = id;
        }

        public void onRendezvousCompleted(byte[] dataSet) {
            synchronized(System.out) {
                System.out.println("sub-rendezvous " + this.id +
                                   " has completed");
                //now register the entire data set for the sub
                int rank;

                try {
                    rank = registerData(eprTop, dataSet);
                }
                catch (Exception e) {
                    String errorMessage =
                        "could not register sub data with top rendezvous";
                    throw new RuntimeException(e);
                }
                System.out.println(
                    "Registered whole data set of sub-rendezvous " + this.id +
                    " to top-level rendezvous. Returned rank = " + rank);
            }
        }

        private String id;

    } //end SubRendezvousCallback

    private String hostName = "localhost";

    private byte[] applicationData;
    private byte[] inputData;

    private EndpointReferenceType eprTop;
    private byte[] wholeData;

    private EndpointReferenceType eprSub1;
    private EndpointReferenceType eprSub2;

    private RendezvousHelper invocationHelper = new RendezvousHelper();

    private static final Log logger =
        LogFactory.getLog(TestClient.class.getName());

}
