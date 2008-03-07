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
package org.globus.wsrf.container;

import java.util.List;
import java.util.Properties;

import javax.xml.rpc.Stub;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import org.globus.wsrf.client.BaseClient;
import org.globus.wsrf.core.shutdown.ShutdownPortType;
import org.globus.wsrf.core.shutdown.service.ShutdownServiceAddressingLocator;
import org.globus.wsrf.utils.FaultHelper;

/**
 * Client for shutdown service implementation.
 */
public class ShutdownClient extends BaseClient {

    private static final String FOOTER =
        "Where:\n" +
        "  'soft' - lets threads die naturally (default)\n" +
        "  'hard' - forces JVM shutdown\n";

    public static void main(String [] args) throws Exception {
        Properties defaultOptions = new Properties();
        // default service address
        defaultOptions.put(BaseClient.SERVICE_URL.getOpt(),
                           "https://localhost:8443/wsrf/services/ShutdownService");
        // GSI Secure Msg (signature)
        defaultOptions.put(BaseClient.PROTECTION.getOpt(), "sig");

        // no authorization
        defaultOptions.put(BaseClient.AUTHZ.getOpt(),
                           "none");

        ShutdownClient client = new ShutdownClient();
        client.setCustomUsage("[soft | hard]");
        client.setHelpFooter(FOOTER);

        boolean hard = false;

        try {
            CommandLine line = client.parse(args, defaultOptions);
            List options = line.getArgList();
            if (options == null || options.isEmpty()) {
                // do nothing assume defaults
            } else if (options.size() == 1) {
                String arg = (String)options.get(0);
                if (arg.equalsIgnoreCase("soft")) {
                    hard = false;
                } else if (arg.equalsIgnoreCase("hard")) {
                    hard = true;
                } else {
                    throw new Exception("Invalid argument: " + arg);
                }
            }
        } catch(ParseException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(COMMAND_LINE_ERROR);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(COMMAND_LINE_ERROR);
        }

        ShutdownServiceAddressingLocator locator =
            new ShutdownServiceAddressingLocator();

        try {
            ShutdownPortType port =
                locator.getShutdownPortTypePort(client.getEPR());
            client.setOptions((Stub)port);

            port.shutdown(hard);
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
