package fr.in2p3.jsaga.engine.security;

import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;

import java.util.Iterator;
import java.util.List;

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

    public ContextImpl selectContextByTypeIndice(String type, String indice) throws BadParameter, IncorrectState, Timeout, PermissionDenied, NoSuccess, AuthorizationFailed, NotImplemented, AuthenticationFailed {
        if (type == null) {
            throw new BadParameter("Provided context type is: null");
        }
        if (indice == null) {
            throw new BadParameter("Provided context instance indice is: null");
        }
        if (m_session == null) {
            throw new IncorrectState("No session");
        }
        boolean typeFound = false;
        List list = m_session.listContexts();
        for (Iterator it=list.iterator(); it.hasNext(); ) {
            ContextImpl context = (ContextImpl) it.next();
            try {
                if (context.getAttribute("Type").equals(type)) {
                    typeFound = true;
                    if (context.getAttribute("Indice").equals(indice)) {
                        return context;
                    }
                }
            } catch(DoesNotExist e) {
            } catch(ReadOnly e) {
            }
        }
        if (typeFound) {
            throw new IncorrectState("Session has no context of type: "+type);
        } else {
            throw new IncorrectState("Session has no context instance with indice: "+indice);
        }
    }

    public ContextImpl selectContextByName(String name) throws BadParameter, IncorrectState, Timeout, PermissionDenied, NoSuccess, AuthorizationFailed, NotImplemented, AuthenticationFailed {
        if (name == null) {
            throw new BadParameter("Provided context instance name is: null");
        }
        if (m_session == null) {
            throw new IncorrectState("No session");
        }
        List list = m_session.listContexts();
        for (Iterator it=list.iterator(); it.hasNext(); ) {
            ContextImpl context = (ContextImpl) it.next();
            try {
                if (context.getAttribute("Name").equals(name)) {
                    return context;
                }
            } catch(DoesNotExist e) {
            } catch(ReadOnly e) {
            }
        }
        throw new IncorrectState("Session has no context instance with name: "+name);
    }
}
