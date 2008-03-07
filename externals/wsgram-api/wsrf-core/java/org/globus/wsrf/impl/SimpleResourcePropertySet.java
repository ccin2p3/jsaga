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

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.StringWriter;

import org.globus.wsrf.ResourceProperty;
import org.globus.wsrf.ResourcePropertySet;
import org.globus.wsrf.ResourcePropertyMetaData;
import org.globus.wsrf.encoding.SerializationException;
import org.globus.wsrf.utils.XmlUtils;
import org.globus.wsrf.utils.AnyHelper;
import org.globus.wsrf.utils.StringBufferReader;
import org.globus.util.I18n;
import org.globus.wsrf.utils.Resources;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;

import org.apache.axis.message.MessageElement;

/**
 * Simple in-memory implementation of <code>ResourcePropertySet</code>.
 *
 * This class is not thread-safe.
 */
public class SimpleResourcePropertySet implements ResourcePropertySet {

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    private QName name;
    private Map propertiesMap;
    private List propertiesList;
    private boolean openContent;
    private boolean localNameMatching = false;
    
    public SimpleResourcePropertySet(QName name) {
        this.propertiesMap = new HashMap();
        this.propertiesList = new ArrayList();
        this.name = name;
    }

    public QName getName() {
        return this.name;
    }

    public ResourceProperty create(ResourcePropertyMetaData rpMetaData) {
        return new SimpleResourceProperty(rpMetaData);
    }

    public ResourceProperty get(QName name) {
        ResourceProperty prop = (ResourceProperty)this.propertiesMap.get(name);
        if (prop != null) {
            return prop;
        }
        if (this.localNameMatching) {
            // try local name matching
            Iterator iter = this.propertiesMap.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry entry = (Map.Entry)iter.next();
                QName rp = (QName)entry.getKey();
                if (rp.getLocalPart().equals(name.getLocalPart())) {
                    return (ResourceProperty)entry.getValue();
                }
            }
        }
        return null;
    }

    public boolean add(ResourceProperty property) {
        QName name = property.getMetaData().getName();

        Object previous = this.propertiesMap.put(name, property);
        if (previous != null) {
            int pos = this.propertiesList.indexOf(previous);
            this.propertiesList.set(pos, property);
        } else {
            this.propertiesList.add(property);
        }

        return true;
    }

    public boolean remove(QName name) {
        ResourceProperty prop = 
            (ResourceProperty)this.propertiesMap.remove(name);
        if (prop != null) {
            this.propertiesList.remove(prop);
            return true;
        } else {
            return false;
        }
    }
    
    public Iterator iterator() {
        return this.propertiesList.iterator();
    }

    public boolean isOpenContent() {
        return this.openContent;
    }

    public void clear() {
        this.propertiesMap.clear();
        this.propertiesList.clear();
    }

    public int size() {
        return this.propertiesMap.size();
    }

    public boolean isEmpty() {
        return this.propertiesMap.isEmpty();
    }
    
    /**
     * Configures open content property.
     *
     * @param open true if arbitrary resource property can be added to the set.
     */
    public void setOpenContent(boolean open) {
        this.openContent = open;
    }
    
    /**
     * Returns whether local name matching is allowed. Local name matching
     * is disabled by default.
     *
     * @return true if local name matching is allowed. False otherwise.
     */
    public boolean isLocalNameMatching() {
        return this.localNameMatching;
    }

    /**
     * Configures if ResourceProperty can be retrieved just by matching the
     * local name (namespace part is ignored).
     *
     * @param matching true to allow local name matching.
     */
    public void setLocalNameMatching(boolean matching) {
        this.localNameMatching = matching;
    }
 
    public Element toElement() 
        throws SerializationException {
        return toElementSpecific();
    }

    /* 
     * This method is general purpose method and will work with any RP.
     * However, it can be slow on large RPs.
     */
    protected Element toElementGeneral()
        throws SerializationException {
        Document doc = null;
        try {
            doc = XmlUtils.newDocument();
        } catch (Exception e) {
            throw new SerializationException(
                            i18n.getMessage("rpDocSerializationError"), e);
        }

        Element rootElement = doc.createElementNS(this.name.getNamespaceURI(),
                                                  this.name.getLocalPart());
        doc.appendChild(rootElement);

        Iterator iter = iterator();
        while(iter.hasNext()) {
            ResourceProperty prop = (ResourceProperty)iter.next();
            Element [] values = prop.toElements();
            if (values == null) {
                continue;
            }
            for (int i=0;i<values.length;i++) {
                // they will not match but for future reference
                if (doc == values[i].getOwnerDocument()) {
                    rootElement.appendChild(values[i]);
                } else {
                    rootElement.appendChild(doc.importNode(values[i], true));
                }
            }
        }

        return rootElement;
    }

    /* 
     * This method relies on the fact that all SOAPElements are really
     * Axis' MessageElements. If SOAPElement returned by RP is not a
     * MessageElement this method will fail.
     */
    protected Element toElementSpecific()
        throws SerializationException {

        StringWriter writer = new StringWriter();
        writer.write("<ns0:" + this.name.getLocalPart() + " xmlns:ns0=\"" +
                     this.name.getNamespaceURI() + "\">");
        
        Iterator iter = iterator();
        while(iter.hasNext()) {
            ResourceProperty prop = (ResourceProperty)iter.next();
            SOAPElement [] values = prop.toSOAPElements();
            if (values == null) {
                continue;
            }
            for (int i=0;i<values.length;i++) {
                if (values[i] instanceof MessageElement) {
                    try {
                        AnyHelper.write(writer, (MessageElement)values[i]);
                    } catch (Exception e) {
                        throw new SerializationException(
                            i18n.getMessage("rpDocSerializationError"), e);
                    }
                } else {
                    return toElementGeneral();
                }
            }
        }

        writer.write("</ns0:" + this.name.getLocalPart() + ">");
        writer.flush();

        StringBufferReader reader = 
            new StringBufferReader(writer.getBuffer());

        try {
            Document doc = XmlUtils.newDocument(new InputSource(reader));
            return doc.getDocumentElement();
        } catch (Exception e) {
            throw new SerializationException(
                            i18n.getMessage("rpDocSerializationError"), e);
        }
    }

    public SOAPElement toSOAPElement()
        throws SerializationException {
        MessageElement rootElement = 
            new MessageElement(this.name.getNamespaceURI(),
                               this.name.getLocalPart());
        
        Iterator iter = iterator();
        while(iter.hasNext()) {
            ResourceProperty prop = (ResourceProperty)iter.next();
            SOAPElement [] values = prop.toSOAPElements();
            if (values == null) {
                continue;
            }
            try {
                for (int i=0;i<values.length;i++) {
                    rootElement.addChildElement(values[i]);
                }
            } catch (Exception e) {
                throw new SerializationException(
                                i18n.getMessage("rpDocSerializationError"), e);
            }
        }
        
        return rootElement;
    }
    
}
