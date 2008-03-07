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

import org.globus.wsrf.utils.ContextUtils;

import org.apache.axis.MessageContext;

import org.globus.wsrf.impl.security.util.AuthUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is used to store configuration information of interceptors
 * in a ServiceProperties object associated with a service.
 * For more information about the API see
 * {@link org.globus.wsrf.security.authorization.PDPConfig PDPConfig}.
 */
public class ServicePropertiesPDPConfig extends BasePDPConfig {

    private static Log logger =
        LogFactory.getLog(ServicePropertiesPDPConfig.class.getName());

    /**
     * Constructor
     *
     * @param msgCtx
     *        message context, typically associated with current thread
     * @param _serviceName
     *        name of service with which these properties are
     *        associated
     * @param _chain
     *        Chain of PDP and PIP names
     */
    public ServicePropertiesPDPConfig(MessageContext msgCtx,
                                      String _serviceName,
                                      String _chain) {
        this.serviceName = _serviceName;
        this.chain = AuthUtil.substitutePDPNames(_chain);
        this.msgCtx = msgCtx;
    }

    /**
     * Returns value of property identified by <i>name-property</i> 
     * stored in the service's deployment descriptor. 
     *
     * @param name
     *        scope of the property 
     * @param property 
     *        name of the property
     */
    public Object getProperty(String name, String property) {
        Object obj = null;
        try {
            obj = ContextUtils.getServiceProperty(this.msgCtx,
                                                  this.serviceName,
                                                  name + "-" + property);
        } catch (Exception exp) {
            logger.debug("", exp);
        }
        return obj;
    }

    /**
     * Sets the value of property identified by <i>name-property</i> in
     * the global deployment descriptor.
     * 
     * @param name
     *        scope of the property 
     * @param property 
     *        name of the property
     * @param obj
     *        Value of the property
     */
    public void setProperty(String name, String property, Object obj) {
        try {
            ContextUtils.setServiceProperty(this.msgCtx, this.serviceName,
                                            name + "-" + property, obj);
        } catch (Exception exp) {
            logger.debug("", exp);
        }
    }
}
