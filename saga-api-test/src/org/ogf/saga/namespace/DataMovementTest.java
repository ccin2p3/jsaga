package org.ogf.saga.namespace;

import org.junit.Test;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.abstracts.AbstractDataMovement;
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
 *
 */
public abstract class DataMovementTest extends DataReadOnlyMovementTest {
    protected DataMovementTest(String protocol, String targetProtocol) throws Exception {
        super(protocol, targetProtocol);
    }

    @Test(expected = DoesNotExistException.class)
    public void test_move() throws Exception {
        URL target = createURL(m_dirUrl2, DEFAULT_FILENAME);
        m_file.move(m_dirUrl2, Flags.NONE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
        NSFactory.createNSEntry(m_session, m_fileUrl, Flags.NONE.getValue());
    }

    @Test(expected = DoesNotExistException.class)
    public void test_rename() throws Exception {
        URL target = createURL(m_dirUrl, DEFAULT_FILENAME);
        m_file.move(m_dirUrl, Flags.NONE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
        NSFactory.createNSEntry(m_session, m_fileUrl, Flags.NONE.getValue());
    }

    @Test(expected = DoesNotExistException.class)
    public void test_move_recurse() throws Exception {
        URL target = createURL(m_subDirUrl2, DEFAULT_SUBDIRNAME +DEFAULT_FILENAME);
        m_subDir.move(m_subDirUrl2, Flags.RECURSIVE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
        NSFactory.createNSDirectory(m_session, m_subDirUrl, Flags.NONE.getValue());
    }

    @Test(expected = DoesNotExistException.class)
    public void test_move_recurse_overwrite() throws Exception {
        URL target = createURL(m_subDirUrl2, DEFAULT_SUBDIRNAME +DEFAULT_FILENAME);
        m_subDir.move(m_subDirUrl2, Flags.RECURSIVE.getValue()+Flags.OVERWRITE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
        NSFactory.createNSDirectory(m_session, m_subDirUrl, Flags.NONE.getValue());
    }

    @Test(expected = DoesNotExistException.class)
    public void test_rename_directory() throws Exception {
    	URL newSubDirUrl = createURL(m_dirUrl,"newsubdir/");
        URL target = createURL(newSubDirUrl, DEFAULT_FILENAME);
        m_subDir.move(newSubDirUrl, Flags.RECURSIVE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
        NSFactory.createNSDirectory(m_session, m_subDirUrl, Flags.NONE.getValue());
    }
}
