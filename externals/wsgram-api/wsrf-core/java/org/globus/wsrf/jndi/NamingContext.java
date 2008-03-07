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
package org.globus.wsrf.jndi;

import java.util.Iterator;
import java.util.Set;

import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.NameNotFoundException;

import org.apache.naming.ResourceRef;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.tools.jndi.ConfigContext;
import org.globus.wsrf.tools.jndi.Environment;
import org.globus.wsrf.tools.jndi.Resource;
import org.globus.wsrf.tools.jndi.ResourceLink;
import org.globus.wsrf.tools.jndi.ResourceParameters;
import org.globus.wsrf.utils.Resources;
import org.globus.util.I18n;

import org.apache.axis.AxisEngine;

public class NamingContext
{
    private static Log logger =
        LogFactory.getLog(NamingContext.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    private Context context;
    private AxisEngine engine;

    public NamingContext(Context context, AxisEngine engine)
    {
        this.context = context;
        this.engine = engine;
    }

    public void addEnvironment(Environment environment)
        throws IllegalArgumentException, NamingException
    {
        Object value = null;

        // Instantiate a new instance of the correct object type, and
        // initialize it.

        String type = environment.getType();

        try
        {
            if(type.equals("java.lang.String"))
            {
                value = environment.getValue();
            }
            else if(type.equals("java.lang.Byte"))
            {
                if(environment.getValue() == null)
                {
                    value = new Byte((byte) 0);
                }
                else
                {
                    value = Byte.decode(environment.getValue());
                }
            }
            else if(type.equals("java.lang.Short"))
            {
                if(environment.getValue() == null)
                {
                    value = new Short((short) 0);
                }
                else
                {
                    value = Short.decode(environment.getValue());
                }
            }
            else if(type.equals("java.lang.Integer"))
            {
                if(environment.getValue() == null)
                {
                    value = new Integer(0);
                }
                else
                {
                    value = Integer.decode(environment.getValue());
                }
            }
            else if(type.equals("java.lang.Long"))
            {
                if(environment.getValue() == null)
                {
                    value = new Long(0);
                }
                else
                {
                    value = Long.decode(environment.getValue());
                }
            }
            else if(type.equals("java.lang.Boolean"))
            {
                value = Boolean.valueOf(environment.getValue());
            }
            else if(type.equals("java.lang.Double"))
            {
                if(environment.getValue() == null)
                {
                    value = new Double(0);
                }
                else
                {
                    value = Double.valueOf(environment.getValue());
                }
            }
            else if(type.equals("java.lang.Float"))
            {
                if(environment.getValue() == null)
                {
                    value = new Float(0);
                }
                else
                {
                    value = Float.valueOf(environment.getValue());
                }
            }
            else if(type.equals("java.lang.Character"))
            {
                if(environment.getValue() == null)
                {
                    value = new Character((char) 0);
                }
                else
                {
                    if(environment.getValue().length() == 1)
                    {
                        value = new Character(environment.getValue().charAt(0));
                    }
                    else
                    {
                        throw new IllegalArgumentException();
                    }
                }
            }
            else
            {
                throw new IllegalArgumentException(
                    i18n.getMessage("invalidType", type));
            }
        }
        catch(NumberFormatException e)
        {
            String msg = i18n.getMessage("invalidValueForType",
                                         new Object[]{environment.getValue(),
                                                      type});
            logger.error(msg, e);
            throw new IllegalArgumentException(msg);
        }
        // Bind the object to the appropriate name
        createSubcontexts(environment.getName());
        this.context.bind(environment.getName(), value);
        if(logger.isDebugEnabled())
        {
            logger.debug("Added environment entry with name: " +
                         environment.getName());
            logger.debug("value: " + value + " and type: " +
                         environment.getType());
        }
    }

    public void addResource(Resource resource)
        throws NamingException
    {
        Reference reference = new ResourceRef(resource.getType(),
                                              resource.getDescription(),
                                              resource.getScope(),
                                              resource.getAuth());
        this.addParameters(reference, resource.getParameters());
        this.createSubcontexts(resource.getName());
        this.context.bind(resource.getName(), reference);
        logger.debug("Added resource entry with name: " +
                     resource.getName());
    }

    public void addServiceResource(Resource resource, String serviceName)
        throws NamingException
    {
        Reference reference = new ServiceResourceRef(resource.getType(),
                                                     resource.getDescription(),
                                                     resource.getScope(),
                                                     resource.getAuth(),
                                                     this.engine,
                                                     serviceName);
        this.addParameters(reference, resource.getParameters());
        this.createSubcontexts(resource.getName());
        this.context.bind(resource.getName(), reference);
        logger.debug("Added service resource entry with name: " +
                     resource.getName());
    }
    
    public void addResourceLink(ResourceLink resourceLink)
        throws NamingException
    {
        LinkRef link = new LinkRef(resourceLink.getTarget());
        this.createSubcontexts(resourceLink.getName());
        this.context.bind(resourceLink.getName(), link);
        if(logger.isDebugEnabled())
        {
            logger.debug("Added resource link with name: " +
                         resourceLink.getName());
            logger.debug("Pointing to: " +
                         resourceLink.getTarget());
        }
    }

    public void addSubContext(ConfigContext subContext)
        throws NamingException
    {
        addService(subContext);
    }

    public void addService(ConfigContext subContext)
        throws NamingException
    {
        String serviceName = subContext.getName();
        Set names;
        Iterator nameIterator;
        NamingContext newContext;


        Context servicesContext = null;
        try 
        {
            servicesContext = (Context)this.context.lookup("services");
        } 
        catch (NameNotFoundException e) 
        {
            servicesContext = this.context.createSubcontext("services");
        }

        JNDIUtils.createSubcontexts(servicesContext, serviceName);

        try
        {
            newContext = new NamingContext(
                servicesContext.createSubcontext(serviceName),
                this.engine);
            logger.debug("Created new subcontext with name: " +
                         serviceName);
        }
        catch(Exception e)
        {
            newContext = new NamingContext(
                (Context) servicesContext.lookup(serviceName),
                this.engine);
            logger.debug("Adding entries to existing subcontext with name: " +
                         serviceName);
        }
                
        names = subContext.getEnvironmentNames();
        nameIterator = names.iterator();

        while(nameIterator.hasNext())
        {
            newContext.addEnvironment(
                subContext.getEnvironment((String) nameIterator.next()));
        }

        names = subContext.getResourceNames();
        nameIterator = names.iterator();

        while(nameIterator.hasNext())
        {
            newContext.addServiceResource(
                subContext.getResource((String) nameIterator.next()),
                serviceName);
        }

        names = subContext.getResourceLinkNames();
        nameIterator = names.iterator();

        while(nameIterator.hasNext())
        {
            newContext.addResourceLink(
                subContext.getResourceLink((String) nameIterator.next()));
        }
    }

    private void createSubcontexts(String name)
        throws NamingException
    {
        JNDIUtils.createSubcontexts(this.context, name);
    }

    private void addParameters(
        Reference reference,
        ResourceParameters parameters)
    {
        if(parameters != null)
        {
            Set names = parameters.getParameterNames();
            Iterator nameIterator = names.iterator();
            RefAddr parameter;
            String parameterName;
            while(nameIterator.hasNext())
            {
                parameterName = (String) nameIterator.next();
                parameter = new StringRefAddr(parameterName,
                                              parameters.getParameter(
                                                  parameterName));
                reference.add(parameter);
            }
        }
    }
}
