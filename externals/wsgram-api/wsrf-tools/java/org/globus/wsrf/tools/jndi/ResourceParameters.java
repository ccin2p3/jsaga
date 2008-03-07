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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ResourceParameters
{
    private Map parameters = new HashMap();
    
    public void addParameter(String name, String value)
    {
        this.parameters.put(name, value);
    }
    
    public String getParameter(String name)
    {
        return (String) this.parameters.get(name);
    }
    
    public Set getParameterNames()
    {
        return this.parameters.keySet();
    }
    
    public String toString()
    {
        StringBuffer out = new StringBuffer();
        Set names;
        String name;
        Iterator nameIterator;
        
        out.append("            <resourceParams>\r\n");

        
        names = this.getParameterNames();
        nameIterator = names.iterator();
        
        while(nameIterator.hasNext())
        {
            name = (String) nameIterator.next();
            out.append("                <parameter>\r\n");
            out.append("                    <name>\r\n");
            out.append("                        ");
            out.append(name);
            out.append("\r\n");
            out.append("                    </name>\r\n");        
            out.append("                    <value>\r\n");
            out.append("                        ");
            out.append(this.parameters.get(name));
            out.append("\r\n");
            out.append("                    </value>\r\n");        
            out.append("                </parameter>\r\n");
        }

        out.append("            </resourceParams>\r\n");

        return out.toString();
    }
}
