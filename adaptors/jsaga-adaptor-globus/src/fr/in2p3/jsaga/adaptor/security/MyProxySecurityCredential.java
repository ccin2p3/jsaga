package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityCredential;
import org.globus.myproxy.CredentialInfo;
import org.globus.myproxy.InfoParams;
import org.globus.myproxy.MyProxy;
import org.globus.util.Util;
import org.ietf.jgss.GSSCredential;

import java.io.File;
import java.io.PrintStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MyProxySecurityCredential
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   13 ao�t 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class MyProxySecurityCredential extends GSSCredentialSecurityCredential implements SecurityCredential {
    private String m_server;
    // --- private String m_userId;
    // --- private String m_myProxyPass;
    private InfoParams m_proxyParameters;
    
    // --- public MyProxySecurityCredential(GSSCredential proxy, File certRepository, String server, String userId, String myProxyPass) {
    public MyProxySecurityCredential(GSSCredential proxy, File certRepository, String server, InfoParams proxyParameters) {
        super(proxy, certRepository);
        m_server = server;
        // --- m_userId = userId;
        // --- m_myProxyPass = myProxyPass;
        m_proxyParameters = proxyParameters;
    }

    /** override super.dump() */
    public void dump(PrintStream out) throws Exception {
        MyProxy server = MyProxySecurityAdaptor.getServer(m_server);

        // server info
        // --- CredentialInfo info = server.info(m_proxy, m_userId, m_myProxyPass);
        CredentialInfo[] infos = server.info(m_proxy, m_proxyParameters);
        out.println("Owner    : "+infos[0].getOwner());
        for (int i=0;i<infos.length;i++) {
        	CredentialInfo info = infos[i];
            out.println ((info.getName() == null) ? "default:" : info.getName() +":");
	        out.println("  StartTime: "+info.getStartTimeAsDate());
	        out.println("  EndTime  : "+info.getEndTimeAsDate());
	        long now = System.currentTimeMillis();
	        if (info.getEndTime() > now) {
	            out.println("  LifeTime : "+Util.formatTimeSec((info.getEndTime() - now)/1000));
	        } else {
	            out.println("  LifeTime : expired");
	        }
	        //if (info.getName() != null) {
	        //    out.println("  Name     : "+info.getName());
	        //}
	        if (info.getRetrievers() != null) {
	            out.println("  Retrievers : "+info.getRetrievers());
	        }
	        if (info.getRenewers() != null) {
	            out.println("  Renewers : "+info.getRenewers());
	        }
	        if (info.getDescription() != null) {
	            out.println("  Description : "+info.getDescription());
	        }
        }
        // local info
        out.println("  Delegated LifeTime : "+format(m_proxy.getRemainingLifetime()));
    }
}
