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
import org.oasis.wsrf.properties.SetResourceProperties_PortType;
import org.oasis.wsrf.properties.SetResourceProperties_Element;
import org.oasis.wsrf.properties.DeleteType;

import org.globus.wsrf.utils.FaultHelper;

import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.CommandLine;

import javax.xml.namespace.QName;

import javax.xml.rpc.Stub;

public class DeleteProperty extends BaseClient {

    private static final String FOOTER =
        "Where:\n" +
        "  property is of form '{namespaceURI}localPart'\n";

    public static void main(String[] args) {
        DeleteProperty client = new DeleteProperty();
        client.setCustomUsage("property");
        client.setHelpFooter(FOOTER);

        QName rp = null;
        try {
            CommandLine line = client.parse(args);

            List options = line.getArgList();
            if (options == null || options.isEmpty()) {
                throw new ParseException("Expected property name");
            }
            rp = QName.valueOf((String)options.get(0));
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
            SetResourceProperties_PortType port =
                locator.getSetResourcePropertiesPort(client.getEPR());
	    client.setOptions((Stub)port);            
            DeleteType delete = new DeleteType();
            delete.setResourceProperty(rp);
            
            SetResourceProperties_Element request = 
                new SetResourceProperties_Element();
            request.setDelete(delete);

            port.setResourceProperties(request);

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
