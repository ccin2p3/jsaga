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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Element;

import org.globus.wsrf.ResourcePropertyMetaData;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.encoding.SerializationException;
import org.globus.wsrf.utils.Resources;
import org.globus.util.I18n;

import org.apache.axis.Constants;
import org.apache.axis.utils.cache.MethodCache;

public class ReflectionResourceProperty extends BaseResourceProperty {

    private static Log logger =
        LogFactory.getLog(ReflectionResourceProperty.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    private static final Object[] NULL_ARGS =
        new Object[] {null};

    private static MethodCache methodCache = MethodCache.getInstance();

    private String propertyName;
    private Object obj;
    private Method getMethod;
    private Method setMethod;

    /**
     * Used by subclasses only. Subclasses must call {@link #setObject(Object)
     * setObject()} and {@link #setPropertyName(String) setPropertyName()}
     * and {@link #initialize() initialize()} first.
     */
    protected ReflectionResourceProperty(ResourcePropertyMetaData metaData) {
        super(metaData);
    }

    public ReflectionResourceProperty(ResourcePropertyMetaData metaData,
                                      String propertyName,
                                      Object obj)
        throws Exception {
        this(metaData);
        setObject(obj);
        setPropertyName(propertyName);
        initialize();
    }

    public ReflectionResourceProperty(QName name,
                                      String propertyName,
                                      Object obj)
        throws Exception {
        this(new SimpleResourcePropertyMetaData(name),
             propertyName,
             obj);
    }

    public ReflectionResourceProperty(QName name, Object obj)
        throws Exception {
        this(new SimpleResourcePropertyMetaData(name),
             name.getLocalPart(),
             obj);
    }

    public ReflectionResourceProperty(ResourcePropertyMetaData metaData,
                                      Object obj)
        throws Exception {
        this(metaData,
             metaData.getName().getLocalPart(),
             obj);
    }

    protected void setPropertyName(String propertyName) {
        if (propertyName == null) {
            throw new IllegalArgumentException(
                i18n.getMessage("nullArgument", "propertyName"));
        }
        this.propertyName = propertyName;
    }

    protected void setObject(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException(
                i18n.getMessage("nullArgument", "obj"));
        }
        this.obj = obj;
    }

    /**
     * Must be called after object and propertyName have been set.
     */
    protected void initialize()
        throws Exception {
        if (this.obj == null || this.propertyName == null) {
            throw new Exception(
                i18n.getMessage("reflectionRPMembersNotInitialized"));
        }

        String methodPostfix = 
            Character.toUpperCase(this.propertyName.charAt(0)) +
            this.propertyName.substring(1);
                                  
        try {
            this.getMethod = methodCache.getMethod(this.obj.getClass(),
                                                   "get" + methodPostfix, 
                                                   null);
        } catch (NoSuchMethodException e) {
        }

        if (this.getMethod == null) {
            try {
                //this may be a boolean/Boolean JavaBean property
                this.getMethod = methodCache.getMethod(this.obj.getClass(),
                                                       "is" + methodPostfix, 
                                                       null);
            } catch (NoSuchMethodException e) {
            }
        }

        if (this.getMethod == null) {
            throw new Exception(
                i18n.getMessage("reflectionRPNoAccessorMethod", 
                                this.metaData.getName()));
        } else {
            logger.debug("Found get method: " + this.getMethod);
        }

        if (!this.metaData.isReadOnly()) {
            try {
                this.setMethod = methodCache.getMethod(
                          this.obj.getClass(),
                          "set" + methodPostfix,
                          new Class [] {this.getMethod.getReturnType()});
                logger.debug("Found set method: " + this.setMethod);
            } catch (NoSuchMethodException e) {
            }
            
            if (this.setMethod == null) {
                logger.debug("Did not find set method");
            } else {
                logger.debug("Found set method: " + this.setMethod);
            }
        }

        Class type = null;

        Class returnType = this.getMethod.getReturnType();
        if (isArray(returnType)) {
            type = returnType.getComponentType();
        } else if (List.class.isAssignableFrom(returnType)) {
            type = this.metaData.getType();
        } else {
            type = returnType;
        }

        QName xmlType = null;
        if (this.metaData instanceof SimpleResourcePropertyMetaData) {
            xmlType = ((SimpleResourcePropertyMetaData)this.metaData).getXmlType();
        }
        
        // update meta data info
        this.metaData =
            new SimpleResourcePropertyMetaData(this.metaData.getName(),
                                               this.metaData.getMinOccurs(),
                                               this.metaData.getMaxOccurs(),
                                               this.metaData.isNillable(),
                                               type,
                                               this.metaData.isReadOnly(),
                                               xmlType);
    }

    private RuntimeException handleException(Throwable e) {
        logger.debug("", e);
        if (e instanceof InvocationTargetException) {
            e = ((InvocationTargetException)e).getTargetException();
        }

        if (e instanceof RuntimeException) {
            return (RuntimeException)e;
        } else {
            return new RuntimeException(e.getMessage());
        }
    }

    protected boolean isArray(Class returnType) {
        if (!returnType.isArray()) {
            return false;
        }
        
        return (!(byte[].class == returnType &&
                  Constants.XSD_BASE64.equals(((SimpleResourcePropertyMetaData)this.metaData).getXmlType())));
    }

    // ----------------- IMPL FUNCTIONS ------------------------

    public int size() {
        Class returnType = this.getMethod.getReturnType();
        if (isArray(returnType)) {
            return sizeArray();
        } else if (List.class.isAssignableFrom(returnType)) {
            return sizeList();
        } else {
            return sizeSimple();
        }
    }

    public boolean isEmpty() {
        Class returnType = this.getMethod.getReturnType();
        if (isArray(returnType)) {
            return isEmptyArray();
        } else if (List.class.isAssignableFrom(returnType)) {
            return isEmptyList();
        } else {
            return isEmptySimple();
        }
    }

    public Iterator iterator() {
        Class returnType = this.getMethod.getReturnType();
        if (isArray(returnType)) {
            return iteratorArray();
        } else if (List.class.isAssignableFrom(returnType)) {
            return iteratorList();
        } else {
            return iteratorSimple();
        }
    }

    public Object get(int index) {
        Class returnType = this.getMethod.getReturnType();
        if (isArray(returnType)) {
            return getArray(index);
        } else if (List.class.isAssignableFrom(returnType)) {
            return getList(index);
        } else {
            return getSimple(index);
        }
    }

    public void set(int index, Object value) {
        Class returnType = this.getMethod.getReturnType();
        if (isArray(returnType)) {
            setArray(index, value);
        } else if (List.class.isAssignableFrom(returnType)) {
            setList(index, value);
        } else {
            setSimple(index, value);
        }
    }

    public void add(Object value) {
        Class returnType = this.getMethod.getReturnType();
        if (isArray(returnType)) {
            addArray(value);
        } else if (List.class.isAssignableFrom(returnType)) {
            addList(value);
        } else {
            addSimple(value);
        }
    }

    /* (non-Javadoc)
     * @see org.globus.wsrf.ResourceProperty#remove(java.lang.Object)
     */
    public boolean remove(Object value) {
        Class returnType = this.getMethod.getReturnType();
        if (isArray(returnType)) {
            return removeArray(value);
        } else if (List.class.isAssignableFrom(returnType)) {
            return removeList(value);
        } else {
            return removeSimple(value);
        }
    }

    public void clear() {
        Class returnType = this.getMethod.getReturnType();
        if (isArray(returnType)) {
            clearArray();
        } else if (List.class.isAssignableFrom(returnType)) {
            clearList();
        } else {
            clearSimple();
        }
    }

    public SOAPElement[] toSOAPElements()
        throws SerializationException {
        Class returnType = this.getMethod.getReturnType();
        if (isArray(returnType)) {
            return toSOAPElementArray();
        } else if (List.class.isAssignableFrom(returnType)) {
            return toSOAPElementList();
        } else {
            return toSOAPElementSimple();
        }
    }

    public Element[] toElements()
        throws SerializationException {
        Class returnType = this.getMethod.getReturnType();
        if (isArray(returnType)) {
            return toElementArray();
        } else if (List.class.isAssignableFrom(returnType)) {
            return toElementList();
        } else {
            return toElementSimple();
        }
    }

    // ***************** Simple property methods ***************

    protected Object getValueSimple() {
        try {
            return this.getMethod.invoke(this.obj, null);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private int sizeSimple() {
        Object returnValue = getValueSimple();
        return (returnValue == null) ? 0 : 1;
    }

    private boolean isEmptySimple() {
        return (sizeSimple() == 0);
    }

    private Iterator iteratorSimple() {
        List list = null;
        Object returnValue = getValueSimple();
        if (returnValue != null) {
            list = new ArrayList(1);
            list.add(returnValue);
        } else {
            list = new ArrayList(0);
        }
        return list.iterator();
    }

    private Object getSimple(int index) {
        if (index != 0) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        Object returnValue = getValueSimple();
        return returnValue;
    }

    private void setSimple(int index, Object value) {
        if (this.setMethod == null) {
            throw new UnsupportedOperationException();
        }
        if (index != 0) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        value = convert(value);
        try {
            this.setMethod.invoke(this.obj, new Object[] {value});
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private void clearSimple() {
        if (this.setMethod == null) {
            throw new UnsupportedOperationException();
        }
        if (!this.metaData.getType().isPrimitive()) {
            try {
                this.setMethod.invoke(this.obj, NULL_ARGS);
            } catch (Exception e) {
                throw handleException(e);
            }
        }
    }

    private void addSimple(Object value) {
        Object returnValue = getValueSimple();
        if (returnValue == null || this.metaData.getType().isPrimitive()) {
            setSimple(0, value);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private boolean removeSimple(Object value) {
        if (this.setMethod == null || this.metaData.getType().isPrimitive()) {
            throw new UnsupportedOperationException();
        }
        Object currentObject = getValueSimple();
        Object convertedValue = convert(value);
        if (convertedValue.equals(currentObject)) {
            try {
                this.setMethod.invoke(this.obj, NULL_ARGS);
            } catch (Exception e) {
                throw handleException(e);
            }
            return true;
        }
        return false;
    }

    private SOAPElement[] toSOAPElementSimple()
        throws SerializationException {
        Object returnValue = getValueSimple();
        SOAPElement [] elements = null;
        boolean nillable = this.metaData.isNillable();
        QName name = getMetaData().getName();
        if (returnValue == null) {
            if (nillable) {
                elements = new SOAPElement[1];
                elements[0] = ObjectSerializer.toSOAPElement(returnValue,
                                                             name,
                                                             nillable);
            }
        } else {
            elements = new SOAPElement[1];
            elements[0] = ObjectSerializer.toSOAPElement(returnValue,
                                                         name,
                                                         nillable);
        }
        return elements;
    }

    private Element[] toElementSimple()
        throws SerializationException {
        Object returnValue = getValueSimple();
        Element [] elements = null;
        boolean nillable = this.metaData.isNillable();
        QName name = getMetaData().getName();
        if (returnValue == null) {
            if (nillable) {
                elements = new Element[1];
                elements[0] = ObjectSerializer.toElement(returnValue,
                                                         name,
                                                         nillable);
            }
        } else {
            elements = new Element[1];
            elements[0] = ObjectSerializer.toElement(returnValue,
                                                     name,
                                                     nillable);
        }
        return elements;
    }

    // ************* Array-based property methods *************

    private Object getValueArray() {
        try {
            return this.getMethod.invoke(this.obj, null);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private int sizeArray() {
        Object array = getValueArray();
        return (array == null) ? 0 : Array.getLength(array);
    }

    private boolean isEmptyArray() {
        return (sizeArray() == 0);
    }

    private Iterator iteratorArray() {
        List list = null;
        Object array = getValueArray();
        if (array == null) {
            list = new ArrayList(0);
        } else {
            list = Arrays.asList((Object[])array);
        }
        return list.iterator();
    }

    private Object getArray(int index) {
        Object array = getValueArray();
        return Array.get(array, index);
    }

    private void setArray(int index, Object value) {
        Object array = getValueArray();
        if (array == null) {
            String errorMessage = "Value was not initialized.";
            throw new NullPointerException(errorMessage);
        }
        Array.set(array, index, convert(value));
    }

    private void clearArray() {
        if (this.setMethod == null) {
            throw new UnsupportedOperationException();
        }
        try {
            this.setMethod.invoke(this.obj, NULL_ARGS);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private void addArray(Object value) {
        if (this.setMethod == null) {
            throw new UnsupportedOperationException();
        }
        try {
            Object array = this.getMethod.invoke(this.obj, null);
            Class componentType =
                this.getMethod.getReturnType().getComponentType();
            Object newArray = null;
            if (array == null) {
                newArray = Array.newInstance(componentType, 1);
                Array.set(newArray, 0, convert(value));
            } else {
                int len = Array.getLength(array);
                newArray = Array.newInstance(componentType, len + 1);
                System.arraycopy(array, 0, newArray, 0, len);
                Array.set(newArray, len, convert(value));
            }
            this.setMethod.invoke(this.obj, new Object[] {newArray});
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private boolean removeArray(Object value) {
        Object array = getValueArray();
        if (array == null) {
            return false;
        }
        Object convertedValue = convert(value);

        int len = Array.getLength(array);
        int j = 0;
        Object currValue = null;
        for (int i = 0; i < len; i++) {
            currValue = Array.get(array, i);
            if (!currValue.equals(convertedValue)) {
                if (j != i) {
                    Array.set(array, j, currValue);
                }
                j++;
            }
        }

        if (j != len) {
            Object newArray = null;
            if (j > 0) {
                Class componentType =
                    this.getMethod.getReturnType().getComponentType();
                newArray = Array.newInstance(componentType, j);
                System.arraycopy(array, 0, newArray, 0, j);
            }
            try {
                this.setMethod.invoke(this.obj, new Object[] {newArray});
            } catch (Exception e) {
                throw handleException(e);
            }
            return true;
        } else {
            return false;
        }
    }

    private SOAPElement[] toSOAPElementArray()
        throws SerializationException {
        Object array = getValueArray();
        SOAPElement [] elements = null;
        boolean nillable = this.metaData.isNillable();
        QName name = getMetaData().getName();
        if (array == null || Array.getLength(array) == 0) {
            if (nillable) {
                elements = new SOAPElement[1];
                elements[0] = ObjectSerializer.toSOAPElement(null,
                                                             name,
                                                             nillable);
            }
        } else {
            int len = Array.getLength(array);
            Object value = null;
            elements = new SOAPElement[len];
            for (int i=0;i<len;i++) {
                value = Array.get(array, i);
                elements[i] = ObjectSerializer.toSOAPElement(value,
                                                             name,
                                                             nillable);
            }
        }
        return elements;
    }

    private Element[] toElementArray()
        throws SerializationException {
        Object array = getValueArray();
        Element [] elements = null;
        boolean nillable = this.metaData.isNillable();
        QName name = getMetaData().getName();
        if (array == null || Array.getLength(array) == 0) {
            if (nillable) {
                elements = new Element[1];
                elements[0] = ObjectSerializer.toElement(null,
                                                         name,
                                                         nillable);
            }
        } else {
            int len = Array.getLength(array);
            Object value = null;
            elements = new Element[len];
            for (int i=0;i<len;i++) {
                value = Array.get(array, i);
                elements[i] = ObjectSerializer.toElement(value,
                                                         name,
                                                         nillable);
            }
        }
        return elements;
    }

    // ************** List-based property methods ***************

    private List getValueList() {
        try {
            return (List)this.getMethod.invoke(this.obj, null);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private int sizeList() {
        List returnValue = getValueList();
        return (returnValue == null) ? 0 : returnValue.size();
    }

    private boolean isEmptyList() {
        return (sizeList() == 0);
    }

    private Iterator iteratorList() {
        List list = null;
        List returnValue = getValueList();
        if (returnValue == null) {
            list = new ArrayList(0);
        } else {
            list = returnValue;
        }
        return list.iterator();
    }

    private Object getList(int index) {
        List returnValue =  getValueList();
        return returnValue.get(index);
    }

    private void setList(int index, Object value) {
        List returnValue = getValueList();
        returnValue.set(index, convert(value));
    }

    private void clearList() {
        List returnValue = getValueList();
        returnValue.clear();
    }

    private void addList(Object value) {
        List returnValue = getValueList();
        if (returnValue == null) {
            if (this.setMethod == null) {
                throw new UnsupportedOperationException();
            }
            try {
                returnValue =
                    (List)this.getMethod.getReturnType().newInstance();
            } catch (Exception e) {
                throw handleException(e);
            }
            returnValue.add(convert(value));
            try {
                this.setMethod.invoke(this.obj, new Object[] {returnValue});
            } catch (Exception e) {
                throw handleException(e);
            }
        } else {
            returnValue.add(convert(value));
        }
    }

    private boolean removeList(Object value) {
        List returnValue = getValueList();
        boolean result;
        if(returnValue == null) {
            result = false;
        } else {
            result = returnValue.remove(convert(value));
        }
        return result;
    }

    private SOAPElement[] toSOAPElementList()
        throws SerializationException {
        List returnValue = getValueList();
        SOAPElement [] elements = null;
        boolean nillable = this.metaData.isNillable();
        QName name = getMetaData().getName();
        if (returnValue == null || returnValue.size() == 0) {
            if (nillable) {
                elements = new SOAPElement[1];
                elements[0] = ObjectSerializer.toSOAPElement(returnValue,
                                                             name,
                                                             nillable);
            }
        } else {
            elements = new SOAPElement[returnValue.size()];
            Iterator iter = returnValue.iterator();
            int i = 0;
            while(iter.hasNext()) {
                elements[i++] = ObjectSerializer.toSOAPElement(iter.next(),
                                                               name,
                                                               nillable);
            }
        }
        return elements;
    }

    private Element[] toElementList()
        throws SerializationException {
        List returnValue = getValueList();
        Element [] elements = null;
        boolean nillable = this.metaData.isNillable();
        QName name = getMetaData().getName();
        if (returnValue == null || returnValue.size() == 0) {
            if (nillable) {
                elements = new Element[1];
                elements[0] = ObjectSerializer.toElement(returnValue,
                                                         name,
                                                         nillable);
            }
        } else {
            elements = new Element[returnValue.size()];
            Iterator iter = returnValue.iterator();
            int i = 0;
            while(iter.hasNext()) {
                elements[i++] = ObjectSerializer.toElement(iter.next(),
                                                           name,
                                                           nillable);
            }
        }
        return elements;
    }

}
