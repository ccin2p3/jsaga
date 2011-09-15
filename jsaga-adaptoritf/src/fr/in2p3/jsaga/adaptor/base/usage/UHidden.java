package fr.in2p3.jsaga.adaptor.base.usage;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UHidden
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   3 aout 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UHidden extends U {
    public UHidden(String name) {
        super(name);
    }

    public String toString() {
        return "*"+m_name+"*";
    }
}
