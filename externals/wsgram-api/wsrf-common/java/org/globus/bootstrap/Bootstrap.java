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

public class Bootstrap extends BootstrapBase {

    private static final String GLOBUS_LOCATION = "GLOBUS_LOCATION";

    public static void main(String[] args) throws BootstrapException {
        
        String base = System.getProperty(GLOBUS_LOCATION);
        if (base == null) {
            throw new BootstrapException(GLOBUS_LOCATION + 
                                         " system property not set");
        }

        if (args.length == 0) {
            throw new BootstrapException("Class name argument required");
        }

        Bootstrap boot = new Bootstrap();
        boot.addDirectory(base);
        boot.addLibDirectory(base);
        
        String launchClass = args[0];
        String[] launchArgs = new String[args.length-1];
        System.arraycopy(args, 1, launchArgs, 0, launchArgs.length);

        boot.launch(launchClass, launchArgs); 
    }
}
