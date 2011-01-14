package fr.in2p3.jsaga.adaptor.data.http_socket;

import java.io.IOException;
import java.io.OutputStream;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   HttpOutputStreamSocketbased
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   14 Jan 2011
* ***************************************************
* Description:                                      */

public class HttpOutputStreamSocketbased extends OutputStream {

	private HttpRequest _request;
	
	public HttpOutputStreamSocketbased(HttpRequest request) {
		_request = request;
	}

	public void write(int b) throws IOException {
		_request.write(b);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		_request.write(b,off,len);
	}
	
	public void close() throws IOException {
		if (_request != null) {
			_request.send();
			String status = _request.getStatus();
			_request = null;
	        if (! status.endsWith("200 OK")) {
	            throw new IOException(status);
	        }
		}
        super.close();
	}
}
