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


public class Resource
{
    private String description = null;
    private String name = null;
    private String scope = null;
    private String type = null;
    private String auth = null;
    private ResourceParameters parameters = null;
    
    
    /**
     * @return Returns the parameters.
     */
    public ResourceParameters getParameters()
    {
        return parameters;
    }

    /**
     * @param parameters The parameters to set.
     */
    public void setParameters(ResourceParameters parameters)
    {
        this.parameters = parameters;
    }

    /**
     * @return Returns the auth.
     */
    public String getAuth()
    {
        return auth;
    }

    /**
     * @param auth The auth to set.
     */
    public void setAuth(String auth)
    {
        this.auth = auth;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description)
    {
        this.description = description;
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

    /**
     * @return Returns the scope.
     */
    public String getScope()
    {
        return scope;
    }

    /**
     * @param scope The scope to set.
     */
    public void setScope(String scope)
    {
        this.scope = scope;
    }

    /**
     * @return Returns the type.
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(String type)
    {
        this.type = type;
    }

    public String toString()
    {
        StringBuffer out = new StringBuffer();
        out.append("        <resource name=\"");
        out.append(this.name);
        out.append("\" \r\n"); 
        out.append("                  type=\"");
        out.append(this.type);
        if(this.scope != null)
        {
            out.append("\"\r\n");
            out.append("                  scope=\"");
            out.append(this.scope);
        }
        if(this.auth != null)
        {
            out.append("\"\r\n");
            out.append("                  auth=\"");
            out.append(this.auth);
        }
        if(this.description != null)
        {
            out.append("\"\r\n");
            out.append("                  auth=\"");
            out.append(this.description);
        }
        out.append("\">\r\n");
        if (this.parameters != null) 
        {
            out.append(this.parameters.toString());
        }
        out.append("        </resource>\r\n");
        return out.toString();
    }
}
