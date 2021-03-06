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
public abstract class DataReadOnlyMovementTest extends AbstractDataMovement {
    protected DataReadOnlyMovementTest(String protocol, String targetProtocol) throws Exception {
        super(protocol, targetProtocol);
    }

    @Test
    public void test_copy() throws Exception {
        URL target = createURL(m_dirUrl2, DEFAULT_FILENAME);
        m_file.copy(m_dirUrl2, Flags.NONE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
    }

    @Test
    public void test_copy_and_rename() throws Exception {
        URL target = createURL(m_subDirUrl2, "copy.txt");
        m_file.copy(target, Flags.NONE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
    }

    @Test(expected = AlreadyExistsException.class)
    public void test_copy_nooverwrite() throws Exception {
        URL target = createURL(m_subDirUrl2, DEFAULT_FILENAME_2);
        m_file.copy(target, Flags.NONE.getValue());
    }

    @Test
    public void test_copy_overwrite() throws Exception {
        URL target = createURL(m_subDirUrl2, DEFAULT_FILENAME_2);
        m_file.copy(target, Flags.OVERWRITE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
    }

    @Test
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

    @Test
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

    @Test
    public void test_copy_recurse() throws Exception {
        URL target = createURL(m_dirUrl2, DEFAULT_DIRNAME+DEFAULT_SUBDIRNAME+DEFAULT_FILENAME);
        m_dir.copy(m_dirUrl2, Flags.RECURSIVE.getValue());
        checkCopied(target, DEFAULT_CONTENT);
    }

    @Test
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

    @Test
    public void test_copy_recurse_overwrite() throws Exception {
        URL target = createURL(m_dirUrl2, DEFAULT_SUBDIRNAME +DEFAULT_FILENAME);
        m_subDir.copy(m_dirUrl2, Flags.RECURSIVE.or(Flags.OVERWRITE));
        checkCopied(target, DEFAULT_CONTENT);
    }

}
