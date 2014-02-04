package org.ogf.saga.namespace;

import org.ogf.saga.error.*;
import org.ogf.saga.namespace.abstracts.AbstractNSCopyTest;
import org.ogf.saga.url.URL;

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
 * @deprecated
 */
@Deprecated
public abstract class NSCopyTest extends AbstractNSCopyTest {
    protected NSCopyTest(String protocol, String targetProtocol) throws Exception {
        super(protocol, targetProtocol);
    }

    public void test_copy() throws Exception {
        URL target = createURL(m_dirUrl2, DEFAULT_FILENAME);
        m_file.copy(m_dirUrl2, Flags.NONE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
    }

    public void test_copy_and_rename() throws Exception {
        URL target = createURL(m_subDirUrl2, "copy.txt");
        m_file.copy(target, Flags.NONE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
    }

    public void test_copy_nooverwrite() throws Exception {
        URL target = createURL(m_subDirUrl2, DEFAULT_FILENAME_2);
        try {
            m_file.copy(target, Flags.NONE.getValue());
            fail("Expected AlreadyExist exception");
        } catch(AlreadyExistsException e) {
            checkCopied(target, DEFAULT_CONTENT_2);
        }
    }

    public void test_copy_overwrite() throws Exception {
        URL target = createURL(m_subDirUrl2, DEFAULT_FILENAME_2);
        m_file.copy(target, Flags.OVERWRITE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
    }

    public void test_copy_lateExistenceCheck() throws Exception {
        NSEntry entry = null;
        try {
            entry = NSFactory.createNSEntry(m_session, createURL(m_subDirUrl, "unexisting.txt"), FLAGS_BYPASSEXIST);
        } catch(DoesNotExistException e) {
            fail("Unexpected exception: "+ DoesNotExistException.class);
        }
        try {
            entry.copy(m_subDirUrl2, Flags.NONE.getValue());
            fail("Expected exception: "+ IncorrectStateException.class);
        } catch(IncorrectStateException e) {
        } finally {
        	entry.close();
        }
        try {
            NSFactory.createNSEntry(m_session, createURL(m_subDirUrl2, "unexisting.txt"), Flags.NONE.getValue());
            fail("Expected exception: "+ DoesNotExistException.class);
        } catch(DoesNotExistException e) {
        }
    }
}
