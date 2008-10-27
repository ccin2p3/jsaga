package fr.in2p3.jsaga.adaptor.data.http_socket;

import fr.in2p3.jsaga.adaptor.data.HttpDataAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import org.ogf.saga.error.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   HttpDataAdaptorSocketBased
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class HttpDataAdaptorSocketBased extends HttpDataAdaptorAbstract implements FileReaderStreamFactory {
    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
            this.getRequest(absolutePath, additionalArgs, HttpRequest.TYPE_HEAD);
            return true;
        } catch (DoesNotExistException e) {
            return false;
        }
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        HttpRequest request = this.getRequest(absolutePath, additionalArgs, HttpRequest.TYPE_HEAD);
        return new HttpFileAttributesSockedBased(absolutePath, request);
    }

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        HttpRequest request = this.getRequest(absolutePath, additionalArgs, HttpRequest.TYPE_GET);
        return new HttpInputStreamSocketBased(request);
    }

    protected HttpRequest getRequest(String absolutePath, String additionalArgs, String requestType) throws PermissionDeniedException, DoesNotExistException, NoSuccessException {
        if (m_baseUrl == null) {
            throw new NoSuccessException("Connection is closed");
        }

        // get path
        String path = absolutePath + (additionalArgs!=null ? "?"+additionalArgs : "");

        // get request
        HttpRequest request;
        try {
            Socket socket = new Socket(m_baseUrl.getHost(), m_baseUrl.getPort());
            request = new HttpRequest(requestType, path, socket);
        } catch (UnknownHostException e) {
            throw new DoesNotExistException("Unknown host: "+m_baseUrl.getHost(), e);
        } catch (IOException e) {
            throw new NoSuccessException(e);
        }

        // check status
        String status = request.getStatus();
        if (status.endsWith("200 OK")) {
            return request;
        } else if (status.endsWith("404 Not Found")) {
            throw new DoesNotExistException(status);
        } else if (status.endsWith("403 Forbidden")) {
            throw new PermissionDeniedException(status);
        } else {
            throw new NoSuccessException(status);
        }
    }
}
