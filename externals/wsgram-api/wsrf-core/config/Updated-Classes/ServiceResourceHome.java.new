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
package org.globus.wsrf.impl;

import org.apache.axis.MessageContext;
import org.apache.axis.AxisEngine;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.java.JavaProvider;
import org.apache.axis.constants.Scope;
import org.apache.axis.utils.cache.ClassCache;
import org.apache.axis.utils.cache.JavaClass;

import org.globus.util.I18n;
import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.jndi.Initializable;
import org.globus.wsrf.utils.Resources;
import org.globus.axis.providers.RPCProvider;

/**
 * A ResourceHome that always returns its corresponding service object,
 * in response to a null key. Note that the service MUST have been deployed
 * with application scope (for example, with wsdl parameter <code>scope</code>
 * set to value <code>Application</code>); otherwise there is no meaningful
 * single object for this class to return. Also MUST be deployed with
 * <code>loadOnStartup</code> parameter set to <code>true</code>.
 */
public class ServiceResourceHome
    extends SingletonResourceHome
    implements Initializable {

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    private Resource service;

    public synchronized void initialize() throws Exception {
        if (this.service != null) {
            return;
        }

        /*
         * Since must be deployed with loadOnStartup the MessageContext
         * will be set property at initialize time.
         */
        MessageContext msgContext = MessageContext.getCurrentContext();
        if (msgContext == null) {
            throw new Exception(i18n.getMessage("noMsgContext"));
        }
        String targetService = msgContext.getTargetService();
        if (targetService == null) {
            throw new Exception(i18n.getMessage("noTargetServiceSet"));
        }
        SOAPService service = msgContext.getService();
        if (service == null) {
            throw new Exception(i18n.getMessage("noServiceSet"));
        }

        Scope scope = Scope.getScope(
                         (String)service.getOption(JavaProvider.OPTION_SCOPE),
                         Scope.DEFAULT);
        if (scope != Scope.APPLICATION) {
            throw new Exception(i18n.getMessage("applicationScopeNeeded",
                                                targetService));
        }

        AxisEngine engine = msgContext.getAxisEngine();
        Object serviceInstance = engine.getApplicationSession().get(
            targetService);

        if (serviceInstance == null) {

            String clsName =
                (String)service.getOption(JavaProvider.OPTION_CLASSNAME);

            ClassLoader cl = msgContext.getClassLoader();
            ClassCache cache = engine.getClassCache();
            JavaClass  jc = cache.lookup(clsName, cl);

            if (!Resource.class.isAssignableFrom(jc.getJavaClass())) {
                throw new Exception(i18n.getMessage(
                    "invalidResourceType", jc.getJavaClass().getName()));
            }


            serviceInstance = RPCProvider.getNewServiceInstance(
                msgContext, jc.getJavaClass());
            engine.getApplicationSession().set(targetService,
                                               serviceInstance);
        }
        this.service = (Resource)serviceInstance;
    }

    /**
     * Finds the service object associated with this resource home.
     */
    protected Resource findSingleton()
        throws ResourceException {
        return this.service;
    }
}

