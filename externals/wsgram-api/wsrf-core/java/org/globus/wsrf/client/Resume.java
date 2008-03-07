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

import org.oasis.wsn.WSBaseNotificationServiceAddressingLocator;
import org.oasis.wsn.SubscriptionManager;
import org.oasis.wsn.ResumeSubscription;

import org.globus.wsrf.utils.FaultHelper;

import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.CommandLine;

import javax.xml.rpc.Stub;

public class Resume extends BaseClient {

    public static void main(String[] args) {
        Resume client = new Resume();

        try {
            CommandLine line = client.parse(args);
        } catch(ParseException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(COMMAND_LINE_ERROR);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(COMMAND_LINE_ERROR);
        }

        WSBaseNotificationServiceAddressingLocator locator = 
            new WSBaseNotificationServiceAddressingLocator();

        try {
            SubscriptionManager port = 
                locator.getSubscriptionManagerPort(client.getEPR());
	    client.setOptions((Stub)port);
            
            port.resumeSubscription(new ResumeSubscription());
        } catch(Exception e) {
            if (client.isDebugMode()) {
                FaultHelper.printStackTrace(e);
            } else {
                System.err.println("Error: " + FaultHelper.getMessage(e));
            }
            System.exit(APPLICATION_ERROR);
        }
    }
    
}
