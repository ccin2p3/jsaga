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

import org.globus.wsrf.WSRFConstants;
import org.globus.wsrf.core.notification.ResourcePropertyValueChangeNotificationElementType;
import org.oasis.wsrf.properties.ResourcePropertyValueChangeNotificationType;

import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.encoding.ObjectConverter;
import org.globus.wsrf.encoding.SerializationException;

import javax.xml.soap.SOAPElement;
import javax.xml.namespace.QName;

public class NotificationTypeConverter implements ObjectConverter {

    private static final QName RP_CHANGE =
        new QName(WSRFConstants.PROPERTIES_NS, 
                  "ResourcePropertyValueChangeNotificationType");
    
    public SOAPElement toSOAPElement(Object obj) 
        throws SerializationException {
        if (obj instanceof ResourcePropertyValueChangeNotificationElementType) {
            ResourcePropertyValueChangeNotificationElementType elem =
                (ResourcePropertyValueChangeNotificationElementType)obj;
            ResourcePropertyValueChangeNotificationType message =
                elem.getResourcePropertyValueChangeNotification();
            return ObjectSerializer.toSOAPElement(message, RP_CHANGE);
        } 
        return null;
    }
    
}
