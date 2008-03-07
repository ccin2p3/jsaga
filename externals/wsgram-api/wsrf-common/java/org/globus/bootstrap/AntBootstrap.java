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

public class AntBootstrap extends BootstrapBase {

    private static final String GLOBUS_LOCATION = "GLOBUS_LOCATION";
    private static final String ANT_HOME = "ANT_HOME";

    public static void main(String[] args) throws BootstrapException {
        
        String globusLocation = System.getProperty(GLOBUS_LOCATION);
        if (globusLocation == null) {
            throw new BootstrapException(GLOBUS_LOCATION + 
                                         " system property not set");
        }

        String antHome = System.getProperty(ANT_HOME);
        if (antHome == null) {
            throw new BootstrapException(ANT_HOME + 
                                         " system property not set");
        }

        int i = 0;
        String launchClass = null;
        String glFilter = null;
        String antFilter = null;
        for (i=0;i<args.length;i++) {
            if (args[i].startsWith("-")) {
                if (args[i].equals("-glfilter")) {
                    if (i < args.length - 1) {
                        glFilter = args[++i];
                    } else {
                        throw new BootstrapException(
                               "Missing value for -gtfilter argument");
                    }
                } else if (args[i].equals("-antfilter")) {
                    if (i < args.length - 1) {
                        antFilter = args[++i];
                    } else {
                        throw new BootstrapException(
                               "Missing value for -antfilter argument");
                    }
                } else {
                    throw new BootstrapException("Invalid argument: " +
                                                 args[i]);
                }
            } else {
                launchClass = args[i];
                break;
            }
        }
        
        if (launchClass == null) {
            throw new BootstrapException("Class name argument required");
        }
        
        AntBootstrap boot = new AntBootstrap();
        boot.addDirectory(globusLocation);
        boot.addLibDirectory(globusLocation, glFilter);
        boot.addLibDirectory(antHome, antFilter);
        
        String[] launchArgs = new String[args.length-i-1];
        System.arraycopy(args, i+1, launchArgs, 0, launchArgs.length);
        
        boot.launch(launchClass, launchArgs); 
    }
}
