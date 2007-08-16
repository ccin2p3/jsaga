package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.schema.config.ContextInstance;
import fr.in2p3.jsaga.engine.security.SessionImpl;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.Exception;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SessionFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SessionFactoryImpl extends SessionFactory {
    protected Session doCreateSession(boolean defaults) throws NoSuccess {
        // load config
        ContextInstance[] xmlInstanceArray = Configuration.getInstance().getConfigurations().getContextCfg().toXMLArray();
        // create new session
        Session sessionInstance = new SessionImpl();
        // may create default contexts
        if (defaults) {
            for (int c=0; c<xmlInstanceArray.length; c++) {
                ContextInstance xmlInstance = xmlInstanceArray[c];
                // create new context
                Context contextInstance = ContextFactoryImpl.createContext();
                try {
                    contextInstance.setAttribute("Type", xmlInstance.getType());
                    if (xmlInstance.hasIndice()) {
                        contextInstance.setAttribute("Indice", String.valueOf(xmlInstance.getIndice()));
                    }
                    if (xmlInstance.getName() != null) {
                        contextInstance.setAttribute("Name", xmlInstance.getName());
                    }
                    contextInstance.setDefaults();
                } catch(Exception e) {
                    throw new NoSuccess(e);
                }
                sessionInstance.addContext(contextInstance);
            }
        }
        return sessionInstance;
    }
}
