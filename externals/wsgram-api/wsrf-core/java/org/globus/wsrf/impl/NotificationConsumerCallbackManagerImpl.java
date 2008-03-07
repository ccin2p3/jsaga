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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.globus.wsrf.NotificationConsumerCallbackManager;
import org.globus.wsrf.NotifyCallback;

import org.globus.wsrf.security.SecureResource;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;

public class NotificationConsumerCallbackManagerImpl
    implements NotificationConsumerCallbackManager, SecureResource
{
    ResourceSecurityDescriptor resDesc = null;
    Map callbackTable = new HashMap();

    public NotificationConsumerCallbackManagerImpl(
				    ResourceSecurityDescriptor desc)
    {
        this.resDesc = desc;
    }

    public void registerCallback(
        List topicPath,
        NotifyCallback callback)
    {
        this.callbackTable.put(topicPathToString(topicPath), callback);
    }

    public NotifyCallback getCallback(List topicPath)
    {
        return (NotifyCallback) this.callbackTable.get(
            topicPathToString(topicPath));
    }

    private static String topicPathToString(List topicPath)
    {
        if(topicPath == null)
        {
            return null;
        }

        Iterator nameIterator = topicPath.iterator();
        if(nameIterator.hasNext())
        {
            StringBuffer result = 
                new StringBuffer(((QName) nameIterator.next()).toString());

            while(nameIterator.hasNext())
            {
                result.append("/");
                result.append(((QName) nameIterator.next()).getLocalPart());
            }
            return result.toString();
        }
        else
        {
            return null;
        }
    }

    public ResourceSecurityDescriptor getSecurityDescriptor()
    {
        return this.resDesc;
    }
}
