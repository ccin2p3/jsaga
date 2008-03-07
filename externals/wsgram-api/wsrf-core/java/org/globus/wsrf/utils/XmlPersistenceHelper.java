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
package org.globus.wsrf.utils;

import java.io.Writer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.namespace.QName;

import org.xml.sax.InputSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.impl.ReflectionResource;
import org.globus.wsrf.encoding.ObjectDeserializationContext;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.util.I18n;

/**
 * This helper is used to persist a {@link ReflectionResource
 * ReflectionResource}
 * by serializing the resource implementation JavaBean used in constructing the
 * ReflectionResource. The result of storing the resource is an XML
 * file that corresponds exactly to the XML Schema Resource document defined
 * for this resource. Future versions will offer alternative modes of
 * persistence.<p>
 * <p>
 * <p>
 * Usage:
 * <p>
 * Use this helper on the model of
 * {@link org.globus.wsrf.impl.PersistentReflectionResource
 * PersistentReflectionResource}
 * <p>
 *
 * @see ReflectionResource
 * @see org.globus.wsrf.impl.PersistentReflectionResource
 * @see org.globus.wsrf.impl.ResourceHomeImpl
 * @see org.globus.wsrf.PersistentResource
 * @see org.globus.wsrf.RemoveCallback
 */
public class XmlPersistenceHelper extends FilePersistenceHelper {

    private static I18n i18n =
        I18n.getI18n(Resources.class.getName());

    private static Log logger =
        LogFactory.getLog(XmlPersistenceHelper.class.getName());

    private static final String FILE_SUFFIX = ".xml";

    public XmlPersistenceHelper(Class beanClass)
        throws IOException {
        super(beanClass, FILE_SUFFIX);
    }

    /**
     * Loads and initialize the resource.
     *
     * @param key the key of the potentially new resource
     * @param resource the new resource to load.
     *        Its key and implementation bean are null.
     *        They will be set by a call from this method to
     *        resource.initialize().
     * @see org.globus.wsrf.PersistentResource
     */
    public void load(Object key, ReflectionResource resource)
        throws ResourceException {
        logger.debug("Loading the resource from an XML file");

        File resourceFile = getKeyAsFile(key);
        if (!resourceFile.exists()) {
            logger.debug(
                i18n.getMessage(
                    "backingFileNotFound",
                    new Object[]{resourceFile.getPath(), key}));
            throw new NoSuchResourceException();
        }

        QName resourceElementQName;
        Object loadedResourceBean;

        FileInputStream in = null;
        try {
            in = new FileInputStream(resourceFile);

            ObjectDeserializationContext deserializer
                = new ObjectDeserializationContext(new InputSource(in),
                                                   this.beanClass);

            deserializer.parse();

            loadedResourceBean = deserializer.getValue();
            resourceElementQName = deserializer.getQName();
        } catch (Exception e) {
            throw new ResourceException(i18n.getMessage("resourceLoadFailed"),
                                        e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ee) {}
            }
        }

        resource.initialize(loadedResourceBean, resourceElementQName, key);
    }

    /**
     * Store the resource into an XML document (current implementation).
     * The name of the file is governed by {@link #getKeyAsFile(Object)
     * getKeyAsFile()}.
     *
     * This stores the state of the implementation JavaBean. If some resource
     * properties have been implemented with something else (for instance
     * getters and setters from another object) they will not be
     * persisted with the current state. This is not a problem if their state
     * is immutable after initial creation, as their values will be set by
     * {@link ReflectionResource#initialize(Object, QName, Object) initialize},
     * ReflectionResource.initialize()} which is called by
     * this method.
     * (TODO: persist based on each RP?)
     *
     * @param resource the resource to store the state of.
     * @throws ResourceException if the resource could not be stored
     * @see #getKeyAsFile(Object) getKeyAsFile()
     * @see org.globus.wsrf.PersistentResource
     */
    public void store(ReflectionResource resource)
        throws ResourceException {
        store(resource.getID(),
              resource.getResourceBean(),
              resource.getResourcePropertySet().getName());
    }

    /**
     * Removes the resource from persistent storage.
     *
     * @param resource the resource to remove from storage.
     * @throws ResourceException if the resource could not be removed.
     * @see org.globus.wsrf.RemoveCallback
     */
    public void remove(ReflectionResource resource)
        throws ResourceException {
        Class resourceBean = resource.getResourceBean().getClass();
        if (!this.beanClass.isAssignableFrom(resourceBean)) {
            throw new IllegalArgumentException(
                        i18n.getMessage("expectedType", this.beanClass));
        }
        remove(resource.getID());
    }

    /**
     * Loads and returns the object of the given key from the persistent
     * storage.
     *
     * @param key key of object to load.
     * @return loaded Object instance.
     * @throws ResourceException If the object cannot be loaded from file.
     */
    public Object load(Object key)
        throws ResourceException {
        logger.debug( "Loading object by deserializing an XML file");

        File resourceFile = getKeyAsFile(key);
        if (!resourceFile.exists()) {
            logger.debug(
                i18n.getMessage(
                "backingFileNotFound",
                new Object[] {resourceFile.getPath(), key}));
            throw new NoSuchResourceException();
        }

        Object loadedBean;

        FileInputStream in = null;
        try {
            in = new FileInputStream(resourceFile);

            ObjectDeserializationContext deserializer
                = new ObjectDeserializationContext(new InputSource(in),
                                                   this.beanClass);
            deserializer.parse();

            loadedBean = deserializer.getValue();
        } catch (Exception e) {
            throw new ResourceException(i18n.getMessage("resourceLoadFailed"),
                                        e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ee) {}
            }
        }

        return loadedBean;
    }

    /**
     * Stores the object of the given key to persistent storage.
     * <br><b>Note:</b> Calls to this function must be synchronized on
     * per key basis.
     *
     * @param key key of object.
     * @param object object to persist.
     * @param topElementQName the top element name of the XML object.
     * @throws ResourceException If the object cannot be saved to a file.
     */
    public void store(Object key, Object object, QName topElementQName)
        throws ResourceException {
        if (!this.beanClass.isAssignableFrom(object.getClass())) {
            throw new IllegalArgumentException(
                        i18n.getMessage("expectedType", this.beanClass));
        }

        logger.debug("Storing object to an XML file");

        Writer writer = null;
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("xph", ".tmp",
                                          getStorageDirectory());
            writer = new BufferedWriter(new FileWriter(tmpFile));

            ObjectSerializer.serialize(writer,
                                       object,
                                       topElementQName);
        } catch (Exception e) {
            if (tmpFile != null) {
                tmpFile.delete();
            }
            throw new ResourceException(
                     i18n.getMessage("resourceStoreFailed"), e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ee) {}
            }
        }

        File file = getKeyAsFile(key);
        if (file.exists()) {
            file.delete();
        }
        if (!tmpFile.renameTo(file)) {
            file.delete();
            throw new ResourceException(
                     i18n.getMessage("resourceStoreFailed"));
        }
    }
}
