package fr.in2p3.jsaga.adaptor.data;

import org.globus.ftp.Session;
import org.globus.ftp.exception.FTPException;
import org.globus.io.streams.FTPOutputStream;
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
public class GsiftpOutputStream extends FTPOutputStream {

    public GsiftpOutputStream(GsiftpClient ftpClient, String file, boolean append) 
            throws IOException, FTPException {
	    
	    ftp = ftpClient;
	    
	    put(true, Session.TYPE_IMAGE, file, append);
	}

    /** override super.close() catch the exception at close() */
    public void close() throws IOException {
    	try {
			super.close();
		} catch (IOException e) {
			// when socket is already closed, ignore it
			// use getMessage() because buggy getCause() returns null
			if (e.getMessage()==null || !e.getMessage().toLowerCase().contains("socket closed")) {
				throw e;
			}
		}
    }
}
