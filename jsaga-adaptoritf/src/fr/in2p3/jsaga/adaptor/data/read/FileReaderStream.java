package fr.in2p3.jsaga.adaptor.data.read;

import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.Timeout;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileReaderStream
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   15 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface FileReaderStream {
    /**
     * Reads up to <code>len</code> bytes from the file into the buffer.
     * @param buffer buffer to read data.
     * @param len number of bytes to be read.
     * @return number of bytes successfully read.
     */
    public int read(byte[] buffer, int len) throws Timeout;

    /**
     * Close the stream
     */
    public void close() throws IncorrectState;
}
