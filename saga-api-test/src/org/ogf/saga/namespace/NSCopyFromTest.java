package org.ogf.saga.namespace;

import org.ogf.saga.URI;
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
        URI target = m_dirUri.resolve("unexisting.txt");
        NamespaceEntry newFile = null;
        try {
            System.setProperty("saga.existence.check.skip", "true");
            newFile = NamespaceFactory.createNamespaceEntry(m_session, target, Flags.NONE);
            System.setProperty("saga.existence.check.skip", "false");
        } catch(DoesNotExist e) {
            fail("Unexpected DoesNotExist exception");
        }
        copyFrom(newFile, m_fileUri2, Flags.NONE);
        checkCopied(target, DEFAULT_CONTENT_2);
    }

/*
    public void test_copy_nooverwrite() throws Exception {
        URI target = m_fileUri;
        try {
            copyFrom(m_file, m_fileUri2, Flags.NONE);
            fail("Expected AlreadyExist exception");
        } catch(AlreadyExists e) {
            checkCopied(target, DEFAULT_CONTENT);
        }
    }
*/

    public void test_copy_overwrite() throws Exception {
        URI target = m_fileUri;
        copyFrom(m_file, m_fileUri2, Flags.OVERWRITE);
        checkCopied(target, DEFAULT_CONTENT_2);
    }

    private static void copyFrom(NamespaceEntry entry, URI uri, Flags... flags) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method m = entry.getClass().getMethod("copyFrom", URI.class, Flags[].class);
        if (m != null) {
            m.invoke(entry, uri, flags);
        }
    }
}
