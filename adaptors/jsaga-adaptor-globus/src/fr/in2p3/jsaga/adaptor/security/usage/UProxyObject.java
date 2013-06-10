package fr.in2p3.jsaga.adaptor.security.usage;

import fr.in2p3.jsaga.adaptor.base.usage.UNoPrompt;
import fr.in2p3.jsaga.adaptor.security.impl.InMemoryProxySecurityCredential;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.IncorrectStateException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UProxyObject
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   13 aout 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UProxyObject extends UNoPrompt {
    private int m_minLifeTime;

    public UProxyObject(int id, String name, int minLifeTime) {
        super(id, name);
        m_minLifeTime = minLifeTime;
    }

    public String toString() {
        return "_"+m_name+":"+m_minLifeTime+"_";
    }

    protected Object throwExceptionIfInvalid(Object value) throws Exception {
        String v = (String) super.throwExceptionIfInvalid(value);
        GSSCredential cred = InMemoryProxySecurityCredential.toGSSCredential(v);
        if (cred.getRemainingLifetime() < m_minLifeTime) {
            throw new IncorrectStateException("Proxy object remaining lifetime is not enougth: "+cred.getRemainingLifetime());
        }
        return cred;
    }
}
