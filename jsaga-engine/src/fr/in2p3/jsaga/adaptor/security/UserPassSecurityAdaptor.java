package fr.in2p3.jsaga.adaptor.security;

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
    private String m_userName;
    private String m_userPass;

    public UserPassSecurityAdaptor(String userName, String userPass) {
        m_userName = userName;
        m_userPass = userPass;
    }

    public String getUserName() {
        return m_userName;
    }

    public String getUserPass() {
        return m_userPass;
    }

    public void close() throws Exception {
        // do nothing
    }
}
