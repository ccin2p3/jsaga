package org.ogf.saga.bootstrap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Properties for Saga. The {@link #getDefaultProperties()} method obtains
 * the properties in the
 * following order: a file <code>saga.properties</code> is searched for in
 * the classpath, and in the current directory.
 * If found, it is read as a properties file, and the properties contained in
 * it are set.
 * Next, the system properties are obtained. These may override the properties
 * set so far.
 * 
 * The properties are used to determine the Saga implementation, and may
 * also be used by Saga implementations for implementation-specific
 * properties.
 */
public final class SagaProperties {

    /** Filename for the properties. */
    public static final String PROPERTIES_FILENAME = "saga.properties";

    /** All our own properties start with this prefix. */
    public static final String PREFIX = "saga.";

    /** Property name for selecting a Saga implementation. */
    public static final String FACTORY = PREFIX + "factory";

    /** Property name of the property file. */
    public static final String PROPERTIES_FILE = PREFIX + "properties.file";

    /** List of {NAME, DEFAULT_VALUE, DESCRIPTION} for properties. */
    private static final String[][] propertiesList =
            new String[][] {
                { FACTORY, null,
                    "Classname of a Saga implementation factory."},
                { PROPERTIES_FILE, null,
                    "Name of the property file used for the configuration of Saga." },
            };

    private static Properties defaultProperties;

    /**
     * Private constructor, to prevent construction of an SagaProperties object.
     */
    private SagaProperties() {
        // nothing
    }

    /**
     * Returns the hard-coded properties of Saga.
     * 
     * @return
     *          the resulting properties.
     */
    public static Properties getHardcodedProperties() {
        Properties properties = new Properties();

        for (String[] element : propertiesList) {
            if (element[1] != null) {
                properties.setProperty(element[0], element[1]);
            }
        }

        return properties;
    }

    /**
     * Returns a map mapping hard-coded property names to their descriptions.
     * 
     * @return
     *          the name/description map.
     */
    public static Map<String, String> getDescriptions() {
        Map<String, String> result = new LinkedHashMap<String, String>();

        for (String[] element : propertiesList) {
            result.put(element[0], element[2]);
        }

        return result;
    }

    /**
     * Adds the properties as loaded from the specified stream to the specified
     * properties.
     * 
     * @param inputStream
     *            the input stream.
     * @param properties
     *            the properties.
     */
    private static void load(InputStream inputStream, Properties properties) {
        if (inputStream != null) {
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                // ignored
            } finally {
                try {
                    inputStream.close();
                } catch (Throwable e1) {
                    // ignored
                }
            }
        }
    }

    /**
     * Loads properties from the standard configuration file locations.
     */
    @SuppressWarnings("unchecked")
    public static synchronized Properties getDefaultProperties() {
        if (defaultProperties == null) {
            defaultProperties = getHardcodedProperties();

            // Load properties from the classpath
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            InputStream inputStream =
                classLoader.getResourceAsStream(PROPERTIES_FILENAME);
            load(inputStream, defaultProperties);

            // See if there is an saga.properties file in the current
            // directory.
            try {
                inputStream =
                    new FileInputStream(PROPERTIES_FILENAME);
                load(inputStream, defaultProperties);
            } catch (FileNotFoundException e) {
                // ignored
            }

            Properties systemProperties = System.getProperties();

            // Then see if the user specified an properties file.
            String file =
                systemProperties.getProperty(PROPERTIES_FILE);
            if (file != null) {
                try {
                    inputStream = new FileInputStream(file);
                    load(inputStream, defaultProperties);
                } catch (FileNotFoundException e) {
                    System.err.println("User specified preferences \"" + file
                            + "\" not found!");
                }
            }

            // Finally, add the properties from the command line to the result,
            // possibly overriding entries from file or the defaults.
            for (Enumeration<String> e = (Enumeration<String>)systemProperties.propertyNames(); e.hasMoreElements();) {
                String key = e.nextElement();
                String value = systemProperties.getProperty(key);
                defaultProperties.setProperty(key, value);
            }
        }

        return new Properties(defaultProperties);
    }
}
