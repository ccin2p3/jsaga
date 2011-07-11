package fr.in2p3.jsaga.adaptor.arex.data;

import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;

import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.http_socket.HttpOutputStreamSocketbased;
import fr.in2p3.jsaga.adaptor.data.http_socket.HttpRequest;
import fr.in2p3.jsaga.adaptor.data.https.HttpsDataAdaptorSocketBased;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStreamFactory;
import fr.in2p3.jsaga.adaptor.security.impl.JKSSecurityCredential;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ArexHttpsDataAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   11 Jan 2011
* ***************************************************
* Description:                                      */

public class ArexHttpsDataAdaptor extends HttpsDataAdaptorSocketBased implements FileWriterStreamFactory {

    public String getType() {
        return "arex";
    }

    public Class[] getSupportedSecurityCredentialClasses() {
        return new Class[]{JKSSecurityCredential.class};
    }

    public int getDefaultPort() {
        return 2010;
    }

    /**
     * Build and send a HTTP GET request
     * 
     * HTTP HEAD is not supported by A-REX, so all HEAD requests are replaced by GET
     */
    protected HttpRequest getRequest(String absolutePath, String additionalArgs, String requestType) throws PermissionDeniedException, DoesNotExistException, NoSuccessException {
        
    	if (requestType.equals(HttpRequest.TYPE_HEAD)) {
    		requestType = HttpRequest.TYPE_GET;
    	}
    	try {
    		return super.getRequest(absolutePath, additionalArgs, requestType);
    	} catch (NoSuccessException e) {
    		// A-REX sends 500 Internal error if file does not exists
    		if (e.getMessage().endsWith("500 Internal error")) {
    			throw new DoesNotExistException(e);
    		}
    		throw e;
    	}
    }

    //////////////////////////////////////////////////
    // Implementation of FileWriterStreamFactory
    //////////////////////////////////////////////////
	public OutputStream getOutputStream(String parentAbsolutePath,
			String fileName, boolean exclusive, boolean append,
			String additionalArgs) throws PermissionDeniedException,
			BadParameterException, AlreadyExistsException, ParentDoesNotExist,
			TimeoutException, NoSuccessException {
		if (append) throw new BadParameterException("Append is not supported");

        // buildrequest
        HttpRequest request;
        try {
            SSLSocket socket = (SSLSocket) m_socketFactory.createSocket(m_baseUrl.getHost(), m_baseUrl.getPort());
            socket.startHandshake();
            request = new HttpRequest(HttpRequest.TYPE_PUT, parentAbsolutePath + fileName, socket, false);
            request.setVersion("1.1");
        } catch (UnknownHostException e) {
            throw new ParentDoesNotExist("Unknown host: "+m_baseUrl.getHost(), e);
        } catch (SSLHandshakeException e) {
            throw new PermissionDeniedException("User not allowed: "+m_userID, e);
        } catch (IOException e) {
            throw new NoSuccessException(e);
        }

        //HttpPutRequest request = this.getPutRequest(parentAbsolutePath, fileName, additionalArgs);
        return new HttpOutputStreamSocketbased(request);
	}

	// Unsupported operations
	public void makeDir(String parentAbsolutePath, String directoryName,
			String additionalArgs) throws PermissionDeniedException,
			BadParameterException, AlreadyExistsException, ParentDoesNotExist,
			TimeoutException, NoSuccessException {
		throw new BadParameterException("MKDIR is not supported");
		
	}

	public void removeDir(String parentAbsolutePath, String directoryName,
			String additionalArgs) throws PermissionDeniedException,
			BadParameterException, DoesNotExistException, TimeoutException,
			NoSuccessException {
		throw new BadParameterException("RMDIR is not supported");
		
	}

	public void removeFile(String parentAbsolutePath, String fileName,
			String additionalArgs) throws PermissionDeniedException,
			BadParameterException, DoesNotExistException, TimeoutException,
			NoSuccessException {
		throw new BadParameterException("RM is not supported");
		
	}

}
