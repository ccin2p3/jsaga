package fr.in2p3.jsaga.adaptor.security.usage;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UOptional
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   8 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UOptional extends U {
    public UOptional(String name) {
        super(name);
    }

    public String toString() {
        return "["+m_name+"]";
    }

    protected void throwExceptionIfInvalid(Object value) throws Exception {
        // value is always valid (even if null)
    }
}
