package org.ogf.saga.namespace;

import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.abstracts.AbstractNSCopyTest;

import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NSCopyRecursiveTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NSCopyRecursiveTest extends AbstractNSCopyTest {
    public NSCopyRecursiveTest(String protocol, String targetProtocol) throws Exception {
        super(protocol, targetProtocol);
    }

    public void test_copy_norecurse() throws Exception {
        URI target = m_dirUri2.resolve(DEFAULT_DIRNAME);
        try {
            m_dir.copy(m_dirUri2, Flags.NONE);
            fail("Expected BadParameter exception");
        } catch(BadParameter e) {
        }
        try {
            NamespaceFactory.createNamespaceDirectory(m_session, target, Flags.NONE);
            fail("Expected DoesNotExist exception");
        } catch(DoesNotExist e) {
        }
    }

    public void test_copy_recurse() throws Exception {
        URI target = m_dirUri2.resolve(DEFAULT_DIRNAME).resolve(DEFAULT_FILENAME);
        m_dir.copy(m_dirUri2, Flags.RECURSIVE);
        checkCopied(target, DEFAULT_CONTENT);
    }

    public void test_copy_recurse_nooverwrite() throws Exception {
        URI target = m_rootUri2.resolve(DEFAULT_DIRNAME).resolve(DEFAULT_FILENAME);
        try {
            m_dir.copy(m_rootUri2, Flags.RECURSIVE);
            fail("Expected AlreadyExists exception");
        } catch(AlreadyExists e) {
        }
        try {
            NamespaceFactory.createNamespaceEntry(m_session, target, Flags.NONE);
            fail("Expected DoesNotExist exception");
        } catch(DoesNotExist e) {
        }
    }

    public void test_copy_recurse_overwrite() throws Exception {
        URI target = m_rootUri2.resolve(DEFAULT_DIRNAME).resolve(DEFAULT_FILENAME);
        m_dir.copy(m_rootUri2, Flags.RECURSIVE, Flags.OVERWRITE);
        checkCopied(target, DEFAULT_CONTENT);
    }

    public void test_move_recurse() throws Exception {
        URI target = m_dirUri2.resolve(DEFAULT_DIRNAME).resolve(DEFAULT_FILENAME);
        m_dir.move(m_dirUri2, Flags.RECURSIVE);
        checkCopied(target, DEFAULT_CONTENT);
        try {
            NamespaceFactory.createNamespaceDirectory(m_session, m_dirUri, Flags.NONE);
            fail("Expected DoesNotExist exception");
        } catch(DoesNotExist e) {
        }
    }
}
