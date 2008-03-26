package org.ogf.saga.namespace;

import org.ogf.saga.URL;
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
        URL target = createURL(m_dirUrl2, DEFAULT_DIRNAME);
        try {
            m_dir.copy(m_dirUrl2, Flags.NONE.getValue());
            fail("Expected BadParameter exception");
        } catch(BadParameter e) {
        }
        try {
            NSFactory.createNSDirectory(m_session, target, Flags.NONE.getValue());
            fail("Expected DoesNotExist exception");
        } catch(DoesNotExist e) {
        }
    }

    public void test_copy_recurse() throws Exception {
        URL target = createURL(m_dirUrl2, DEFAULT_DIRNAME+DEFAULT_FILENAME);
        m_dir.copy(m_dirUrl2, Flags.RECURSIVE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
    }

    public void test_copy_recurse_nooverwrite() throws Exception {
        URL target = createURL(m_rootUrl2, DEFAULT_DIRNAME+DEFAULT_FILENAME);
        try {
            m_dir.copy(m_rootUrl2, Flags.RECURSIVE.getValue());
            fail("Expected AlreadyExists exception");
        } catch(AlreadyExists e) {
        }
        try {
            NSFactory.createNSEntry(m_session, target, Flags.NONE.getValue());
            fail("Expected DoesNotExist exception");
        } catch(DoesNotExist e) {
        }
    }

    public void test_copy_recurse_overwrite() throws Exception {
        URL target = createURL(m_rootUrl2, DEFAULT_DIRNAME+DEFAULT_FILENAME);
        m_dir.copy(m_rootUrl2, Flags.RECURSIVE.or(Flags.OVERWRITE));
        checkCopied(target, DEFAULT_CONTENT);
    }
}
