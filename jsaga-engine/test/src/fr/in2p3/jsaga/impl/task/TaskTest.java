package fr.in2p3.jsaga.impl.task;

import org.junit.Assert;
import org.junit.Test;
import org.ogf.saga.monitoring.*;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.task.Task;
import fr.in2p3.jsaga.impl.task.impl.TaskForTesting;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   TaskTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class TaskTest extends Assert implements Callback {

    @Test
    public void test_notified_task() throws Exception {
        Task task = new TaskForTesting(true);
        task.addCallback(Task.TASK_STATE, this);
        task.run();
        task.waitFor();
        assertEquals(
                "result",
                task.getResult());
    }

    @Test
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
