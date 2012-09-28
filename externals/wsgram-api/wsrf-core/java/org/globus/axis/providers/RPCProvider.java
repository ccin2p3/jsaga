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
package org.globus.axis.providers;

import java.util.Map;

import javax.xml.rpc.holders.IntHolder;
import javax.xml.rpc.server.ServiceLifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.Handler;
import org.apache.axis.MessageContext;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.constants.Scope;

import java.lang.reflect.Method;

import javax.security.auth.Subject;

import org.globus.gsi.gssapi.jaas.JaasSubject;

import org.globus.axis.description.ServiceDescUtil;

import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

import org.globus.wsrf.impl.security.authentication.Constants;

public class RPCProvider extends org.apache.axis.providers.java.RPCProvider {

    private static Log logger =
        LogFactory.getLog(RPCProvider.class.getName());

    /**
     */
    public Object getServiceObject (MessageContext msgContext,
                                    Handler service,
                                    String clsName,
                                    IntHolder scopeHolder)
        throws Exception {
        Map mapping = (Map)service.getOption(ServiceDescUtil.PROVIDER_MAPPING);
        if (mapping == null) {
            return super.getServiceObject(msgContext, service,
                                          clsName, scopeHolder);
        }
        OperationDesc operation = msgContext.getOperation();
        if (operation == null) {
            return super.getServiceObject(msgContext, service,
                                          clsName, scopeHolder);
        }
        Object provider = mapping.get(operation);
        if (provider == null) {
            return super.getServiceObject(msgContext, service,
                                          clsName, scopeHolder);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking '" + operation.getName() +
                         "' operation on " + provider);
        }

        Scope scope = Scope.getScope((String)service.getOption(OPTION_SCOPE),
                                     Scope.DEFAULT);
        scopeHolder.value = scope.getValue();
        if (scope == Scope.APPLICATION) {
            return provider;
        } else {
            // must be REQUEST scope
            return getNewServiceInstance(msgContext, (Class)provider);
        }
    }

    protected Object invokeMethod(MessageContext msgContext, Method method,
                                  Object obj, Object[] argValues)
        throws Exception {

        Subject subject =
            (Subject) msgContext.getProperty(Constants.INVOCATION_SUBJECT);

        if (subject == null) {
            return invokeMethodSub(msgContext, method, obj, argValues);
        } else {
            PrivilegedExceptionAction action =
                new PrivilegedInvokeMethodAction(this, msgContext, method, obj,
                                                 argValues);

            try {
                return JaasSubject.doAs(subject, action);
            } catch (PrivilegedActionException e) {
                throw e.getException();
            }
        }
    }

    protected Object invokeMethodSub(MessageContext msgContext, Method method,
                                     Object obj, Object[] argValues)
        throws Exception {
        return super.invokeMethod(msgContext, method, obj, argValues);
    }

    
    public static Object getNewServiceInstance(MessageContext msgCtx,
                                               Class serviceClass)
        throws Exception {
        // this should match what's done in JavaProvider.getNewServiceObject()
        Object service = serviceClass.newInstance();
        if (service instanceof ServiceLifecycle) {
            Object ctx = msgCtx.getProperty(
                org.apache.axis.Constants.MC_SERVLET_ENDPOINT_CONTEXT);
            ((ServiceLifecycle)service).init(ctx);
        }
        return service;
    }

}
