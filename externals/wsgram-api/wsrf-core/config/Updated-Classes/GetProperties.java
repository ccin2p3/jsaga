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

import org.oasis.wsrf.properties.WSResourcePropertiesServiceAddressingLocator;
import org.oasis.wsrf.properties.GetMultipleResourcePropertiesResponse;
import org.oasis.wsrf.properties.GetMultipleResourceProperties_Element;
import org.oasis.wsrf.properties.GetMultipleResourceProperties_PortType;

import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.CommandLine;

import org.globus.wsrf.utils.AnyHelper;
import org.globus.wsrf.utils.FaultHelper;

import javax.xml.namespace.QName;

import javax.xml.rpc.Stub;

public class GetProperties extends BaseClient {

    private static final String FOOTER =
        "Where:\n" +
        "  propertyN is of form '{namespaceURI}localPart'\n";

    public static void main(String[] args) {
        GetProperties client = new GetProperties();
        client.setCustomUsage("property1 property2 ...");
        client.setHelpFooter(FOOTER);

        QName [] rps = null;

        try {
            CommandLine line = client.parse(args);

            List options = line.getArgList();
            if (options == null || options.isEmpty()) {
                throw new ParseException("Expected resource property name");
            }
            rps = new QName[options.size()];
            for (int i=0;i<options.size();i++) {
                rps[i] = QName.valueOf((String)options.get(i));
            }
        } catch(ParseException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(COMMAND_LINE_ERROR);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(COMMAND_LINE_ERROR);
        }

        WSResourcePropertiesServiceAddressingLocator locator =
            new WSResourcePropertiesServiceAddressingLocator();

        try {
            GetMultipleResourceProperties_PortType port =
                locator.getGetMultipleResourcePropertiesPort(client.getEPR());
	    client.setOptions((Stub)port);

            GetMultipleResourceProperties_Element request =
                new GetMultipleResourceProperties_Element();
            request.setResourceProperty(rps);

            GetMultipleResourcePropertiesResponse response =
                port.getMultipleResourceProperties(request);

            System.out.println(AnyHelper.toSingleString(response));

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
