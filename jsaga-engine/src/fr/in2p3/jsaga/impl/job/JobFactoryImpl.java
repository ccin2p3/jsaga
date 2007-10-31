package fr.in2p3.jsaga.impl.job;

import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.job.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobFactoryImpl extends JobFactory {
    public JobFactoryImpl() {
        //todo: implement constructor
    }

    protected JobDescription doCreateJobDescription() throws NotImplemented {
        throw new NotImplemented("not implemented yet..."); //todo: implement method doCreateJobDescription()
    }

    protected JobService doCreateJobService(Session session, URL rm) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        throw new NotImplemented("not implemented yet..."); //todo: implement method doCreateJobService()
    }

    protected Task<JobService> doCreateJobService(TaskMode mode, Session session, URL rm) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }
}
