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
package org.globus.wsrf.client;

import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

import org.oasis.wsn.WSBaseNotificationServiceAddressingLocator;
import org.oasis.wsn.NotificationProducer;
import org.oasis.wsn.TopicExpressionType;
import org.oasis.wsrf.properties.ResourcePropertyValueChangeNotificationType;

import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import org.apache.axis.message.addressing.EndpointReferenceType;

import org.globus.wsrf.NotifyCallback;
import org.globus.wsrf.NotificationConsumerManager;
import org.globus.wsrf.WSNConstants;
import org.globus.wsrf.utils.AnyHelper;
import org.globus.wsrf.utils.XmlUtils;
import org.globus.wsrf.utils.FaultHelper;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.core.notification.ResourcePropertyValueChangeNotificationElementType;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;

import javax.xml.rpc.Stub;

import org.globus.wsrf.impl.security.descriptor.ResourceSecurityConfig;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;

// add support for subscribing to ResourceDestructionNotification?
public class Subscribe extends BaseClient implements NotifyCallback {

    private static final QName NAME = 
        new QName("", "SubscriptionReference");

    private static final Option SUB_EPR_FILE =
        OptionBuilder.withArgName( "file" )
        .hasArg()
        .withDescription("Save subscription EPR")
        .withLongOpt("subEpr")
        .create("b");

    private static final Option RES_DESC_FILE =
        OptionBuilder.withArgName( "file" )
        .hasArg()
        .withDescription("Resource descriptor file for consumer security")
        .withLongOpt("resDescFile")
        .create("r");

    public Subscribe() {
        super();
        options.addOption(SUB_EPR_FILE);
        options.addOption(RES_DESC_FILE);
    }

    public static void main(String[] args) {
	String resDescFilename = null;
        Subscribe client = new Subscribe();
        client.setCustomUsage("topic");
        CommandLine line = null;

        QName topicQName = null;

        try {
            line = client.parse(args);
            
            List options = line.getArgList();
            if (options == null || options.isEmpty()) {
                throw new ParseException("topic name expected");
            }
            topicQName = QName.valueOf((String)options.get(0));
        } catch(ParseException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(COMMAND_LINE_ERROR);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(COMMAND_LINE_ERROR);
        }

        WSBaseNotificationServiceAddressingLocator locator =
            new WSBaseNotificationServiceAddressingLocator();
        
        NotificationConsumerManager consumer = null;

        int exitCode = 0;

        try {
            NotificationProducer port = 
                locator.getNotificationProducerPort(client.getEPR());
	    client.setOptions((Stub)port);

            consumer = NotificationConsumerManager.getInstance();
            consumer.startListening();

            if (line.hasOption("r")) {
                resDescFilename = line.getOptionValue("r");
            }

	    ResourceSecurityDescriptor resDesc = null;
	    if (resDescFilename != null) {
		ResourceSecurityConfig config = 
		    new ResourceSecurityConfig(resDescFilename);
		config.init();
		resDesc = config.getSecurityDescriptor();
	    }

            EndpointReferenceType consumerEPR =
                consumer.createNotificationConsumer(client, resDesc);

            TopicExpressionType topicExpression = new TopicExpressionType();
            topicExpression.setDialect(WSNConstants.SIMPLE_TOPIC_DIALECT);
            topicExpression.setValue(topicQName);

            org.oasis.wsn.Subscribe request = 
                new org.oasis.wsn.Subscribe();
            request.setUseNotify(Boolean.TRUE);
            request.setConsumerReference(consumerEPR);
            request.setTopicExpression(topicExpression);

            org.oasis.wsn.SubscribeResponse response = 
                port.subscribe(request);

            System.out.println("Subscription successful");

            EndpointReferenceType epr = response.getSubscriptionReference();
            System.out.println(epr);
            if (line.hasOption("b")) {
                FileWriter out = null;
                try {
                    out = new FileWriter(line.getOptionValue("b"));
                    ObjectSerializer.serialize(out, epr, NAME);
                    out.write('\n');
                } catch (IOException e) {
                    System.err.println("Failed to write subscription epr: " +
                                       e.getMessage());
                } finally {
                    if (out != null) {
                        try { out.close(); } catch (Exception ee) {}
                    }
                }
            }

            synchronized(request) {
                request.wait();
            }

        } catch(Exception e) {
            if (client.isDebugMode()) {
                FaultHelper.printStackTrace(e);
            } else {
                System.err.println("Error: " + FaultHelper.getMessage(e));
            }
            exitCode = APPLICATION_ERROR;
        } finally {
            if (consumer != null) {
                try { consumer.stopListening(); } catch (Exception ee) {}
            }
        }

        System.exit(exitCode);
    }

    public void deliver(List topicPath,
                        EndpointReferenceType producer,
                        Object message) {
        if (message instanceof ResourcePropertyValueChangeNotificationElementType) {
            ResourcePropertyValueChangeNotificationType changeMessage =
                ((ResourcePropertyValueChangeNotificationElementType) message).
            getResourcePropertyValueChangeNotification();
            
            System.out.println("Received:");
            try {
                System.out.println(AnyHelper.toSingleString(changeMessage.getNewValue()));
            } catch (Exception e) {
                System.err.println("Error converting: " + e.getMessage());
            }
        } else if (message instanceof Element) {
             System.out.println("Received:");
             System.out.println(XmlUtils.toString( (Element)message ));
        } else {
            System.out.println("Received message of type: " + 
                               message.getClass().getName());
        }
    }
    
}
