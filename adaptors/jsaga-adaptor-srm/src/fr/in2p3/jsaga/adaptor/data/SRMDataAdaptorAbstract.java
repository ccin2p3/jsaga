package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.transport.http.HTTPSender;
import org.globus.axis.transport.GSIHTTPSender;
import org.globus.axis.transport.HTTPSSender;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.*;

import java.util.Map;
import java.util.StringTokenizer;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SRMDataAdaptorAbstract
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class SRMDataAdaptorAbstract implements DataAdaptor {
    private static final String TRANSFER_PROTOCOLS = "TransferProtocols";    
    protected static SimpleProvider s_provider;
    protected GSSCredential m_credential;
    protected String m_host;
    protected int m_port;
    protected String[] m_transferProtocols;

    static {
        s_provider = new SimpleProvider();
        s_provider.deployTransport("httpg", new SimpleTargetedChain(new GSIHTTPSender()));
        s_provider.deployTransport("https", new SimpleTargetedChain(new HTTPSSender()));
        s_provider.deployTransport("http", new SimpleTargetedChain(new HTTPSender()));
        org.globus.axis.util.Util.registerTransport();
    }

    protected abstract void ping() throws BadParameterException, NoSuccessException;

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        m_host = host;
        m_port = port;
        if (attributes!=null && attributes.containsKey(TRANSFER_PROTOCOLS)) {
            String value = (String) attributes.get(TRANSFER_PROTOCOLS);
            StringTokenizer tokenizer = new StringTokenizer(value, ", \t\n\r\f");
            m_transferProtocols = new String[tokenizer.countTokens()];
            for (int i=0; i<m_transferProtocols.length; i++) {
                m_transferProtocols[i] = tokenizer.nextToken();
            }
        }
    }

    public Usage getUsage() {
        return new UOptional(TRANSFER_PROTOCOLS);
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return new Default[]{new Default(TRANSFER_PROTOCOLS, "gsiftp")};
    }

    public Class[] getSupportedSecurityAdaptorClasses() {
        return new Class[]{GSSCredentialSecurityAdaptor.class};
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        m_credential = ((GSSCredentialSecurityAdaptor) securityAdaptor).getGSSCredential();
    }

    public BaseURL getBaseURL() throws IncorrectURLException {
        return new BaseURL(8443);
    }
}
