package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.WaitForEverAdaptorAbstract;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ogf.saga.JSAGABaseTest;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.file.File;
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
 * File:   TimeoutableFileImplTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class TimeoutableFileImplTest extends JSAGABaseTest {
    private static final String m_url = "waitforever://host/directory/file";
    private File m_readfile, m_writefile;

    public TimeoutableFileImplTest() throws Exception {
        super();
    }

    @Before
    public void setUp() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_url);
        m_readfile = FileFactory.createFile(emptySession, url, Flags.READ.getValue());
        m_writefile = FileFactory.createFile(emptySession, url, Flags.WRITE.getValue());
    }

    @After
    public void tearDown() throws Exception {
        m_readfile.close();
    }

    @Test
    public void test_getSize() throws Exception {
        try {
            m_readfile.getSize();
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    @Test
    public void test_read() throws Exception {
        try {
            m_readfile.read(BufferFactory.createBuffer(1024));
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    @Test
    public void test_write() throws Exception {
        try {
            m_writefile.write(BufferFactory.createBuffer(1024));
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }
}
