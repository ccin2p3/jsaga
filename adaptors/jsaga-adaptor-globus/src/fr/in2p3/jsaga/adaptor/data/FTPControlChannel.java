package fr.in2p3.jsaga.adaptor.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.globus.ftp.exception.FTPReplyParseException;
import org.globus.ftp.exception.ServerException;
import org.globus.ftp.exception.UnexpectedReplyCodeException;
import org.globus.ftp.vanilla.Reply;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   FTPControlChannel
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   15 dec 2011
 * ***************************************************
 * Description:                                      */

//TODO: remove this class when Globus API will provide the getLastReply() method
public class FTPControlChannel extends org.globus.ftp.extended.GridFTPControlChannel {

	private String welcomeMsg;
	
	public FTPControlChannel(String host, int port) {
		super(host, port);
	}

	public FTPControlChannel(InputStream in, OutputStream out) {
		super(in, out);
	}
	
	public String getWelcome() {
		return this.welcomeMsg;
	}
	
	@Override
    protected void readInitialReplies() throws IOException, ServerException {
        Reply reply = null;
        
        try {
            reply = read();
            // Save the welcome message
            this.welcomeMsg = reply.getMessage();
        } catch (FTPReplyParseException rpe) {
            throw ServerException.embedFTPReplyParseException(
                                rpe,
                                "Received faulty initial reply");
        }

        if (Reply.isPositivePreliminary(reply)) {
            try {
                reply = read();
            } catch (FTPReplyParseException rpe) {
                throw ServerException.embedFTPReplyParseException(
                                        rpe,
                                        "Received faulty second reply");
            }
        }

        if (!Reply.isPositiveCompletion(reply)) {
            close();
            throw ServerException.embedUnexpectedReplyCodeException(
                                new UnexpectedReplyCodeException(reply),
                                "Server refused connection.");
        }
    }

	
}
