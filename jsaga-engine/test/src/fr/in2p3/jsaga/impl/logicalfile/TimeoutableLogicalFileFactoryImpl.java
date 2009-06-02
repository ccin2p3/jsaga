package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.WaitForEverAdaptorAbstract;
import org.ogf.saga.AbstractTest;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.logicalfile.LogicalFileFactory;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   TimeoutableLogicalFileFactoryImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class TimeoutableLogicalFileFactoryImpl extends AbstractTest {
    private static final String m_urlDir = "waitforever-logical://host/directory/?hangatconnect";
    private static final String m_urlFile = "waitforever-logical://host/directory/file?hangatconnect";

    public TimeoutableLogicalFileFactoryImpl() throws Exception {
        super();
    }

    public void test_createLogicalFile() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_urlFile);
        try {
            LogicalFileFactory.createLogicalFile(emptySession, url);
            fail("Expected exception: "+ TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }

    public void test_createLogicalDirectory() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_urlDir);
        try {
            LogicalFileFactory.createLogicalDirectory(emptySession, url);
            fail("Expected exception: "+TimeoutException.class);
        } catch (TimeoutException e) {
            assertTrue("Should be hanged", WaitForEverAdaptorAbstract.isHanging());
        }
    }
}
