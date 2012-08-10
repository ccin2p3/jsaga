package fr.in2p3.jsaga.adaptor.data.http;

import fr.in2p3.jsaga.adaptor.data.HtmlFileAttributes;
import fr.in2p3.jsaga.adaptor.data.HttpDataAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;

import org.apache.commons.codec.EncoderException;
import org.ogf.saga.error.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   HttpDataAdaptorDefault
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class HttpDataAdaptorDefault extends HttpDataAdaptorAbstract implements FileReaderStreamFactory {
    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
            this.getConnection(absolutePath, additionalArgs);
            return true;
        } catch (DoesNotExistException e) {
            return false;
        }
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        URLConnection cnx = this.getConnection(absolutePath, additionalArgs);
        return new HttpFileAttributesDefault(cnx);
    }

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        URLConnection cnx = this.getConnection(absolutePath, additionalArgs);
        try {
            return cnx.getInputStream();
        } catch (FileNotFoundException e) {
            throw new DoesNotExistException(e);
        } catch (IOException e) {
            throw new NoSuccessException(e);
        }
    }

    protected HttpURLConnection getConnection(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, NoSuccessException {
        if (m_baseUrl == null) {
            throw new NoSuccessException("Connection is closed");
        }

        // get URL
        URL url;
        try {
            String fullPath = fr.in2p3.jsaga.impl.url.URLEncoder.encodePathOnly(absolutePath) + (additionalArgs!=null ? "?"+additionalArgs : "");
            url = new URL(m_baseUrl.getProtocol(), m_baseUrl.getHost(), m_baseUrl.getPort(), fullPath);
        } catch (MalformedURLException e) {
            throw new NoSuccessException(e);
		}

        // get connection
        HttpURLConnection cnx;
        try {
            cnx = (HttpURLConnection) url.openConnection();
        } catch(NoRouteToHostException e) {
            throw new DoesNotExistException("No route to host: "+url.getHost(), e);
        } catch(ConnectException e) {
            throw new NoSuccessException("Failed to connect to server: "+url.getHost()+":"+url.getPort(), e);
        } catch(IOException e) {
            throw new NoSuccessException(e);
        }

        // check status
        String status = cnx.getHeaderField(null);
        if (status == null) {
            cnx.disconnect();
            throw new NoSuccessException("Failed to connect to url: "+url);
        } else if (status.endsWith("200 OK")) {
            return cnx;
        } else if (status.endsWith("404 Not Found")) {
            throw new DoesNotExistException(status);
        } else if (status.endsWith("403 Forbidden")) {
            throw new PermissionDeniedException(status);
        } else {
            throw new NoSuccessException(status);
        }
    }
    

}
