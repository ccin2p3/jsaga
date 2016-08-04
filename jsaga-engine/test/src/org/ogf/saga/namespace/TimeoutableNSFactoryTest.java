package org.ogf.saga.namespace;

import org.junit.Test;
import org.ogf.saga.JSAGABaseTest;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   TimeoutableNSFactoryTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class TimeoutableNSFactoryTest extends JSAGABaseTest {
    private static final String m_urlDir = "waitforever://host/directory/?hangatconnect";
    private static final String m_urlFile = "waitforever://host/directory/file?hangatconnect";

    public TimeoutableNSFactoryTest() throws Exception {
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
