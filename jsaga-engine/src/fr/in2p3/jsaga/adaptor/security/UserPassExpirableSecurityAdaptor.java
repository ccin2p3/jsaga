package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityAdaptor;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;

import java.io.PrintStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UserPassExpirableSecurityAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UserPassExpirableSecurityAdaptor extends UserPassSecurityAdaptor {
    private int m_expiryDate;

    public UserPassExpirableSecurityAdaptor(String userId, String userPass, int expiryDate) {
        super(userId, userPass);
        // set expiration date
        m_expiryDate = expiryDate;
    }

    /** override super.getAttribute() */
    public String getAttribute(String key) throws NotImplemented, NoSuccess {
        if (Context.LIFETIME.equals(key)) {
            return ""+this.getLifeTime();
        } else {
            return super.getAttribute(key);
        }
    }

    public void dump(PrintStream out) throws Exception {
        super.dump(out);
        out.println("  LifeTime : "+GSSCredentialSecurityAdaptor.format(this.getLifeTime()));
    }

    private int getLifeTime() {
        int currentDate = (int) (System.currentTimeMillis()/1000);
        int lifeTime = m_expiryDate - currentDate;
        return (lifeTime>0 ? lifeTime : 0);
    }
}
