package fr.in2p3.jsaga.adaptor.data;

import java.io.IOException;
import java.util.StringTokenizer;

import org.globus.ftp.GridFTPClient;
import org.globus.ftp.MlsxEntry;
import org.globus.ftp.Session;
import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.FTPException;
import org.globus.ftp.exception.FTPReplyParseException;
import org.globus.ftp.exception.ServerException;
import org.globus.ftp.exception.UnexpectedReplyCodeException;
import org.globus.ftp.vanilla.Command;
import org.globus.ftp.vanilla.FTPServerFacade;
import org.globus.ftp.vanilla.Reply;

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

	public GsiftpClient(String host, int port) throws IOException,
			ServerException {
		super(host, port);
		//TODO: remove following lines when Globus API will provide the getLastReply() method

		// Close standard GridFTPControlChannel to reopen the Channel with our object
		// in order to get the welcome message
		controlChannel.close();
        controlChannel = new FTPControlChannel(host, port);
        controlChannel.open();
	}

	// TODO: modify this method when Globus API will provide the getLastReply() method
	public String getWelcome() {
		return ((FTPControlChannel)controlChannel).getWelcome();
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
		super.close();
	}
}
