package fr.in2p3.jsaga.impl.task;

import junit.framework.TestCase;
import org.ogf.saga.monitoring.*;
import org.ogf.saga.task.Task;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.AuthorizationFailedException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractTaskImplTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class AbstractTaskImplTest extends TestCase implements Callback {
    public void test_notified_task() throws Exception {
        Task task = new TaskForTesting(true);
        task.addCallback(Task.TASK_STATE, this);
        task.run();
        task.waitFor();
        assertEquals(
                "result",
                task.getResult());
    }

    public void test_unnotified_task() throws Exception {
        Task task = new TaskForTesting(false);
        task.addCallback(Task.TASK_STATE, this);
        task.run();
        task.waitFor();
        assertEquals(
                "result",
                task.getResult());
    }

    public boolean cb(Monitorable mt, Metric metric, Context ctx) throws NotImplementedException, AuthorizationFailedException {
        try {
            String name = metric.getAttribute(Metric.NAME);
            String value = metric.getAttribute(Metric.VALUE);
//            System.out.println("  "+name+" = "+value);
        } catch (Exception e) {
            throw new NotImplementedException(e);
        }
        return true;
    }
}
