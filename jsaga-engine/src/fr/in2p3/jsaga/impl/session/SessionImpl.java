package fr.in2p3.jsaga.impl.session;

import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.helpers.cloner.SagaObjectCloner;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.ogf.saga.SagaObject;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

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
public class SessionImpl extends AbstractSagaObjectImpl implements Session {
    private List<ContextImpl> m_contexts;

    /** constructor */
    public SessionImpl() {
        super();
        m_contexts = new ArrayList<ContextImpl>();
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        SessionImpl clone = (SessionImpl) super.clone();
        clone.m_contexts = new SagaObjectCloner<Void,ContextImpl>().cloneList(m_contexts);
        return clone;
    }

    public void addContext(Context context) throws NoSuccessException, TimeoutException {
        if (context instanceof ContextImpl) {
            ContextImpl ctxImpl = (ContextImpl) context;
            // Check if Type is set
            try {
                if ("Unknown".equals((String)context.getAttribute(Context.TYPE))) {
                    throw new NoSuccessException("The context is missing the attribute Type");
                }
            } catch (NotImplementedException | AuthenticationFailedException | PermissionDeniedException | AuthorizationFailedException e) {
                throw new NoSuccessException(e.getMessage(), e);
            } catch (IncorrectStateException | DoesNotExistException e) {
                throw new NoSuccessException("The context is missing the attribute Type");
            }
            if (! m_contexts.contains(ctxImpl)) {
                // check if new context will conflict with old contexts
                boolean check = EngineProperties.getBoolean(EngineProperties.JSAGA_DEFAULT_CONTEXTS_CHECK_CONFLICTS);
                if (check) {
                    for (ContextImpl refContext : m_contexts) {
                        ctxImpl.throwIfConflictsWith(refContext);
                    }
                }
                // set UrlPrefix (if needed)
                ctxImpl.setUrlPrefix(m_contexts.size()+1);
                // add context
                m_contexts.add(ctxImpl);
            }
            // create or renew credential
            try {
                ctxImpl.createCredential();
            } catch (NotImplementedException | IncorrectStateException e) {
                throw new NoSuccessException(e);
            }
        } else {
            throw new NoSuccessException("Unsupported context implementation: "+context.getClass());
        }
    }

    public void removeContext(Context context) throws DoesNotExistException {
        if (context instanceof ContextImpl) {
            ContextImpl ctxImpl = (ContextImpl) context;
            // FIXME: throw DoesNotExistException if remove returns false (SAGA Session spec)
            m_contexts.remove(ctxImpl);
        } else {
            throw new DoesNotExistException("Unsupported context implementation: "+context.getClass());
        }
    }

    public Context[] listContexts() {
        return m_contexts.toArray(new Context[m_contexts.size()]);
    }

    public void close() {
        for (ContextImpl context : m_contexts) {
            context.close();
        }
    }

    public void close(float timeoutInSeconds) {
        this.close();
    }

    /** use this method when you need to be sure to select the right context */
    public ContextImpl findContext(URL url) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        // look for matching filter
        for (ContextImpl ctxImpl : m_contexts) {
            if (ctxImpl.matches(url.toString())) {
                return ctxImpl;
            }
        }

        // else look for matching prefix
        for (ContextImpl ctxImpl : m_contexts) {
            String prefix;
            try {
                prefix = ctxImpl.getAttribute(ContextImpl.URL_PREFIX)+"-";
            } catch (IncorrectStateException e) {
                throw new NoSuccessException(e);
            }
            if (url.getScheme().startsWith(prefix)) {
                return ctxImpl;
            }
        }

        // else returns null
        return null;
    }

    /** use this method when you need to select the best matching context */
    public ContextImpl getBestMatchingContext(URL url) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        ContextImpl context = this.findContext(url);
        if (context != null) {
            return context;
        } else if (m_contexts.size() == 1) {
            return m_contexts.get(0);
        } else {
            try {
                return (ContextImpl) ContextFactory.createContext(JSAGA_FACTORY, "None");
            } catch (IncorrectStateException e) {
                throw new NoSuccessException(e);
            }
        }
    }
}
