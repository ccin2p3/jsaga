package org.ogf.saga.namespace;

import org.ogf.saga.URL;
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
        URL target = createURL(m_rootUrl2, DEFAULT_FILENAME);
        m_file.copy(m_rootUrl2, Flags.NONE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
    }

    public void test_copy_and_rename() throws Exception {
        URL target = createURL(m_dirUrl2, "copy.txt");
        m_file.copy(target, Flags.NONE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
    }

    public void test_copy_nooverwrite() throws Exception {
        URL target = createURL(m_dirUrl2, DEFAULT_FILENAME_2);
        try {
            m_file.copy(target, Flags.NONE.getValue());
            fail("Expected AlreadyExist exception");
        } catch(AlreadyExists e) {
            checkCopied(target, DEFAULT_CONTENT_2);
        }
    }

    public void test_copy_overwrite() throws Exception {
        URL target = createURL(m_dirUrl2, DEFAULT_FILENAME_2);
        m_file.copy(target, Flags.OVERWRITE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
    }

    public void test_copy_lateExistenceCheck() throws Exception {
        NSEntry entry = null;
        try {
            entry = NSFactory.createNSEntry(m_session, createURL(m_dirUrl, "unexisting.txt"), FLAGS_BYPASSEXIST);
        } catch(DoesNotExist e) {
            fail("Unexpected DoesNotExist exception");
        }
        try {
            entry.copy(m_dirUrl2, Flags.NONE.getValue());
            fail("Expected IncorrectState exception");
        } catch(IncorrectState e) {
        }
        try {
            NSFactory.createNSEntry(m_session, createURL(m_dirUrl2, "unexisting.txt"), Flags.NONE.getValue());
            fail("Expected DoesNotExist exception");
        } catch(DoesNotExist e) {
        }
    }

    public void test_move() throws Exception {
        URL target = createURL(m_rootUrl2, DEFAULT_FILENAME);
        m_file.move(m_rootUrl2, Flags.NONE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
        try {
            NSFactory.createNSEntry(m_session, m_fileUrl, Flags.NONE.getValue());
            fail("Expected DoesNotExist exception");
        } catch(DoesNotExist e) {
        }
    }

    public void test_rename() throws Exception {
        URL target = createURL(m_rootUrl, DEFAULT_FILENAME);
        m_file.move(m_rootUrl, Flags.NONE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
        try {
            NSFactory.createNSEntry(m_session, m_fileUrl, Flags.NONE.getValue());
            fail("Expected DoesNotExist exception");
        } catch(DoesNotExist e) {
        }
    }
}
