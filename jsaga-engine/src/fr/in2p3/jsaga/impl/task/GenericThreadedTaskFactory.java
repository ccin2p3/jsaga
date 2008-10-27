package fr.in2p3.jsaga.impl.task;

import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.SagaException;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GenericThreadedTaskFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GenericThreadedTaskFactory<T,E> {
    public Task<T,E> create(TaskMode mode, Session session, T object, String method, Class[] argTypes, Object[] argValues) throws NotImplementedException {
        try {
            Task<T,E> task = new GenericThreadedTask<T,E>(
                    session,
                    object,
                    object.getClass().getMethod(method, argTypes),
                    argValues);
            switch(mode) {
                case TASK:
                    return task;
                case ASYNC:
                    task.run();
                    return task;
                case SYNC:
                    task.run();
                    task.waitFor();
                    return task;
                default:
                    throw new NotImplementedException("INTERNAL ERROR: unexpected exception");
            }
        } catch (NoSuchMethodException e) {
            throw new NotImplementedException(e);
        } catch (NotImplementedException e) {
            throw e;
        } catch (SagaException e) {
            throw new NotImplementedException(e);
        }
    }
}
