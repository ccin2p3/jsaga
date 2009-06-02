package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.WaitForEverAdaptorAbstract;
import org.apache.log4j.Logger;
import org.ogf.saga.AbstractTest;
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
public class TimeoutableLogicalDirectoryImplTest extends AbstractTest {
    private static final String m_url = "waitforever-logical://host/directory/";
    private static Logger s_logger = Logger.getLogger(TimeoutableLogicalDirectoryImplTest.class);
    private LogicalDirectory m_directory;

    public TimeoutableLogicalDirectoryImplTest() throws Exception {
        super();
    }

    public void setUp() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_url);
        m_directory = LogicalFileFactory.createLogicalDirectory(emptySession, url, Flags.NONE.getValue());
    }

    public void tearDown() throws Exception {
        m_directory.close();
    }

    public void test_find() throws Exception {
        try {
            m_directory.find("mymetadata", new String[0]);
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    /** timeout not supported */
    public void test_openLogicalDir() throws Exception {
        try {
            m_directory.openLogicalDir(URLFactory.createURL(m_url));
            fail("Expected exception: "+ TimeoutException.class);
        } catch (NotImplementedException e) {
            s_logger.info(e.getMessage());
        }
    }

    /** timeout not supported */
    public void test_openLogicalFile() throws Exception {
        try {
            m_directory.openLogicalFile(URLFactory.createURL(m_url+"file"));
            fail("Expected exception: "+ TimeoutException.class);
        } catch (NotImplementedException e) {
            s_logger.info(e.getMessage());
        }
    }
}
