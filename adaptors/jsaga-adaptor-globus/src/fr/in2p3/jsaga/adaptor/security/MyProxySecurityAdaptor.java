package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
import org.globus.myproxy.CredentialInfo;
import org.globus.myproxy.MyProxy;
import org.globus.util.Util;
import org.ietf.jgss.GSSCredential;

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
    private String m_userName;
    private String m_myProxyPass;

    public MyProxySecurityAdaptor(GSSCredential proxy, String userName, String myProxyPass) {
        super(proxy);
        m_userName = userName;
        m_myProxyPass = myProxyPass;
    }

    /** override super.dump() */
    public void dump(PrintStream out) throws Exception {
        CredentialInfo info = new MyProxy().info(m_proxy, m_userName, m_myProxyPass);
        if (info.getName() != null) {
            out.println("  "+info.getName()+":");
        } else {
            out.println("  default:");
        }
        out.println("  Start Time  : "+info.getStartTime());
        out.println("  End Time    : "+info.getEndTime());
        long now = System.currentTimeMillis();
        if (info.getEndTime() > now) {
            out.println("  Time left   : "+Util.formatTimeSec((info.getEndTime() - now)/1000));
        } else {
            out.println("  Time left   : expired");
        }
        if (info.getRetrievers() != null) {
            out.println("  Retrievers  : "+info.getRetrievers());
        }
        if (info.getRenewers() != null) {
            out.println("  Renewers    : "+info.getRenewers());
        }
        if (info.getDescription() != null) {
            out.println("  Description : "+info.getDescription());
        }
    }
}
