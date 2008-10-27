package fr.in2p3.jsaga.adaptor.security.impl;

import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

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

    public String getAttribute(String key) throws NotImplementedException, NoSuccessException {
        if (Context.USERPASS.equals(key)) {
            return m_userPass;
        } else {
            throw new NotImplementedException("Attribute not supported: "+key);
        }
    }

    public String getUserPass() {
        return m_userPass;
    }

    public void close() throws Exception {
        // do nothing
    }

    public void dump(PrintStream out) throws Exception {
        out.println("  UserID   : "+ m_userId);
        out.print("  UserPass : ");
        for (int i=0; m_userPass!=null && i<m_userPass.length(); i++) {
            out.print('*');
        }
        out.println();
    }
}
