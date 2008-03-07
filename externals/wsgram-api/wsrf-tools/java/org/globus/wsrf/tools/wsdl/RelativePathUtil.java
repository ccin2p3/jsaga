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
package org.globus.wsrf.tools.wsdl;

import java.io.File;

import java.util.LinkedList;


public class RelativePathUtil {
    /**
     * Returns the name of one file relative to another.  The name it returns
     * uses forward slashes "/" instead of the system specific file separator.
     */
    public static String getRelativeFileName(
        File target,
        File realativeTo
    ) {
        LinkedList targetList = getPathList(target);
        LinkedList relativeList = getPathList(realativeTo);

        while (
            !targetList.isEmpty() && !relativeList.isEmpty() &&
                targetList.getFirst().equals(relativeList.getFirst())
        ) {
            targetList.removeFirst();
            relativeList.removeFirst();
        }

        StringBuffer fileName = new StringBuffer();

        while (!relativeList.isEmpty()) {
            fileName.append("../");
            relativeList.removeFirst();
        }

        while (!targetList.isEmpty()) {
            fileName.append(targetList.removeFirst());
            fileName.append('/');
        }

        fileName.append(target.getName());

        return fileName.toString();
    }

    private static LinkedList getPathList(File file) {
        if (!file.isAbsolute()) {
            file = file.getAbsoluteFile();
        }

        LinkedList list = new LinkedList();

        if (!file.isDirectory()) {
            file = file.getParentFile();
        }

        while (file != null) {
            if (file.getName().length() > 0) { //getName() returns "" for the root dir 
                list.addFirst(file.getName());
            }

            file = file.getParentFile();
        }

        return list;
    }
}
