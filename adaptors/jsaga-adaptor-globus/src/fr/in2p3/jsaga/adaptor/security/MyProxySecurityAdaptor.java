package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
import org.globus.myproxy.CredentialInfo;
import org.globus.myproxy.MyProxy;
import org.globus.util.Util;
import org.ietf.jgss.GSSCredential;

import java.io.File;
import java.io.PrintStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MyProxySecurityAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   13 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class MyProxySecurityAdaptor extends GSSCredentialSecurityAdaptor implements SecurityAdaptor {
    private String m_server;
    private String m_userId;
    private String m_myProxyPass;

    public MyProxySecurityAdaptor(GSSCredential proxy, File certRepository, String server, String userId, String myProxyPass) {
        super(proxy, certRepository);
        m_server = server;
        m_userId = userId;
        m_myProxyPass = myProxyPass;
    }

    /** override super.dump() */
    public void dump(PrintStream out) throws Exception {
        MyProxy server = MyProxySecurityAdaptorBuilder.getServer(m_server);

        // server info
        CredentialInfo info = server.info(m_proxy, m_userId, m_myProxyPass);
        out.println("  Owner    : "+info.getOwner());
        out.println("  StartTime: "+info.getStartTimeAsDate());
        out.println("  EndTime  : "+info.getEndTimeAsDate());
        long now = System.currentTimeMillis();
        if (info.getEndTime() > now) {
            out.println("  LifeTime : "+Util.formatTimeSec((info.getEndTime() - now)/1000));
        } else {
            out.println("  LifeTime : expired");
        }
        if (info.getName() != null) {
            out.println("  Name     : "+info.getName());
        }
        if (info.getRetrievers() != null) {
            out.println("  Retrievers : "+info.getRetrievers());
        }
        if (info.getRenewers() != null) {
            out.println("  Renewers : "+info.getRenewers());
        }
        if (info.getDescription() != null) {
            out.println("  Description : "+info.getDescription());
        }

        // local info
        out.println("  Delegated LifeTime : "+format(m_proxy.getRemainingLifetime()));
    }
}
