package org.ogf.saga.file;

import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.namespace.abstracts.AbstractNSEntryTest;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

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
public abstract class FileWriteTest extends AbstractNSEntryTest {
    // test data
    private static final String DEFAULT_CONTENT2 = "Another text !";

    protected FileWriteTest(String protocol) throws Exception {
        super(protocol);
    }

    public void test_write_nocreate()throws Exception {
        if (m_file instanceof File) {
            try {
                NSFactory.createNSEntry(m_session, createURL(m_dirUrl, "ThisFileDoesNotExist"), Flags.WRITE.getValue());
                fail("Expected DoesNotExist exception");
            } catch(DoesNotExistException e) {
            }
        } else {
            fail("Not an instance of class: File");
        }
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
            writer.close();
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
            writer.close();
            checkWrited(m_fileUrl, DEFAULT_CONTENT+DEFAULT_CONTENT2);
        } else {
            fail("Not an instance of class: File");
        }
    }
    
    public void test_read_and_write() throws Exception {
        if (m_file instanceof File) {
            Buffer buffer;
            buffer = BufferFactory.createBuffer(DEFAULT_CONTENT.getBytes());
            File reader = (File) NSFactory.createNSEntry(m_session, m_fileUrl, Flags.READ.or(Flags.WRITE.or(Flags.CREATE.getValue())));
            // read
            reader.read(buffer);
            // check size
            //assertEquals(0, reader.getSize());
            // write
            buffer = BufferFactory.createBuffer(DEFAULT_CONTENT2.getBytes());
            reader.write(buffer);
            // check new size
            assertEquals(DEFAULT_CONTENT2.length(), reader.getSize());
            // check new content
            reader.close();
            checkWrited(m_fileUrl, DEFAULT_CONTENT2);
        } else {
            fail("Not an instance of class: File");
        }
    }
}
