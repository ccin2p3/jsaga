package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.WaitForEverAdaptorAbstract;
import org.ogf.saga.AbstractTest;
import org.ogf.saga.error.TimeoutException;
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
 * File:   TimeoutableFileFactoryImplTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class TimeoutableFileFactoryImplTest extends AbstractTest {
    private static final String m_urlDir = "waitforever://host/directory/?hangatconnect";
    private static final String m_urlFile = "waitforever://host/directory/file?hangatconnect";

    public TimeoutableFileFactoryImplTest() throws Exception {
        super();
    }

    public void test_createFile() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_urlFile);
        try {
            FileFactory.createFile(emptySession, url, Flags.READWRITE.getValue());
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_createFileInputStream() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_urlFile);
        try {
            FileFactory.createFileInputStream(emptySession, url);
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_createFileOutputStream() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_urlFile);
        try {
            FileFactory.createFileOutputStream(emptySession, url);
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_createDirectory() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_urlDir);
        try {
            FileFactory.createDirectory(emptySession, url, Flags.READWRITE.getValue());
            fail("Expected exception: "+TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }
}
