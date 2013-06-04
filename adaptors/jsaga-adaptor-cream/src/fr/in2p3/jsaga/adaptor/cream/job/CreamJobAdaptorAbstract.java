package fr.in2p3.jsaga.adaptor.cream.job;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityCredential;

import org.glite.x2007.x11.ce.cream.CREAMLocator;
import org.glite.x2007.x11.ce.cream.CREAMPort;
import org.glite.x2007.x11.ce.cream.CreamBindingStub;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.xml.rpc.ServiceException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CreamJobAdaptorAbstract
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   10 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class CreamJobAdaptorAbstract implements ClientAdaptor {
    private static final String DELEGATION_ID = "delegationId";

    protected GSSCredential m_credential;
    protected String m_vo;
    protected File m_certRepository;

    protected String m_delegationId;
//    protected CreamBindingStub m_creamStub;
    protected CREAMPort m_creamStub;

    protected String m_creamVersion = "";
    
    public String getType() {
        return "cream";
    }

    public Class[] getSupportedSecurityCredentialClasses() {
        return new Class[]{GSSCredentialSecurityCredential.class};
    }

    public void setSecurityCredential(SecurityCredential credential) {
        m_credential = ((GSSCredentialSecurityCredential) credential).getGSSCredential();
        try {
            m_vo = credential.getAttribute(Context.USERVO);
        } catch (Exception e) {
            /* ignore */
        }
        try {
            m_certRepository = ((GSSCredentialSecurityCredential) credential).getCertRepository();
        } catch (Exception e) {
            /* ignore */
        }
    }

    public int getDefaultPort() {
        return 8443;
    }

    public Usage getUsage() {
        return new UOptional(DELEGATION_ID);
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;    // no default
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        // set DELEGATION_ID
        if (attributes.containsKey(DELEGATION_ID)) {
            m_delegationId = (String) attributes.get(DELEGATION_ID);
        } else {
            try {
                String dn = m_credential.getName().toString();
                m_delegationId = "delegation-"+Math.abs(dn.hashCode());
            } catch (GSSException e) {
                throw new NoSuccessException(e);
            }
        }
//        m_creamStub = new CreamStub(host, port, m_vo);
    	CREAMLocator cream_service = new CREAMLocator();
    	try {
			// TODO: check CREAM2 ou CREAM ???
			cream_service.setCREAM2EndpointAddress(new URL("https", host, port, "/ce-cream/services/CREAM").toString());
			m_creamStub = cream_service.getCREAM2();
		} catch (MalformedURLException e) {
            throw new BadParameterException(e.getMessage(), e);
		} catch (ServiceException e) {
            throw new BadParameterException(e.getMessage(), e);
		}
    }

    public void disconnect() throws NoSuccessException {
        m_creamStub = null;
    }
}
