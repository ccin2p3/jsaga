package org.ogf.saga.file;

import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.namespace.abstracts.AbstractNSEntryTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileWriteTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FileWriteTest extends AbstractNSEntryTest {
    // test data
    private static final String DEFAULT_CONTENT2 = "Another text !";

    public FileWriteTest(String protocol) throws Exception {
        super(protocol);
    }

    public void test_write_nooverwrite() throws Exception {
        if (m_file instanceof File) {
            try {
                NSFactory.createNSEntry(m_session, m_fileUrl, Flags.WRITE.or(Flags.EXCL));
                fail("Expected AlreadyExist exception");
            } catch(AlreadyExistsException e) {
                checkWrited(m_fileUrl, DEFAULT_CONTENT);
            }
        } else {
            fail("Not an instance of class: File");
        }
    }

    public void test_write_overwrite() throws Exception {
        if (m_file instanceof File) {
            Buffer buffer = BufferFactory.createBuffer(DEFAULT_CONTENT2.getBytes());
            File writer = (File) NSFactory.createNSEntry(m_session, m_fileUrl, Flags.WRITE.getValue());
            writer.write(buffer);
            writer.close(0);
            checkWrited(m_fileUrl, DEFAULT_CONTENT2);
        } else {
            fail("Not an instance of class: File");
        }
    }

    public void test_write_append() throws Exception {
        if (m_file instanceof File) {
            Buffer buffer = BufferFactory.createBuffer(DEFAULT_CONTENT2.getBytes());
            File writer = (File) NSFactory.createNSEntry(m_session, m_fileUrl, Flags.WRITE.or(Flags.APPEND));
            writer.write(buffer);
            writer.close(0);
            checkWrited(m_fileUrl, DEFAULT_CONTENT+DEFAULT_CONTENT2);
        } else {
            fail("Not an instance of class: File");
        }
    }
}
