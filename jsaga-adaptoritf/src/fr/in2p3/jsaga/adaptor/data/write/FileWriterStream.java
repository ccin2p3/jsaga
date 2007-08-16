package fr.in2p3.jsaga.adaptor.data.write;

import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.Timeout;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileWriterStream
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   15 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface FileWriterStream {
    /**
     * Writes up to <code>len</code> bytes from buffer into the file at the current file position.
     * @param buffer buffer to write data from.
     * @param offset beginning of the buffer segment to write.
     * @param len number of bytes to write.
     * @return number of bytes successfully written.
     */
    public int write(byte[] buffer, int offset, int len) throws Timeout;

    /**
     * Close the stream
     */
    public void close() throws IncorrectState;
}
