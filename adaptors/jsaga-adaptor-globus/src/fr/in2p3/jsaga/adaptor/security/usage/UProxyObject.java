package fr.in2p3.jsaga.adaptor.security.usage;

import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.IncorrectState;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UProxyObject
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   13 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UProxyObject extends UNoPrompt {
    private int m_minLifeTime;

    public UProxyObject(String name, int minLifeTime) {
        super(name);
        m_minLifeTime = minLifeTime;
    }

    public String toString() {
        return "_"+m_name+":"+m_minLifeTime+"_";
    }

    protected void throwExceptionIfInvalid(Object value) throws Exception {
        super.throwExceptionIfInvalid(value);
        GSSCredential cred = (GSSCredential) value;
        if (cred.getRemainingLifetime() < m_minLifeTime) {
            throw new IncorrectState("Proxy object remaining lifetime is not enougth: "+cred.getRemainingLifetime());
        }
    }
}
