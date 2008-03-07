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

import java.util.Calendar;
import java.util.TimeZone;
import java.util.List;
import java.text.SimpleDateFormat;

import org.oasis.wsrf.lifetime.WSResourceLifetimeServiceAddressingLocator;
import org.oasis.wsrf.lifetime.ScheduledResourceTermination;

import org.globus.wsrf.utils.FaultHelper;

import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import javax.xml.rpc.Stub;

public class SetTerminationTime extends BaseClient {

    private static final Option UTC_OPTION = 
        OptionBuilder.withDescription("Display time in UTC")
        .withLongOpt("utc")
        .create("u");

    public SetTerminationTime() {
        super();
        options.addOption(UTC_OPTION);
    }

    public static void main(String[] args) {
        SetTerminationTime client = new SetTerminationTime();
        client.setCustomUsage("seconds | 'infinity'");

        Calendar termTime = null;
        CommandLine line = null;
        try {
            line = client.parse(args);
            
            List options = line.getArgList();
            if (options == null || options.isEmpty()) {
                throw new ParseException("Expected timeout value");
            }
            String value = (String)options.get(0);
            if (!value.equalsIgnoreCase("infinity")) {
                termTime = Calendar.getInstance();
                termTime.add(Calendar.SECOND, Integer.parseInt(value));
            }
        } catch(ParseException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(COMMAND_LINE_ERROR);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(COMMAND_LINE_ERROR);
        }

        WSResourceLifetimeServiceAddressingLocator locator =
            new WSResourceLifetimeServiceAddressingLocator();

        try {
            ScheduledResourceTermination port = 
                locator.getScheduledResourceTerminationPort(client.getEPR());
	    client.setOptions((Stub)port);

            org.oasis.wsrf.lifetime.SetTerminationTime request = 
                new org.oasis.wsrf.lifetime.SetTerminationTime();
            request.setRequestedTerminationTime(termTime);
            
            org.oasis.wsrf.lifetime.SetTerminationTimeResponse response =
                port.setTerminationTime(request);

            Calendar newTermTime = response.getNewTerminationTime();

            SimpleDateFormat df = 
                new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

            if (line.hasOption("u")) {
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                System.out.println("set timizzone");
            }

            System.out.println("requested: " + 
                 ((termTime == null) ? 
                  "infinity" : df.format(termTime.getTime())));
            System.out.println("scheduled: " + 
                 ((newTermTime == null) ? 
                  "infinity" : df.format(newTermTime.getTime())));
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
