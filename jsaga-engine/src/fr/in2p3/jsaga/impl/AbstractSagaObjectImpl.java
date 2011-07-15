package fr.in2p3.jsaga.impl;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.engine.config.TimeoutConfiguration;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;

import java.util.UUID;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractSagaObjectImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractSagaObjectImpl implements SagaObject {
    protected static final String JSAGA_FACTORY = Base.getSagaFactory();

    protected Session m_session;
    private UUID m_uuid;

    /** constructor */
    public AbstractSagaObjectImpl(Session session) {
        m_session = session;
        m_uuid = UUID.randomUUID();
    }

    /** constructor */
    public AbstractSagaObjectImpl() {
        m_session = null;
        m_uuid = UUID.randomUUID();
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        AbstractSagaObjectImpl clone = (AbstractSagaObjectImpl) super.clone();
        clone.m_session = m_session;
        clone.m_uuid = UUID.randomUUID();
        return clone;
    }

    /////////////////////////////////////////// implementation ///////////////////////////////////////////

    public Session getSession() throws DoesNotExistException {
        if (m_session != null) {
            return m_session;
        } else {
            throw new DoesNotExistException("This object does not have a session attached", this);
        }
    }

    public String getId() {
        return m_uuid.toString();
    }

    //////////////////////////////////////////// protected methods ////////////////////////////////////////////

    public static float getTimeout(Class itf, String methodName, String protocolScheme) throws NoSuccessException {
        return TimeoutConfiguration.getInstance().getTimeout(itf, methodName, protocolScheme);
    }

    public static Object getResult(Task task, float timeout)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException,
            TimeoutException, NoSuccessException, SagaIOException
    {
        task.waitFor(timeout);
        switch(task.getState()) {
            case DONE:
                return task.getResult();
            case CANCELED:
            case FAILED:
                task.rethrow();
                throw new NoSuccessException("[INTERNAL ERROR] Task failed");
            default:
                try{task.cancel();} catch(Exception e){/*ignore*/}
                throw new TimeoutException("User timeout occured after "+timeout+" seconds. If this happens too often, modify configuration file: "+EngineProperties.getURL(EngineProperties.JSAGA_TIMEOUT));
        }
    }
}
