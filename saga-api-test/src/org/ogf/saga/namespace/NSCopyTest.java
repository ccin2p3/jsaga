package org.ogf.saga.namespace;

import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.abstracts.AbstractNSCopyTest;

import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NSCopyTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NSCopyTest extends AbstractNSCopyTest {
    public NSCopyTest(String protocol, String targetProtocol) throws Exception {
        super(protocol, targetProtocol);
    }

    public void test_copy() throws Exception {
        URI target = m_rootUri2.resolve(DEFAULT_FILENAME);
        m_file.copy(m_rootUri2, Flags.NONE);
        checkCopied(target, DEFAULT_CONTENT);
    }

    public void test_copy_and_rename() throws Exception {
        URI target = m_dirUri2.resolve("copy.txt");
        m_file.copy(target, Flags.NONE);
        checkCopied(target, DEFAULT_CONTENT);
    }

    public void test_copy_nooverwrite() throws Exception {
        URI target = m_dirUri2.resolve(DEFAULT_FILENAME_2);
        try {
            m_file.copy(target, Flags.NONE);
            fail("Expected AlreadyExist exception");
        } catch(AlreadyExists e) {
            checkCopied(target, DEFAULT_CONTENT_2);
        }
    }

    public void test_copy_overwrite() throws Exception {
        URI target = m_dirUri2.resolve(DEFAULT_FILENAME_2);
        m_file.copy(target, Flags.OVERWRITE);
        checkCopied(target, DEFAULT_CONTENT);
    }

    public void test_copy_lateExistenceCheck() throws Exception {
        NamespaceEntry entry = null;
        try {
            System.setProperty("saga.existence.check.skip", "true");
            entry = NamespaceFactory.createNamespaceEntry(m_session, m_dirUri.resolve("unexisting.txt"), Flags.NONE);
            System.setProperty("saga.existence.check.skip", "false");
        } catch(DoesNotExist e) {
            fail("Unexpected DoesNotExist exception");
        }
        try {
            entry.copy(m_dirUri2, Flags.NONE);
            fail("Expected IncorrectState exception");
        } catch(IncorrectState e) {
        }
        try {
            NamespaceFactory.createNamespaceEntry(m_session, m_dirUri2.resolve("unexisting.txt"), Flags.NONE);
            fail("Expected DoesNotExist exception");
        } catch(DoesNotExist e) {
        }
    }

    public void test_move() throws Exception {
        URI target = m_rootUri2.resolve(DEFAULT_FILENAME);
        m_file.move(m_rootUri2, Flags.NONE);
        checkCopied(target, DEFAULT_CONTENT);
        try {
            NamespaceFactory.createNamespaceEntry(m_session, m_fileUri, Flags.NONE);
            fail("Expected DoesNotExist exception");
        } catch(DoesNotExist e) {
        }
    }

    public void test_rename() throws Exception {
        URI target = m_rootUri.resolve(DEFAULT_FILENAME);
        m_file.move(m_rootUri, Flags.NONE);
        checkCopied(target, DEFAULT_CONTENT);
        try {
            NamespaceFactory.createNamespaceEntry(m_session, m_fileUri, Flags.NONE);
            fail("Expected DoesNotExist exception");
        } catch(DoesNotExist e) {
        }
    }
}
