package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityCredential;
import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.Call;
import org.apache.axis.configuration.SimpleProvider;
import org.globus.axis.transport.GSIHTTPSender;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.*;

import java.io.File;
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
    protected File m_certRepository;
    protected String m_host;
    protected int m_port;
    protected String[] m_transferProtocols;

    static {
        s_provider = new SimpleProvider();
        s_provider.deployTransport("httpg", new SimpleTargetedChain(new GSIHTTPSender()));
        Call.initialize();
        Call.addTransportPackage("org.globus.net.protocol");
        Call.setTransportForProtocol("httpg", org.globus.axis.transport.GSIHTTPTransport.class);
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

    public Class[] getSupportedSecurityCredentialClasses() {
        return new Class[]{GSSCredentialSecurityCredential.class};
    }

    public void setSecurityCredential(SecurityCredential credential) {
        GSSCredentialSecurityCredential proxyAdaptor = (GSSCredentialSecurityCredential) credential;
        m_credential = proxyAdaptor.getGSSCredential();
        m_certRepository = proxyAdaptor.getCertRepository();
    }

    public BaseURL getBaseURL() throws IncorrectURLException {
        return new BaseURL(8446);
    }
}
