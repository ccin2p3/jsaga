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
package org.oasis;

import java.util.Iterator;

import javax.xml.namespace.QName;

public interface AnyListType {

    /**
     * Iterator over values.
     */
    Iterator iterator();

    /**
     * Returns element name associated with the 
     * given type.
     */
    QName getElementName(Class type);
    
    /**
     * Returns class for the given element name
     */
    Class getType(QName elementName);
    
    void add(Object value);
    
    int size();
    
    Object get(int index);
    
}
