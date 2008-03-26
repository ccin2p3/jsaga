package org.ogf.saga.namespace;

import org.ogf.saga.URL;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.namespace.abstracts.AbstractNSCopyTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NSCopyFromTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   30 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NSCopyFromTest extends AbstractNSCopyTest {
    public NSCopyFromTest(String protocol, String targetProtocol) throws Exception {
        super(protocol, targetProtocol);
    }

    public void test_copy() throws Exception {
        URL target = createURL(m_subDirUrl, "unexisting.txt");
        NSEntry newFile = null;
        try {
            newFile = NSFactory.createNSEntry(m_session, target, FLAGS_BYPASSEXIST);
        } catch(DoesNotExist e) {
            fail("Unexpected DoesNotExist exception");
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
        } catch(AlreadyExists e) {
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
