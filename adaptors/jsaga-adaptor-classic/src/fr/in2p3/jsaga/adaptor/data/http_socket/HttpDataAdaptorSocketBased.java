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
    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDenied, Timeout, NoSuccess {
        try {
            this.getRequest(absolutePath, additionalArgs, HttpRequest.TYPE_HEAD);
            return true;
        } catch (DoesNotExist e) {
            return false;
        }
    }

    public boolean isDirectory(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        HttpRequest request = this.getRequest(absolutePath, additionalArgs, HttpRequest.TYPE_HEAD);
        return (request.getLastModified() == null);
    }

    public long getSize(String absolutePath, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        HttpRequest request = this.getRequest(absolutePath, additionalArgs, HttpRequest.TYPE_HEAD);
        return request.getContentLength();
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        HttpRequest request = this.getRequest(absolutePath, additionalArgs, HttpRequest.TYPE_HEAD);
        return new HttpFileAttributesSockedBased(absolutePath, request);
    }

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        HttpRequest request = this.getRequest(absolutePath, additionalArgs, HttpRequest.TYPE_GET);
        return new HttpInputStreamSocketBased(request);
    }

    protected HttpRequest getRequest(String absolutePath, String additionalArgs, String requestType) throws PermissionDenied, DoesNotExist, NoSuccess {
        if (m_baseUrl == null) {
            throw new NoSuccess("Connection is closed");
        }

        // get path
        String path = absolutePath + (additionalArgs!=null ? "?"+additionalArgs : "");

        // get request
        HttpRequest request;
        try {
            Socket socket = new Socket(m_baseUrl.getHost(), m_baseUrl.getPort());
            request = new HttpRequest(requestType, path, socket);
        } catch (UnknownHostException e) {
            throw new DoesNotExist("Unknown host: "+m_baseUrl.getHost(), e);
        } catch (IOException e) {
            throw new NoSuccess(e);
        }

        // check status
        String status = request.getStatus();
        if (status.endsWith("200 OK")) {
            return request;
        } else if (status.endsWith("404 Not Found")) {
            throw new DoesNotExist(status);
        } else if (status.endsWith("403 Forbidden")) {
            throw new PermissionDenied(status);
        } else {
            throw new NoSuccess(status);
        }
    }
}
