package fr.in2p3.jsaga.adaptor.arex.data;

import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.PermissionDeniedException;

import fr.in2p3.jsaga.adaptor.data.http_socket.HttpRequest;
import fr.in2p3.jsaga.adaptor.data.https.HttpsDataAdaptorSocketBased;
import fr.in2p3.jsaga.adaptor.security.JKSSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.JKSSecurityCredential;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   HttpsDataAdaptorSocketBased
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ArexHttpsDataAdaptor extends HttpsDataAdaptorSocketBased  {

    /*public ArexHttpsDataAdaptor() {
        // set default security (no certificate)
        m_userID = "No certificate";
        m_keyManager = null;
    }*/

    public String getType() {
        return "arex";
    }

    public Class[] getSupportedSecurityCredentialClasses() {
        return new Class[]{JKSSecurityCredential.class};
    }

    /*public void setSecurityCredential(SecurityCredential credential) {
        X509SecurityCredential adaptor = (X509SecurityCredential) credential;
        if (adaptor != null) {  // also support no security context
            m_userID = adaptor.getUserID();
            m_keyManager = adaptor.getKeyManager();
        }
    }*/

    public int getDefaultPort() {
        return 2010;
    }

    /*absolutePath = "/arex-x509/218251294736394460886100/worker-c90cbbc3-18dc-4a04-8ba8-c22722a40d11.output";
    additionalArgs = null;
    requestType = HttpRequest.TYPE_GET;*/
    
    protected HttpRequest getRequest(String absolutePath, String additionalArgs, String requestType) throws PermissionDeniedException, DoesNotExistException, NoSuccessException {
        
    	if (requestType.equals(HttpRequest.TYPE_HEAD)) {
    		requestType = HttpRequest.TYPE_GET;
    	}
    	return super.getRequest(absolutePath, additionalArgs, requestType);
    	/*
    	if (m_baseUrl == null) {
            throw new NoSuccessException("Connection is closed");
        }

        // get path
        String path = absolutePath + (additionalArgs!=null ? "?"+additionalArgs : "");

        // get request
        HttpRequest request;
        try {
            SSLSocket socket = (SSLSocket) m_socketFactory.createSocket(m_baseUrl.getHost(), m_baseUrl.getPort());
            socket.startHandshake();
            request = new HttpRequest(requestType, path, socket);
        } catch (UnknownHostException e) {
            throw new DoesNotExistException("Unknown host: "+m_baseUrl.getHost(), e);
        } catch (SSLHandshakeException e) {
            throw new PermissionDeniedException("User not allowed: "+m_userID, e);
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
        
        */
    }
}
