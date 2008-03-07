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
package org.globus.wsrf.impl.security.authentication.wssec;

import org.apache.axis.utils.XMLUtils;

import javax.xml.namespace.QName;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.Properties;

public class WSSecurityFault {
    public static final QName FAILURE =
        new QName(WSConstants.WSSE_NS, "General");
    public static final QName UNSUPPORTED_SECURITY_TOKEN =
        new QName(WSConstants.WSSE_NS, "UnsupportedSecurityToken");
    public static final QName UNSUPPORTED_ALGORITHM =
        new QName(WSConstants.WSSE_NS, "UnsupportedAlgorithm");
    public static final QName INVALID_SECURITY =
        new QName(WSConstants.WSSE_NS, "InvalidSecurity");
    public static final QName INVALID_SECURITY_TOKEN =
        new QName(WSConstants.WSSE_NS, "InvalidSecurityToken");
    public static final QName FAILED_AUTHENTICATION =
        new QName(WSConstants.WSSE_NS, "FailedAuthentication");
    public static final QName FAILED_CHECK =
        new QName(WSConstants.WSSE_NS, "FailedCheck");
    public static final QName SECURITY_TOKEN_UNAVAILABLE =
        new QName(WSConstants.WSSE_NS, "SecurityTokenUnavailable");
    private static Properties mapping = new Properties();

    static {
        register(WSSecurityException.FAILURE, FAILURE);
        register(
            WSSecurityException.UNSUPPORTED_SECURITY_TOKEN,
            UNSUPPORTED_SECURITY_TOKEN
        );
        register(
            WSSecurityException.UNSUPPORTED_ALGORITHM, UNSUPPORTED_ALGORITHM
        );
        register(WSSecurityException.INVALID_SECURITY, INVALID_SECURITY);
        register(
            WSSecurityException.INVALID_SECURITY_TOKEN, INVALID_SECURITY_TOKEN
        );
        register(
            WSSecurityException.FAILED_AUTHENTICATION, FAILED_AUTHENTICATION
        );
        register(WSSecurityException.FAILED_CHECK, FAILED_CHECK);
        register(
            WSSecurityException.SECURITY_TOKEN_UNAVAILABLE,
            SECURITY_TOKEN_UNAVAILABLE
        );
    }

    private WSSecurityFault() {
    }

    public static SOAPFaultException makeFault(Exception exception) {
        QName faultCode = null;
        String faultString = exception.getMessage();
        String faultActor = null;
        Detail detail = null;

        // fault code
        if (exception instanceof WSSecurityException) {
            faultCode =
                getFaultCode(((WSSecurityException) exception).getErrorCode());
        }

        if (faultCode == null) {
            faultCode = FAILURE;
        }

        // detail
        try {
            SOAPFactory factory = SOAPFactory.newInstance();
            detail = factory.createDetail();

            // TODO: pick better name?
            Name nm =
                factory.createName(
                    "stackTrace", null, "http://xml.apache.org/axis/"
                );
            DetailEntry entry = detail.addDetailEntry(nm);
            entry.addTextNode(getStack(exception));
        } catch (SOAPException ee) {
            // FIXME: ????
        }

        return new SOAPFaultException(
            faultCode, faultString, faultActor, detail
        );
    }

    public static QName getFaultCode(int errorCode) {
        return (QName) mapping.get(String.valueOf(errorCode));
    }

    private static void register(
        int errorCode,
        QName error
    ) {
        mapping.put(String.valueOf(errorCode), error);
    }

    // TODO: factor out
    private static String getStack(Exception e) {
        StringWriter errorWriter = new StringWriter();
        PrintWriter out = new PrintWriter(errorWriter);
        e.printStackTrace(out);
        out.close();

        return XMLUtils.xmlEncodeString(errorWriter.toString());
    }
}
