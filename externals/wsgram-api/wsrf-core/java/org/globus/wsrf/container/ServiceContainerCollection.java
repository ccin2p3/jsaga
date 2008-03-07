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
package org.globus.wsrf.container;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Enumeration;
import java.util.Hashtable;
import java.io.IOException;

import org.globus.wsrf.utils.Resources;
import org.globus.util.I18n;

public class ServiceContainerCollection {
    private static Hashtable containers = new Hashtable();

    static I18n i18n = I18n.getI18n(Resources.class.getName());

    static Log logger =
        LogFactory.getLog(ServiceContainerCollection.class.getName());

    public static void register(String name, ServiceContainer container) {
        containers.put(name, container);
    }

    public static void unregister(String name) {
        containers.remove(name);
    }

    public static ServiceContainer get(String name) {
        return (ServiceContainer) containers.get(name);
    }

    public static void stopAll(boolean force) throws ContainerException {
        for (Enumeration e = containers.elements(); e.hasMoreElements();) {
            ServiceContainer container = (ServiceContainer) e.nextElement();
            if (force) {
                try {
                    container.close();
                } catch (IOException ex) {
                    throw new ContainerException(
                        i18n.getMessage("containerStopError"), ex);
                }
            } else {
                container.stop();
            }
        }
    }
}
