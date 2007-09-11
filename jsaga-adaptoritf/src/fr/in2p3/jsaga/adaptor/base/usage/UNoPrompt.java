package fr.in2p3.jsaga.adaptor.base.usage;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UNoPrompt
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   8 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UNoPrompt extends U {
    public UNoPrompt(String name) {
        super(name);
    }

    public String toString() {
        return "_"+m_name+"_";
    }

    public void promptForValues(Map attributes, String id) throws Exception {
        if (!attributes.containsKey(m_name)) {
            throw new Exception("Can not prompt for attribute: "+m_name);
        }
    }
}
