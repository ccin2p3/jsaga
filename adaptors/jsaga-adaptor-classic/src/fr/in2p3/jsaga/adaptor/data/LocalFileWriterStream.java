package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.write.FileWriterStream;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.Timeout;

import java.io.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   LocalFileWriterStream
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   16 août 2007
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class LocalFileWriterStream implements FileWriterStream {
    private OutputStream m_stream;

    public LocalFileWriterStream(File file, boolean append) throws FileNotFoundException {
        m_stream = new FileOutputStream(file, append);
    }

    public int write(byte[] buffer, int offset, int len) throws Timeout {
        try {
            m_stream.write(buffer, offset, len);
            return (len - offset);
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
