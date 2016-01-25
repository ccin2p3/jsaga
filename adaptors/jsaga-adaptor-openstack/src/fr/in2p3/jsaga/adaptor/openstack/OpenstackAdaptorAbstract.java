package fr.in2p3.jsaga.adaptor.openstack;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.openstack4j.api.OSClient;
import org.openstack4j.core.transport.Config;
import org.openstack4j.model.identity.Token;
import org.openstack4j.openstack.OSFactory;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UHidden;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.openstack.security.OpenstackSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.openstack.security.OpenstackSecurityCredential;
import fr.in2p3.jsaga.adaptor.openstack.util.OpenstackConstants;
import fr.in2p3.jsaga.adaptor.openstack.util.OpenstackRESTClient;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.X509SecurityCredential;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   OpenstackAdaptorAbstract
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   30 sept 2013
 * ***************************************************/

public abstract class OpenstackAdaptorAbstract implements ClientAdaptor {

    protected Logger m_logger = Logger.getLogger(this.getClass());

    protected OpenstackSecurityCredential m_credential;
    protected URL m_url;
//    protected String m_tokenId;
    protected Token m_token;
    protected OSClient m_os;

    @Override
    public String getType() {
        return "openstack";
    }

    public Class[] getSupportedSecurityCredentialClasses() {
        return new Class[] { OpenstackSecurityCredential.class };
    }

    public void setSecurityCredential(SecurityCredential credential) {
        m_credential = (OpenstackSecurityCredential) credential;
    }

    public int getDefaultPort() {
        return NO_DEFAULT;
    }

    public void connect(String userInfo, String host, int port,
            String basePath, Map attributes) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            IncorrectURLException, BadParameterException, TimeoutException,
            NoSuccessException {
        // remote host closed connection during handshake
        m_logger.debug("Connecting to " + host);
        m_os = OSFactory.builder()
                .endpoint("https://" + host + ":" + port + "/v2.0/")
                .withConfig(Config.newConfig()
                        .withSSLVerificationDisabled()
                        .withHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {return true;}
            })
                )
                .credentials(m_credential.getUserID(), m_credential.getUserPass())
                .tenantName(m_credential.getAttribute(OpenstackSecurityAdaptor.PARAM_TENANT))
                .authenticate();
        m_token = m_os.getToken();
        m_logger.debug(m_token.toString());
    }

    public void disconnect() throws NoSuccessException {
    }

//    private JSONObject createAuth(String login, String password, String tenant) {
//        Map mapCred = new HashMap();
//        mapCred.put("username", login);
//        mapCred.put("password", password);
//        Map mapAuth = new HashMap();
//        mapAuth.put("tenantName", tenant);
//        mapAuth.put("passwordCredentials", mapCred);
//        return new JSONObject(mapAuth);
//    }
}