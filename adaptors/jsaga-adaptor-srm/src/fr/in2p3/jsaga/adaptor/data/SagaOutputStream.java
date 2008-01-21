package fr.in2p3.jsaga.adaptor.data;

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

    public SagaOutputStream(File file) {
        m_file = file;
    }

    public void write(int i) throws IOException {
        try {
            Buffer buffer = BufferFactory.createBuffer(1);
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
            Buffer buffer = BufferFactory.createBuffer(bytes);
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
            m_file.close();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
