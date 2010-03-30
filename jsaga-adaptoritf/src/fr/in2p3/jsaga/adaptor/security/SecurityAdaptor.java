package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.Adaptor;
import org.ogf.saga.error.*;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SecurityAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface SecurityAdaptor extends Adaptor {
    /**
     * @return the security credential class supported by this adaptor.
     */
    public Class getSecurityCredentialClass();

    /**
     * Create a security credential and initialize it with the provided attributes.
     * @param usage the identifier of the usage.
     * @param attributes the provided attributes.
     * @param contextId the identifier of the context instance.
     * @return the security credential.
     * @throws IncorrectStateException if the attributes refer to a context that is not of expected type
     * @throws NoSuccessException if creating the adaptor failed
     */
    public SecurityCredential createSecurityCredential(int usage, Map attributes, String contextId) throws IncorrectStateException, TimeoutException, NoSuccessException;
}
