package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
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
    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDenied, Timeout, NoSuccess {
        try {
            this.getConnection(absolutePath, additionalArgs);
            return true;
        } catch (DoesNotExist e) {
            return false;
        }
    }

    public boolean isDirectory(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        URLConnection cnx = this.getConnection(absolutePath, additionalArgs);
        return (cnx.getLastModified() == 0);
    }

    public long getSize(String absolutePath, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        URLConnection cnx = this.getConnection(absolutePath, additionalArgs);
        return cnx.getContentLength();
    }

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        URLConnection cnx = this.getConnection(absolutePath, additionalArgs);
        try {
            return cnx.getInputStream();
        } catch (FileNotFoundException e) {
            throw new DoesNotExist(e);
        } catch (IOException e) {
            throw new NoSuccess(e);
        }
    }

    protected HttpURLConnection getConnection(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, NoSuccess {
        if (m_baseUrl == null) {
            throw new NoSuccess("Connection is closed");
        }

        // get URL
        URL url;
        try {
            String fullPath = absolutePath + (additionalArgs!=null ? "?"+additionalArgs : "");
            url = new URL(m_baseUrl.getProtocol(), m_baseUrl.getHost(), m_baseUrl.getPort(), fullPath);
        } catch (MalformedURLException e) {
            throw new NoSuccess(e);
        }

        // get connection
        HttpURLConnection cnx;
        try {
            cnx = (HttpURLConnection) url.openConnection();
        } catch(NoRouteToHostException e) {
            throw new DoesNotExist("No route to host: "+url.getHost(), e);
        } catch(ConnectException e) {
            throw new NoSuccess("Failed to connect to server: "+url.getHost()+":"+url.getPort(), e);
        } catch(IOException e) {
            throw new NoSuccess(e);
        }

        // check status
        String status = cnx.getHeaderField(null);
        if (status == null) {
            cnx.disconnect();
            throw new NoSuccess("Failed to connect to url: "+url);
        } else if (status.endsWith("200 OK")) {
            return cnx;
        } else if (status.endsWith("404 Not Found")) {
            throw new DoesNotExist(status);
        } else if (status.endsWith("403 Forbidden")) {
            throw new PermissionDenied(status);
        } else {
            throw new NoSuccess(status);
        }
    }
}
