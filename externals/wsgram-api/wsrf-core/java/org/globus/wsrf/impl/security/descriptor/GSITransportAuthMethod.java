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
package org.globus.wsrf.impl.security.descriptor;

import javax.xml.rpc.handler.MessageContext;

import org.globus.util.I18n;
import org.globus.wsrf.impl.security.authentication.Constants;

/**
 * Represents GSI Secure Transport
 */
public class GSITransportAuthMethod implements AuthMethod
{
    private static I18n i18n = I18n.getI18n(SecurityDescriptor.RESOURCE);
    static final int BOTH_TYPE = 0;
    static final int INTEGRITY_TYPE = 1;
    static final int PRIVACY_TYPE = 2;
    private int type;

    public static final GSITransportAuthMethod BOTH =
        new GSITransportAuthMethod(BOTH_TYPE);
    public static final GSITransportAuthMethod PRIVACY =
        new GSITransportAuthMethod(PRIVACY_TYPE);
    public static final GSITransportAuthMethod INTEGRITY =
        new GSITransportAuthMethod(INTEGRITY_TYPE);

    public GSITransportAuthMethod(int type)
    {
        this.type = type;
    }

    public boolean isAuthenticated(MessageContext ctx)
    {
        switch(this.type)
        {
        case BOTH_TYPE: 
                return (Constants.SIGNATURE.equals(
                              ctx.getProperty(Constants.GSI_TRANSPORT)) ||
                        Constants.ENCRYPTION.equals(
                              ctx.getProperty(Constants.GSI_TRANSPORT)));

        case INTEGRITY_TYPE:
                return (Constants.SIGNATURE.equals(
                    ctx.getProperty(Constants.GSI_TRANSPORT)));
                
        case PRIVACY_TYPE:
            return (Constants.ENCRYPTION.equals(
                    ctx.getProperty(Constants.GSI_TRANSPORT)));

        default:
                return false;
        }
    }

    public String getName()
    {
        switch(this.type)
        {
            case BOTH_TYPE:
                return i18n.getMessage("gsiTransportMethod");

            case INTEGRITY_TYPE:
                return i18n.getMessage("gsiTransportMethodSig");

            case PRIVACY_TYPE:
                return i18n.getMessage("gsiTransportMethodEnc");

            default:
                return "GSI transport";
        }
    }
}
