package fr.in2p3.jsaga.impl.task;

import org.junit.Assert;
import org.junit.Test;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.monitoring.*;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GenericThreadedTaskTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GenericThreadedTaskTest extends Assert implements Callback {
    
    @Test
    public void test_sync() throws Exception {
        Task<AsyncTest,String> task = new AsyncTest().getHello(TaskMode.SYNC, "test");
        assertEquals(
                "Hello test !",
                task.getResult());
    }

    @Test
    public void test_task() throws Exception {
        Task<AsyncTest,String> task = new AsyncTest().getHello(TaskMode.TASK, "test");
        task.addCallback(Task.TASK_STATE, this);
        task.run();
        task.waitFor();
        assertEquals(
                "Hello test !",
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
