package fr.in2p3.jsaga.adaptor.naregi.job;

import fr.in2p3.jsaga.adaptor.base.SagaSecureAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityAdaptor;
import org.ietf.jgss.GSSCredential;
import org.naregi.ss.service.client.JobScheduleService;
import org.naregi.ss.service.client.JobScheduleServiceFactory;
import org.ogf.saga.error.*;

import java.io.*;
import java.lang.Exception;
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
public abstract class SuperSchedulerJobAdaptorAbstract implements SagaSecureAdaptor {
    protected GSSCredential m_credential;
    protected String m_account;
    protected String m_passPhrase;

    protected JobScheduleService m_jss;

    public String getType() {
        return "naregi";
    }

    public Class[] getSupportedSecurityAdaptorClasses() {
        return new Class[]{GSSCredentialSecurityAdaptor.class, UserPassSecurityAdaptor.class};
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        if (securityAdaptor instanceof GSSCredentialSecurityAdaptor) {
            m_credential = ((GSSCredentialSecurityAdaptor) securityAdaptor).getGSSCredential();
        } else if (securityAdaptor instanceof UserPassSecurityAdaptor) {
            m_account = ((UserPassSecurityAdaptor) securityAdaptor).getUserID();
            m_passPhrase = ((UserPassSecurityAdaptor) securityAdaptor).getUserPass();
        }
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
        m_jss = JobScheduleServiceFactory.create();
        if (host != null) {
            m_jss.setJmHost(host);
        }
        if (port != 0) {
            m_jss.setJmPort(port);
        }        
    }

    public void disconnect() throws NoSuccess {
        m_jss = null;
    }

    protected static String getJobID(File eprFile) throws NoSuccess {
        StringBuffer bfs = new StringBuffer();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(eprFile)));
            String line;
            while ((line = br.readLine()) != null) {
                bfs.append(line);
            }
        } catch (IOException e) {
            throw new NoSuccess(e);
        } finally {
            if (null != br) {
                try{br.close();} catch(Exception e){}
            }
        }
        return new String(bfs);
    }
}