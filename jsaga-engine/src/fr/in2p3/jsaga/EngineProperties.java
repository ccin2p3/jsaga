package fr.in2p3.jsaga;

import org.apache.log4j.Logger;
import org.ogf.saga.error.SagaError;

import java.io.*;
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
    public static final String JSAGA_CONFIGURATION = "jsaga.configuration";
    public static final String LOG4J_CONFIGURATION = "log4j.configuration";
    public static final String IGNORE_MISSING_ADAPTOR = "ignore.missing.adaptor";
    public static final String JOB_DESCRIPTION_DEFAULT = "job.description.default";
    public static final String JOB_MONITOR_ERROR_THRESHOLD = "job.monitor.error.threshold";
    public static final String JOB_CONTROL_CHECK_MATCH = "job.control.check.match";

    private static Logger s_logger = Logger.getLogger(EngineProperties.class);
    private static Properties s_prop;

    public static Properties getProperties() {
        if (s_prop == null) {
            // set default properties
            s_prop = new Properties();
            s_prop.setProperty(JSAGA_CONFIGURATION, "etc/jsaga-config.xml");
            s_prop.setProperty(LOG4J_CONFIGURATION, "etc/log4j.properties");
            s_prop.setProperty(IGNORE_MISSING_ADAPTOR, "true");
            s_prop.setProperty(JOB_DESCRIPTION_DEFAULT, "etc/jsaga-default.jsdl");
            s_prop.setProperty(JOB_MONITOR_ERROR_THRESHOLD, "3");
            s_prop.setProperty(JOB_CONTROL_CHECK_MATCH, "true");

            // load properties
            File file = new File(Base.JSAGA_HOME, "etc/jsaga-engine.properties");
            try {
                InputStream in = new FileInputStream(file);
                s_prop.load(in);
                in.close();
            } catch (IOException e) {
                s_logger.warn("Failed to load properties: "+file.getAbsolutePath(), e);
            }
        }
        return s_prop;
    }

    public static String getProperty(String name) {
        String value = getProperties().getProperty(name);
        if (value != null) {
            return value;
        } else {
            throw new SagaError("[INTERNAL ERROR] Engine property not found: "+name);
        }
    }

    public static File getFile(String name) {
        return new File(Base.JSAGA_HOME, getProperty(name));
    }

    public static File getRequiredFile(String name) throws FileNotFoundException {
        File file = getFile(name);
        if (file.exists()) {
            return file;
        } else {
            throw new FileNotFoundException("File not found: "+file.getAbsolutePath());
        }
    }

    public static int getInteger(String name) throws NumberFormatException {
        return Integer.parseInt(getProperty(name));
    }

    public static boolean getBoolean(String name) {
        return "true".equalsIgnoreCase(getProperty(name));
    }
}
