package fr.in2p3.jsaga;

import fr.in2p3.jsaga.engine.config.ConfigurationException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   EngineProperties
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class EngineProperties {
    public static final String JSAGA_DEFAULT_CONTEXTS = "jsaga.default.contexts";
    public static final String JSAGA_TIMEOUT = "jsaga.timeout";
    public static final String LOG4J_CONFIGURATION = "log4j.configuration";
    public static final String JSAGA_DEFAULT_CONTEXTS_CHECK_CONFLICTS = "jsaga.default.contexts.check.conflicts";
    public static final String DATA_IMPLICIT_CLOSE_TIMEOUT = "data.implicit.close.timeout";
    public static final String DATA_COPY_BUFFER_SIZE = "data.copy.buffer.size";
    public static final String DATA_ATTRIBUTES_CACHE_LIFETIME = "data.attributes.cache.lifetime";
    public static final String JOB_MONITOR_POLL_PERIOD = "job.monitor.poll.period";
    public static final String JOB_MONITOR_ERROR_THRESHOLD = "job.monitor.error.threshold";
    public static final String JOB_CONTROL_CHECK_AVAILABILITY = "job.control.check.availability";
    public static final String JOB_CONTROL_CHECK_MATCH = "job.control.check.match";
    public static final String JOB_CANCEL_CHECK_STATUS = "job.cancel.check.status";
    public static final String JAVAX_NET_SSL_KEYSTORE = "javax.net.ssl.keyStore";
    public static final String JAVAX_NET_SSL_KEYSTOREPASSWORD = "javax.net.ssl.keyStorePassword";
    public static final String JAVAX_NET_SSL_TRUSTSTORE = "javax.net.ssl.trustStore";
    public static final String JAVAX_NET_SSL_TRUSTSTOREPASSWORD = "javax.net.ssl.trustStorePassword";
    
    private static Exception s_exception;
    private static Properties s_prop;

    public static Properties getProperties() {
        if (s_prop == null) {
            // set default properties
            s_prop = new Properties();
            //s_prop.setProperty(JSAGA_DEFAULT_CONTEXTS, "etc/jsaga-default-contexts.xml");
            //s_prop.setProperty(JSAGA_TIMEOUT, "etc/jsaga-timeout.properties");
            //s_prop.setProperty(LOG4J_CONFIGURATION, "etc/log4j.properties");
            s_prop.setProperty(JSAGA_DEFAULT_CONTEXTS_CHECK_CONFLICTS, "true");
            //s_prop.setProperty(DATA_IMPLICIT_CLOSE_TIMEOUT, "-1");
            s_prop.setProperty(DATA_COPY_BUFFER_SIZE, "16384");
            s_prop.setProperty(DATA_ATTRIBUTES_CACHE_LIFETIME, "60000");
            s_prop.setProperty(JOB_MONITOR_POLL_PERIOD, "60000");
            s_prop.setProperty(JOB_MONITOR_ERROR_THRESHOLD, "3");
            s_prop.setProperty(JOB_CONTROL_CHECK_AVAILABILITY, "false");
            s_prop.setProperty(JOB_CONTROL_CHECK_MATCH, "false");
            s_prop.setProperty(JOB_CANCEL_CHECK_STATUS, "true");

            // load properties
            File file = new File(Base.JSAGA_HOME, "etc/jsaga-config.properties");
            try {
                InputStream in = new FileInputStream(file);
                s_prop.load(in);
                in.close();
            } catch (IOException e) {
                s_exception = e;
            }
        }
        return s_prop;
    }

    public static Exception getException() {
        return s_exception;
    }

    public static void setProperty(String name, String value) {
        getProperties().setProperty(name, value);
    }

    private static String getEngineProperty(String name) {
        String value = getProperties().getProperty(name);
        if (value != null) {
            return value;
        } else {
            return null;
        }
    }

    public static String getProperty(String name) {
        // try with system property
        String value = System.getProperty(name);
        if (value != null) {
            return value;
        } else {
            // try with engine property
            return EngineProperties.getEngineProperty(name);
        }
    }

    /**
     * Get the URL corresponding to property <code>name</code>.
     * The URL can be either a JAR resource (System properties only)
     * or a local file path (Engine properties only).
     * @param name the name of the property
     * @return the URL
     */
    public static URL getURL(String name) throws ConfigurationException {
        // try with system property
        String value = System.getProperty(name);
        if (value != null) {
            try {
                return new URL(value);
            } catch (MalformedURLException e) {
                throw new ConfigurationException("Malformed URL: "+value, e);
            }
        } else {
            // try with engine property
            String path = getProperties().getProperty(name);
            if (path != null) {
                File file;
                if (new File(path).isAbsolute()) {
                    file = new File(path);
                } else {
                    file = new File(Base.JSAGA_HOME, path);
                }
                if (! file.exists()) {
                    throw new ConfigurationException("File not found: "+file);
                }
                try {
                    return file.toURL();
                } catch (MalformedURLException e) {
                    throw new ConfigurationException("Malformed URL: "+file, e);
                }
            } else {
                return null;
            }
        }
    }

    public static Integer getInteger(String name) throws NumberFormatException {
        String value = getProperty(name);
        if (value != null) {
            return Integer.parseInt(value);
        } else {
            return null;
        }
    }

    public static Boolean getBoolean(String name) {
        String value = getProperty(name);
        if (value != null) {
            return "true".equalsIgnoreCase(value);
        } else {
            return null;
        }
    }
}
