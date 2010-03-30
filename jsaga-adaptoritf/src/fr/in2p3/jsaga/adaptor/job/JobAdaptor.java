package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobAdaptor extends ClientAdaptor {
    public static final String CHECK_AVAILABILITY = "CheckAvailability";

    /**
     * @return the default server port.
     */
    public int getDefaultPort();
}
