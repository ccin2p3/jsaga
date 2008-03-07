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

import java.util.Iterator;
import java.util.List;

import javax.xml.soap.SOAPElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Element;

import org.globus.wsrf.ResourceProperty;
import org.globus.wsrf.ResourcePropertyMetaData;
import org.globus.wsrf.encoding.SerializationException;

/**
 * A wrapper for a ResourceProperty object. If
 * {@link PersistentResourceProperty#set set} is called it will set the dirty
 * flag of the associated {@link DirtyFlagHolder DirtyFlagHolder} to true.
 *
 * @see PersistentReflectionResource
 */
public class PersistentResourceProperty implements ResourceProperty {

    private ResourceProperty rp;
    private DirtyFlagHolder dirtyFlagHolder;

    /**
     * @param resourceProperty ResourceProperty to wrap
     * @param dirtyFlagHolder DirtyFlagHolder to be set to "dirty" when the
     *                                        property is changed
     * @throws Exception
     */
    public PersistentResourceProperty(ResourceProperty resourceProperty,
                                      DirtyFlagHolder dirtyFlagHolder)
        throws Exception {
        this.rp = resourceProperty;
        this.dirtyFlagHolder = dirtyFlagHolder;
    }

    public void set(int index, Object value) {
        rp.set(index,value);
        dirtyFlagHolder.setDirty(true);
    }

    public void add(Object value) {
        rp.add(value);
        dirtyFlagHolder.setDirty(true);
    }

    public boolean remove(Object value) {
        if (rp.remove(value)) {
            dirtyFlagHolder.setDirty(true);
            return true;
        } else {
            return false;
        }
    }

    public Object get(int index) { return rp.get(index); }

    public void clear() {
        rp.clear();
        dirtyFlagHolder.setDirty(true);
    }

    public int size() { return rp.size(); }

    public boolean isEmpty() { return rp.isEmpty(); }

    public Iterator iterator() { return rp.iterator(); }

    public SOAPElement[] toSOAPElements()
        throws SerializationException { return rp.toSOAPElements(); }

    public Element[] toElements()
        throws SerializationException { return rp.toElements(); }

    public ResourcePropertyMetaData getMetaData() { return rp.getMetaData(); }

}
