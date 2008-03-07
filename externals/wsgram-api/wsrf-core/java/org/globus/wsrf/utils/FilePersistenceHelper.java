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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.Constants;
import org.globus.wsrf.container.ServiceHost;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceKey;
import org.globus.util.I18n;

/**
 * This helper contains common functions for managing persistent information
 * stored as files.
 */
public class FilePersistenceHelper {

    private static I18n i18n = 
        I18n.getI18n(Resources.class.getName());
    
    public static final String SERVER_ID = 
        Constants.CONTAINER_PROPERTY + ".server.id";
    
    private static final String FILE_PERSISTENCE_DIR =
        System.getProperty("user.home") + File.separatorChar +
        ".globus" + File.separatorChar + "persisted";
    
    private static Log logger =
        LogFactory.getLog(FilePersistenceHelper.class.getName());
    
    protected Class beanClass;
    protected File storageDir;
    protected String fileSuffix;

    private static String getServerID() {
        String id = System.getProperty(SERVER_ID);
        if (id == null) {
            try {
                id = ServiceHost.getDefaultHost();
            } catch (IOException e) {
                id = "localhost";
            }
        } 
        return id;
    }

    private static String getDefaultStorageDir(Class beanClass) 
        throws IOException {
        if (beanClass == null) {
            return null;
        }
        String fullClassName = beanClass.getName();
        String className = fullClassName.substring(
                      beanClass.getPackage().getName().length() + 1);
        
        String dir = FILE_PERSISTENCE_DIR + File.separatorChar +  
            getServerID() + File.separatorChar + className;

        File baseDir = new File(FILE_PERSISTENCE_DIR);
        File storageDir = new File(dir);
        if (!storageDir.getCanonicalPath().startsWith(baseDir.getCanonicalPath())) {
            throw new IOException(i18n.getMessage("invalidStorageDir", dir));
        }
        
        return dir;
    }

    /**
     * Creates FilePersistenceHelper with default storage directory based on
     * the beanClass name and specified suffix.
     */
    public FilePersistenceHelper(Class beanClass, 
                                 String suffix) 
        throws IOException {
        this(beanClass, getDefaultStorageDir(beanClass), suffix);
    }

    /**
     * Creates FilePersistenceHelper with specific storage directory and
     * file suffix.
     */
    public FilePersistenceHelper(Class beanClass, 
                                 String storageDir, 
                                 String suffix) 
        throws IOException {
        if (beanClass == null) {
            throw new IllegalArgumentException(
                    i18n.getMessage("nullArgument", "beanClass"));
        }
        if (storageDir == null) {
            throw new IllegalArgumentException(
                    i18n.getMessage("nullArgument", "storageDir"));
        }
        if (suffix == null) {
            throw new IllegalArgumentException(
                    i18n.getMessage("nullArgument", "suffix"));
        }
        this.beanClass = beanClass;
        this.fileSuffix = suffix;
        setStorageDirectory(storageDir);
    }

    protected void setStorageDirectory(String strDir) 
        throws IOException {
        File dir = new File(strDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException(i18n.getMessage("storDirFailed", dir));
            }
        } else {
            if (!dir.canWrite() || !dir.canRead()) {
                throw new IOException(i18n.getMessage("storDirPerm", dir));
            }
        }
        this.storageDir = dir;
    }

    public File getStorageDirectory() {
        return this.storageDir;
    }

    public String getFileSuffix() {
        return this.fileSuffix;
    }

    public Class getBeanClass() {
        return this.beanClass;
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
     * @param key the key of the object
     * @return File
     */
    public File getKeyAsFile(Object key) {
        if (logger.isDebugEnabled()) {
            logger.debug("Type of key is: " + key.getClass().getName());
        }
        Object keyValue = key;
        if (keyValue instanceof ResourceKey) {
            //this is ugly! Key business is messy. Must make it less ambiguous.
            keyValue = ((ResourceKey)key).getValue();
        }
        
        File file = new File(getStorageDirectory(), 
                             keyValue + this.fileSuffix);
        if (logger.isDebugEnabled()) {
            logger.debug("File used for persistence: " + 
                         file.getAbsolutePath());
        }
        return file;
    }

    public void remove(Object key)
        throws ResourceException {
        File f = getKeyAsFile(key);
        if (logger.isDebugEnabled()) {
            logger.debug("Deleting state file for: " + key);
        }
        f.delete();
    }

    /**
     * This function returns the keys of the objects that have been stored.
     * This should be used by the home in order to recover state,
     * by listing all the stored object keys and adding them to the
     * map of resources. The consumer code must test for null.
     *
     * @return List the list of key values
     */
    public List list()
        throws IOException {
        logger.debug("Loading the list of resource keys");

        //read list of persistence resource files
        File persistenceDirectory = getStorageDirectory();
        File [] keyFiles = 
            persistenceDirectory.listFiles(new Filter(this.fileSuffix));
        List keyValueList = null;
        if (keyFiles != null) {
            keyValueList = new ArrayList(keyFiles.length);
            for (int i = 0; i < keyFiles.length; i++) {
                String fileName = keyFiles[i].getName();
                int keyEnd = fileName.lastIndexOf(this.fileSuffix);
                String keyValue = fileName.substring(0, keyEnd);
                keyValueList.add(keyValue);
            }
        } else {
            logger.debug("Persistence directory does not exist yet");
            keyValueList = new ArrayList(0);
        }

        return keyValueList;
    }

    /**
     * Removes all stored objects from file system.
     */
    public void removeAll()
        throws IOException {
        File persistenceDirectory = getStorageDirectory();
        File [] keyFiles = 
            persistenceDirectory.listFiles(new Filter(this.fileSuffix));
        for (int i = 0; i < keyFiles.length; i++) {
            keyFiles[i].delete();
        }
    }

    private static class Filter implements FilenameFilter {

        private String suffix;

        public Filter(String suffix) {
            this.suffix = suffix;
        }

        public boolean accept(File dir, String name) {
            return (name.endsWith(this.suffix));
        }
    }

}
