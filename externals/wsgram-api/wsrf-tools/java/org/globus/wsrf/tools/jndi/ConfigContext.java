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


public class ConfigContext
{
    protected Map subContexts = new HashMap();
    protected Map environmentEntries = new HashMap();
    protected Map resourceEntries = new HashMap();
    protected Map resourceLinks = new HashMap();
    protected String name = null;
    protected boolean global;
    
    public ConfigContext()
    {
        this(false);
    }
    
    public ConfigContext(boolean global)
    {
        this.global = global;
    }
    
    /**
     * @return Returns the global.
     */
    public boolean isGlobal()
    {
        return this.global;
    }

    /**
     * @param global The global to set.
     */
    public void setGlobal(boolean global)
    {
        this.global = global;
    }

    public String getName()
    {
        return this.name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public void addEnvironment(Environment environment)
    {
        this.environmentEntries.put(environment.getName(), environment);
    }
    
    public Environment getEnvironment(String name)
    {
        return (Environment) this.environmentEntries.get(name);
    }
    
    public Set getEnvironmentNames()
    {
        return this.environmentEntries.keySet();
    }
    
    public void addResource(Resource resource)
    {
        this.resourceEntries.put(resource.getName(), resource);
    }
    
    public Resource getResource(String name)
    {
        return (Resource) this.resourceEntries.get(name);
    }
    
    public Set getResourceNames()
    {
        return this.resourceEntries.keySet();
    }

    public void addSubContext(ConfigContext context)
    {
        this.subContexts.put(context.getName(), context);
    }
    
    public ConfigContext getSubContext(String name)
    {
        return (ConfigContext) this.subContexts.get(name);
    }
    
    public Set getSubContextNames()
    {
        return this.subContexts.keySet();
    }
    
    public void removeSubContext(String name)
    {
        this.subContexts.remove(name);
    }
    
    public void addResourceLink(ResourceLink link)
    {
        this.resourceLinks.put(link.getName(), link);
    }
    
    public ResourceLink getResourceLink(String name)
    {
        return (ResourceLink) this.resourceLinks.get(name);
    }
    
    public Set getResourceLinkNames()
    {
        return this.resourceLinks.keySet();
    }
    
    public String toString()
    {
        StringBuffer out = new StringBuffer();
        Set names;
        Iterator nameIterator;
        
        if(global == true)
        {
            out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
            out.append("<jndiConfig xmlns=\"http://wsrf.globus.org/jndi/config\">\r\n");
            out.append("    <global>\r\n");
        }
        else
        {
            out.append("    <service name=\"");
            out.append(this.name);
            out.append("\">\r\n");
        }
        
        names = this.getEnvironmentNames();
        nameIterator = names.iterator();
        
        while(nameIterator.hasNext())
        {
            out.append(
                ((Environment) this.environmentEntries.get(
                    nameIterator.next())).toString());
        }

        names = this.getResourceNames();
        nameIterator = names.iterator();
        
        while(nameIterator.hasNext())
        {
            out.append(
                ((Resource) this.resourceEntries.get(
                    nameIterator.next())).toString());            
        }

        names = this.getResourceLinkNames();
        nameIterator = names.iterator();
        
        while(nameIterator.hasNext())
        {
            out.append(
                ((ResourceLink) this.resourceLinks.get(
                    nameIterator.next())).toString());            
        }

        if(global == true)
        {
            out.append("    </global>\r\n");
            //process sub contexts
            names = this.getSubContextNames();
            nameIterator = names.iterator();
            
            while(nameIterator.hasNext())
            {
                out.append(
                    ((ConfigContext) this.subContexts.get(
                        nameIterator.next())).toString());
            }
            
            out.append("</jndiConfig>\r\n");
        }
        else
        {
            out.append("    </service>\r\n");
        }

        return out.toString();
    }
}
