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
import org.oasis.wsrf.properties.QueryResourceProperties_Element;
import org.oasis.wsrf.properties.QueryResourcePropertiesResponse;
import org.oasis.wsrf.properties.QueryExpressionType;
import org.oasis.wsrf.properties.QueryResourceProperties_PortType;

import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.CommandLine;

import org.globus.wsrf.WSRFConstants;
import org.globus.wsrf.utils.AnyHelper;
import org.globus.wsrf.utils.FaultHelper;

import javax.xml.rpc.Stub;

public class Query extends BaseClient {

    private static final String FOOTER =
        "Where:\n" +
        "  [expression] - query expression\n" +
        "  [dialect]    - query dialect\n";

    public static void main(String[] args) {
        Query client = new Query();
        client.setCustomUsage("[expression] [dialect]");
        client.setHelpFooter(FOOTER);

        String dialect = WSRFConstants.XPATH_1_DIALECT;
        String expression = "/";

        try {
            CommandLine line = client.parse(args);

            List options = line.getArgList();
            if (options == null || options.isEmpty()) {
                // do nothing assume defaults
            } else if (options.size() == 1) {
                expression =(String)options.get(0);
                // default dialect
            } else {
                expression =(String)options.get(0);
                dialect = (String)options.get(1);
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
            QueryExpressionType query = new QueryExpressionType();
            query.setDialect(dialect);
            query.setValue(expression);
            
            QueryResourceProperties_PortType port =
                locator.getQueryResourcePropertiesPort(client.getEPR());
	    client.setOptions((Stub)port);

            QueryResourceProperties_Element request
                = new QueryResourceProperties_Element();
            request.setQueryExpression(query);

            QueryResourcePropertiesResponse response =
                port.queryResourceProperties(request);
            
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
