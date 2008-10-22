package org.ogf.saga.namespace.abstracts;

import org.ogf.saga.url.URL;
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
	protected static final String DEFAULT_SUBDIRNAME_2 = "subdir2/";
	protected URL m_subDirUrl2;
    protected NSDirectory m_subDir2;

    public AbstractNSDirectoryMakeTest(String protocol) throws Exception {
        super(protocol);
        m_subDirUrl2 = createURL(m_subDirUrl, DEFAULT_SUBDIRNAME_2);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        if (m_subDir2 != null) {
        	m_subDir2.close();
        }
        super.tearDown();
    }

    public void test_makeDir_child() throws Exception {
    	m_subDir.remove(Flags.RECURSIVE.getValue());
        try {
    		NSFactory.createNSDirectory(m_session, m_subDirUrl2, Flags.CREATE.getValue());
            fail("Expected DoesNotExist exception");
        } catch(DoesNotExist e) {
        }
    }

    public void test_makeDir_recursive() throws Exception {
    	m_subDir.remove(Flags.RECURSIVE.getValue());
    	m_subDir2 = NSFactory.createNSDirectory(m_session, m_subDirUrl2, Flags.CREATE.or(Flags.CREATEPARENTS));
    	assertEquals(true, m_subDir2.exists(m_subDirUrl2));
    }

    public void test_makeDir_noexclusive() throws Exception {
    	NSFactory.createNSDirectory(m_session, m_subDirUrl, Flags.CREATE.getValue());
		assertEquals(true, m_subDir.exists(m_subDirUrl));
    }

    public void test_makeDir_exclusive() throws Exception {
    	try {
    		NSFactory.createNSDirectory(m_session, m_subDirUrl, Flags.CREATE.or(Flags.EXCL));
            fail("Expected AlreadyExist exception");
        } catch(AlreadyExists e) {
        }
    }


    public void test_remove() throws Exception {
        m_subDir.remove(Flags.RECURSIVE.getValue());
        try {
            NSFactory.createNSDirectory(m_session, m_subDirUrl, Flags.NONE.getValue());
            fail("Expected DoesNotExist exception");
        } catch(DoesNotExist e) {
        }
    }

    public void test_remove_norecursive() throws Exception {
    	m_subDir2 = m_dir.openDir(m_subDirUrl2, Flags.CREATE.getValue());
        try {
        	m_subDir.remove(Flags.NONE.getValue());
            fail("Expected NoSuccess exception");
        } catch(NoSuccess e) {
        }
    }
}
