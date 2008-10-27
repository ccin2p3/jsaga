package fr.in2p3.jsaga.impl.task;

import org.ogf.saga.error.*;
import org.ogf.saga.task.TaskContainer;
import org.ogf.saga.task.TaskFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   TaskFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class TaskFactoryImpl extends TaskFactory {
    protected TaskContainer doCreateTaskContainer() throws NotImplementedException, TimeoutException, NoSuccessException {
        return new TaskContainerImpl(null);
    }
}
