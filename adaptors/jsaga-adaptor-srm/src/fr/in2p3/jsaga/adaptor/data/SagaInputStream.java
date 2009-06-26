package fr.in2p3.jsaga.adaptor.data;

import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.file.File;

import java.io.IOException;
import java.io.InputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SagaInputStream
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SagaInputStream extends InputStream {
    private File m_file;

    // for releasing SRM file
    private String m_token;
    private String m_srmPath;
    private StreamCallback m_callback;

    public SagaInputStream(File file, String token, String srmPath, StreamCallback callback) {
        m_file = file;

        // for releasing SRM file
        m_token = token;
        m_srmPath = srmPath;
        m_callback = callback;
    }

    public int read() throws IOException {
        try {
            Buffer buffer = BufferFactory.createBuffer(1);
            if (m_file.read(buffer, 1) == 1) {
                byte b = buffer.getData()[0];
                return (int) b & 0x000000FF;
            } else {
                return -1;
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public int read(byte[] bytes, int off, int len) throws IOException {
        try {
            Buffer buffer = BufferFactory.createBuffer(bytes);
            return m_file.read(buffer, len);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public void close() throws IOException {
        try {
            // close stream
            m_file.close();

            // release SRM file
            if (m_callback != null) {
                m_callback.freeInputStream(m_token, m_srmPath);
                m_callback = null;
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
