package fr.in2p3.jsaga.adaptor.data;

import java.io.File;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.Call;
import org.apache.axis.configuration.SimpleProvider;
import org.globus.axis.transport.GSIHTTPTransport;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityCredential;

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
    private static final String PREPARE_TIMEOUT = "PrepareTimeout";

    protected static SimpleProvider s_provider;
    protected GSSCredential m_credential;
    protected File m_certRepository;
    protected String m_host;
    protected int m_port;
    protected String[] m_transferProtocols;
    protected long m_prepareTimeout;
    protected String m_vo;
    
    static {
    	// Set provider
    	Call.setTransportForProtocol("httpg", GSIHTTPTransport.class);
    	s_provider = new SimpleProvider();
        SimpleTargetedChain c = new SimpleTargetedChain(new HTTPGHandler());
        s_provider.deployTransport("httpg", c);
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
        if (attributes!=null && attributes.containsKey(PREPARE_TIMEOUT)) {
            String value = (String) attributes.get(PREPARE_TIMEOUT);
            try {
                m_prepareTimeout = Integer.parseInt(value) * 1000;
            } catch (NumberFormatException e) {
                throw new BadParameterException("Unexpected value type for attribute: "+ PREPARE_TIMEOUT, e);
            }
        }
    }

    public Usage getUsage() {
        return new UAnd.Builder()
                        .and(new UOptional(TRANSFER_PROTOCOLS))
                        .and(new UOptional(PREPARE_TIMEOUT))
                        .build();
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return new Default[]{
                new Default(TRANSFER_PROTOCOLS, "gsiftp"),
                new Default(PREPARE_TIMEOUT, "300")
        };
    }

    public Class[] getSupportedSecurityCredentialClasses() {
        return new Class[]{GSSCredentialSecurityCredential.class};
    }

    public void setSecurityCredential(SecurityCredential credential) {
        GSSCredentialSecurityCredential proxyAdaptor = (GSSCredentialSecurityCredential) credential;
        m_credential = proxyAdaptor.getGSSCredential();
        m_certRepository = proxyAdaptor.getCertRepository();
        try {
			m_vo = proxyAdaptor.getAttribute(Context.USERVO);
		} catch (Exception e) {
			m_vo = "Unknown_VO";
		}
    }

    public int getDefaultPort() {
        return 8446;
    }
}
