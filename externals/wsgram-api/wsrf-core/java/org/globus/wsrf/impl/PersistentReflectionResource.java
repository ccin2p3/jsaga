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

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.PersistenceCallback;
import org.globus.wsrf.RemoveCallback;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.ResourceProperty;
import org.globus.wsrf.utils.XmlPersistenceHelper;
import org.globus.wsrf.utils.Resources;
import org.globus.util.I18n;

/**
 * A persistent specialization of
 * {@link ReflectionResource ReflectionResource}. It persists itself by
 * serializing the resource implementation JavaBean used in constructing an
 * object as a ReflectionResource. The result of storing the resource is an XML
 * file that corresponds exactly to the XML Schema Resource document defined
 * for this resource. Future versions will offer alternative modes of
 * persistence.<p>
 * <p>
 * This class can be very useful for rapid prototyping of persistent resources.
 * In addition, it is possible to refine the persistence model by overriding
 * the {@link #load(ResourceKey) load()} and {@link #store() store()} methods.
 * <p>
 * Usage:
 * <ul>
*  <li>extend this class by implementing the function
 * {@link #getResourceBeanClass() getResourceBeanClass}.</li>
 * <li>apply usage rules for extending
 * {@link ReflectionResource ReflectionResource}:
 *     <ul><li>override initialize() in order to add run-time-defined
 *         resource properties and/or topics.</li>
 *         <li>add specialized behavior, for instance domain-specific functions.
 *         </li>
 *     </ul>
 * </li>
 * <li>As a resource home, use a specialization of a PersistentResource-managing
 * ResourceHome such as {@link ResourceHomeImpl ResourceHomeImpl}
 * and follow its set of usage rules.</li>
 * <li>initialize the objects by calling
 *  {@link ReflectionResource#initialize(Object, QName, Object) initialize}.
 * </li>
 * <li>Call the store() method after initial creation of the resource (since
 *     this is not triggered by {@link ResourceHomeImpl
 *     ResourceHomeImpl}), and then whenever
 *     adequate (for instance when a resource property value, or a set thereof,
 *     is modified). Remember to set the dirty flag to true first.
 *     (see {@link #setDirty(boolean) setDirty})
 * </li>
 * </ul>
 * <p>
 * Refining the persistence model:<p>
 * The default persistence model implemented by this class may be sufficient in
 * most simple cases, but refining it may sometimes make sense, for instance
 * in cases where:
 * <ul>
 *   <li> when it is not necessary to persist every resource property.</li>
 *   <li> when there is some implementation state that needs to be stored
 *        that is not a resource property.</li>
 * </ul>
 * Note: The implementation of this class use the PerformanceLogger in order to log
 * the duration of storing and loading the resource. The log subcategory is
 * <code>performance</code>.
 * <p>
 * Dirty flag:<p>
 * A dirty flag is set to true every time a resource property of the
 * Schema-defined resource property set is set to a new value. This enables to
 * optimize storing so the resource is not actually persisted each time it is
 * passivated by ResourceHomeImpl. (note: maybe this is overkill and not
 * necessary?)
 * <p>

 *
 * @see ReflectionResource
 * @see ResourceHomeImpl
 * @see PersistenceCallback
 * @see RemoveCallback
 */
public abstract class PersistentReflectionResource
    extends ReflectionResource
    implements RemoveCallback, PersistenceCallback, DirtyFlagHolder {

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    private static Log logger =
        LogFactory.getLog(PersistentReflectionResource.class.getName());

    /**
     * The dirty flag is set to true at instanciation time, so that the
     * initial creator of the resource just needs to call {@link #store() store}
     * in order to actually persist the new resource for the first time.
     */
    private boolean dirty = true; 

    //The dirty flacreated clean since in most cases,
    //creation will occur after a persisted version of the resource exists
    //already in the persistence store.

    private XmlPersistenceHelper persistenceHelper;
    
    protected synchronized XmlPersistenceHelper getPersistenceHelper() {
        if (this.persistenceHelper == null) {
            try {
                this.persistenceHelper = 
                    new XmlPersistenceHelper(getResourceBeanClass());
            } catch (IOException e) {
                logger.error(i18n.getMessage("resourceInitError"), e);
                throw new RuntimeException(e.getMessage());
            }
        }
        return this.persistenceHelper;
    }
        
    /**
     *
     * @return Class The class of the Axis-generated Serializable
     * implementation bean for the resource property set. 
     * @see ReflectionResource
     */
    abstract protected Class getResourceBeanClass();

    /**
     * Called by ResourceHomeImpl when activating a resource.
     * This is called after an instance has been created with the
     * parameterless constructor, so nothing has been initialized yet.
     * After deserialization, this calls initialize() so as to initialize
     * the resource properly, with its key and the stored state of its
     * resource properties.
     * <p>
     * If the resource was successfully loaded, its dirty flag is set to false.
     * <p>
     * <b>Postcondition</b> getDirty() == false
     * @see #setDirty(boolean) setDirty()
     * @see PersistenceCallback
     */
    public synchronized void load(ResourceKey key) throws ResourceException {
        getPersistenceHelper().load(key, this);
        setDirty(false);
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
     * <p>
     * If the resource was successfully stored, its dirty flag is set to false.
     * <p>
     * <b>Postcondition</b> getDirty() == false
     * @see #setDirty(boolean) setDirty()
     * @see #getKeyAsFile(Object) getKeyAsFile()
     * @see PersistenceCallback
     * @throws ResourceException if the resource could not be stored
     */
    public synchronized void store() throws ResourceException {
        if (!this.dirty) {
            if (logger.isDebugEnabled()) {
                logger.debug("Dirty flag not set: resource will not be stored");
            }
            return;
        }
        getPersistenceHelper().store(this);
        setDirty(false);
    }

    /**
     * Create a file object based on the key supplied in parameter.
     * The file name will follow the format:<p>
     * file name :== (class name)_(key scalar value).xml<p>
     * where:
     * <ul>
     * <li>(class name) is class name of the implementation resource bean.</li>
     * <li>(key scalar value) is the toString value of the key passed in
     *     parameter. If the key is an instance of ResourceKey, its value is
     *     the result of key.getValue().</li>
     *
     * @param key Object the key of the resource
     * @return File
     */
    protected File getKeyAsFile(Object key) {
        return getPersistenceHelper().getKeyAsFile(key);
    }

    protected File getStorageDirectory() {
        return getPersistenceHelper().getStorageDirectory();
    }

    /**
     * @see RemoveCallback
     */
    public void remove() throws ResourceException {
        getPersistenceHelper().remove(this);
    }

    /**
     * @param rpQName QName
     * @param resourceBean Object
     * @throws Exception
     * @return ResourceProperty
     */
    protected ResourceProperty createNewResourceProperty(
        QName rpQName,
        Object resourceBean)
        throws Exception {
        ResourceProperty prop =
            super.createNewResourceProperty(rpQName, resourceBean);

        return new PersistentResourceProperty(prop, this);
    }


    /**
     * Sets the dirty flag on this persistent object. The resource will not be
     * persisted unless it the firty flag is true.
     *
     * @param changed boolean To flag the resource as changed i.e. "dirty" and
     * have it persisted by the next invocation of {@link #store() store}
     */
    public void setDirty(boolean changed) {
        this.dirty = changed;
    }

    public boolean getDirty() {
        return this.dirty;
    }

    /**
     * This function returns the keys of the resources that have been stored.
     * This should be used by the home in order to recover state,
     * by listing all the stored resource keys and adding them to the
     * map of resources. The consumer code must test for null.
     * @return List the list of key values
     */
    public List getStoredResourceKeyValues() throws IOException {
        return getPersistenceHelper().list();
    }
    
}
