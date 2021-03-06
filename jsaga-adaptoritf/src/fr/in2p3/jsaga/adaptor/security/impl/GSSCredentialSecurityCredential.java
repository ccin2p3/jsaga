package fr.in2p3.jsaga.adaptor.security.impl;

import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import java.io.File;
import java.io.PrintStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GSSCredentialSecurityCredential
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class GSSCredentialSecurityCredential implements SecurityCredential {
    protected GSSCredential m_proxy;
    protected File m_certRepository;

    public GSSCredentialSecurityCredential(GSSCredential proxy, File certRepository) {
        m_proxy = proxy;
        m_certRepository = certRepository;
    }

    public GSSCredential getGSSCredential() {
        return m_proxy;
    }

    public File getCertRepository() {
        return m_certRepository;
    }

    public String getUserID() throws Exception {
        return m_proxy.getName().toString();
    }

    public String getAttribute(String key) throws NotImplementedException, NoSuccessException {
        if (Context.LIFETIME.equals(key)) {
            try {
                return ""+m_proxy.getRemainingLifetime();
            } catch (GSSException e) {
                throw new NoSuccessException(e);
            }
        } else {
            throw new NotImplementedException("Attribute not supported: "+key);
        }
    }

    public void close() throws Exception {
        //m_proxy.dispose();
    }

    public void dump(PrintStream out) throws Exception {
        out.println("  UserID   : "+this.getUserID());
        out.println("  LifeTime : "+format(m_proxy.getRemainingLifetime()));
    }

    public static String format(int seconds) {
        // convert
        int hours = seconds / 3600;
        seconds -= hours*3600;
        int minutes = seconds / 60;
        seconds -= minutes*60;

        // format
        StringBuffer buf = new StringBuffer();
        if(hours>0) {
            buf.append(hours);
            buf.append(" h, ");
        }
        if (hours>0 || minutes>0) {
            buf.append(minutes);
            buf.append(" min, ");
        }
        buf.append(seconds);
        buf.append(" sec");
        return buf.toString();
    }
}
