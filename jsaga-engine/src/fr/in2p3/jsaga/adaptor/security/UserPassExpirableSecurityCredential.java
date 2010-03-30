package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityCredential;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import java.io.PrintStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UserPassExpirableSecurityCredential
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UserPassExpirableSecurityCredential extends UserPassSecurityCredential {
    private int m_expiryDate;

    public UserPassExpirableSecurityCredential(String userId, String userPass, int expiryDate) {
        super(userId, userPass);
        // set expiration date
        m_expiryDate = expiryDate;
    }

    /** override super.getAttribute() */
    public String getAttribute(String key) throws NotImplementedException, NoSuccessException {
        if (Context.LIFETIME.equals(key)) {
            return ""+this.getLifeTime();
        } else {
            return super.getAttribute(key);
        }
    }

    public void dump(PrintStream out) throws Exception {
        super.dump(out);
        out.println("  LifeTime : "+ GSSCredentialSecurityCredential.format(this.getLifeTime()));
    }

    private int getLifeTime() {
        int currentDate = (int) (System.currentTimeMillis()/1000);
        int lifeTime = m_expiryDate - currentDate;
        return (lifeTime>0 ? lifeTime : 0);
    }
}
