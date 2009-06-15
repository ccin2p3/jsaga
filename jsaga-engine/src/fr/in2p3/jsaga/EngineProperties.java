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
    public static final String JSAGA_UNIVERSE = "jsaga.universe";
    public static final String JSAGA_UNIVERSE_ENABLE_CACHE = "jsaga.universe.enable.cache";
    public static final String JSAGA_UNIVERSE_IGNORE_MISSING_ADAPTOR = "jsaga.universe.ignore.missing.adaptor";
    public static final String JSAGA_TIMEOUT = "jsaga.timeout";
    public static final String LOG4J_CONFIGURATION = "log4j.configuration";
    public static final String DATA_COPY_BUFFER_SIZE = "data.copy.buffer.size";
    public static final String JOB_DESCRIPTION_DEFAULT = "job.description.default";
    public static final String JOB_MONITOR_POLL_PERIOD = "job.monitor.poll.period";
    public static final String JOB_MONITOR_ERROR_THRESHOLD = "job.monitor.error.threshold";
    public static final String JOB_CONTROL_CHECK_AVAILABILITY = "job.control.check.availability";
    public static final String JOB_CONTROL_CHECK_MATCH = "job.control.check.match";

    private static Exception s_exception;
    private static Properties s_prop;

    public static Properties getProperties() {
        if (s_prop == null) {
            // set default properties
            s_prop = new Properties();
            //s_prop.setProperty(JSAGA_UNIVERSE, "etc/jsaga-universe.xml");
            s_prop.setProperty(JSAGA_UNIVERSE_ENABLE_CACHE, "true");
            s_prop.setProperty(JSAGA_UNIVERSE_IGNORE_MISSING_ADAPTOR, "true");
            //s_prop.setProperty(JSAGA_TIMEOUT, "etc/jsaga-timeout.properties");
            //s_prop.setProperty(LOG4J_CONFIGURATION, "etc/log4j.properties");
            s_prop.setProperty(DATA_COPY_BUFFER_SIZE, "16384");
            //s_prop.setProperty(JOB_DESCRIPTION_DEFAULT, "etc/jsaga-default.jsdl");
            s_prop.setProperty(JOB_MONITOR_POLL_PERIOD, "1000");
            s_prop.setProperty(JOB_MONITOR_ERROR_THRESHOLD, "3");
            s_prop.setProperty(JOB_CONTROL_CHECK_AVAILABILITY, "false");
            s_prop.setProperty(JOB_CONTROL_CHECK_MATCH, "true");

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
            throw new RuntimeException("[INTERNAL ERROR] Engine property not found: "+name);
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
            String path = EngineProperties.getEngineProperty(name);
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

    public static int getInteger(String name) throws NumberFormatException {
        return Integer.parseInt(getProperty(name));
    }

    public static boolean getBoolean(String name) {
        return "true".equalsIgnoreCase(getProperty(name));
    }
}
