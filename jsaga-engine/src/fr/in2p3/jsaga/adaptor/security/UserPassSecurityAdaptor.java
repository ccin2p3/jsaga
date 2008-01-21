package fr.in2p3.jsaga.adaptor.security;

import java.io.PrintStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UserPassSecurityAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   19 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UserPassSecurityAdaptor implements SecurityAdaptor {
    private String m_userId;
    private String m_userPass;

    public UserPassSecurityAdaptor(String userId, String userPass) {
        m_userId = userId;
        m_userPass = userPass;
    }

    public String getUserID() {
        return m_userId;
    }

    public String getUserPass() {
        return m_userPass;
    }

    public int getTimeLeft() {
        return INFINITE;
    }

    public void close() throws Exception {
        // do nothing
    }

    public void dump(PrintStream out) {
        out.println("  UserID : "+ m_userId);
        out.print("  UserPass : ");
        for (int i=0; m_userPass!=null && i<m_userPass.length(); i++) {
            out.print('*');
        }
        out.println();
    }
}
