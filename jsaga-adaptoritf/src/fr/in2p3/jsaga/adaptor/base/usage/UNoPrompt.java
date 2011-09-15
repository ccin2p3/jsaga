package fr.in2p3.jsaga.adaptor.base.usage;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UNoPrompt
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   8 aout 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UNoPrompt extends U {
    public UNoPrompt(String name) {
        super(name);
    }

    public UNoPrompt(int id, String name) {
        super(id, name);
    }

    public String toString() {
        return "_"+m_name+"_";
    }
}
