package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;

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

    public UserPassExpirableSecurityAdaptor(String userName, String userPass, int expiryDate) {
        super(userName, userPass);
        // set expiration date
        m_expiryDate = expiryDate;
    }

    public int getTimeLeft() {
        int currentDate = (int) (System.currentTimeMillis()/1000);
        return m_expiryDate - currentDate;
    }

    public void dump(PrintStream out) {
        super.dump(out);
        // display time left
        int timeleft = this.getTimeLeft();
        out.println("  timeleft : "+ GSSCredentialSecurityAdaptor.format(timeleft>0 ? timeleft : 0));
    }
}
