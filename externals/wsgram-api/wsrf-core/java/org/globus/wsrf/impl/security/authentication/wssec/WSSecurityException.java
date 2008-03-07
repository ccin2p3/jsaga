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

import java.rmi.RemoteException;

import java.text.MessageFormat;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class WSSecurityException extends RemoteException {
    // this is a generic error
    public static final int FAILURE = 0;
    public static final int UNSUPPORTED_SECURITY_TOKEN = 1;
    public static final int UNSUPPORTED_ALGORITHM = 2;
    public static final int INVALID_SECURITY = 3;
    public static final int INVALID_SECURITY_TOKEN = 4;
    public static final int FAILED_AUTHENTICATION = 5;
    public static final int FAILED_CHECK = 6;
    public static final int SECURITY_TOKEN_UNAVAILABLE = 7;
    public static final int MESSAGE_EXPIRED = 8;
    private static ResourceBundle resources;

    static {
        try {
            resources =
                ResourceBundle.getBundle(
                    "org.globus.wsrf.impl.security.authentication.wssec.errors"
                );
        } catch (MissingResourceException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private int errorCode;

    public WSSecurityException(
        int errorCode,
        String msgId,
        Object[] args,
        Throwable exception
    ) {
        super(getMessage(errorCode, msgId, args), exception);
        this.errorCode = errorCode;
    }

    public WSSecurityException(
        int errorCode,
        String msgId,
        Object[] args
    ) {
        super(getMessage(errorCode, msgId, args));
        this.errorCode = errorCode;
    }

    public WSSecurityException(
        int errorCode,
        String msgId
    ) {
        this(errorCode, msgId, null);
    }

    public WSSecurityException(int errorCode) {
        this(errorCode, null, null);
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    private static String getMessage(int errorCode, String msgId,
                                     Object[] args) {
        String msg = null;

        try {
            msg = resources.getString(String.valueOf(errorCode));

            if (msgId != null) {
                return msg += (" (" +
                               MessageFormat.format(resources.getString(msgId),
                                                    args) + ")");
            }
        } catch (MissingResourceException e) {
            throw new RuntimeException("Undefined '" + msgId
                                       + "' resource property");
        }

        return msg;
    }
}
