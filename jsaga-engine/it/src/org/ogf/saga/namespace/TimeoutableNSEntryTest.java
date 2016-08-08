package org.ogf.saga.namespace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ogf.saga.JSAGABaseTest;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.permissions.Permission;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   TimeoutableNSEntryTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class TimeoutableNSEntryTest extends JSAGABaseTest {
    private static final String m_url = "waitforever://host/directory/file";
    private NSEntry m_entry;

    public TimeoutableNSEntryTest() throws Exception {
        super();
    }

    @Before
    public void setUp() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_url);
        m_entry = NSFactory.createNSEntry(emptySession, url, Flags.NONE.getValue());
    }

    @After
    public void tearDown() throws Exception {
        m_entry.close();
    }

    @Test(expected=TimeoutException.class)
    public void test_isDir() throws Exception {
        m_entry.isDir();
    }

    @Test(expected=TimeoutException.class)
    public void test_isEntry() throws Exception {
        m_entry.isEntry();
    }

    @Test(expected=TimeoutException.class)
    public void test_isLink() throws Exception {
        m_entry.isLink();
    }

    @Test(expected=TimeoutException.class)
    public void test_readLink() throws Exception {
        m_entry.readLink();
    }

    @Test(expected=TimeoutException.class)
    public void test_copy() throws Exception {
        m_entry.copy(URLFactory.createURL(m_url));
    }

    @Test(expected=TimeoutException.class)
    public void test_link() throws Exception {
        m_entry.link(URLFactory.createURL(m_url));
    }

    @Test(expected=TimeoutException.class)
    public void test_move() throws Exception {
        m_entry.move(URLFactory.createURL(m_url));
    }

    @Test(expected=TimeoutException.class)
    public void test_remove() throws Exception {
        m_entry.remove();
    }

    @Test(expected=TimeoutException.class)
    public void test_permissionsAllow() throws Exception {
        m_entry.permissionsAllow("*", Permission.READ.getValue(), Flags.NONE.getValue());
    }

    @Test(expected=TimeoutException.class)
    public void test_permissionsDeny() throws Exception {
        m_entry.permissionsDeny("*", Permission.READ.getValue(), Flags.NONE.getValue());
    }
}
