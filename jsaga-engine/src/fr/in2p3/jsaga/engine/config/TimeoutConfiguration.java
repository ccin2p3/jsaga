package fr.in2p3.jsaga.engine.config;

import fr.in2p3.jsaga.EngineProperties;
import org.ogf.saga.SagaObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ${NAME}
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 mai 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public class TimeoutConfiguration {
    private static TimeoutConfiguration _instance;
    private Properties m_timeout;

    public static TimeoutConfiguration getInstance() throws ConfigurationException {
        if (_instance == null) {
            _instance = new TimeoutConfiguration();
        }
        return _instance;
    }
    private TimeoutConfiguration() throws ConfigurationException {
        // init
        m_timeout = new Properties();

        // load configuration
        URL timeoutCfgURL = EngineProperties.getURL(EngineProperties.JSAGA_TIMEOUT);
        if (timeoutCfgURL != null) {
            try {
                InputStream timeoutCfgStream = timeoutCfgURL.openStream();
                m_timeout.load(timeoutCfgStream);
                timeoutCfgStream.close();
            } catch (IOException e) {
                throw new ConfigurationException("Failed to load configuration file: "+timeoutCfgURL, e);
            }
        }
    }

    public float getTimeout(Class itf, String methodName, String protocolScheme) throws ConfigurationException {
        // get property
        String methodKey = itf.getName()+"#"+methodName;
        String protocolKey = methodKey+"|"+protocolScheme;
        String timeout = m_timeout.getProperty(protocolKey);
        if (timeout == null) {
            timeout = m_timeout.getProperty(methodKey);
        }

        // return timeout
        if (timeout!=null && !timeout.trim().equals("")) {
            try {
                return Float.parseFloat(timeout);
            } catch (NumberFormatException e) {
                throw new ConfigurationException("Configured timeout is not a float for: "+protocolKey, e);
            }
        } else {
            return SagaObject.WAIT_FOREVER;
        }
    }
}
