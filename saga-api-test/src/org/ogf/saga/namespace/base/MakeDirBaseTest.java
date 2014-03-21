package org.ogf.saga.namespace.base;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.namespace.abstracts.AbstractDirectory;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MakeDirBaseTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Author: lionel.schwarz@in2P3.fr
* Date:   5 NOV 2013
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class MakeDirBaseTest extends AbstractDirectory {

	// subdir configuration
	protected static final String DEFAULT_SUBDIRNAME_2 = "subdir2/";
	protected URL m_subDirUrl2;
    protected NSDirectory m_subDir2;

    public MakeDirBaseTest(String protocol) throws Exception {
        super(protocol);
        m_subDirUrl2 = createURL(m_subDirUrl, DEFAULT_SUBDIRNAME_2);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        if (m_subDir2 != null) {
        	m_subDir2.close();
        }
        super.tearDown();
    }

    @Test(expected=DoesNotExistException.class)
    public void test_makeDir_child() throws Exception {
    	m_subDir.remove(Flags.RECURSIVE.getValue());
    	NSFactory.createNSDirectory(m_session, m_subDirUrl2, Flags.CREATE.getValue());
    }

    @Test
    public void test_makeDir_recursive() throws Exception {
    	m_subDir.remove(Flags.RECURSIVE.getValue());
    	m_subDir2 = NSFactory.createNSDirectory(m_session, m_subDirUrl2, Flags.CREATE.or(Flags.CREATEPARENTS));
    	assertEquals(true, m_subDir2.exists(m_subDirUrl2));
    }

    @Test
    public void test_makeDir_noexclusive() throws Exception {
    	NSFactory.createNSDirectory(m_session, m_subDirUrl, Flags.CREATE.getValue());
		assertEquals(true, m_subDir.exists(m_subDirUrl));
    }

    @Test(expected=AlreadyExistsException.class)
    public void test_makeDir_exclusive() throws Exception {
    	NSFactory.createNSDirectory(m_session, m_subDirUrl, Flags.CREATE.or(Flags.EXCL));
    }


    @Test(expected=DoesNotExistException.class)
    public void test_remove() throws Exception {
        m_subDir.remove(Flags.RECURSIVE.getValue());
        NSFactory.createNSDirectory(m_session, m_subDirUrl, Flags.NONE.getValue());
    }

    @Test(expected=IncorrectStateException.class)
    public void test_remove_notexist() throws Exception {
        m_subDir.remove(Flags.RECURSIVE.getValue());
        m_subDir.remove(Flags.NONE.getValue());
    }

    @Test(expected=IncorrectStateException.class)
    public void test_remove_recursive_notexist() throws Exception {
        m_subDir.remove(Flags.RECURSIVE.getValue());
        m_subDir.remove(Flags.RECURSIVE.getValue());
    }

    @Test(expected=BadParameterException.class)
    public void test_remove_norecursive() throws Exception {
    	m_subDir2 = m_dir.openDir(m_subDirUrl2, Flags.CREATE.getValue());
        m_subDir.remove(Flags.NONE.getValue());
    }
}
