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

    public SagaInputStream(File file) {
        m_file = file;
    }

    public int read() throws IOException {
        try {
            Buffer buffer = BufferFactory.createBuffer(1);
            if (m_file.read(buffer, 1) == 1) {
                return buffer.getData()[0];
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        throw new IOException();
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
            m_file.close();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
