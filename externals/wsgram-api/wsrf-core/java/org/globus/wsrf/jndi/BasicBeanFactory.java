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

import org.globus.wsrf.utils.Resources;
import org.globus.util.I18n;

public class BasicBeanFactory extends org.apache.naming.factory.BeanFactory {
    
    protected static I18n i18n = I18n.getI18n(Resources.class.getName());

    /**
     * Create a new Bean instance. If the created bean implements
     * <code>Initializable</code> interface, the <code>initialize()</code>
     * function will be called after the bean is created and all properties
     * are set.
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                    Hashtable environment)
        throws NamingException {
        Object bean = super.getObjectInstance(obj, name, nameCtx, environment);
        if (bean instanceof Initializable) {
            try {
                ((Initializable)bean).initialize();
            } catch (Exception e) {
                NamingException ex =
                    new NamingException(i18n.getMessage("beanInitFailed"));
                ex.setRootCause(e);
                throw ex;
            }
        }
        return bean;
    }
    
}
