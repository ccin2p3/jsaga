package fr.in2p3.jsaga.impl.session;

import fr.in2p3.jsaga.impl.AbstractSagaBaseImpl;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.ogf.saga.ObjectType;
import org.ogf.saga.SagaBase;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.session.Session;

import java.util.ArrayList;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SessionImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SessionImpl extends AbstractSagaBaseImpl implements Session {
    private List<Context> m_contexts;

    /** constructor */
    public SessionImpl() {
        super();
        m_contexts = new ArrayList<Context>();
    }

    /** constructor for deepCopy */
    protected SessionImpl(SessionImpl source) {
        super((AbstractSagaBaseImpl) source);
        m_contexts = deepCopy(source.m_contexts);
    }
    public SagaBase deepCopy() {
        return new SessionImpl(this);
    }

    public ObjectType getType() {
        return ObjectType.SESSION;
    }

    public void addContext(Context context) {
        if (! m_contexts.contains(context)) {
            m_contexts.add(context);
        }
    }

    public void removeContext(Context context) throws DoesNotExist {
        m_contexts.remove(context);
    }

    public List<Context> listContexts() {
        return m_contexts;
    }

    public void close() {
        for (Context context : m_contexts) {
            ((ContextImpl) context).close();
        }
    }

    public void close(float timeoutInSeconds) {
        this.close();
    }
}
