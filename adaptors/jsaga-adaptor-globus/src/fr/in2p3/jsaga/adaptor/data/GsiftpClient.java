package fr.in2p3.jsaga.adaptor.data;

import java.io.EOFException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.exception.ServerException;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   GsiftpClient
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   13 dec 2011
 * ***************************************************
 * Description:                                      */
/**
 * This class uses the local FTPControlChannel class instead of Globus GridFTPControlChannel to get the welcome message
 */

public class GsiftpClient extends GridFTPClient {
	private static Logger logger = Logger.getLogger(GsiftpClient.class);
	public String welcomeMessage = null;
	public GsiftpClient(String host, int port) throws IOException,
			ServerException {
		super(host, port);

		welcomeMessage = controlChannel.getLastReply().getMessage();
	}

	public String getWelcome() {
		return this.welcomeMessage;
	}
	
	public boolean isAppendSupported() {
		// Some servers do not support 'append' flag at PUT
		// the 'APPE' will simply overwrite the file without any error
		// Known servers are those with welcome message containing "[VDT patched x.x.x]"
		if (this.getWelcome().contains("[VDT patched")) {
			return false;
		}
		return true;
	}
	
	@Override
	public void close() throws ServerException, IOException {
		// Do not close
	}
	
	public void disconnect() throws ServerException, IOException {
		try{
			super.close();
		}catch(EOFException e){
			//Already closed ?
			logger.warn("The GSIFTP connection seems already closed: " + e.getMessage());
		}
	}
}
