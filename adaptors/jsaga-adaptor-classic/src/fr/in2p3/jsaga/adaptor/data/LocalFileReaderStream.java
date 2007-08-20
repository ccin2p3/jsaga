package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.read.FileReaderStream;

import java.io.*;

import org.ogf.saga.error.Timeout;
import org.ogf.saga.error.IncorrectState;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   LocalFileReaderStream
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   16 août 2007
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class LocalFileReaderStream implements FileReaderStream {
    private InputStream m_stream;

    public LocalFileReaderStream(File file) throws FileNotFoundException {
        m_stream = new FileInputStream(file);
    }

    public int read(byte[] buffer, int len) throws Timeout {
        try {
            return m_stream.read(buffer, 0, len);
        } catch (IOException e) {
            throw new Timeout(e);
        }
    }

    public void close() throws IncorrectState {
        try {
            m_stream.close();
        } catch (IOException e) {
            throw new IncorrectState(e);
        }
    }
}
