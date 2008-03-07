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
package org.globus.wsrf.impl.security.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;

import java.lang.reflect.Proxy;

public class FixedObjectInputStream extends ObjectInputStream {
    
    public FixedObjectInputStream(InputStream in) 
        throws IOException, StreamCorruptedException {
        super(in);
    }

    protected Class resolveClass(ObjectStreamClass v)
        throws IOException, ClassNotFoundException {
        
        ClassLoader loader = FixedObjectInputStream.class.getClassLoader();
        return Class.forName(v.getName(), false, loader);
    }
    
    protected Class resolveProxyClass(String[] interfaces)
	throws IOException, ClassNotFoundException {
        
        ClassLoader loader = FixedObjectInputStream.class.getClassLoader();

	Class[] classObjs = new Class[interfaces.length];
	for (int i = 0; i < interfaces.length; i++) {
	    classObjs[i] = Class.forName(interfaces[i], false, loader);
	}
	try {
	    return Proxy.getProxyClass(loader, classObjs);
	} catch (IllegalArgumentException e) {
	    throw new ClassNotFoundException(null, e);
	}
    }
}
