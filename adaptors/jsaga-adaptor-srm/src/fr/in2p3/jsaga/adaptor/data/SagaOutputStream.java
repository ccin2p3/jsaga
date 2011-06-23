package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.Adaptor;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.file.File;

import java.io.IOException;
import java.io.OutputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SagaOutputStream
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SagaOutputStream extends OutputStream {
    private File m_file;

    // for releasing SRM file
    private String m_token;
    private String m_srmPath;
    private StreamCallback m_callback;

    public SagaOutputStream(File file, String token, String srmPath, StreamCallback callback) {
        m_file = file;

        // for releasing SRM file
        m_token = token;
        m_srmPath = srmPath;
        m_callback = callback;
    }

    public void write(int i) throws IOException {
        try {
            Buffer buffer = BufferFactory.createBuffer(Adaptor.JSAGA_FACTORY, 1);
            if (m_file.write(buffer, 1) == 1) {
                return;
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        throw new IOException();
    }

    public void write(byte[] bytes, int off, int len) throws IOException {
        try {
            Buffer buffer = BufferFactory.createBuffer(Adaptor.JSAGA_FACTORY, bytes);
            if (m_file.write(buffer, len) > 0) {
                return;
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        throw new IOException();
    }

    public void close() throws IOException {
        try {
            // close stream
            m_file.close();

            // release SRM file
            if (m_callback != null) {
                m_callback.freeOutputStream(m_token, m_srmPath);
                m_callback = null;
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
