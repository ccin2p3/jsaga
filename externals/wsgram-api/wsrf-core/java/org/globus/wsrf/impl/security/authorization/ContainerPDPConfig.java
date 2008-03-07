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

import org.apache.axis.MessageContext;

import org.globus.wsrf.impl.security.util.AuthUtil;

import org.globus.wsrf.config.ContainerConfig;

/**
 * PDP Configuration class that set/retrieves data from global
 * parameters in the deployment descriptor. It expects scoped
 * parameters of the format &quot;scope1-paramterName&quot;
 */
public class ContainerPDPConfig extends BasePDPConfig {

    public ContainerPDPConfig(String chain, MessageContext ctx) {
	this.chain = AuthUtil.substitutePDPNames(chain);
        this.msgCtx = ctx;
    }

    /**
     * Returns value of property identified by <i>name-property</i> 
     * stored in the global deployment descriptor. 
     *
     * @param name
     *        scope of the property 
     * @param property 
     *        name of the property
     */
    public Object getProperty(String name, String property) {
        return ContainerConfig.getConfig(this.msgCtx.getAxisEngine())
            .getOption(name + "-" + property);
    }

    /**
     * Sets the value of property identified by <i>name-property</i> in
     * the global deployment descriptor. <b> The property value has to
     * be a String</b>
     * 
     * @param name
     *        scope of the property 
     * @param property 
     *        name of the property
     * @param obj
     *        Value of the property, needs to be a String
     */
    public void setProperty(String name, String property, Object obj) {
        ContainerConfig.getConfig(this.msgCtx.getAxisEngine())
            .setOption(name + "-" + property, (String)obj);
    }
}
