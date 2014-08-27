package fr.in2p3.jsaga.engine.session;

import fr.in2p3.jsaga.impl.context.ContextImpl;
import fr.in2p3.jsaga.impl.session.SessionImpl;
import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.rules.ExpectedException;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URLFactory;

import java.io.*;
import java.net.URL;
import java.util.Arrays;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   SessionConfigurationTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class SessionConfigurationTest {
    private static final String CONFIG_SUCCESS = "/config/jsaga-default-contexts-success.xml";
    private static final String CONFIG_FAILURE = "/config/jsaga-default-contexts-failure.xml";
    private static final String CONFIG_SCHEMES = "/config/jsaga-default-contexts-schemes.xml";
    private static final String CONFIG_INVALID_ATTRIBUTE = "/config/jsaga-default-contexts-invalidAttribute.xml";
    
    @Test
    public void test_dumpXML() throws Exception {
        URL configUrl = SessionConfigurationTest.class.getResource(CONFIG_SUCCESS);
        SessionConfiguration config = new SessionConfiguration(configUrl);
        String expected = getResourceAsString("/config/expected.xml");
        //TODO: remove this workaround when castor will be replaced with JAXB
        expected = expected.replaceAll("\\r\\n", "\n");
        Assert.assertTrue(configUrl != null);
        Assert.assertEquals(expected, config.toXML());
    }

    @Test
    public void test_dumpSession() throws Exception {
        Session session = createConfiguredSession(CONFIG_SUCCESS);
        for (Context context : session.listContexts()) {
            System.out.println("-------------------------");
            for (String key : context.listAttributes()) {
                if (context.isVectorAttribute(key)) {
                    System.out.println(key+"="+ Arrays.toString(context.getVectorAttribute(key)));
                } else {
                    System.out.println(key+"="+context.getAttribute(key));
                }
            }
        }
    }
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test_failure() throws Exception {
        thrown.expect(NoSuccessException.class);
        thrown.expectMessage("conflicts with");
        createConfiguredSession(CONFIG_FAILURE);
    }

    @Test
    public void test_success() throws Exception {
        createConfiguredSession(CONFIG_SUCCESS);
    }

    @Test
    public void test_invalidAttribute() throws Exception {
        thrown.expect(NoSuccessException.class);
        thrown.expectMessage("Invalid");
        createConfiguredSession(CONFIG_INVALID_ATTRIBUTE);
    }
    
    @Test
    public void test_findContext() throws Exception {
        SessionImpl session = (SessionImpl) createConfiguredSession(CONFIG_SUCCESS);
        Assert.assertEquals("DGrid", findPrefixByUrl(session, "gridftp://cclcgvmli07.in2p3.fr/pnfs/dteam/myfile.txt"));
        Assert.assertEquals("DGrid", findPrefixByUrl(session, "gsiftp://myhost.mydomain.de/tmp"));
        Assert.assertEquals("DGrid", findPrefixByUrl(session, "gsiftp://myhost.fzk.de:6666/tmp"));
        Assert.assertEquals("EGEE-dteam", findPrefixByUrl(session, "gsiftp://myhost.fzk.de/tmp"));
        Assert.assertEquals("EGEE-dteam", findPrefixByUrl(session, "EGEE-dteam-srm://ccsrm.in2p3.fr/pnfs/dteam"));
        Assert.assertEquals("EGEE-myvo", findPrefixByUrl(session, "srm://ccsrm.in2p3.fr/pnfs/dteam"));
        Assert.assertEquals(null, findPrefixByUrl(session, "gridftp://cclcgvmli07.in2p3.fr/tmp"));
    }
    
    @Test
    public void test_checkAttributes() throws Exception {
    	String[] expectedKeys = new String[]{ContextImpl.BASE_URL_EXCLUDES,
    			ContextImpl.BASE_URL_INCLUDES,
    			ContextImpl.TYPE,
    			"Att",
    			ContextImpl.URL_PREFIX,
    			ContextImpl.JOB_SERVICE_ATTRIBUTES,
    			ContextImpl.DATA_SERVICE_ATTRIBUTES};
        SessionImpl session = (SessionImpl) createConfiguredSession(CONFIG_SCHEMES);
        
        // Check we have 1 context only
        Context[] ctxs = session.listContexts();
        Assert.assertEquals(1, ctxs.length);
        Context context = ctxs[0];
        // Check keys
        Arrays.sort(expectedKeys);
        String[] _listAttributes = context.listAttributes();
        Arrays.sort(_listAttributes);
        Assert.assertArrayEquals(expectedKeys, _listAttributes);
    	// Check values
        for (String key : context.listAttributes()) {
            if (key.equals(ContextImpl.BASE_URL_EXCLUDES)) {
                Assert.assertArrayEquals(new String[]{}, context.getVectorAttribute(key));
            } else if (key.equals(ContextImpl.BASE_URL_INCLUDES)) {
                Assert.assertArrayEquals(new String[]{"unicore://*","unicore://*"}, context.getVectorAttribute(key));
            } else if (key.equals(ContextImpl.TYPE)) {
                Assert.assertEquals("VOMS",context.getAttribute(key));
            } else if (key.equals("Att")) {
                Assert.assertEquals("Value",context.getAttribute(key));
            } else if (key.equals(ContextImpl.URL_PREFIX)) {
                Assert.assertEquals("Demo",context.getAttribute(key));
            } else if (key.equals(ContextImpl.JOB_SERVICE_ATTRIBUTES)) {
                Assert.assertArrayEquals(new String[]{"unicore.MaxJobsQueued=100","unicore.ServiceName=JobManagement"}, context.getVectorAttribute(key));
            } else if (key.equals(ContextImpl.DATA_SERVICE_ATTRIBUTES)) {
                Assert.assertArrayEquals(new String[]{"unicore.ServiceName=StorageManagement"}, context.getVectorAttribute(key));
            }
        }
    }
    
    private static String findPrefixByUrl(SessionImpl session, String url) throws Exception {
        Context context = session.findContext(URLFactory.createURL(url));
        if (context != null) {
            return context.getAttribute(ContextImpl.URL_PREFIX);
        } else {
            return null;
        }
    }

    public static String getResourceAsString(String path) throws IOException {
        InputStream in = SessionConfigurationTest.class.getResourceAsStream(path);
        if (in != null) {
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (int len; (len=in.read(buffer))>-1; ) {
                out.write(buffer, 0, len);
            }
            return out.toString();
        } else {
            throw new FileNotFoundException("Resource not found: "+path);
        }
    }

    private static Session createConfiguredSession(String configPath) throws IncorrectStateException, NoSuccessException, TimeoutException {
        URL configUrl = SessionConfigurationTest.class.getResource(configPath);
        SessionConfiguration config = new SessionConfiguration(configUrl);
		Session session = SessionFactory.createSession(false);
        config.setDefaultSession(session);
        
        return session;
    }
}
