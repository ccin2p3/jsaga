package fr.in2p3.jsaga.adaptor.resource;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AmbiguousMatch
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class AmbiguousMatch extends Exception {
    public AmbiguousMatch(String message) {
        super(message);
    }
}
