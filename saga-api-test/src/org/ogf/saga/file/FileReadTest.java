package org.ogf.saga.file;

import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.namespace.abstracts.AbstractNSEntryReadTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileReadTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class FileReadTest extends AbstractNSEntryReadTest {
    protected FileReadTest(String protocol) throws Exception {
        super(protocol);
    }

    public void test_getSize() throws Exception {
        if (m_file instanceof File) {
            assertEquals(
                    DEFAULT_CONTENT.length(),
                    ((File)m_file).getSize());
        } else {
            fail("Not an instance of class: File");
        }
    }

    public void test_read_applicationManagedBuffer() throws Exception {
        if (m_file instanceof File) {
            Buffer buffer = BufferFactory.createBuffer(new byte[1024]);
            File reader = (File) NSFactory.createNSEntry(m_session, m_fileUrl, Flags.READ.getValue());
            reader.read(buffer);
            byte[] bytes = new byte[DEFAULT_CONTENT.length()];
            System.arraycopy(buffer.getData(), 0, bytes, 0, DEFAULT_CONTENT.length());
            assertEquals(
                    DEFAULT_CONTENT,
                    new String(bytes));
            reader.close();
        } else {
            fail("Not an instance of class: File");
        }
    }

    public void test_read() throws Exception {
        if (m_file instanceof File) {
            checkWrited(m_fileUrl, DEFAULT_CONTENT);
        } else {
            fail("Not an instance of class: File");
        }
    }

    public void test_seek() throws Exception {
        // not implemented
    }
}
