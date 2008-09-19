package fr.in2p3.jsaga;

import fr.in2p3.jsaga.engine.config.ConfigurationException;
import org.ogf.saga.error.SagaError;

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
    public static final String LOG4J_CONFIGURATION = "log4j.configuration";
    public static final String DATA_COPY_BUFFER_SIZE = "data.copy.buffer.size";
    public static final String DATA_COPY_KEEP_LAST_MODIFIED = "data.copy.keep.last.modified";
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
            s_prop.setProperty(JSAGA_UNIVERSE, "etc/jsaga-universe.xml");
            s_prop.setProperty(JSAGA_UNIVERSE_ENABLE_CACHE, "true");
            s_prop.setProperty(JSAGA_UNIVERSE_IGNORE_MISSING_ADAPTOR, "true");
            s_prop.setProperty(LOG4J_CONFIGURATION, "etc/log4j.properties");
            s_prop.setProperty(DATA_COPY_BUFFER_SIZE, "16384");
            s_prop.setProperty(DATA_COPY_KEEP_LAST_MODIFIED, "false");
            s_prop.setProperty(JOB_DESCRIPTION_DEFAULT, "etc/jsaga-default.jsdl");
            s_prop.setProperty(JOB_MONITOR_POLL_PERIOD, "1000");
            s_prop.setProperty(JOB_MONITOR_ERROR_THRESHOLD, "3");
            s_prop.setProperty(JOB_CONTROL_CHECK_AVAILABILITY, "true");
            s_prop.setProperty(JOB_CONTROL_CHECK_MATCH, "true");

            // load properties
            File file = new File(Base.JSAGA_HOME, "etc/jsaga-engine.properties");
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

    public static String getProperty(String name) {
        String value = getProperties().getProperty(name);
        if (value != null) {
            return value;
        } else {
            throw new SagaError("[INTERNAL ERROR] Engine property not found: "+name);
        }
    }

    /**
     * Get the stream corresponding to property <code>name</code>.
     * The property value can be either an URL (System properties only)
     * or a file path (Engine properties only).
     * @param name the name of the property
     * @return the input stream
     */
    public static InputStream getRequiredStream(String name) throws ConfigurationException {
        InputStream stream;
        String value = System.getProperty(name);
        if (value != null) {
            try {
                URL url = new URL(value);
                stream = url.openStream();
            } catch (MalformedURLException e) {
                throw new ConfigurationException("Malformed URL: "+value, e);
            } catch (IOException e) {
                throw new ConfigurationException("Failed to open stream: "+value, e);
            }
        } else {
            String path = getProperty(name);
            File file;
            if (new File(path).isAbsolute()) {
                file = new File(path);
            } else {
                file = new File(Base.JSAGA_HOME, path);
            }
            try {
                stream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new ConfigurationException("File not found: "+file, e);
            }
        }
        return stream;
    }
    public static InputStream getStream(String name) {
        try {
            return getRequiredStream(name);
        } catch (ConfigurationException e) {
            return null;
        }
    }

    public static int getInteger(String name) throws NumberFormatException {
        return Integer.parseInt(getProperty(name));
    }

    public static boolean getBoolean(String name) {
        return "true".equalsIgnoreCase(getProperty(name));
    }
}
