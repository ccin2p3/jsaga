package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.WaitForEverAdaptorAbstract;
import org.apache.log4j.Logger;
import org.ogf.saga.AbstractTest;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   TimeoutableAbstractNSDirectoryImplTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class TimeoutableAbstractNSDirectoryImplTest extends AbstractTest {
    private static final String m_url = "waitforever://host/directory/";
    private static Logger s_logger = Logger.getLogger(TimeoutableAbstractNSDirectoryImplTest.class);
    private NSDirectory m_directory;

    public TimeoutableAbstractNSDirectoryImplTest() throws Exception {
        super();
    }

    public void setUp() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_url);
        m_directory = NSFactory.createNSDirectory(emptySession, url, Flags.NONE.getValue());
    }

    public void tearDown() throws Exception {
        m_directory.close();
    }

    public void test_changeDir() throws Exception {
        // can not hang...
        m_directory.changeDir(URLFactory.createURL("new_directory/"));
        // TODO uncomment this when changeDir will be possible on absolute URLs
        //m_directory.changeDir(URLFactory.createURL("waitforever://host/new_directory/"));
    }

    public void test_list() throws Exception {
        try {
            m_directory.list();
            fail("Expected exception: "+TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_find() throws Exception {
        try {
            m_directory.find("*");
            fail("Expected exception: "+TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_getNumEntries() throws Exception {
        try {
            m_directory.getNumEntries();
            fail("Expected exception: "+TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_getEntry() throws Exception {
        try {
            m_directory.getEntry(0);
            fail("Expected exception: "+TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_makeDir() throws Exception {
        try {
            m_directory.makeDir(URLFactory.createURL(m_url));
            fail("Expected exception: "+TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_openDir() throws Exception {
        try {
            m_directory.openDir(URLFactory.createURL(m_url));
            fail("Expected exception: "+NotImplementedException.class);
        } catch (NotImplementedException e) {
            s_logger.info(e.getMessage());
        }
    }

    public void test_open() throws Exception {
        try {
            m_directory.open(URLFactory.createURL(m_url));
            fail("Expected exception: "+NotImplementedException.class);
        } catch (NotImplementedException e) {
            s_logger.info(e.getMessage());
        }
    }
}
