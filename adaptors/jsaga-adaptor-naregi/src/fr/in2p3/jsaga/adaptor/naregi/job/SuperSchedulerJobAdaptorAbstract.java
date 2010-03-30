package fr.in2p3.jsaga.adaptor.naregi.job;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.naregi.security.NaregiSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
import org.ietf.jgss.GSSCredential;
import org.naregi.ss.service.client.*;
import org.ogf.saga.error.*;

import java.util.Iterator;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SuperSchedulerJobAdaptorAbstract
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   7 août 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class SuperSchedulerJobAdaptorAbstract implements ClientAdaptor {
    protected GSSCredential m_credential;
    protected String m_account;
    protected String m_passPhrase;

    protected JobScheduleService m_jss;

    public String getType() {
        return "naregi";
    }

    public Class[] getSupportedSecurityAdaptorClasses() {
        return new Class[]{GSSCredentialSecurityAdaptor.class, NaregiSecurityAdaptor.class};
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        if (securityAdaptor instanceof GSSCredentialSecurityAdaptor) {
            m_credential = ((GSSCredentialSecurityAdaptor) securityAdaptor).getGSSCredential();
        } else if (securityAdaptor instanceof NaregiSecurityAdaptor) {
            m_account = ((NaregiSecurityAdaptor) securityAdaptor).getUserID();
            m_passPhrase = ((NaregiSecurityAdaptor) securityAdaptor).getUserPass();
        }
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        // modify configuration of NAREGI API
        try {
            for (Iterator it=attributes.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry) it.next();
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                ConfigManager.setProperty(key, value);
            }
            String cmd = ConfigManager.getProperty("ss.command.location");
            String svr = ConfigManager.getProperty("Server");
            String env = ConfigManager.getProperty("ss.command.env");
            if (env==null || env.trim().equals("")) {
                throw new NoSuccessException("Missing required property: ss.command.env");
            }
            env += ", LD_LIBRARY_PATH="+cmd+"/lib:"+cmd+"/libexec";
            env += ", BSC_CLIENT_SERVICE_URL=http://"+svr+":8080/wsrf/services/BpelWFServiceContainer2";
            ConfigManager.setProperty("ss.command.env", env);
        } catch (JobScheduleServiceException e) {
            throw new NoSuccessException(e);
        }

        // create client
        m_jss = JobScheduleServiceFactory.create();
        if (host != null) {
            m_jss.setJmHost(host);
        }
        if (port != 0) {
            m_jss.setJmPort(port);
        }        
    }

    public void disconnect() throws NoSuccessException {
        m_jss = null;
    }
}