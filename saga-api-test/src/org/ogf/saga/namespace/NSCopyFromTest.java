package org.ogf.saga.namespace;

import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.namespace.abstracts.AbstractNSCopyTest;
import org.ogf.saga.url.URL;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NSCopyFromTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   30 ao�t 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class NSCopyFromTest extends AbstractNSCopyTest {
    protected NSCopyFromTest(String protocol, String targetProtocol) throws Exception {
        super(protocol, targetProtocol);
    }

    public void test_copy() throws Exception {
        URL target = createURL(m_subDirUrl, "unexisting.txt");
        NSEntry newFile = null;
        try {
            newFile = NSFactory.createNSEntry(m_session, target, Flags.CREATE.or(FLAGS_BYPASSEXIST));
        } catch(DoesNotExistException e) {
            fail("Unexpected exception: "+ DoesNotExistException.class);
        }
        copyFrom(newFile, m_fileUrl2, Flags.NONE.getValue());
        checkCopied(target, DEFAULT_CONTENT_2);
    }

/*
    public void test_copy_nooverwrite() throws Exception {
        URL target = m_fileUrl;
        try {
            copyFrom(m_file, m_fileUrl2, Flags.NONE);
            fail("Expected AlreadyExist exception");
        } catch(AlreadyExistsException e) {
            checkCopied(target, DEFAULT_CONTENT);
        }
    }
*/

    public void test_copy_overwrite() throws Exception {
        URL target = m_fileUrl;
        copyFrom(m_file, m_fileUrl2, Flags.OVERWRITE.getValue());
        checkCopied(target, DEFAULT_CONTENT_2);
    }

    private static void copyFrom(NSEntry entry, URL url, int flags) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method m = entry.getClass().getMethod("copyFrom", URL.class, int.class);
        if (m != null) {
            m.invoke(entry, url, flags);
        }
    }
}
