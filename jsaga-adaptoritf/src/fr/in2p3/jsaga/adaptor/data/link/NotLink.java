package fr.in2p3.jsaga.adaptor.data.link;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NotLink
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   15 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NotLink extends Exception {
    public NotLink(String absolutePath, Throwable e) {
        super("Entry is not a link: "+absolutePath, e);
    }

    public NotLink(String absolutePath) {
        super("Entry is not a link: "+absolutePath);
    }
}
