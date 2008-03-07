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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.globus.wsrf.impl.security.util.FixedObjectInputStream;

/**
 * Represents a service's security descriptor.
 */
public class ServiceSecurityDescriptor extends SecurityDescriptor
    implements RunAsParserCallback, AuthMethodParserCallback {

    private int defaultRunAsType = -1;
    private Map methodRunAsTypes;
    private List defaultAuthMethods = null;
    private Map methodAuthMethods;

    public ServiceSecurityDescriptor() {
        super();

        register(MethodParser.QNAME, new MethodParser(this));
        register(RunAsParser.QNAME, new RunAsParser(this));
        register(AuthMethodParser.QNAME, new AuthMethodParser(this));
        this.methodRunAsTypes = new HashMap();
        this.methodAuthMethods = new HashMap();
    }

    /**
     * Sets the credentials that need to be used in invocation
     * of methods that do not have any run-as specified using
     * <code>setMethodRunAsType</code>.
     * @param runAsType can be one of <code>RunAsConstants</code>
     */
    public void setRunAsType(int runAsType)
        throws SecurityDescriptorException {
        setRunAsType(runAsType, false);
    }

    /**
     * Sets the credentials that need to be used in invocation
     * of methods that do not have any run-as specified using
     * <code>setMethodRunAsType</code>.
     * @param runAsType can be one of <code>RunAsConstants</code>
     * @param overwrite if true, overwrite existing configuration
     */
    public void setRunAsType(int runAsType, boolean overwrite)
        throws SecurityDescriptorException {
        if ((overwrite) || (this.defaultRunAsType == -1)) {
            this.defaultRunAsType = runAsType;
        } else {
            throw new SecurityDescriptorException(
                i18n.getMessage("defaultRunAs"));
        }
    }

    /**
     * Sets the credentials that need to be used in invoking method
     * @param method method name
     * @param identity can be one of <code>RunAsConstants</code>
     */
    public void setMethodRunAsType(QName method, int identity)
        throws SecurityDescriptorException {
        setMethodRunAsType(method, identity, false);
    }

    /**
     * Sets the credentials that need to be used in invoking method
     * @param method method name
     * @param identity can be one of <code>RunAsConstants</code>
     * @param overwrite if true, overwrite existing configuration
     */
    public void setMethodRunAsType(QName method, int identity,
                                   boolean overwrite)
        throws SecurityDescriptorException {
        if (method == null) {
            throw new IllegalArgumentException("method == null");
        }

        Object ret = this.methodRunAsTypes.put(method, new Integer(identity));
        if ((!overwrite) && (ret != null)) {
            throw new SecurityDescriptorException(
                i18n.getMessage("methodRunAs", method)
            );
        }
    }

    /**
     * Sets the authentication mechanism required for invocation of
     *  methods.
     * @param authMethods list of authentication mechanism that
     * implement <code>AuthMethod</code>
     */
    public void setAuthMethods(List authMethods)
        throws SecurityDescriptorException {
        setAuthMethods(authMethods, false);
    }

    /**
     * Sets the authentication mechanism required for invocation of
     *  methods.
     * @param authMethods list of authentication mechanism that
     * implement <code>AuthMethod</code>
     * @param overwrite if true, overwrite existing configuration
     */
    public void setAuthMethods(List authMethods, boolean overwrite)
        throws SecurityDescriptorException {

        if ((overwrite) || (this.defaultAuthMethods == null)) {
            this.defaultAuthMethods = authMethods;
        } else {
            throw new SecurityDescriptorException(
                i18n.getMessage("defaultAuthMethods"));
        }
    }

    /**
     * Sets the authentication mechanism required for invocation of
     *  said method.
     * @param method method name
     * @param authMethods list of authentication mechanism that
     * implement <code>AuthMethod</code>
     */
    public void setMethodAuthMethods(QName method, List authMethods)
        throws SecurityDescriptorException {
        setMethodAuthMethods(method, authMethods, false);
    }

    /**
     * Sets the authentication mechanism required for invocation of
     *  said method.
     * @param method method name
     * @param authMethods list of authentication mechanism that
     * implement <code>AuthMethod</code>
     * @param overwrite if true, overwrite existing configuration
     */
    public void setMethodAuthMethods(QName method, List authMethods,
                                     boolean overwrite)
        throws SecurityDescriptorException {
        if (authMethods == null) {
            throw new IllegalArgumentException("authMethods == null");
        }

        Object obj = this.methodAuthMethods.put(method, authMethods);

        if ((!overwrite) && (obj != null)) {
            throw new SecurityDescriptorException(
                i18n.getMessage("methodAuthMethods", method)
            );
        }
    }

    // public API
    /**
     * Returns default run-as type. If not set, run-as resource is
     * returned.
     */
    public int getDefaultRunAsType() {
        return this.defaultRunAsType;
    }

    /**
     * Returns configured run-as type. If nothing is set, default
     * run-as is returned.
     */
    public int getRunAsType(QName method) {
        Integer runAsType = (Integer) this.methodRunAsTypes.get(method);
        if (runAsType == null) {
            QName methodName = new QName(method.getLocalPart());
            runAsType = (Integer) this.methodRunAsTypes.get(methodName);
        }
        return (runAsType == null) ? -1 : runAsType.intValue();
    }

    /**
     * Returns default authentication methods that need to be
     * enforced.
     */
    public List getDefaultAuthMethods() {
        return this.defaultAuthMethods;
    }

    /**
     * Returns the authentication methods that need to be enforced for
     * a said method. If none is explicitly configured, default
     * authentication methods are used.
     *
     * @param method
     *        QName of the method
     */
    public List getAuthMethods(QName method) {
        List methods = (List) this.methodAuthMethods.get(method);
        if (methods == null) {
            QName methodName = new QName(method.getLocalPart());
            methods = (List) this.methodAuthMethods.get(methodName);
        }
        return methods;
    }

    public String getRequiredAuthMethodsErrorMessage(
        List methods,
        QName opName
        ) {
        return i18n.getMessage(
            "authRequired",
            new Object[] {
                AuthMethodParser.getAuthMethodsAsString(methods), opName
            }
        );
    }

    protected void writeObject(ObjectOutputStream oos) throws IOException {
        super.writeObject(oos);
        oos.writeInt(defaultRunAsType);
        oos.writeObject(methodRunAsTypes);
        oos.writeObject(defaultAuthMethods);
        oos.writeObject(methodAuthMethods);
    }

    protected void readObject(FixedObjectInputStream ois)
        throws IOException, ClassNotFoundException {
        super.readObject(ois);
        this.defaultRunAsType = ois.readInt();
        this.methodRunAsTypes = (Map)ois.readObject();
        this.defaultAuthMethods = (List)ois.readObject();
        this.methodAuthMethods = (Map)ois.readObject();
    }
}
