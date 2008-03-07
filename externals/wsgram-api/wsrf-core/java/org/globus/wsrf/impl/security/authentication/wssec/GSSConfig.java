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

import org.apache.xml.security.Init;

import org.globus.wsrf.providers.JCEProvider;

public class GSSConfig
{
    public static final String XMLSEC_CONFIG_PROPERTY =
        "org.apache.xml.security.resource.config";
    public static final String XMLSEC_CONFIG =
        "/org/globus/wsrf/impl/security/authentication/globus-xmlsec-config.xml";
    private static boolean alreadyInitialized = false;

    /**
     * Initializes xmlsec
     */
    public static void init()
    {
        if(alreadyInitialized)
        {
            return;
        }
        synchronized(GSSConfig.class)
        {
            if(alreadyInitialized)
            {
                return;
            }
            System.setProperty(XMLSEC_CONFIG_PROPERTY,
                               XMLSEC_CONFIG);
            Init.init();
            JCEProvider.addProvider();
            alreadyInitialized = true;
        }
    }
}
