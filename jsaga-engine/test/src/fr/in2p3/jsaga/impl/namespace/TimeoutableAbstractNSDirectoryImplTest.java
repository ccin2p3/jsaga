package fr.in2p3.jsaga.impl.namespace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ogf.saga.AbstractTest_JUNIT4;
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
public class TimeoutableAbstractNSDirectoryImplTest extends AbstractTest_JUNIT4 {
    private static final String m_url = "waitforever://host/directory/";
    private NSDirectory m_directory;

    public TimeoutableAbstractNSDirectoryImplTest() throws Exception {
        super();
    }

    @Before
    public void setUp() throws Exception {
        Session emptySession = SessionFactory.createSession(false);
        URL url = URLFactory.createURL(m_url);
        m_directory = NSFactory.createNSDirectory(emptySession, url, Flags.NONE.getValue());
    }

    @After
    public void tearDown() throws Exception {
        m_directory.close();
    }

    @Test
    public void test_changeDir() throws Exception {
        // can not hang...
        m_directory.changeDir(URLFactory.createURL("new_directory/"));
        // TODO uncomment this when changeDir will be possible on absolute URLs
        //m_directory.changeDir(URLFactory.createURL("waitforever://host/new_directory/"));
    }

    @Test(expected=TimeoutException.class)
    public void test_list() throws Exception {
        m_directory.list();
    }

    @Test(expected=TimeoutException.class)
    public void test_find() throws Exception {
            m_directory.find("*");
    }

    @Test(expected=TimeoutException.class)
    public void test_getNumEntries() throws Exception {
            m_directory.getNumEntries();
    }

    @Test(expected=TimeoutException.class)
    public void test_getEntry() throws Exception {
            m_directory.getEntry(0);
    }

    @Test(expected=TimeoutException.class)
    public void test_makeDir() throws Exception {
            m_directory.makeDir(URLFactory.createURL(m_url));
    }

    @Test(expected=TimeoutException.class)
    public void test_copy() throws Exception {
            m_directory.copy(URLFactory.createURL(m_url), Flags.RECURSIVE.getValue());
    } 
    
    @Test(expected=NotImplementedException.class)
    public void test_openDir() throws Exception {
            m_directory.openDir(URLFactory.createURL(m_url));
    }

    @Test(expected=NotImplementedException.class)
    public void test_open() throws Exception {
            m_directory.open(URLFactory.createURL(m_url));
    }
}
