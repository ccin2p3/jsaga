package fr.in2p3.jsaga.adaptor.base.usage;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   Usage
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface Usage {
    public boolean containsName(String attributeName);
    public void updateAttributes(Map attributes) throws Exception;
    public Usage getMissingValues(Map attributes);
    public void promptForValues(Map attributes, String id) throws Exception;
    public String toString();
}
