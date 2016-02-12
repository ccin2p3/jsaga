package fr.in2p3.jsaga.adaptor.openstack;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import org.apache.log4j.Logger;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.types.ServiceType;
import org.openstack4j.model.identity.Token;
import org.openstack4j.model.identity.Access.Service;
import org.openstack4j.openstack.OSFactory;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.openstack.security.OpenstackSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.openstack.security.OpenstackSecurityCredential;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   OpenstackAdaptorAbstract
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   26 JAN 2016
 * ***************************************************/

public abstract class OpenstackAdaptorAbstract implements ClientAdaptor {

    protected Logger m_logger = Logger.getLogger(this.getClass());

    protected OpenstackSecurityCredential m_credential;
    protected URL m_url;
    protected Token m_token;
    protected OSClient m_os;
    protected String m_tenant;
    
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
        m_logger.debug("Connecting to " + host);
        m_tenant = m_credential.getAttribute(OpenstackSecurityAdaptor.PARAM_TENANT);
        m_os = OSFactory.builder()
                .endpoint("https://" + host + ":" + port + basePath)
                .credentials(m_credential.getUserID(), m_credential.getUserPass())
                .tenantName(m_tenant)
                .authenticate();
        m_token = m_os.getToken();
        m_logger.info("Connected to TENANT " + m_credential.getAttribute(OpenstackSecurityAdaptor.PARAM_TENANT));
        m_logger.info("Token expires " + m_token.getExpires());
        m_logger.debug(m_token.toString());
    }

    public void disconnect() throws NoSuccessException {
    }
    
    @Deprecated
    protected ServiceType typeFromServiceURL(String url) throws MalformedURLException, DoesNotExistException {
        URL templateAddress = new URL(url);
        for (Service serv: m_os.getAccess().getServiceCatalog()) {
            URI serviceURI = serv.getEndpoints().get(0).getPublicURL();
            if (serviceURI.getHost().equals(templateAddress.getHost())
                    && serviceURI.getPort() == templateAddress.getPort()) {
                return serv.getServiceType();
            }
        }
        throw new DoesNotExistException();
    }

}