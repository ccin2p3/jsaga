package org.ogf.saga.namespace;

import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.namespace.abstracts.AbstractNSCopyTest;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NSMoveTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 mars 2008
* ***************************************************
* Description:                                      */
/**
 * @deprecated
 */
@Deprecated
public abstract class NSMoveTest extends AbstractNSCopyTest {
    protected NSMoveTest(String protocol, String targetProtocol) throws Exception {
        super(protocol, targetProtocol);
    }

    public void test_move() throws Exception {
        URL target = createURL(m_dirUrl2, DEFAULT_FILENAME);
        m_file.move(m_dirUrl2, Flags.NONE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
        try {
            NSFactory.createNSEntry(m_session, m_fileUrl, Flags.NONE.getValue());
            fail("Expected exception: "+ DoesNotExistException.class);
        } catch(DoesNotExistException e) {
        }
    }

    public void test_rename() throws Exception {
        URL target = createURL(m_dirUrl, DEFAULT_FILENAME);
        m_file.move(m_dirUrl, Flags.NONE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
        try {
            NSFactory.createNSEntry(m_session, m_fileUrl, Flags.NONE.getValue());
            fail("Expected exception: "+ DoesNotExistException.class);
        } catch(DoesNotExistException e) {
        }
    }

    public void test_move_recurse() throws Exception {
        URL target = createURL(m_subDirUrl2, DEFAULT_SUBDIRNAME +DEFAULT_FILENAME);
        m_subDir.move(m_subDirUrl2, Flags.RECURSIVE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
        try {
            NSFactory.createNSDirectory(m_session, m_subDirUrl, Flags.NONE.getValue());
            fail("Expected exception: "+ DoesNotExistException.class);
        } catch(DoesNotExistException e) {
        }
    }

    public void test_move_recurse_overwrite() throws Exception {
        URL target = createURL(m_subDirUrl2, DEFAULT_SUBDIRNAME +DEFAULT_FILENAME);
        m_subDir.move(m_subDirUrl2, Flags.RECURSIVE.getValue()+Flags.OVERWRITE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
        try {
            NSFactory.createNSDirectory(m_session, m_subDirUrl, Flags.NONE.getValue());
            fail("Expected exception: "+ DoesNotExistException.class);
        } catch(DoesNotExistException e) {
        }
    }

    public void test_rename_directory() throws Exception {
    	URL newSubDirUrl = createURL(m_dirUrl,"newsubdir/");
        URL target = createURL(newSubDirUrl, DEFAULT_FILENAME);
        m_subDir.move(newSubDirUrl, Flags.RECURSIVE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
        try {
            NSFactory.createNSDirectory(m_session, m_subDirUrl, Flags.NONE.getValue());
            fail("Expected exception: "+ DoesNotExistException.class);
        } catch(DoesNotExistException e) {
        }
    }
}
