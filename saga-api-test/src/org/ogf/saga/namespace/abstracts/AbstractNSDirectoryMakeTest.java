package org.ogf.saga.namespace.abstracts;

import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;

import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNSDirectoryMakeTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractNSDirectoryMakeTest extends AbstractNSDirectoryTest {

	// subdir configuration
	protected static final String DEFAULT_SUBDIRNAME = "subdir/";
	protected URL m_subDirUrl;
    protected NSDirectory m_subDir;

    public AbstractNSDirectoryMakeTest(String protocol) throws Exception {
        super(protocol);
        m_subDirUrl = createURL(m_dirUrl, DEFAULT_SUBDIRNAME);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        if (m_subDir != null) {
        	m_subDir.close();
        }
        super.tearDown();
    }

    public void test_makeDir_child() throws Exception {
    	m_dir.remove(Flags.RECURSIVE.getValue());
        try {
    		NSFactory.createNSDirectory(m_session, m_subDirUrl, Flags.CREATE.getValue());
            fail("Expected DoesNotExist exception");
        } catch(DoesNotExist e) {
        }
    }

    public void test_makeDir_recursive() throws Exception {
    	m_dir.remove(Flags.RECURSIVE.getValue());
    	m_subDir = NSFactory.createNSDirectory(m_session, m_subDirUrl, Flags.CREATE.or(Flags.CREATEPARENTS));
    	assertEquals(true, m_subDir.exists(m_subDirUrl));
    }

    public void test_makeDir_noexclusive() throws Exception {
    	NSFactory.createNSDirectory(m_session, m_dirUrl, Flags.CREATE.getValue());
		assertEquals(true, m_dir.exists(m_dirUrl));
    }

    public void test_makeDir_exclusive() throws Exception {
    	try {
    		NSFactory.createNSDirectory(m_session, m_dirUrl, Flags.CREATE.or(Flags.EXCL));
            fail("Expected AlreadyExist exception");
        } catch(AlreadyExists e) {
        }
    }


    public void test_remove() throws Exception {
        m_dir.remove(Flags.RECURSIVE.getValue());
        try {
            NSFactory.createNSDirectory(m_session, m_dirUrl, Flags.NONE.getValue());
            fail("Expected DoesNotExist exception");
        } catch(DoesNotExist e) {
        }
    }

    public void test_remove_norecursive() throws Exception {
    	m_subDir = m_root.openDir(m_subDirUrl, Flags.CREATE.getValue());
        try {
        	m_dir.remove(Flags.NONE.getValue());
            fail("Expected NoSuccess exception");
        } catch(NoSuccess e) {
        }
    }
}
