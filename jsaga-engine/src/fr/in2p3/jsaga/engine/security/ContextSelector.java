package fr.in2p3.jsaga.engine.security;

import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ContextSelector
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ContextSelector {
    private Session m_session;

    public ContextSelector(Session session) {
        m_session = session;
    }

    public ContextImpl selectContextByTypeIndice(String type, String indice) throws BadParameter, Timeout, PermissionDenied, NoSuccess, AuthorizationFailed, NotImplemented, AuthenticationFailed {
        if (type == null) {
            throw new BadParameter("Provided context type is: null");
        }
        if (indice == null) {
            throw new BadParameter("Provided context instance indice is: null");
        }
        if (m_session == null) {
            throw new NoSuccess("No session");
        }
        boolean typeFound = false;
        Context[] contexts = m_session.listContexts();
        for (int i=0; i<contexts.length; i++) {
            ContextImpl context = (ContextImpl) contexts[i];
            try {
                if (context.getAttribute("Type").equals(type)) {
                    typeFound = true;
                    if (context.getAttribute("Indice").equals(indice)) {
                        return context;
                    }
                }
            } catch(IncorrectState e) {
            } catch(DoesNotExist e) {
            }
        }
        if (typeFound) {
            throw new NoSuccess("Session has no context of type: "+type);
        } else {
            throw new NoSuccess("Session has no context instance with indice: "+indice);
        }
    }

    public ContextImpl selectContextByName(String name) throws BadParameter, Timeout, PermissionDenied, NoSuccess, AuthorizationFailed, NotImplemented, AuthenticationFailed {
        if (name == null) {
            throw new BadParameter("Provided context instance name is: null");
        }
        if (m_session == null) {
            throw new NoSuccess("No session");
        }
        Context[] contexts = m_session.listContexts();
        for (int i=0; i<contexts.length; i++) {
            ContextImpl context = (ContextImpl) contexts[i];
            try {
                if (context.getAttribute("Name").equals(name)) {
                    return context;
                }
            } catch(IncorrectState e) {
            } catch(DoesNotExist e) {
            }
        }
        throw new NoSuccess("Session has no context instance with name: "+name);
    }
}
