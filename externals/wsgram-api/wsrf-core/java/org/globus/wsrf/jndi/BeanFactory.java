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
package org.globus.wsrf.jndi;

import java.util.Hashtable;

import javax.naming.Name;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.axis.AxisEngine;
import org.apache.axis.MessageContext;
import org.apache.axis.server.AxisServer;

import org.globus.wsrf.security.SecurityManager;
import org.globus.wsrf.impl.security.descriptor.ServiceSecurityConfig;

import org.globus.wsrf.container.ServiceManager;

import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
 
import javax.security.auth.Subject;
import org.globus.gsi.gssapi.jaas.JaasSubject;

public class BeanFactory extends BasicBeanFactory {

    /**
     * Create a new Bean instance. If <code>obj</code> is of type
     * {@link ServiceResourceRef ServiceResourceRef} the bean will be
     * created and initialized with security credentials associated 
     * with the current thread if the service associated with this bean
     * has a security descriptor configured.
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                    Hashtable environment)
        throws NamingException {
        Subject subject = null;
        MessageContext msgCtx = null;

        if (obj instanceof ServiceResourceRef) {
            ServiceResourceRef resource = (ServiceResourceRef)obj;
            AxisEngine engine = resource.getAxisEngine();
            String serviceName =  resource.getServiceName();
            if (engine == null) {
                throw new NamingException(i18n.getMessage("noServiceSet"));
            }
            if (serviceName == null) {
                throw new NamingException(i18n.getMessage("noEngineSet"));
            }

            ServiceManager serviceManager = 
                ServiceManager.getServiceManager((AxisServer)engine);
            SecurityManager securityManager = 
                SecurityManager.getManager();

            try {
                msgCtx = serviceManager.createMessageContext(serviceName);
                ServiceSecurityConfig.initialize(msgCtx);
                subject = securityManager.getServiceSubject(serviceName);
            } catch (Exception e) {
                NamingException ne = 
                    new NamingException(i18n.getMessage("beanSecInitFailed"));
                ne.setRootCause(e);
                throw ne;
            }
        }

        try {
            if (subject == null) {
                return getInstance(msgCtx, obj, name, nameCtx, environment);
            } else {
                GetInstanceAction action = 
                    new GetInstanceAction(msgCtx, obj, name, 
                                          nameCtx, environment);
                return JaasSubject.doAs(subject, action);
            } 
        } catch (NamingException e) {
            throw e;
        } catch (PrivilegedActionException e) {
            Exception cause = e.getException();
            if (cause instanceof NamingException) {
                throw (NamingException)cause;
            } else {
                NamingException nm = 
                    new NamingException(i18n.getMessage("beanInitFailed"));
                nm.setRootCause(cause);
                throw nm;
            }
        } catch (Exception e) {
            NamingException nm = 
                new NamingException(i18n.getMessage("beanInitFailed"));
            nm.setRootCause(e);
            throw nm;
        }
    }

    private Object getInstance(MessageContext msgCtx, Object obj, Name name,
                               Context nameCtx, Hashtable environment)
        throws NamingException {
        MessageContext oldCtx = 
            ServiceManager.HelperAxisEngine.getCurrentMessageContext();
        ServiceManager.HelperAxisEngine.setCurrentMessageContext(msgCtx);
        try {
            return super.getObjectInstance(obj, name, nameCtx, environment);
        } finally {
            ServiceManager.HelperAxisEngine.setCurrentMessageContext(oldCtx);
        }
    }
    
    private class GetInstanceAction implements PrivilegedExceptionAction {

        private MessageContext msgCtx;
        private Object obj;
        private Name name;
        private Context nameCtx;
        private Hashtable environment;

        private GetInstanceAction(MessageContext msgCtx, Object obj, Name name,
                                  Context nameCtx, Hashtable environment) {
            this.msgCtx = msgCtx;
            this.obj = obj;
            this.name = name;
            this.nameCtx = nameCtx;
            this.environment = environment;
        }

        public Object run() throws Exception {
            return getInstance(this.msgCtx, this.obj, this.name, 
                               this.nameCtx, this.environment);
        }
    }
    
    
}
