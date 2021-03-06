package org.ogf.saga.file;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;

import org.junit.Ignore;
import org.junit.Test;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.namespace.base.ReadBaseTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileReadTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Author: lionel.schwarz@in2p3.fr
* Date:   5 NOV 2013
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class ReadTest extends ReadBaseTest {
    protected ReadTest(String protocol) throws Exception {
        super(protocol);
    }

    @Test
    public void test_getSize() throws Exception {
        if (m_file instanceof File) {
            assertEquals(
                    DEFAULT_CONTENT.length(),
                    ((File)m_file).getSize());
        } else {
        	fail("Not an instance of class: File");
        }
    }

    @Test
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

    @Test
    public void test_read() throws Exception {
        if (m_file instanceof File) {
            checkWrited(m_fileUrl, DEFAULT_CONTENT);
        } else {
        	fail("Not an instance of class: File");
        }
    }

    @Test
    public void test_inputStream() throws Exception {
    	FileInputStream fis = FileFactory.createFileInputStream(m_session, m_fileUrl);
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	int read;
    	while ((read=fis.read()) != -1) {
    		baos.write(read);
    	}
    	fis.close();
    	assertEquals(
    			DEFAULT_CONTENT,
    			baos.toString());
    	baos.close();
    }
    
    @Test
    @Ignore
    public void test_seek() throws Exception {
        // not implemented
    }
}
