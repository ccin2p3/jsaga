package fr.in2p3.jsaga.impl.session;

import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.schema.config.ContextInstance;
import fr.in2p3.jsaga.impl.context.ContextFactoryImpl;
import org.ogf.saga.context.Context;
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
    protected Session doCreateSession(boolean defaults) {
        Session session = new SessionImpl();
        if (defaults) {
            try {
                // load config
                ContextInstance[] xmlInstanceArray = Configuration.getInstance().getConfigurations().getContextCfg().toXMLArray();

                // create default contexts
                for (ContextInstance xmlInstance : xmlInstanceArray) {
                    // create context
                    Context context = ContextFactoryImpl.createContext();
                    context.setAttribute("Type", xmlInstance.getType());
                    if (xmlInstance.hasIndice()) {
                        context.setAttribute("Indice", String.valueOf(xmlInstance.getIndice()));
                    }
                    if (xmlInstance.getName() != null) {
                        context.setAttribute("Name", xmlInstance.getName());
                    }

                    // set context defaults
                    context.setDefaults();

                    // add context to session
                    session.addContext(context);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return session;
    }
}
