package integration;

import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.EngineProperties;
import junit.framework.TestCase;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ConfigurationTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   7 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ConfigurationTest extends TestCase {
    public void test_config() throws Exception {
        // configure JSAGA engine
        System.setProperty("debug", "true");
        EngineProperties.setProperty(EngineProperties.JSAGA_CONFIGURATION, "../test/resources/config/jsaga-config.xml");
        EngineProperties.setProperty(EngineProperties.JSAGA_CONFIGURATION_ENABLE_CACHE, "false");

        // dump effective config
        Configuration.getInstance().getConfigurations().dump(System.out);
    }
}
