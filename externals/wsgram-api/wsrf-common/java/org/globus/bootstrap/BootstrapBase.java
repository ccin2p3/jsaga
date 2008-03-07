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
package org.globus.bootstrap;

import java.io.IOException;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class BootstrapBase {

    private static final Class[] MAIN_PARAMS_TYPE = {String[].class};

    private ArrayList libs = new ArrayList();

    private File getDirectory(String dir) 
        throws BootstrapException {
        File baseDir = new File(dir);
        if (!baseDir.exists() || 
            !baseDir.isDirectory() ||
            !baseDir.canRead()) {
            throw new BootstrapException(baseDir + " does not exist, is not a directory, or is not readable");
        }
        try {
            return baseDir.getCanonicalFile();
        } catch (IOException e) {
            throw new BootstrapException("Failed to get the canonical form of " + dir);
        }
    }

    public void addDirectory(String dir)
        throws BootstrapException {
        File baseDir = getDirectory(dir);

        try {
            this.libs.add(baseDir.toURL());
        } catch (IOException e) {
            throw new BootstrapException("Error during startup processing", e);
        }
    }

    public void addLibDirectory(String dir)
        throws BootstrapException {
        addLibDirectory(dir, null);
    }

    public void addLibDirectory(String dir, String filter)
        throws BootstrapException {
        File baseDir = getDirectory(dir);

        File libDir = new File(baseDir, "lib");
        if (!libDir.exists() || 
            !libDir.isDirectory() ||
            !libDir.canRead()) {
            throw new BootstrapException(libDir + " does not exist, is not a directory, or is not readable");
        }
        
        try {
            File[] jars = libDir.listFiles(new JarFilter(filter));
            for (int i=0;i<jars.length;i++) {
                this.libs.add(jars[i].toURL());
            }
        } catch (IOException e) {
            throw new BootstrapException("Error during startup processing", e);
        }
    }

    public void launch(String launchClass, String [] launchArgs)
        throws BootstrapException {
        
        URL[] urlJars = new URL[this.libs.size()];
        urlJars = (URL[])this.libs.toArray(urlJars);
        
        URLClassLoader loader = new URLClassLoader(urlJars);

        Thread.currentThread().setContextClassLoader(loader);

        try {
            Class mainClass = loader.loadClass(launchClass);

            Method mainMethod = mainClass.getMethod("main", MAIN_PARAMS_TYPE);

            mainMethod.invoke(null, new Object[] {launchArgs});
        } catch (ClassNotFoundException e) {
            throw new BootstrapException("Class '" + launchClass + "' not found");
        } catch (NoSuchMethodException e) {
            throw new BootstrapException("Class '" + launchClass + "' has no main(String[]) method");
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            throw new BootstrapException("Error during startup processing", cause);
        } catch (IllegalAccessException e) {
            throw new BootstrapException("Error during startup processing", e);
        }
    }
    
    private static class JarFilter implements FilenameFilter {
        
        private String filter;
        
        public JarFilter(String filter) {
            this.filter = (filter == null) ? ".jar" : filter;
        }
        
        public boolean accept(File dir, String name) {
            return (name.endsWith(this.filter));
        }
    }
        
}
