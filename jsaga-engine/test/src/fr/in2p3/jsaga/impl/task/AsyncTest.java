package fr.in2p3.jsaga.impl.task;

import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.task.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AsyncTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class AsyncTest extends AbstractSagaObjectImpl implements SagaObject, Async {
    public String getHello(String name) {
//        try {Thread.currentThread().sleep(100);} catch (InterruptedException e) {/*ignore*/}
        return "Hello "+name+" !";
    }

    public Task<AsyncTest, String> getHello(TaskMode mode, String name) throws NotImplementedException {
        return new GenericThreadedTaskFactory<AsyncTest,String>().create(
                mode, m_session, this,
                "getHello",
                new Class[]{String.class},
                new Object[]{name});
    }
}
