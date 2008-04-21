package fr.in2p3.jsaga.impl.job.service;

import fr.in2p3.jsaga.impl.job.description.XJSDLJobDescriptionImpl;
import fr.in2p3.jsaga.impl.job.instance.LateBindedJobImpl;
import org.ogf.saga.ObjectType;
import org.ogf.saga.error.*;
import org.ogf.saga.job.*;
import org.ogf.saga.session.Session;

import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LateBindedJobServiceImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   27 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class LateBindedJobServiceImpl extends AbstractAsyncJobServiceImpl implements JobService {
    /** constructor */
    public LateBindedJobServiceImpl(Session session) {
        super(session);
    }

    public ObjectType getType() {
        return ObjectType.JOBSERVICE;
    }

    public Job createJob(JobDescription jd) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        return new LateBindedJobImpl(m_session, (XJSDLJobDescriptionImpl) jd);
    }

    public List<String> list() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented yet..."); //todo: implement method list()
    }

    public Job getJob(String jobId) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        try {
            return new LateBindedJobImpl(m_session, null, jobId);   //fixme: split jobId into rm + nativeJobId
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        }
    }

    public JobSelf getSelf() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }
}
