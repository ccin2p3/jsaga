package integration.abstracts;

import java.io.*;
import java.util.Properties;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ResourcesLoader
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ResourcesLoader {
    public static final String JOB = "job";
    public static final String RESOURCES = "resources";
    public static final String EXPECTED_TRANSLATED = "expected-translated";
    public static final String EXPECTED_SPLITTED = "expected-splitted";
    public static final String EXPECTED_ALLOCATED = "expected-";
    public static final String EXPECTED_STAGING = "expected-staging";
    private String m_testName;
    private ClassLoader m_resources;
    private Properties m_prop;

    public ResourcesLoader(String testName) throws IOException {
        m_testName = testName;
        m_resources = this.getClass().getClassLoader();
        m_prop = new Properties();
        m_prop.setProperty(JOB, "job.xml");
        m_prop.setProperty(RESOURCES, "../resources.xml");
        m_prop.setProperty(EXPECTED_TRANSLATED, "expected.xml");
        m_prop.setProperty(EXPECTED_SPLITTED, "expected-splitted.xml");
        m_prop.setProperty(EXPECTED_ALLOCATED+"MYJOB_1", "expected-MYJOB_1.xml");
        m_prop.setProperty(EXPECTED_ALLOCATED+"MYJOB_2", "expected-MYJOB_2.xml");
        m_prop.setProperty(EXPECTED_ALLOCATED+"MYJOB_3", "expected-MYJOB_3.xml");
        m_prop.setProperty(EXPECTED_STAGING, "expected-staging.xml");
        try {
            // override default properties
            InputStream stream = this.getInputStreamByFileName("test.properties");
            m_prop.load(stream);
            stream.close();
        } catch(FileNotFoundException e) {}
    }

    public InputStream getInputStreamByPropertyName(String propertyName) throws FileNotFoundException {
        String filename = m_prop.getProperty(propertyName);
        if (filename == null) {
            if (JOB.equals(propertyName)) {
                filename = "job.xml";
            } else if (RESOURCES.equals(propertyName)) {
                filename = "../resources.xml";
            } else {
                throw new FileNotFoundException("Property not found: "+propertyName);
            }
        } else if ("".equals(filename)) {
            // this test will be ignored
            return null;
        }
        return this.getInputStreamByFileName(filename);
    }

    public InputStream getInputStreamByFileName(String fileName) throws FileNotFoundException {
        String resourcePath = "jobcollection/"+m_testName+"/"+fileName;
        InputStream stream = m_resources.getResourceAsStream(resourcePath);
        if (stream != null) {
            return stream;
        } else {
            throw new FileNotFoundException("Resource not found: "+resourcePath);
        }
    }
}
