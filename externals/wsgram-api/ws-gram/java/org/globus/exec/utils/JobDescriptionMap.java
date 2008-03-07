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
package org.globus.exec.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.exec.generated.JobDescriptionType;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class JobDescriptionMap extends HashMap {
    static Log logger = LogFactory.getLog(JobDescriptionMap.class);

    //privatized constructors
    public JobDescriptionMap() { }
    /*
    private JobDescriptionMap(int initialCapacity) { }
    private JobDescriptionMap(int initialCapacity, float loadFactor) { }
    private JobDescriptionMap(Map m) { }
    */

    public JobDescriptionMap(
            JobDescriptionType                  jobDescription)
            throws                              IntrospectionException,
                                                IllegalAccessException,
                                                InvocationTargetException {
        //populate the HashMap using read methods of the jobDescription bean
        BeanInfo beanInfo
            = Introspector.getBeanInfo(jobDescription.getClass());
        PropertyDescriptor[] propertyDescriptors
            = beanInfo.getPropertyDescriptors();
        if (logger.isDebugEnabled()) {
            logger.debug("processing " + propertyDescriptors.length
                        + " properties...");
        }
        for (int index=0; index<propertyDescriptors.length; index++) {
            String name = propertyDescriptors[index].getName();
            if (name.equals("serializer") || name.equals("deserializer")
                            || name.equals("class")) {
                continue;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("processing property " + name + "...");
            }

            Method readMethod = propertyDescriptors[index].getReadMethod();
            super.put(name, readMethod.invoke(jobDescription, null));
        }
    }
}
