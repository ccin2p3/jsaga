package org.ogf.saga;

//import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.io.*;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BaseTest
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   4 NOV 2013
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractTest_JUNIT4 extends Assert {
    /** jobservice.url (required): for testing job service */
    protected static final String CONFIG_JOBSERVICE_URL = "jobservice.url";
    /** base.url (required): for testing any protocol */
    protected static final String CONFIG_BASE_URL = "base.url";
    /** base2.url (optional): for testing protocols supporting third-party transfer */
    protected static final String CONFIG_BASE2_URL = "base2.url";
    /** physical.protocol (optional): for testing logical protocols */
    protected static final String CONFIG_PHYSICAL_PROTOCOL = "physical.protocol";

    private Properties m_properties;
    private long startTime;
    private Logger logger = Logger.getLogger(this.getClass());

    public AbstractTest_JUNIT4() throws Exception {
        // set configuration files to use
        if (System.getProperty("jsaga.default.contexts") == null) {
            java.net.URL defaultContexts = this.getResource("etc/jsaga-default-contexts.xml");
            if (defaultContexts != null) {
                System.setProperty("jsaga.default.contexts", defaultContexts.toString());
            }
        }
        if (System.getProperty("jsaga.timeout") == null) {
            java.net.URL timeout = this.getResource("etc/jsaga-timeout.properties");
            if (timeout != null) {
                System.setProperty("jsaga.timeout", timeout.toString());
            }
        }
        if (System.getProperty("log4j.configuration") == null) {
            java.net.URL log4j = this.getResource("etc/log4j.properties");
            if (log4j != null) {
                System.setProperty("log4j.configuration", log4j.toString());
            }
        }

        // load test config
        java.net.URL test = this.getResource("saga-test.properties");
        if (test == null) {
            throw new Exception("Resource not found: saga-test.properties");
        }
        m_properties = new Properties();
        m_properties.load(test.openStream());
        // may override test config
        File developerTestProps = new File(new File(System.getProperty("user.home"), ".jsaga"), "saga-test.properties");
        if (developerTestProps.exists()) {
            Properties developerProps = new Properties();
            developerProps.load(new FileInputStream(developerTestProps));
            m_properties.putAll(developerProps);
        }
        // forward test config to System properties
        System.getProperties().putAll(m_properties);
    }

    @Rule
    public TestRule watcher = new TestWatcher() {
       protected void starting(Description description) {
       	if(logger.isInfoEnabled())
    		logger.info(description.getMethodName()+" running...");
    	startTime = new Date().getTime();
       }
       protected void finished(Description description) {
         if(logger.isDebugEnabled())
        	 logger.debug(description.getMethodName()+" - Duration: "+ (new Date().getTime() - startTime)+" ms");
       }

    };
    
    /** Implicitly invoked before executing each test method */
//    protected void setUp() throws Exception {
//    	if(logger.isInfoEnabled())
//    		logger.info(this.getName()+" running...");
//    	startTime = new Date().getTime();
//    	super.setUp();
//    }
        
    /** Implicitly invoked after executing each test method */
//    protected void tearDown() throws Exception {
//        if(logger.isDebugEnabled())
//        	logger.debug(this.getName()+" - Duration: "+ (new Date().getTime() - startTime)+" ms");
//    }

//    protected void ignore(String message) {
//        if(logger.isEnabledFor(Priority.WARN))
//            logger.warn(this.getName()+" ignored"+ (message!=null ? " ("+message+")" : ""));
//    }
    
    protected String getOptionalProperty(String protocol, String name) {
        return m_properties.getProperty(protocol+"."+name);
    }

    protected String getOptionalProperty(String protocol, String name, String defaultValue) {
        return m_properties.getProperty(protocol+"."+name, defaultValue);
    }

    protected String getRequiredProperty(String protocol, String name) throws Exception {
        String value = m_properties.getProperty(protocol+"."+name);
        if (value != null) {
            return value;
        } else {
            throw new Exception("Test properties file is missing required property: "+protocol+"."+name);
        }
    }

    protected static URL createURL(URL base, String name) throws NotImplementedException, NoSuccessException, BadParameterException {
        String basePath = base.getPath();
        String path = (basePath.endsWith("/") ? basePath+name : basePath+"/"+name);
        URL url = URLFactory.createURL(base.toString());
        url.setPath(path);
        return url;
    }

    private java.net.URL getResource(String path) throws IOException {
        ClassLoader loader = this.getClass().getClassLoader();
        // get class JAR
        String classPath = this.getClass().getName().replaceAll("\\.", "/") + ".class";
        java.net.URL classResource = loader.getResource(classPath);
        String classJar = getJar(classResource, classPath);
        // find resource matching class JAR
        for (Enumeration<java.net.URL> e=loader.getResources(path); e.hasMoreElements(); ) {
            // get resource JAR
            java.net.URL resource = e.nextElement();
            String jar = getJar(resource, path);
            // compare
            if (classJar.equals(jar)) {
                return resource;
            }
        }
        return null;
    }
    private static String getJar(java.net.URL resource, String path) {
        int index = resource.toString().indexOf(path);
        if (index > -1) {
            return resource.toString().substring(0, index);
        } else {
            return null;
        }
    }
}
