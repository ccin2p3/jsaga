package fr.in2p3.jsaga.impl.task;

import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import org.ogf.saga.ObjectType;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.NotImplemented;
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
    public ObjectType getType() {
        return ObjectType.UNKNOWN;
    }

    public String getHello(String name) {
//        try {Thread.currentThread().sleep(100);} catch (InterruptedException e) {/*ignore*/}
        return "Hello "+name+" !";
    }

    public Task<String> getHello(TaskMode mode, String name) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AsyncTest.class.getMethod("getHello", new Class[]{String.class}),
                    new Object[]{name}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
}
