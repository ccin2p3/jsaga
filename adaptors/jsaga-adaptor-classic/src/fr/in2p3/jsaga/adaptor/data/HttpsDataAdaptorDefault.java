package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.impl.OneWayAuthenticationTrustManager;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.X509SecurityAdaptor;
import org.ogf.saga.error.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   HttpsDataAdaptorDefault
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   19 mars 2008
* ***************************************************
* Description:                                      */
/**
 * NOTE: this adaptor does not work, use adaptor HttpsDataAdaptorSocketBased instead
 */
public class HttpsDataAdaptorDefault extends HttpDataAdaptorDefault implements FileReaderStreamFactory {
    private static final String MUTUAL_AUTHENTICATION = "MutualAuthentication";
    // security
    private String m_userID;
    private KeyManager[] m_keyManager;
    // conection
    private SSLContext m_sslContext;
    private HostnameVerifier m_verifier;

    public HttpsDataAdaptorDefault() {
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

        // set trust manager
        TrustManager[] trustManager;
        if (attributes.get(MUTUAL_AUTHENTICATION)!=null && attributes.get(MUTUAL_AUTHENTICATION).equals("true")) {
            trustManager = null;
        } else {
            trustManager = new TrustManager[]{new OneWayAuthenticationTrustManager()};
        }

        // set SSL context
        try {
            m_sslContext = SSLContext.getInstance("SSL");
            m_sslContext.init(m_keyManager, trustManager, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuccess(e);
        } catch (KeyManagementException e) {
            throw new AuthenticationFailed(e);
        }

        // set hostname verifier (relaxed about hostnames)
        m_verifier = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
    }

    public void disconnect() throws NoSuccess {
        super.disconnect();

        // unset SSL context
        m_sslContext = null;
        
        // unset hostname verifier
        m_verifier = null;
    }

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        HttpURLConnection connection = this.getConnection(absolutePath, additionalArgs);
        try {
            connection.setRequestMethod("GET");
            connection.connect();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                in.close();
                //todo: detailler les exception
                throw new NoSuccess("Received error message: "+ connection.getResponseMessage());
            }
            return in;
        } catch (SSLHandshakeException e) {
            throw new PermissionDenied("User not allowed: "+m_userID, e);
        } catch (IOException e) {
            throw new NoSuccess(e);
        }
    }

    protected HttpURLConnection getConnection(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, NoSuccess {
        HttpURLConnection connection = super.getConnection(absolutePath, additionalArgs);
        if (connection instanceof HttpsURLConnection) {
            // set HTTPS specific parameters
            HttpsURLConnection conHttps = (HttpsURLConnection) connection;
            conHttps.setSSLSocketFactory(m_sslContext.getSocketFactory());
            conHttps.setHostnameVerifier(m_verifier);
            return conHttps;
        } else if (connection instanceof HttpURLConnection) {
            return connection;
        } else if (connection.getClass().getName().equals("org.globus.net.GSIHttpURLConnection")) {
            throw new NoSuccess("This class cannot be used within a Globus container...");
        } else {
            throw new NoSuccess("Unexpected connection type: "+connection.getClass().getName());
        }
    }
}
