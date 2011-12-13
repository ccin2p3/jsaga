package fr.in2p3.jsaga.adaptor.data;

import org.globus.ftp.exception.FTPException;
import org.globus.io.streams.GridFTPOutputStream;
import org.ietf.jgss.GSSCredential;

import java.io.IOException;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   GsiftpOutputStream
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   13 dec 2011
 * ***************************************************
 * Description:                                      */
/**
 * This class is used to catch the SocketException in IOException sent by close() when the socket is already closed
 */
public class GsiftpOutputStream extends GridFTPOutputStream {
    public GsiftpOutputStream(GSSCredential cred, String host, int port,
			String file, boolean append) throws IOException, FTPException {
		super(cred, host, port, file, append);
	}

    /** override super.close() catch the exception at close() */
    public void close() throws IOException {
    	try {
			super.close();
		} catch (IOException e) {
			// when socket is already closed, ignore it
			// use getMessage() because buggy getCause() returns null
			if (!(e.getMessage().toLowerCase().contains("socket closed"))) {
				throw e;
			}
		}
    }
}
