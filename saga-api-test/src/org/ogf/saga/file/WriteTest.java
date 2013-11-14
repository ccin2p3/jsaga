package org.ogf.saga.file;

import org.junit.Test;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.namespace.base.WriteBaseTest;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileWriteTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Author: lionel.schwarz@in2p3.fr
* Date:   5 NOV 2013
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class WriteTest extends WriteBaseTest {
    // test data
    private static final String DEFAULT_CONTENT2 = "Another text !";
    private static final String DEFAULT_ENCODED_FILENAME = "fileéàê.txt";

    protected WriteTest(String protocol) throws Exception {
        super(protocol);
    }

    @Test(expected = DoesNotExistException.class)
    public void test_write_nocreate()throws Exception {
        if (m_file instanceof File) {
            NSFactory.createNSEntry(m_session, createURL(m_dirUrl, "ThisFileDoesNotExist"), Flags.WRITE.getValue()).close();
        } else {
        	fail("Not an instance of class: File");
        }
    }

    @Test
    public void test_write_nooverwrite() throws Exception {
        if (m_file instanceof File) {
            try {
                NSFactory.createNSEntry(m_session, m_fileUrl, Flags.WRITE.or(Flags.EXCL)).close();
                fail("Expected AlreadyExist exception");
            } catch(AlreadyExistsException e) {
                checkWrited(m_fileUrl, DEFAULT_CONTENT);
            }
        } else {
        	fail("Not an instance of class: File");
        }
    }

    @Test
    public void test_write_encoded_filename() throws Exception {
        URL fileUrl = createURL(m_subDirUrl, DEFAULT_ENCODED_FILENAME);
        NSEntry file = m_dir.open(fileUrl, FLAGS_FILE);
        Buffer buffer = BufferFactory.createBuffer(DEFAULT_CONTENT2.getBytes());
        ((File)file).write(buffer);
        file.close();
        try {
            NSFactory.createNSEntry(m_session, createURL(m_subDirUrl, DEFAULT_ENCODED_FILENAME), Flags.WRITE.or(Flags.EXCL)).close();
            fail("Expected AlreadyExist exception");
        } catch(AlreadyExistsException e) {
        }
        checkWrited(fileUrl, DEFAULT_CONTENT2);
    }
    
    @Test
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

    @Test
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
    
    @Test
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

    @Test
    public void test_outputStream() throws Exception {
        if (m_file instanceof File) {
        	FileOutputStream fos = FileFactory.createFileOutputStream(m_session, m_fileUrl, false);
        	fos.write(DEFAULT_CONTENT2.getBytes());
        	fos.flush();
        	fos.close();
            checkWrited(m_fileUrl, DEFAULT_CONTENT2);
        } else {
        	fail("Not an instance of class: File");
        }
    }

}
