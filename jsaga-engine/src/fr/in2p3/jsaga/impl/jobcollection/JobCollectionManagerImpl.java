package fr.in2p3.jsaga.impl.jobcollection;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.evaluator.Evaluator;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import fr.in2p3.jsaga.jobcollection.*;
import org.ogf.saga.ObjectType;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;

import java.io.File;
import java.lang.Exception;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobCollectionManagerImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobCollectionManagerImpl extends AbstractSagaObjectImpl implements JobCollectionManager {
    private Evaluator m_evaluator;

    /** constructor */
    public JobCollectionManagerImpl(Session session, Evaluator evaluator) {
        super(session);
        m_evaluator = evaluator;
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        JobCollectionManagerImpl clone = (JobCollectionManagerImpl) super.clone();
        clone.m_evaluator = m_evaluator;
        return clone;
    }

    public ObjectType getType() {
        return ObjectType.UNKNOWN;
    }

    public JobCollection createJobCollection(JobCollectionDescription description) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        return new JobCollectionImpl(m_session, description, m_evaluator);
    }

    public JobCollection createJobCollection(JobCollectionDescription description, boolean force) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        if (force) {
            // Cleanup if needed
            try {
                JobCollectionCleaner cleaner = new JobCollectionCleaner(m_session, description.getCollectionName());
                cleaner.cleanup();
            } catch(Exception e) {
                throw new NoSuccess("Failed to cleanup job collection: "+description.getCollectionName(), e);
            }
        }
        // create job collection
        return this.createJobCollection(description);
    }

    public List<String> list() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        return null;  //todo
    }

    public JobCollection getJobCollection(String jobCollectionId) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        File baseDir = new File(new File(Base.JSAGA_VAR, "jobs"), jobCollectionId);
        if (baseDir.exists()) {
            return new JobCollectionReadOnlyImpl(jobCollectionId);
        } else {
            throw new DoesNotExist("Job collection does not exist: "+jobCollectionId);
        }
    }
}
