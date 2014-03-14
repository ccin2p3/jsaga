package fr.in2p3.jsaga.adaptor.unicore;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import de.fzj.unicore.wsrflite.security.ISecurityProperties;
import de.fzj.unicore.wsrflite.security.UASSecurityProperties;
import eu.unicore.security.util.client.IClientProperties;
import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.JKSSecurityCredential;
import fr.in2p3.jsaga.adaptor.unicore.security.UnicoreSecurityProperties;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UnicoreAbstract
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   19 aout 2011
* ***************************************************/


public abstract class UnicoreAbstract implements ClientAdaptor {

    protected static final String SERVICE_NAME = "ServiceName";
    protected static final String RES = "Res";
    protected static final String TARGET = "Target";
    protected String m_target;
    protected JKSSecurityCredential m_credential;
    protected UnicoreSecurityProperties m_uassecprop = null;
    protected EndpointReferenceType m_epr = null;
    private Logger logger = Logger.getLogger(UnicoreAbstract.class);
    
    public Class[] getSupportedSecurityCredentialClasses() {
        return new Class[]{JKSSecurityCredential.class};
    }

    public void setSecurityCredential(SecurityCredential credential) {
         m_credential = (JKSSecurityCredential) credential;
    }

    public int getDefaultPort() {
        return 8080;
    }

    public String getType() {
        return "unicore";
    }

    public Usage getUsage() {
        return new UAnd.Builder()
                        .and(new U(TARGET))
                        .and(new U(SERVICE_NAME))
                        .and(new U(RES))
                        .build();
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        
        m_target = (String) attributes.get(TARGET);
        String serverUrl = "https://"+host+":"+port+"/"+m_target+"/services/"+(String) attributes.get(SERVICE_NAME)+"?res="+(String) attributes.get(RES);
        
//        Properties p = new Properties();
        
        try {
            m_uassecprop = new UnicoreSecurityProperties(m_credential);
        } catch (UnrecoverableKeyException e) {
            throw new AuthenticationFailedException(e);
        } catch (KeyStoreException e) {
            throw new AuthenticationFailedException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuccessException(e);
        } catch (CertificateException e) {
            throw new NoSuccessException(e);
        } catch (IOException e) {
            throw new NoSuccessException(e);
        }

//        m_uassecprop.setProperty(ISecurityProperties.WSRF_SSL, "true");
//        m_uassecprop.setProperty(ISecurityProperties.WSRF_SSL_CLIENTAUTH, "true");
//
//        //keystore and truststore locations
//        m_uassecprop.setProperty(ISecurityProperties.WSRF_SSL_KEYSTORE, m_credential.getKeyStorePath());
//        m_uassecprop.setProperty(ISecurityProperties.WSRF_SSL_KEYPASS, m_credential.getKeyStorePass());
//        m_uassecprop.setProperty(ISecurityProperties.WSRF_SSL_KEYALIAS, m_credential.getKeyStoreAlias());
//        m_uassecprop.setProperty(ISecurityProperties.WSRF_SSL_TRUSTSTORE, m_credential.getTrustStorePath());
//        if (m_credential.getTrustStorePass() != null) {
//                m_uassecprop.setProperty(ISecurityProperties.WSRF_SSL_TRUSTPASS, m_credential.getTrustStorePass());
//        }

        m_epr = EndpointReferenceType.Factory.newInstance();
        m_epr.addNewAddress().setStringValue(serverUrl);
        logger.debug("Connected to " + m_epr.toString());
    }

    public void disconnect() throws NoSuccessException {
        m_credential = null;
        m_target = null;
        m_epr = null;
        m_uassecprop = null;
    }    
    
}
