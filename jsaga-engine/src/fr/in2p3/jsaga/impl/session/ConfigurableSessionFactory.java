package fr.in2p3.jsaga.impl.session;

import fr.in2p3.jsaga.engine.session.SessionConfiguration;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.session.Session;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   ConfigurableSessionFactory
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class ConfigurableSessionFactory {
    public static Session createSession(SessionConfiguration config) throws NoSuccessException {
        return new SessionFactoryImpl(config).doCreateSession(true);
    }
}
