/*
 *
 * COPYRIGHT (C) 2003-2006 National Institute of Informatics, Japan
 *                         All Rights Reserved
 * COPYRIGHT (C) 2003-2006 Fujitsu Limited
 *                         All Rights Reserved
 * 
 * This file is part of the NAREGI Grid Super Scheduler software package.
 * For license information, see the docs/LICENSE file in the top level 
 * directory of the NAREGI Grid Super Scheduler source distribution.
 *
 *
 * Revision history:
 *      $Revision: 1.1 $
 *      $Id: ConfigManager.java,v 1.1 2008/08/07 15:24:32 sreynaud Exp $
 */
package org.naregi.ss.service.client;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
* Class for managing configuration parameters
* 
*/
public class ConfigManager {

	/**
	 * Path of configuration file
	 */
	private static String PROPFILE = "ss_api.properties";
	private static Properties prop = null;
	
	static {
        try
        {
        	String propfilepath;
            try
	        {
            	propfilepath = 
            		System.getProperty("user.home",	"");
            	propfilepath += "/" + PROPFILE;
	        } catch (Exception e) {
	        	propfilepath = "";
	        }
	        if (propfilepath != null && !propfilepath.equals("")) {
	        	File pf = new File(propfilepath);
	        	if (!pf.exists() || !pf.isFile()) {
	        	} else {
	        		prop = new Properties();
	        		prop.load(new FileInputStream(pf));
	        	}
	        }
	        
        } catch (Exception e) {
        	prop = null;
        }
        try
        {
        	if (prop == null) {
            	String propfilepath = "/etc/ss_config/" + PROPFILE;
    	        
            	File pf = new File(propfilepath);
            	if (!pf.exists() || !pf.isFile()) {
            	} else {
            		prop = new Properties();
            		prop.load(new FileInputStream(pf));
            	}
        	}

        } catch (Exception e) {
        	prop = null;
        }
	}

    /**
     * Get property from configuration parameter 
     * Return null, if the property corresponding with the key is not found.
     * 
     * @param key property name
     * @return property value
     * @throws JobScheduleServiceException
     */
    public static String getProperty(String key) throws JobScheduleServiceException {
    	String ret = null;
    	if (prop != null) {
        	ret = prop.getProperty(key);
    	} else {
			String msg = "Unable to read config file." ;
    		throw new JobScheduleServiceException(msg);
    	}
    	return ret;
    }

    /**
     * Get property from configuration parameter 
     * Return defaultValue, if the property corresponding with the key
     * is not found.
     * 
     * @param key property name
     * @param defaultValue default value
     * @return property value
     * @throws JobScheduleServiceException
     */
    public static String getProperty(String key, String defaultValue) throws JobScheduleServiceException {
    	String ret = null;
    	if (prop != null) {
        	ret = prop.getProperty(key, defaultValue);
    	} else {
			String msg = "Unable to read config file." ;
    		throw new JobScheduleServiceException(msg);
    	}
    	return ret;
    }

}
