package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.impl.HttpInputStreamSocketBased;
import fr.in2p3.jsaga.adaptor.data.impl.HttpRequest;
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
            HttpRequest request = this.getRequest(absolutePath, additionalArgs, HttpRequest.TYPE_HEAD);
            String status = request.getStatus();
            if (status.endsWith("200 OK")) {
                return true;
            } else if (status.endsWith("404 Not Found")) {
                return false;
            }
            throw new NoSuccess(status);
        } catch (DoesNotExist e) {
            return false;
        }
    }

    public boolean isDirectory(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        HttpRequest request = this.getRequest(absolutePath, additionalArgs, HttpRequest.TYPE_HEAD);
        String status = request.getStatus();
        if (status.endsWith("200 OK")) {
            return (request.getLastModified() == null);
        } else if (status.endsWith("404 Not Found")) {
            throw new DoesNotExist(status);
        }
        throw new NoSuccess(status);
    }

    public long getSize(String absolutePath, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        HttpRequest request = this.getRequest(absolutePath, additionalArgs, HttpRequest.TYPE_HEAD);
        return request.getContentLength() - 1;
    }

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        HttpRequest request = this.getRequest(absolutePath, additionalArgs, HttpRequest.TYPE_GET);
        return new HttpInputStreamSocketBased(request);
    }

    protected HttpRequest getRequest(String absolutePath, String additionalArgs, String requestType) throws PermissionDenied, DoesNotExist, NoSuccess {
        if (m_baseUrl == null) {
            throw new NoSuccess("Connection is closed");
        }
        String path = absolutePath + (additionalArgs!=null ? "?"+additionalArgs : "");
        try {
            Socket socket = new Socket(m_baseUrl.getHost(), m_baseUrl.getPort());
            return new HttpRequest(requestType, path, socket);
        } catch (UnknownHostException e) {
            throw new DoesNotExist("Unknown host: "+m_baseUrl.getHost(), e);
        } catch (IOException e) {
            throw new NoSuccess(e);
        }
    }
}
