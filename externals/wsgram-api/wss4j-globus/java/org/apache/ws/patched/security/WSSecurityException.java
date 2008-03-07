/*
 * Copyright  2003-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.ws.patched.security;

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Exception class for WS-Security.
 * <p/>
 *
 * @author Davanum Srinivas (dims@yahoo.com).
 */
public class WSSecurityException extends RemoteException {
    public static final int FAILURE = 0;
    public static final int UNSUPPORTED_SECURITY_TOKEN = 1;
    public static final int UNSUPPORTED_ALGORITHM = 2;
    public static final int INVALID_SECURITY = 3;
    public static final int INVALID_SECURITY_TOKEN = 4;
    public static final int FAILED_AUTHENTICATION = 5;
    public static final int FAILED_CHECK = 6;
    public static final int SECURITY_TOKEN_UNAVAILABLE = 7;
    public static final int FAILED_ENC_DEC = 8;
    public static final int FAILED_SIGNATURE = 9;
    private static ResourceBundle resources;

    static {
        try {
            resources = ResourceBundle.getBundle("org.apache.ws.security.errors");
        } catch (MissingResourceException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private int errorCode;

    /**
     * Constructor.
     * <p/>
     *
     * @param errorCode
     * @param msgId
     * @param args
     * @param exception
     */
    public WSSecurityException(int errorCode, String msgId, Object[] args, Throwable exception) {
        super(getMessage(errorCode, msgId, args), exception);
        this.errorCode = errorCode;
    }

    /**
     * Constructor.
     * <p/>
     *
     * @param errorCode
     * @param msgId
     * @param args
     */
    public WSSecurityException(int errorCode, String msgId, Object[] args) {
        super(getMessage(errorCode, msgId, args));
        this.errorCode = errorCode;
    }

    /**
     * Constructor.
     * <p/>
     *
     * @param errorCode
     * @param msgId
     */
    public WSSecurityException(int errorCode, String msgId) {
        this(errorCode, msgId, null);
    }

    /**
     * Constructor.
     * <p/>
     *
     * @param errorCode
     */
    public WSSecurityException(int errorCode) {
        this(errorCode, null, null);
    }

    /**
     * Get the error code.
     * <p/>
     *
     * @return error code of this exception See values above.
     */
    public int getErrorCode() {
        return this.errorCode;
    }

    /**
     * get the message from resource bundle.
     * <p/>
     *
     * @param errorCode
     * @param msgId
     * @param args
     * @return the message translated from the property (message) file.
     */
    private static String getMessage(int errorCode, String msgId, Object[] args) {
        String msg = null;
        try {
            msg = resources.getString(String.valueOf(errorCode));
            if (msgId != null) {
                return msg += (" (" + MessageFormat.format(resources.getString(msgId), args) + ")");
            }
        } catch (MissingResourceException e) {
            throw new RuntimeException("Undefined '" + msgId + "' resource property");
        }
        return msg;
    }
}
