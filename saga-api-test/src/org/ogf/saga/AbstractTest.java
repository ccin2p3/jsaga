package org.ogf.saga;

import junit.framework.TestCase;
import org.ogf.saga.error.*;

import java.io.InputStream;
import java.lang.Exception;
import java.util.Properties;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNSTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractTest extends TestCase {
    /** jobservice.url (required): for testing job service */
    protected static final String CONFIG_JOBSERVICE_URL = "jobservice.url";
    /** base.url (required): for testing any protocol */
    protected static final String CONFIG_BASE_URL = "base.url";
    /** base2.url (optional): for testing protocols supporting third-party transfer */
    protected static final String CONFIG_BASE2_URL = "base2.url";
    /** physical.protocol (optional): for testing logical protocols */
    protected static final String CONFIG_PHYSICAL_PROTOCOL = "physical.protocol";

    private Properties m_properties;

    public AbstractTest() throws Exception {
        // SAGA bootstrap
        System.setProperty("saga.factory", "fr.in2p3.jsaga.impl.SagaFactoryImpl");

        // load test config
        InputStream stream = getClass().getClassLoader().getResourceAsStream("saga-test.properties");
        if (stream == null) {
            throw new Exception("Resource not found: saga-test.properties");
        }
        m_properties = new Properties();
        m_properties.load(stream);
    }

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

    protected static URL createURL(URL base, String name) throws NotImplemented, NoSuccess, BadParameter {
        String basePath = base.getPath();
        String path = (basePath.endsWith("/") ? basePath+name : basePath+"/"+name);
        URL url = new URL(base.toString());
        url.setPath(path);
        return url;
    }
}
