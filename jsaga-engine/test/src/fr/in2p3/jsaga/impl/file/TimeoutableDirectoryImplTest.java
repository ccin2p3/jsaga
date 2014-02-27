package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.WaitForEverAdaptorAbstract;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ogf.saga.JSAGABaseTest;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.file.Directory;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   TimeoutableDirectoryImplTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class TimeoutableDirectoryImplTest extends JSAGABaseTest {
    private static final String m_urlRoot = "waitforever://host/directory/";
    private static final String m_urlDir = m_urlRoot+"?hangatconnect";
    private static final String m_urlFile = m_urlRoot+"file?hangatconnect";
    private static Logger s_logger = Logger.getLogger(TimeoutableDirectoryImplTest.class);
    private Directory m_directory;

    public TimeoutableDirectoryImplTest() throws Exception {
        super();
    }

    @Before
    public void setUp() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_urlRoot);
        m_directory = FileFactory.createDirectory(emptySession, url, Flags.NONE.getValue());
    }

    @After
    public void tearDown() throws Exception {
        m_directory.close();
    }

    /** timeout not supported */
    @Test(expected=NotImplementedException.class)
    public void test_openDirectory() throws Exception {
            m_directory.openDirectory(URLFactory.createURL(m_urlDir), Flags.CREATE.getValue());
    }

    /** timeout not supported */
    @Test(expected=NotImplementedException.class)
    public void test_openFile() throws Exception {
            m_directory.openFile(URLFactory.createURL(m_urlFile), Flags.READ.getValue());
    }

    @Test
    public void test_openFileInputStream() throws Exception {
        try {
            m_directory.openFileInputStream(URLFactory.createURL(m_urlFile));
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    @Test
    public void test_openFileOutputStream() throws Exception {
        try {
            m_directory.openFileOutputStream(URLFactory.createURL(m_urlFile));
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }
}
