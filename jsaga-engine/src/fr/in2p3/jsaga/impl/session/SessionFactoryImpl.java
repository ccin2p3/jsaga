package fr.in2p3.jsaga.impl.session;

import fr.in2p3.jsaga.engine.session.SessionConfiguration;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SessionFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SessionFactoryImpl extends SessionFactory {
    private SessionConfiguration m_config;

    public SessionFactoryImpl(SessionConfiguration config) {
        m_config = config;
    }
    
    protected Session doCreateSession(boolean defaults) throws NotImplementedException, NoSuccessException {
        Session session = new SessionImpl();
        if (defaults) {
            m_config.setDefaultSession(session);
        }
        return session;
    }
}
