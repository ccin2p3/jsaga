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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;

import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.util.I18n;
import org.globus.wsrf.Constants;
import org.globus.wsrf.InvalidResourceKeyException;
import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.PersistenceCallback;
import org.globus.wsrf.RemoveCallback;
import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceHome;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.ResourceLifetime;
import org.globus.wsrf.container.Lock;
import org.globus.wsrf.container.LockManager;
import org.globus.wsrf.impl.lifetime.SetTerminationTimeProvider;
import org.globus.wsrf.jndi.Initializable;
import org.globus.wsrf.jndi.JNDIUtils;
import org.globus.wsrf.utils.Resources;
import org.globus.wsrf.utils.cache.Cache;
import commonj.timers.Timer;
import commonj.timers.TimerManager;

/**
 * An implementation of the <code>ResourceHome</code> interface.
 * This implementation was designed to work with resources that implement
 * the {@link PersistenceCallback PersistenceCallback} interface as well as
 * memory resident resources. If the resource class implements the
 * {@link PersistenceCallback PersistenceCallback} interface
 * <code>SoftReference</code>s will be used to recycle resource
 * objects. The resource class implementation is responsible for saving its
 * state to disk. This implementation will <b>not</b> call
 * {@link PersistenceCallback#store() PersistenceCallback.store()}. The
 * resource implementation must have a default constructor.
 * <br><br>
 * Configuration options:
 * <ul>
 * <li>
 * sweeperDelay - configures how often the resource sweeper runs in msec.
 * By default the resource sweeper runs every minute. For example:
 * <pre>
 *    &lt;parameter&gt;
 *     &lt;name&gt;sweeperDelay&lt;/name&gt;
 *     &lt;value&gt;60000&lt;/value&gt;
 *    &lt;/parameter&gt;
 * </pre>
 * <li>
 * resourceClass - configures the name of the resource class. For example:
 * <pre>
 *    &lt;parameter&gt;
 *     &lt;name&gt;resourceClass&lt;/name&gt;
 *     &lt;value&gt;org.globus.wsrf.samples.counter.PersistentCounter&lt;/value&gt;
 *    &lt;/parameter&gt;
 * </pre>
 * <li>
 * resourceKeyType - configures the key type class. By default
 * <code>java.lang.String</code> is used. For example:
 * <pre>
 *    &lt;parameter&gt;
 *     &lt;name&gt;resourceKeyType&lt;/name&gt;
 *     &lt;value&gt;java.lang.Integer&lt;/value&gt;
 *    &lt;/parameter&gt;
 * </pre>
 * <li>
 * resourceKeyName - configures the key name. For example:
 * <pre>
 *    &lt;parameter&gt;
 *     &lt;name&gt;resourceKeyName&lt;/name&gt;
 *     &lt;value&gt;{http://counter.com}CounterKey&lt;/value&gt;
 *    &lt;/parameter&gt;
 * </pre>
 * </ul>
 * <br>
 * <b>Note:</b> 
 * Must be deployed with <code>org.globus.wsrf.jndi.BeanFactory</code> in JNDI
 * or user must first call {@link #initialize() initialize()} method. 
 * Also when overriding the {@link #initialize() initialize()} method make 
 * sure to call <code>super.initialize();</code>.
 * <br><br>
 * <b>Note:</b> 
 * This {@link ResourceHome ResourceHome} implementation performs per key 
 * synchronization.
 */
public abstract class ResourceHomeImpl
    implements ResourceHome,
               Initializable {

    private static Log logger =
            LogFactory.getLog(ResourceHomeImpl.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    protected Map resources;

    protected QName keyTypeName;
    protected Class resourceClass;
    protected Class keyTypeClass;
    private boolean resourceIsPersistent = false;

    protected LockManager lockManager;

    private String cacheLocation;
    private Cache cache;

    private long sweeperDelay = 60000;
    private Sweeper sweeper;

    private boolean initialized = false;

    public void setResourceClass(String clazz)
        throws ClassNotFoundException {
        this.resourceClass = Class.forName(clazz);
    }

    public String getResourceClass() {
        return this.resourceClass.getName();
    }

    public void setResourceKeyType(String clazz)
        throws ClassNotFoundException {
        this.keyTypeClass = Class.forName(clazz);
    }

    public Class getKeyTypeClass() {
        return this.keyTypeClass;
    }

    public void setResourceKeyName(String keyName) {
        this.keyTypeName = QName.valueOf(keyName);
    }

    public QName getKeyTypeName() {
        return this.keyTypeName;
    }

    public void setSweeperDelay(long delay) {
        this.sweeperDelay = delay;
    }

    public long getSweeperDelay() {
        return this.sweeperDelay;
    }

    public void setCacheLocation(String jndiLocation) {
        this.cacheLocation = jndiLocation;
    }

    public String getCacheLocation() {
        return this.cacheLocation;
    }

    public synchronized void initialize() throws Exception {
        if (this.initialized) {
            return;
        }
        if (this.keyTypeClass == null) {
            this.keyTypeClass = String.class;
        }
        if (this.keyTypeName == null) {
            throw new Exception(i18n.getMessage("resourceKeyConfigError"));
        }
        if (this.resourceClass == null) {
            throw new Exception(i18n.getMessage("resourceClassConfigError"));
        }
        if (!Resource.class.isAssignableFrom(this.resourceClass)) {
            throw new Exception(i18n.getMessage("invalidResourceType",
                                                this.resourceClass.getName()));
        }
        if (PersistenceCallback.class.isAssignableFrom(this.resourceClass)) {
            this.resourceIsPersistent = true;
        }

        Context initialContext = new InitialContext();
        TimerManager timerManager =
            (TimerManager)initialContext.lookup(Constants.DEFAULT_TIMER);

        if (this.resourceIsPersistent) {
            this.resources = new ReferenceMap(ReferenceMap.HARD,
                                              ReferenceMap.SOFT,
                                              true);

            // initialize cache policy
            if (this.cacheLocation != null) {
                this.cache =
                    (Cache)JNDIUtils.lookup(initialContext,
                                            this.cacheLocation,
                                            Cache.class);
            }
        } else {
            this.resources = new HashMap();
        }

        this.resources = Collections.synchronizedMap(this.resources);

        this.lockManager = new LockManager();

        if (ResourceLifetime.class.isAssignableFrom(this.resourceClass)) {
            this.sweeper = new Sweeper(this,
                                       this.resources,
                                       timerManager,
                                       this.sweeperDelay);
        }

        // nothing to do for now
        this.initialized = true;
    }

    protected Resource createNewInstance()
        throws ResourceException {
        try {
            return (Resource)this.resourceClass.newInstance();
        } catch (Exception e) {
            throw new ResourceException("", e);
        }
    }

    protected Resource createNewInstanceAndLoad(ResourceKey key)
        throws ResourceException {
        Resource resource = createNewInstance();
        ((PersistenceCallback)resource).load(key);
        return resource;
    }

    public Resource find(ResourceKey key)
        throws ResourceException {
        if (key == null) {
            throw new InvalidResourceKeyException(
                i18n.getMessage("nullArgument", "key"));
        }

        Resource resource = null;

        Lock lock = this.lockManager.getLock(key);
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            throw new ResourceException(e);
        }
        try {
            resource = get(key);
            if (this.cache != null) {
                this.cache.update(resource);
            }
        } finally {
            lock.release();
        }
        return resource;
    }

    private Resource get(ResourceKey key)
        throws ResourceException {
        Resource resource = (Resource)this.resources.get(key);
        if (resource == null) {
            if (this.resourceIsPersistent) {
                resource = createNewInstanceAndLoad(key);
                // add it to the map
                addResource(key, resource);
                // check the termination time of the service
                if (ResourceSweeper.isExpired(resource)) {
                    // if resource is expired then remove it explicitely
                    remove(key);
                    // throw exception
                    throw new NoSuchResourceException();
                }
            } else {
                throw new NoSuchResourceException();
            }
        }
        return resource;
    }

    /**
     * Removes a resource.
     * If the resource implements the {@link RemoveCallback RemoveCallback}
     * interface the {@link RemoveCallback#remove() RemoveCallback.remove()}
     * operation will be called. The {@link RemoveCallback#remove() 
     * RemoveCallback.remove()} operation is called under a reentrant lock.
     * Therefore, a {@link RemoveCallback#remove() RemoveCallback.remove()}
     * implementation might perform {@link ResourceHome#find(ResourceKey) 
     * ResourceHome.find()} on itself from the same thread but will deadlock
     * if done from another thread.
     *
     * @throws NoSuchResourceException if no resource exists with the given key
     * @throws InvalidResourceKeyException if the resource key is invalid.
     * @throws RemoveNotSupportedException if remove operation is not 
     *         supported.
     * @throws ResourceException if any other error occurs.
     */
    public void remove(ResourceKey key)
        throws ResourceException {
        if (key == null) {
            throw new InvalidResourceKeyException(
                i18n.getMessage("nullArgument", "key"));
        }

        Resource resource = null;

        Lock lock = this.lockManager.getLock(key);
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            throw new ResourceException(e);
        }
        try {
            resource = get(key);

            SetTerminationTimeProvider.sendTerminationNotification(resource);

            if (resource instanceof RemoveCallback) {
                ((RemoveCallback)resource).remove();
            }
            this.resources.remove(key);
            if (this.cache != null) {
                this.cache.remove(resource);
            }
        } finally {
            lock.release();
        }
    }

    protected void add(ResourceKey key, Resource resource) {
        Lock lock = this.lockManager.getLock(key);
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
        try {
            addResource(key, resource);
            if (this.cache != null) {
                this.cache.update(resource);
            }
        } finally {
            lock.release();
        }
    }

    private void addResource(ResourceKey key, Resource resource) {
        this.resources.put(key, resource);
        // schedule sweeper task if needed
        if (this.sweeper != null) {
            this.sweeper.scheduleSweeper();
        }
    }

    /**
     * This ResourceSweeper implementation just returns the resources
     * currently stored in the map. The reason is that the sweeper
     * doesn't have to reactivate/reload a persistent resource if the
     * resource object was reclaimed. So lifetime checks are not
     * done on reclained resources. Lifetime checks have to be done
     * on resource load.
     */
    private static class Sweeper extends ResourceSweeper {

        private TimerManager timerManager;
        private Timer timer;
        private long delay;

        public Sweeper(ResourceHome home, Map resources,
                       TimerManager timerManager, long delay) {
            super(home, resources);
            this.timerManager = timerManager;
            this.delay = delay;
        }

        private synchronized void resetSweeper() {
            this.timer = null;
        }

        synchronized void scheduleSweeper() {
            if (this.timer == null) {
                this.timer = this.timerManager.schedule(this, this.delay);
                this.logger.debug("scheduling sweeper");
            }
        }

        protected Resource getResource(ResourceKey key)
            throws ResourceException {
            return (Resource)this.resources.get(key);
        }

        public void timerExpired(Timer timer) {
            super.timerExpired(timer);
            resetSweeper();
            if (!this.resources.isEmpty()) {
                scheduleSweeper();
            }
        }

    }

}
