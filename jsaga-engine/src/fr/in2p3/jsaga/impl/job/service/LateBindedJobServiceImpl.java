package fr.in2p3.jsaga.impl.job.service;

import fr.in2p3.jsaga.impl.job.description.XJSDLJobDescriptionImpl;
import fr.in2p3.jsaga.impl.job.instance.LateBindedJobImpl;
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
public class LateBindedJobServiceImpl extends JobServiceImpl implements JobService {
    /** constructor */
    public LateBindedJobServiceImpl(Session session) {
        super(session, null, null, null);
    }

    public Job createJob(JobDescription jd) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        return new LateBindedJobImpl(m_session, (XJSDLJobDescriptionImpl) jd);
    }

    public List<String> list() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented yet..."); //todo: implement method list()
    }

    public Job getJob(String jobId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        try {
            return new LateBindedJobImpl(m_session, null, jobId);   //fixme: split jobId into rm + nativeJobId
        } catch (IncorrectURLException e) {
            throw new NoSuccessException(e);
        }
    }

    public JobSelf getSelf() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }
}
