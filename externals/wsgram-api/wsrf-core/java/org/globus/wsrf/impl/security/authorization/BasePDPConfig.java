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
package org.globus.wsrf.impl.security.authorization;

import org.globus.util.I18n;

import org.apache.axis.MessageContext;

import java.util.StringTokenizer;

import org.globus.wsrf.security.authorization.PDPConfig;
import org.globus.wsrf.security.authorization.PDPConstants;

import org.globus.wsrf.impl.security.authorization.exceptions.ConfigException;

/**
 * Base class that implements the {@link PDPConfig PDPConfig} interface.
 */
public abstract class BasePDPConfig implements PDPConfig {

    protected static I18n i18n =
        I18n.getI18n(PDPConstants.RESOURCE,
                     BasePDPConfig.class.getClassLoader());

    protected String chain;
    protected MessageContext msgCtx;
    protected String serviceName;

    /**
     * Returns the value of the property idenitified by its scope and
     * name
     * 
     * @param name
     *         scope of the property
     * @param property
     *        name of the property
     */
    public abstract Object getProperty(String name, String property);

    /**
     * Sets a property value
     *
     * @param name
     *         scope of the property
     * @param property
     *        name of the property
     * @param value
     *        value of property 
     */
    public abstract void setProperty(String name, String property,
                                     Object value);

    /**
     * Returns an array of <code>InterceptorConfig</code> objects,
     * configured as a list of scoped strings, separated by
     * space. Each interceptor name is expected to be preceeded by a
     * scope string and a semi colon (Eg: scope1:Interceptor1). Each
     * interceptor is separated by space
     * (Eg: scope1:Interceptor1 scope2:Interceptor2)
     */
    public InterceptorConfig[] getInterceptors() throws ConfigException {
        if (this.chain == null) {
            return new InterceptorConfig[0];
        }
        StringTokenizer entries = new StringTokenizer(this.chain);
        int numOfTokens = entries.countTokens();
        InterceptorConfig[] config = new InterceptorConfig[numOfTokens];
        int i=0;
        while (entries.hasMoreTokens()) {
            String token = entries.nextToken();
            StringTokenizer scopedName =
                new StringTokenizer(token, ":");
            if (scopedName.countTokens() != 2) {
                throw new ConfigException(i18n.getMessage("noScope",
                                                          token));
            }
            config[i] = new InterceptorConfig(scopedName.nextToken(),
                                              scopedName.nextToken());
            i++;
        }
        return config;
    }
}
