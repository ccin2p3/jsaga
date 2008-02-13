package fr.in2p3.jsaga.adaptor.security.impl;

import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;

import java.io.PrintStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GSSCredentialSecurityAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class GSSCredentialSecurityAdaptor implements SecurityAdaptor {
    protected GSSCredential m_proxy;

    public GSSCredentialSecurityAdaptor(GSSCredential proxy) {
        m_proxy = proxy;
    }

    public GSSCredential getGSSCredential() {
        return m_proxy;
    }

    public String getUserID() throws Exception {
        return m_proxy.getName().toString();
    }

    public String getAttribute(String key) throws NotImplemented, NoSuccess {
        if (Context.LIFETIME.equals(key)) {
            try {
                return ""+m_proxy.getRemainingLifetime();
            } catch (GSSException e) {
                throw new NoSuccess(e);
            }
        } else {
            throw new NotImplemented("Attribute not supported: "+key);
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
