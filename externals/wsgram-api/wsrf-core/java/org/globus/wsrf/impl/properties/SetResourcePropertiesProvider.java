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
package org.globus.wsrf.impl.properties;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.ResourceProperties;
import org.globus.wsrf.ResourceProperty;
import org.globus.wsrf.ResourcePropertySet;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.WSRFConstants;
import org.globus.wsrf.impl.SimpleResourcePropertyMetaData;
import org.globus.wsrf.utils.Resources;
import org.globus.wsrf.utils.FaultHelper;
import org.globus.util.I18n;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.Name;

import org.oasis.wsrf.properties.DeleteType;
import org.oasis.wsrf.properties.InsertType;
import org.oasis.wsrf.properties.UpdateType;
import org.oasis.wsrf.properties.InvalidResourcePropertyQNameFaultType;
import org.oasis.wsrf.properties.InvalidSetResourcePropertiesRequestContentFaultType;
import org.oasis.wsrf.properties.ResourceUnknownFaultType;
import org.oasis.wsrf.properties.SetResourcePropertyRequestFailedFaultType;
import org.oasis.wsrf.properties.UnableToModifyResourcePropertyFaultType;
import org.oasis.wsrf.properties.SetResourceProperties_Element;
import org.oasis.wsrf.properties.SetResourcePropertiesResponse;

public class SetResourcePropertiesProvider {

    private static Log logger =
        LogFactory.getLog(SetResourcePropertiesProvider.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    public SetResourcePropertiesResponse setResourceProperties(SetResourceProperties_Element request)
        throws RemoteException,
               SetResourcePropertyRequestFailedFaultType,
               InvalidResourcePropertyQNameFaultType,
               UnableToModifyResourcePropertyFaultType,
               ResourceUnknownFaultType,
               InvalidSetResourcePropertiesRequestContentFaultType {

        if (request == null) {
            throw new RemoteException(
                i18n.getMessage("nullArgument", "request"));
        }

        Object resource = null;
        try {
            resource = ResourceContext.getResourceContext().getResource();
        } catch (NoSuchResourceException e) {
            ResourceUnknownFaultType fault = 
                new ResourceUnknownFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            throw fault;
        } catch (Exception e) {
            throw new RemoteException(
                i18n.getMessage("resourceDisoveryFailed"), e);
        }

        if (!(resource instanceof ResourceProperties)) {
            throw new RemoteException(i18n.getMessage("rpsNotSupported"));
        }
        
        ResourcePropertySet set =
            ((ResourceProperties)resource).getResourcePropertySet();

        ResourceProperty prop = null;
        QName rp = null;

        Object component = null;
        Iterator iter = request.iterator();
        while(iter.hasNext()) {
            component = iter.next();
            if (component instanceof DeleteType) {
                handleDelete( (DeleteType)component, set );
            } else if (component instanceof InsertType) {
                handleInsert( (InsertType)component, set);
            } else if (component instanceof UpdateType) {
                handleUpdate( (UpdateType)component, set);
            } else {
                SetResourcePropertyRequestFailedFaultType fault =
                    new SetResourcePropertyRequestFailedFaultType();
                FaultHelper faultHelper = new FaultHelper(fault);
                throw fault;
            }
        }
        
        SetResourcePropertiesResponse response =
            new SetResourcePropertiesResponse();

        return response;
    }
        
    private void handleDelete(DeleteType delete, ResourcePropertySet set)
        throws InvalidResourcePropertyQNameFaultType,
               UnableToModifyResourcePropertyFaultType {
        QName rp = delete.getResourceProperty();
        if (rp == null) {
            InvalidResourcePropertyQNameFaultType fault = 
                new InvalidResourcePropertyQNameFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(i18n.getMessage("noRPName"));
            throw fault;
        }
        checkRP(rp);
        ResourceProperty prop = set.get(rp);
        if (prop == null) {
            InvalidResourcePropertyQNameFaultType fault = 
                new InvalidResourcePropertyQNameFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(rp.toString());
            throw fault;
        }
        try {
            prop.clear();
        } catch (RuntimeException e) {
            UnableToModifyResourcePropertyFaultType fault =
                new UnableToModifyResourcePropertyFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            faultHelper.setDescription(rp.toString());
            throw fault;
        }
    }

    private void handleInsert(InsertType insert, ResourcePropertySet set)
        throws InvalidResourcePropertyQNameFaultType,
               UnableToModifyResourcePropertyFaultType {
        SOAPElement [] any = insert.get_any();
        if (any == null) {
            InvalidResourcePropertyQNameFaultType fault = 
                new InvalidResourcePropertyQNameFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(i18n.getMessage("noRPName"));
            throw fault;
        }
        QName rp = null;
        ResourceProperty prop = null;
        for (int j=0;j<any.length;j++) {
            rp = getQName(any[j]);
            checkRP(rp);
            prop = set.get(rp);
            if (prop == null) {
                if (set.isOpenContent()) {
                    SimpleResourcePropertyMetaData metaData = 
                        new SimpleResourcePropertyMetaData(rp);
                    prop = set.create(metaData);
                    set.add(prop);
                } else {
                    InvalidResourcePropertyQNameFaultType fault = 
                        new InvalidResourcePropertyQNameFaultType();
                    FaultHelper faultHelper = new FaultHelper(fault);
                    faultHelper.setDescription(rp.toString());
                    throw fault;
                }
            }
            try {
                prop.add(any[j]);
            } catch (RuntimeException e) {
                UnableToModifyResourcePropertyFaultType fault =
                    new UnableToModifyResourcePropertyFaultType();
                FaultHelper faultHelper = new FaultHelper(fault);
                faultHelper.addFaultCause(e);
                faultHelper.setDescription(rp.toString());
                throw fault;
            }
        }
    }

    private void handleUpdate(UpdateType update, ResourcePropertySet set)
        throws InvalidResourcePropertyQNameFaultType,
               UnableToModifyResourcePropertyFaultType {
        SOAPElement [] any = update.get_any();
        if (any == null) {
            InvalidResourcePropertyQNameFaultType fault = 
                new InvalidResourcePropertyQNameFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(i18n.getMessage("noRPName"));
            throw fault;
        }
        QName rp = null;
        ResourceProperty prop = null;
        Map updates = new HashMap();
        for (int j=0;j<any.length;j++) {
            rp = getQName(any[j]);
            checkRP(rp);
            prop = set.get(rp);
            if (prop == null) {
                if (set.isOpenContent()) {
                    SimpleResourcePropertyMetaData metaData = 
                        new SimpleResourcePropertyMetaData(rp);
                    prop = set.create(metaData);
                    set.add(prop);
                } else {
                    InvalidResourcePropertyQNameFaultType fault = 
                        new InvalidResourcePropertyQNameFaultType();
                    FaultHelper faultHelper = new FaultHelper(fault);
                    faultHelper.setDescription(rp.toString());
                    throw fault;
                }
            }
            List values = (List)updates.get(rp);
            if (values == null) {
                values = new LinkedList();
                updates.put(rp, values);
            }
            values.add(any[j]);
        }
        Iterator iter = updates.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            rp = (QName)entry.getKey();
            prop = set.get(rp);
            // XXX: property was removed during?!
            if (prop == null) {
                continue;
            }
            synchronized(prop) {
                try {
                    prop.clear();
                    Iterator listIter =
                        ((List)entry.getValue()).iterator();
                    while(listIter.hasNext()) {
                        prop.add(listIter.next());
                    }
                } catch (RuntimeException e) {
                    UnableToModifyResourcePropertyFaultType fault =
                        new UnableToModifyResourcePropertyFaultType();
                    FaultHelper faultHelper = new FaultHelper(fault);
                    faultHelper.addFaultCause(e);
                    faultHelper.setDescription(rp.toString());
                    throw fault;
                }
            }
        }
    }

    private void checkRP(QName rp) 
        throws UnableToModifyResourcePropertyFaultType {
        // according to the WSRL spec these cannot be modified
        // via SetResourceProperties operation.
        if (rp.equals(WSRFConstants.CURRENT_TIME) ||
            rp.equals(WSRFConstants.TERMINATION_TIME)) {
            UnableToModifyResourcePropertyFaultType fault =
                new UnableToModifyResourcePropertyFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(rp.toString());
            throw fault;
        }
    }
    
    public static QName getQName(SOAPElement element) {
        Name name = element.getElementName();
        return new QName(name.getURI(), name.getLocalName());
    }

}
