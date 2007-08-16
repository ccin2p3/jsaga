package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.read.FileReaderStream;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.Timeout;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   EmulatorFileReaderStream
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   5 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class EmulatorFileReaderStream implements FileReaderStream {
    private InputStream m_stream;

    public EmulatorFileReaderStream(String content) {
        m_stream = new ByteArrayInputStream(content.getBytes());
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
