package fr.in2p3.jsaga.adaptor.cream.job;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.cream.CreamSocketFactory;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityCredential;

import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.databinding.types.URI.MalformedURIException;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.Logger;
import org.glite.ce.creamapi.ws.cream2.CREAMStub;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobFilter;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobId;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.ServiceInfo;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.ServiceInfoRequest;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

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

    protected CreamClient m_client = null;
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

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) 
            throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, 
            BadParameterException, TimeoutException, NoSuccessException {

        // set DELEGATION_ID
        if (attributes.containsKey(DELEGATION_ID)) {
            m_delegationId = (String) attributes.get(DELEGATION_ID);
        } else {
            try {
                String dn = m_credential.getName().toString();
                m_delegationId = "delegation-";
               	m_delegationId += (m_vo != null)?m_vo+"-":"";
                m_delegationId += Math.abs(dn.hashCode());
            } catch (GSSException e) {
                throw new NoSuccessException(e);
            }
        }
    	try {
    	    m_client = new CreamClient(host, port, m_credential, m_certRepository);
		} catch (MalformedURLException e) {
            throw new BadParameterException(e.getMessage(), e);
		} catch (AxisFault e) {
            throw new BadParameterException(e.getMessage(), e);
		}

    	try {
        	ServiceInfo service_info = m_client.getServiceInfo();
			String cream_desc = host + " (interface version=" + 
								service_info.getInterfaceVersion() + ",service version=" + 
								service_info.getServiceVersion() + ")";
    		Logger.getLogger(CreamJobAdaptorAbstract.class).info("Connecting to "+cream_desc);
    		m_creamVersion = service_info.getServiceVersion();
		} catch (Exception e) {
    		Logger.getLogger(CreamJobAdaptorAbstract.class).info("Could not get service version");
		}
        
    }

    public void disconnect() throws NoSuccessException {
        m_client.disconnect();
    }
    

}
