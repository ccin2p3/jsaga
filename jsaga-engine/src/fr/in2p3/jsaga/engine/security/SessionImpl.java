package fr.in2p3.jsaga.engine.security;

import fr.in2p3.jsaga.engine.base.AbstractSagaBaseImpl;
import org.ogf.saga.SagaBase;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SessionImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SessionImpl extends AbstractSagaBaseImpl implements Session {
    private List m_contexts;

    /** constructor */
    public SessionImpl() {
        super();
        m_contexts = new ArrayList();
    }

    /** constructor for deepCopy */
    protected SessionImpl(SessionImpl source) {
        super((AbstractSagaBaseImpl) source);
        m_contexts = deepCopy(source.m_contexts);
    }
    public SagaBase deepCopy() {
        return new SessionImpl(this);
    }

    public void addContext(Context context) {
        try {
            findContext(context);
        } catch (DoesNotExist doesNotExist) {
            m_contexts.add(context);
        }
    }

    public void removeContext(Context context) throws DoesNotExist {
        int position = findContext(context);
        m_contexts.remove(position);
    }

    public List listContexts() {
        return m_contexts;
    }

    public void close(float timeoutInSeconds) {
        for (Iterator it=m_contexts.iterator(); it.hasNext(); ) {
            ContextImpl currentContext = (ContextImpl) it.next();
            currentContext.close();
        }
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public void init() throws NotImplemented, BadParameter, IncorrectState, NoSuccess {
        for (Iterator it=m_contexts.iterator(); it.hasNext(); ) {
            ContextImpl currentContext = (ContextImpl) it.next();
            currentContext.init();
        }
    }

    private int findContext(Context context) throws DoesNotExist {
        ContextImpl searchedContext = (ContextImpl) context;
        for (int i=0; i<m_contexts.size(); i++) {
            ContextImpl currentContext = (ContextImpl) m_contexts.get(i);
            if (currentContext.equals(searchedContext)) {
                return i;
            }
        }
        throw new DoesNotExist("Context does not exist", this);
    }
}
