package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.WaitForEverAdaptorAbstract;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ogf.saga.JSAGABaseTest;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.logicalfile.LogicalDirectory;
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
 * File:   TimeoutableLogicalDirectoryImplTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class TimeoutableLogicalDirectoryImplTest extends JSAGABaseTest {
    private static final String m_url = "waitforever-logical://host/directory/";
    private static Logger s_logger = Logger.getLogger(TimeoutableLogicalDirectoryImplTest.class);
    private LogicalDirectory m_directory;

    public TimeoutableLogicalDirectoryImplTest() throws Exception {
        super();
    }

    @Before
    public void setUp() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_url);
        m_directory = LogicalFileFactory.createLogicalDirectory(emptySession, url, Flags.NONE.getValue());
    }

    @After
    public void tearDown() throws Exception {
        m_directory.close();
    }

    @Test
    public void test_find() throws Exception {
        try {
            m_directory.find("mymetadata", new String[0]);
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    /** timeout not supported */
    @Test(expected=NotImplementedException.class)
    public void test_openLogicalDir() throws Exception {
            m_directory.openLogicalDir(URLFactory.createURL(m_url));
    }

    /** timeout not supported */
    @Test(expected=NotImplementedException.class)
    public void test_openLogicalFile() throws Exception {
            m_directory.openLogicalFile(URLFactory.createURL(m_url+"file"));
    }
}
