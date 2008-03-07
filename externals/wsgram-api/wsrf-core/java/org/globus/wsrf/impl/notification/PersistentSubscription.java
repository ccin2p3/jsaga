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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;

import org.globus.wsrf.impl.security.util.FixedObjectInputStream;

import org.apache.axis.message.addressing.EndpointReferenceType;

import org.oasis.wsn.TopicExpressionType;
import org.oasis.wsrf.properties.QueryExpressionType;

import org.globus.util.I18n;
import org.globus.wsrf.InvalidResourceKeyException;
import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.PersistenceCallback;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.impl.security.descriptor.ClientSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;
import org.globus.wsrf.utils.FilePersistenceHelper;
import org.globus.wsrf.utils.Resources;

public class PersistentSubscription extends SimpleSubscription
    implements PersistenceCallback
{
    private static I18n i18n = I18n.getI18n(Resources.class.getName());
    private transient FilePersistenceHelper persistenceHelper;
    private static final String FILE_SUFFIX = ".obj";

    public PersistentSubscription()
    {
        super();
    }

    public PersistentSubscription(
        EndpointReferenceType consumerReference,
        EndpointReferenceType producerReference,
        Calendar initialTerminationTime, Object policy,
        QueryExpressionType precondition,
        QueryExpressionType selector, ResourceKey producerKey,
        String producerHomeLocation,
        TopicExpressionType topicExpression, boolean isPaused,
        boolean useNotify,
        ClientSecurityDescriptor notificationSecurityDescriptor,
        ResourceSecurityDescriptor resourceSecurityDescriptor)
    {
        super(consumerReference, producerReference, initialTerminationTime,
              policy, precondition, selector, producerKey,
              producerHomeLocation, topicExpression, isPaused, useNotify,
              notificationSecurityDescriptor, resourceSecurityDescriptor);
    }

    public void load(ResourceKey key) throws ResourceException,
                                             NoSuchResourceException,
                                             InvalidResourceKeyException
    {
        File file = getKeyAsFile(key.getValue());
        if(!file.exists())
        {
            throw new NoSuchResourceException(key.toString());
        }
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(file);
            FixedObjectInputStream ois = new FixedObjectInputStream(fis);
            this.consumerReference = (EndpointReferenceType) ois.readObject();
            this.producerReference = (EndpointReferenceType) ois.readObject();
            this.policy = ois.readObject();
            this.precondition = (QueryExpressionType) ois.readObject();
            this.selector = (QueryExpressionType) ois.readObject();
            this.producerKey = (ResourceKey) ois.readObject();
            this.producerHomeLocation = (String) ois.readObject();
            this.topicExpression = (TopicExpressionType) ois.readObject();
            this.isPaused = ois.readBoolean();
            this.useNotify = ois.readBoolean();
            this.terminationTime = (Calendar) ois.readObject();
            this.creationTime = (Calendar) ois.readObject();
            this.securityDescriptor = (ClientSecurityDescriptor)
                ois.readObject();
            this.resourceSecurityDescriptor = (ResourceSecurityDescriptor)
                ois.readObject();
            this.creationTime = (Calendar) ois.readObject();
            this.id = (String) ois.readObject();
        }
        catch(Exception e)
        {
            String errorMessage = i18n.getMessage("resourceLoadFailed");
            throw new ResourceException(errorMessage, e);
        }
        finally
        {
            if(fis != null)
            {
                try
                {
                    fis.close();
                }
                catch(Exception ee)
                {
                }
            }
        }
    }

    public synchronized void store() throws ResourceException
    {
        FileOutputStream fos = null;
        File tmpFile = null;
        try
        {
            tmpFile = File.createTempFile(
                "subscription", ".tmp",
                getPersistenceHelper().getStorageDirectory());
            fos = new FileOutputStream(tmpFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this.consumerReference);
            oos.writeObject(this.producerReference);
            oos.writeObject(this.policy);
            oos.writeObject(this.precondition);
            oos.writeObject(this.selector);
            oos.writeObject(this.producerKey);
            oos.writeObject(this.producerHomeLocation);
            oos.writeObject(this.topicExpression);
            oos.writeBoolean(this.isPaused);
            oos.writeBoolean(this.useNotify);
            oos.writeObject(this.terminationTime);
            oos.writeObject(this.creationTime);
            oos.writeObject(this.securityDescriptor);
            oos.writeObject(this.resourceSecurityDescriptor);
            oos.writeObject(this.creationTime);
            oos.writeObject(this.id);
        }
        catch(Exception e)
        {
            tmpFile.delete();
            String errorMessage = i18n.getMessage("resourceStoreFailed");
            throw new ResourceException(errorMessage, e);
        }
        finally
        {
            if(fos != null)
            {
                try
                {
                    fos.close();
                }
                catch(Exception ee)
                {
                }
            }
        }

        File file = getKeyAsFile(this.id);
        if (file.exists()) {
            file.delete();
        }
        if (!tmpFile.renameTo(file)) {
            tmpFile.delete();
            throw new ResourceException(
                i18n.getMessage("resourceStoreFailed"));
        }
    }

    private File getKeyAsFile(Object key)
        throws InvalidResourceKeyException
    {
        if(key instanceof String)
        {
            return getPersistenceHelper().getKeyAsFile(key);
        }
        else
        {
            throw new InvalidResourceKeyException();
        }
    }

    public void remove() throws ResourceException
    {
        super.remove();
        getPersistenceHelper().remove(this.id);
    }

    protected synchronized FilePersistenceHelper getPersistenceHelper()
    {
        if(this.persistenceHelper == null)
        {
            try
            {
                this.persistenceHelper = new FilePersistenceHelper(getClass(),
                                                                   FILE_SUFFIX);
            }
            catch(Exception e)
            {
                throw new RuntimeException(e.getMessage());
            }
        }
        return this.persistenceHelper;
    }

    public void setTerminationTime(Calendar time)
    {
        super.setTerminationTime(time);
        try
        {
            this.store();
        }
        catch(Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void pause() throws Exception
    {
        super.pause();
        try
        {
            this.store();
        }
        catch(Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void resume() throws Exception
    {
        super.resume();
        try
        {
            this.store();
        }
        catch(Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }
}
