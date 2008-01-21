package fr.in2p3.jsaga.adaptor.resource;

import org.w3c.dom.Document;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GridSelectionAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface GridSelectionAdaptor {
    public SelectedGrid select(Document jsdlDOM, String[] args)
        throws NoMatch, AmbiguousMatch;
}
