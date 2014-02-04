package org.ogf.saga.namespace;

import org.ogf.saga.error.*;
import org.ogf.saga.namespace.abstracts.AbstractNSCopyTest;
import org.ogf.saga.url.URL;

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
 * @deprecated
 */
@Deprecated
public abstract class NSCopyRecursiveTest extends AbstractNSCopyTest {
    protected NSCopyRecursiveTest(String protocol, String targetProtocol) throws Exception {
        super(protocol, targetProtocol);
    }

    public void test_copy_norecurse() throws Exception {
        URL target = createURL(m_subDirUrl2, DEFAULT_SUBDIRNAME);
        try {
            m_subDir.copy(m_subDirUrl2, Flags.NONE.getValue());
            fail("Expected exception: "+ BadParameterException.class);
        } catch(BadParameterException e) {
        }
        try {
            NSFactory.createNSDirectory(m_session, target, Flags.NONE.getValue());
            fail("Expected exception: "+ DoesNotExistException.class);
        } catch(DoesNotExistException e) {
        }
    }

    public void test_copy_recurse() throws Exception {
        URL target = createURL(m_dirUrl2, DEFAULT_DIRNAME+DEFAULT_SUBDIRNAME+DEFAULT_FILENAME);
        m_dir.copy(m_dirUrl2, Flags.RECURSIVE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
    }

    public void test_copy_recurse_nooverwrite() throws Exception {
        URL target = createURL(m_dirUrl2, DEFAULT_SUBDIRNAME +DEFAULT_FILENAME);
        try {
            m_subDir.copy(m_dirUrl2, Flags.RECURSIVE.getValue());
            fail("Expected exception: "+ AlreadyExistsException.class);
        } catch(AlreadyExistsException e) {
        }
        try {
            NSFactory.createNSEntry(m_session, target, Flags.NONE.getValue());
            fail("Expected exception: "+ DoesNotExistException.class);
        } catch(DoesNotExistException e) {
        }
    }

    public void test_copy_recurse_overwrite() throws Exception {
        URL target = createURL(m_dirUrl2, DEFAULT_SUBDIRNAME +DEFAULT_FILENAME);
        m_subDir.copy(m_dirUrl2, Flags.RECURSIVE.or(Flags.OVERWRITE));
        checkCopied(target, DEFAULT_CONTENT);
    }
}
