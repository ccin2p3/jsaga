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
package org.globus.wsrf.tools.jndi;

public class ResourceLink
{
    private String target = null;
    private String name = null;

    /**
     * @return Returns the target.
     */
    public String getTarget()
    {
        return target;
    }

    /**
     * @param target The target to set.
     */
    public void setTarget(String target)
    {
        this.target = target;
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public String toString()
    {
        StringBuffer out = new StringBuffer();
        out.append("        <resourceLink name=\"");
        out.append(this.name);
        out.append("\" \r\n");
        out.append("                      target=\"");
        out.append(this.target);
        out.append("\"/>\r\n");
        return out.toString();
    }
}
