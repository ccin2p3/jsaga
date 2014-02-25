package fr.in2p3.jsaga.adaptor.data.http;

import fr.in2p3.jsaga.adaptor.data.HttpDataAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.helpers.URLEncoder;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;

import java.io.*;
import java.net.*;

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
            this.getConnection(absolutePath, additionalArgs).disconnect();
            return true;
        } catch (DoesNotExistException e) {
            return false;
        }
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        HttpURLConnection cnx = this.getConnection(absolutePath, additionalArgs);
        HttpFileAttributesDefault attrs =new HttpFileAttributesDefault(cnx);
        cnx.disconnect();
        return attrs;
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
            String fullPath = URLEncoder.encodePathOnly(absolutePath) + (additionalArgs!=null ? "?"+additionalArgs : "");
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
        if (this.m_userID != null) {
            cnx.setRequestProperty("Authorization", "Basic " + 
                        new Base64().encodeAsString(
                                new String(m_userID + ":" + this.m_userPass).getBytes()));
        }
        
        // check status
        String status = cnx.getHeaderField(null);
        if (status == null) {
            cnx.disconnect();
            throw new NoSuccessException("Failed to connect to url: "+url);
        } else if (status.endsWith("200 OK")) {
            return cnx;
        } else if (status.endsWith("404 Not Found")) {
            cnx.disconnect();
            throw new DoesNotExistException(status);
        } else if (status.endsWith("403 Forbidden")) {
            cnx.disconnect();
            throw new PermissionDeniedException(status);
        } else if (status.endsWith("401 Authorization Required")) {
            cnx.disconnect();
            throw new PermissionDeniedException(status);
        } else {
            cnx.disconnect();
            throw new NoSuccessException(status);
        }
    }
    

}
