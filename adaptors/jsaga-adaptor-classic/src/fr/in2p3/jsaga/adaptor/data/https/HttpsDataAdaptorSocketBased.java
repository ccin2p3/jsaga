package fr.in2p3.jsaga.adaptor.data.https;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.http_socket.HttpDataAdaptorSocketBased;
import fr.in2p3.jsaga.adaptor.data.http_socket.HttpRequest;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.X509SecurityCredential;
import org.ogf.saga.error.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

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
public class HttpsDataAdaptorSocketBased extends HttpDataAdaptorSocketBased implements FileReaderStreamFactory {
    private static final String MUTUAL_AUTHENTICATION = "MutualAuthentication";
    // security
    private String m_userID;
    private KeyManager[] m_keyManager;
    // conection
    private SSLSocketFactory m_socketFactory;

    public HttpsDataAdaptorSocketBased() {
        // set default security (no certificate)
        m_userID = "No certificate";
        m_keyManager = null;
    }

    public String getType() {
        return "https";
    }

    public Usage getUsage() {
        return new UOptional(MUTUAL_AUTHENTICATION);
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return new Default[]{new Default(MUTUAL_AUTHENTICATION, "false")};
    }

    public Class[] getSupportedSecurityCredentialClasses() {
        return new Class[]{X509SecurityCredential.class, null}; // also support no security context
    }

    public void setSecurityCredential(SecurityCredential credential) {
        X509SecurityCredential adaptor = (X509SecurityCredential) credential;
        if (adaptor != null) {  // also support no security context
            m_userID = adaptor.getUserID();
            m_keyManager = adaptor.getKeyManager();
        }
    }

    public int getDefaultPort() {
        return 443;
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        super.connect(userInfo, host, port, basePath, attributes);

        // set socket factory
        if (attributes.get(MUTUAL_AUTHENTICATION)!=null && attributes.get(MUTUAL_AUTHENTICATION).equals("true")) {
            m_socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        } else {
            TrustManager[] trustManager = new TrustManager[]{new OneWayAuthenticationTrustManager()};
            try {
                SSLContext context = SSLContext.getInstance("SSL");
                context.init(m_keyManager, trustManager, new java.security.SecureRandom());
                m_socketFactory = context.getSocketFactory();
            } catch (NoSuchAlgorithmException e) {
                throw new NoSuccessException(e);
            } catch (KeyManagementException e) {
                throw new AuthenticationFailedException(e);
            }
        }
    }

    public void disconnect() throws NoSuccessException {
        super.disconnect();

        // unset socket factory
        m_socketFactory = null;
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
    }
}
