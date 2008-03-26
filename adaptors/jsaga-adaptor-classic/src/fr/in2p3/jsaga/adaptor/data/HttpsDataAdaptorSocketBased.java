package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.impl.HttpRequest;
import fr.in2p3.jsaga.adaptor.data.impl.OneWayAuthenticationTrustManager;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.X509SecurityAdaptor;
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

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return new Default[]{new Default(MUTUAL_AUTHENTICATION, "false")};
    }

    public Class[] getSupportedSecurityAdaptorClasses() {
        return new Class[]{X509SecurityAdaptor.class, null}; // also support no security context
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        X509SecurityAdaptor adaptor = (X509SecurityAdaptor) securityAdaptor;
        m_userID = adaptor.getUserID();
        m_keyManager = adaptor.getKeyManager();
    }

    public int getDefaultPort() {
        return 443;
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
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
                throw new NoSuccess(e);
            } catch (KeyManagementException e) {
                throw new AuthenticationFailed(e);
            }
        }
    }

    public void disconnect() throws NoSuccess {
        super.disconnect();

        // unset socket factory
        m_socketFactory = null;
    }

    protected HttpRequest getRequest(String absolutePath, String additionalArgs, String requestType) throws PermissionDenied, DoesNotExist, NoSuccess {
        if (m_baseUrl == null) {
            throw new NoSuccess("Connection is closed");
        }
        String path = absolutePath + (additionalArgs!=null ? "?"+additionalArgs : "");
        try {
            SSLSocket socket = (SSLSocket) m_socketFactory.createSocket(m_baseUrl.getHost(), m_baseUrl.getPort());
            socket.startHandshake();            
            return new HttpRequest(requestType, path, socket);
        } catch (UnknownHostException e) {
            throw new DoesNotExist("Unknown host: "+m_baseUrl.getHost(), e);
        } catch (SSLHandshakeException e) {
            throw new PermissionDenied("User not allowed: "+m_userID, e);
        } catch (IOException e) {
            throw new NoSuccess(e);
        }
    }
}
