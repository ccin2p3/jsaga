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
package org.globus.wsrf.impl.notification;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.NotificationConsumerCallbackManager;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.impl.NotificationConsumerCallbackManagerImpl;
import org.globus.wsrf.impl.ResourceHomeImpl;
import org.globus.wsrf.impl.SimpleResourceKey;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;

public class NotificationConsumerHome extends ResourceHomeImpl
{
    static Log logger =
        LogFactory.getLog(NotificationConsumerHome.class.getName());

    public ResourceKey create()
    {
        return create(null);
    }

    public ResourceKey create(ResourceSecurityDescriptor desc)
    {
        NotificationConsumerCallbackManager consumerResource =
            new NotificationConsumerCallbackManagerImpl(desc);
        ResourceKey key = new SimpleResourceKey(
            keyTypeName,
            String.valueOf(consumerResource.hashCode()));
        this.add(key, consumerResource);
        return key;
    }

}
