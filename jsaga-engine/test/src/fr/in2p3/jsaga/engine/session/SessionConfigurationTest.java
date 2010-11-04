package fr.in2p3.jsaga.engine.session;

import fr.in2p3.jsaga.impl.context.ContextImpl;
import fr.in2p3.jsaga.impl.session.SessionImpl;
import junit.framework.TestCase;
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
public class SessionConfigurationTest extends TestCase {
    private static final String CONFIG_SUCCESS = "/config/jsaga-default-contexts-success.xml";
    private static final String CONFIG_FAILURE = "/config/jsaga-default-contexts-failure.xml";

    public void test_dumpXML() throws Exception {
        URL configUrl = SessionConfigurationTest.class.getResource(CONFIG_SUCCESS);
        SessionConfiguration config = new SessionConfiguration(configUrl);
        String expected = getResourceAsString("/config/expected.xml");
        //TODO: remove this workaround when castor will be replaced with JAXB
        expected = expected.replaceAll("\\r\\n", "\n");
        assertTrue(configUrl != null);
        assertEquals(expected, config.toXML());
    }

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

    public void test_failure() throws Exception {
        try {
            createConfiguredSession(CONFIG_FAILURE);
            fail("Expected exception: "+NoSuccessException.class);
        } catch (NoSuccessException e) {
            if (e.getMessage()!=null && e.getMessage().contains("conflicts with")) {
                // test successful
            } else {
                throw e;
            }
        }
    }

    public void test_success() throws Exception {
        try {
            createConfiguredSession(CONFIG_SUCCESS);
            // test successful
        } catch (NoSuccessException e) {
            if (e.getMessage()!=null && e.getMessage().contains("conflicts with")) {
                fail("Unexpected exception: "+e.getMessage());
            } else {
                throw e;
            }
        }
    }

    public void test_findContext() throws Exception {
        SessionImpl session = (SessionImpl) createConfiguredSession(CONFIG_SUCCESS);
        assertEquals("DGrid", findPrefixByUrl(session, "gridftp://cclcgvmli07.in2p3.fr/pnfs/dteam/myfile.txt"));
        assertEquals("DGrid", findPrefixByUrl(session, "gsiftp://myhost.mydomain.de/tmp"));
        assertEquals("DGrid", findPrefixByUrl(session, "gsiftp://myhost.fzk.de:6666/tmp"));
        assertEquals("EGEE-dteam", findPrefixByUrl(session, "gsiftp://myhost.fzk.de/tmp"));
        assertEquals("EGEE-dteam", findPrefixByUrl(session, "EGEE-dteam-srm://ccsrm.in2p3.fr/pnfs/dteam"));
        assertEquals("EGEE-myvo", findPrefixByUrl(session, "srm://ccsrm.in2p3.fr/pnfs/dteam"));
        assertEquals(null, findPrefixByUrl(session, "gridftp://cclcgvmli07.in2p3.fr/tmp"));
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
