package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import org.ogf.saga.error.IncorrectURLException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface DataAdaptor extends ClientAdaptor {
    /**
     * Get default information to fill URLs provided by users.
     * This information can be, for example, the central logical file server to contact,
     * the default port, the base path, some query parameters...
     * @return the base URL
     */
    public BaseURL getBaseURL() throws IncorrectURLException;
}
