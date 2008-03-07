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

public class MemoryProfiler {
    private static Runtime debugRuntime;
    private static Log logger = LogFactory.getLog(MemoryProfiler.class);

    static {
        if (logger.isDebugEnabled()) {
            debugRuntime = Runtime.getRuntime();
        }
    }

    static public void memoryTrace(Class c, String s) {
        if (logger.isDebugEnabled()) {
            logger.debug("Memory profile at " + c.toString() + "." + s
                    + ": " + usedMemory() + " bytes used");
        }
    }

    static public String usedMemory() {
        try {
            System.gc();
            Thread.sleep(100);
            System.gc();
            Thread.sleep(100);
            System.gc();
            Thread.sleep(100);
            System.gc();
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        return String.valueOf(debugRuntime.totalMemory()
                - debugRuntime.freeMemory());
    }
}
