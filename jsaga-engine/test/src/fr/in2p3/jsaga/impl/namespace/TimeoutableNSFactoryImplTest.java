package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.WaitForEverAdaptorAbstract;

import org.junit.Test;
import org.ogf.saga.AbstractTest;
import org.ogf.saga.AbstractTest_JUNIT4;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   TimeoutableNSFactoryImplTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class TimeoutableNSFactoryImplTest extends AbstractTest_JUNIT4 {
    private static final String m_urlDir = "waitforever://host/directory/?hangatconnect";
    private static final String m_urlFile = "waitforever://host/directory/file?hangatconnect";

    public TimeoutableNSFactoryImplTest() throws Exception {
        super();
    }

    @Test(expected=TimeoutException.class)
    public void test_createNSEntry() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_urlFile);
        NSFactory.createNSEntry(emptySession, url, Flags.NONE.getValue());
    }

    @Test(expected=TimeoutException.class)
    public void test_createNSDirectory() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_urlDir);
        NSFactory.createNSDirectory(emptySession, url, Flags.NONE.getValue());
    }
}
