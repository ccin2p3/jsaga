package fr.in2p3.jsaga.engine.session;

import fr.in2p3.jsaga.impl.context.ContextImpl;
import fr.in2p3.jsaga.impl.context.attrs.DataServiceConfigAttribute;
import fr.in2p3.jsaga.impl.context.attrs.JobServiceConfigAttribute;
import fr.in2p3.jsaga.impl.context.attrs.ServiceConfigAttribute;
import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
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
public class ServiceConfigAttributeTest {
	
    @Test
	public void test_set_job() throws Exception {
		test_set(ContextImpl.JOB_SERVICE_ATTRIBUTES);
	}
	
    @Test
	public void test_set_data() throws Exception {
		test_set(ContextImpl.DATA_SERVICE_ATTRIBUTES);
	}
	
    private void test_set(String serviceType) throws Exception {
        ServiceConfigAttribute vector = getVector(serviceType);
        vector.setValues(new String[]{"srb.Resource=foo", "srb.Zone=bar", "srm.Protocols=gsiftp,gsidcap", "Ping=true"});
        Properties srb = vector.getServiceConfig("srb");
        Assert.assertEquals("foo", srb.getProperty("Resource"));
        Assert.assertEquals("bar", srb.getProperty("Zone"));
        Assert.assertEquals("true", srb.getProperty("Ping"));
        Properties srm = vector.getServiceConfig("srm");
        Assert.assertEquals("gsiftp,gsidcap", srm.getProperty("Protocols"));
        Assert.assertEquals("true", srm.getProperty("Ping"));
    }

    @Test
	public void test_unset_job() throws Exception {
		test_unset(ContextImpl.JOB_SERVICE_ATTRIBUTES);
	}
	
    @Test
	public void test_unset_data() throws Exception {
		test_unset(ContextImpl.DATA_SERVICE_ATTRIBUTES);
	}
	
    private void test_unset(String serviceType) throws Exception {
        ServiceConfigAttribute vector = getVector(serviceType);
        vector.setValues(new String[]{"srm.Protocols=", "*.Ping="});
        Properties srm = vector.getServiceConfig("srm");
        Assert.assertEquals("", srm.getProperty("Protocols"));
        Assert.assertEquals("", srm.getProperty("Ping"));
    }

    @Test
	public void test_error_job() throws Exception {
		test_error(ContextImpl.JOB_SERVICE_ATTRIBUTES);
	}
	
    @Test
	public void test_error_data() throws Exception {
		test_error(ContextImpl.DATA_SERVICE_ATTRIBUTES);
	}
	
    private void test_error(String serviceType) throws Exception {
        ServiceConfigAttribute vector = getVector(serviceType);
        try {
            vector.setValues(new String[]{"srb.Resource"});
            Assert.fail("Expected exception: "+BadParameterException.class);
        } catch (BadParameterException e) {
        }
        try {
            vector.setValues(new String[]{".Resource=foo"});
            Assert.fail("Expected exception: "+BadParameterException.class);
        } catch (BadParameterException e) {
        }
        try {
            vector.setValues(new String[]{"srb.=bar"});
            Assert.fail("Expected exception: "+BadParameterException.class);
        } catch (BadParameterException e) {
        }
    }
    
    private ServiceConfigAttribute getVector(String serviceType) throws Exception {
        if (ContextImpl.JOB_SERVICE_ATTRIBUTES.equals(serviceType)) {
        	return new JobServiceConfigAttribute();
        } else if (ContextImpl.DATA_SERVICE_ATTRIBUTES.equals(serviceType)) {
        	return new DataServiceConfigAttribute();
        }
        throw new Exception("unknown service type: " + serviceType);
    }
}
