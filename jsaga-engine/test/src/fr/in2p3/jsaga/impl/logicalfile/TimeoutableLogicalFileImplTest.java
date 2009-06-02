package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.WaitForEverAdaptorAbstract;
import org.ogf.saga.AbstractTest;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.logicalfile.LogicalFileFactory;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   TimeoutableLogicalFileImplTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class TimeoutableLogicalFileImplTest extends AbstractTest {
    private static final String m_url = "waitforever-logical://host/directory/file";
    private LogicalFile m_file;

    public TimeoutableLogicalFileImplTest() throws Exception {
        super();
    }

    public void setUp() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_url);
        m_file = LogicalFileFactory.createLogicalFile(emptySession, url, Flags.NONE.getValue());
    }

    public void tearDown() throws Exception {
        m_file.close();
    }

    public void test_addLocation() throws Exception {
        try {
            m_file.addLocation(URLFactory.createURL(m_url));
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_removeLocation() throws Exception {
        try {
            m_file.removeLocation(URLFactory.createURL(m_url));
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_updateLocation() throws Exception {
        try {
            m_file.updateLocation(URLFactory.createURL(m_url), URLFactory.createURL(m_url+"new"));
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_listLocations() throws Exception {
        try {
            m_file.listLocations();
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_replicate() throws Exception {
        try {
            m_file.replicate(URLFactory.createURL(m_url));
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }
}
