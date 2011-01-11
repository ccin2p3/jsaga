package fr.in2p3.jsaga.engine.session;

import junit.framework.TestCase;

import java.net.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   SessionConfigurationMergeTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class SessionConfigurationMergeTest extends TestCase {
    private static final String CONFIG = "/home/user-default-contexts.xml";

    public void test_dumpXML() throws Exception {
        System.setProperty("VO", "dteam");

        URL configUrl = SessionConfigurationMergeTest.class.getResource(CONFIG);
        SessionConfiguration config = new SessionConfiguration(configUrl);
        String expected = SessionConfigurationTest.getResourceAsString("/home/expected.xml");
        //TODO: remove this workaround when castor will be replaced with JAXB
        expected = expected.replaceAll("\\r\\n", "\n");
        assertTrue(configUrl != null);
        assertEquals(expected, config.toXML());
    }
}