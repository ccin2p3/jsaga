package fr.in2p3.jsaga.engine.session;

import fr.in2p3.jsaga.impl.context.attrs.ServiceConfigAttribute;
import junit.framework.TestCase;
import org.ogf.saga.error.BadParameterException;

import java.util.Properties;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   ServiceConfigAttributeTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class ServiceConfigAttributeTest extends TestCase {
    public void test_set() throws Exception {
        ServiceConfigAttribute vector = new ServiceConfigAttribute();
        vector.setValues(new String[]{"srb.Resource=foo", "srb.Zone=bar", "srm.Protocols=gsiftp,gsidcap", "Ping=true"});
        Properties srb = vector.getServiceConfig("srb");
        assertEquals("foo", srb.getProperty("Resource"));
        assertEquals("bar", srb.getProperty("Zone"));
        assertEquals("true", srb.getProperty("Ping"));
        Properties srm = vector.getServiceConfig("srm");
        assertEquals("gsiftp,gsidcap", srm.getProperty("Protocols"));
        assertEquals("true", srm.getProperty("Ping"));
    }

    public void test_unset() throws Exception {
        ServiceConfigAttribute vector = new ServiceConfigAttribute();
        vector.setValues(new String[]{"srm.Protocols=", "*.Ping="});
        Properties srm = vector.getServiceConfig("srm");
        assertEquals("", srm.getProperty("Protocols"));
        assertEquals("", srm.getProperty("Ping"));
    }

    public void test_error() throws Exception {
        ServiceConfigAttribute vector = new ServiceConfigAttribute();
        try {
            vector.setValues(new String[]{"srb.Resource"});
            fail("Expected exception: "+BadParameterException.class);
        } catch (BadParameterException e) {
        }
        try {
            vector.setValues(new String[]{".Resource=foo"});
            fail("Expected exception: "+BadParameterException.class);
        } catch (BadParameterException e) {
        }
        try {
            vector.setValues(new String[]{"srb.=bar"});
            fail("Expected exception: "+BadParameterException.class);
        } catch (BadParameterException e) {
        }
    }
}
