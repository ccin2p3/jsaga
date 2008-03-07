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
package org.globus.axis.configuration;

import org.globus.tools.DeployConstants;
import org.globus.wsrf.config.ContainerConfig;

import org.apache.axis.configuration.EngineConfigurationFactoryDefault;
import org.apache.axis.configuration.DirProvider;
import org.apache.axis.AxisProperties;
import org.apache.axis.ConfigurationException;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.EngineConfigurationFactory;
import org.apache.axis.utils.Messages;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.File;

public class EngineConfigurationFactoryServlet
    extends EngineConfigurationFactoryDefault
{
    protected static Log log =
        LogFactory.getLog(EngineConfigurationFactoryServlet.class.getName());

    private ServletContext ctx;
    
    public static EngineConfigurationFactory newFactory(Object param) {
        return (param instanceof ServletConfig)
               ? new EngineConfigurationFactoryServlet((ServletConfig)param)
               : null;
    }

    protected EngineConfigurationFactoryServlet(ServletConfig conf) {
        super();
        this.ctx = conf.getServletContext();
    }

    public EngineConfiguration getServerEngineConfig() {
        return getServerEngineConfig(ctx);
    }

    private static 
            EngineConfiguration getServerEngineConfig(ServletContext ctx) {
        // Respect the system property setting for a different config file
        String configFile = 
                AxisProperties.getProperty(OPTION_SERVER_CONFIG_FILE);
        if (configFile == null) {
            configFile = SERVER_CONFIG_FILE;
        }
        
        String appWebInfPath = "/WEB-INF";
        String realWebInfPath = ctx.getRealPath(appWebInfPath);

        if (realWebInfPath == null) {
            log.error(Messages.getMessage("servletEngineWebInfError00"));
        }

        String configProfile = 
            ctx.getInitParameter(ContainerConfig.CONFIG_PROFILE);
        if (configProfile != null) {
            configFile = configProfile + "-" + configFile;
        }

        DirProvider config = null;

        String baseDir = realWebInfPath + File.separator + 
            DeployConstants.CONFIG_BASE_DIR;
        try {
            config = new DirProvider(baseDir, configFile);
        } catch (ConfigurationException e) {
            log.error(Messages.getMessage("servletEngineWebInfError00"), e);
        }
        
        return config;
    }
    
}
