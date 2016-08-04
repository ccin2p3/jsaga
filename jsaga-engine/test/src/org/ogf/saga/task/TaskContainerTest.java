package org.ogf.saga.task;

import org.junit.Assert;
import org.junit.Test;
import org.ogf.saga.task.impl.AsyncTest;
import org.ogf.saga.task.impl.TaskForTesting;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   TaskContainerTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class TaskContainerTest extends Assert {
    
    @Test
    public void test_sync() throws Exception {
        TaskContainer container = TaskFactory.createTaskContainer();
        Task<?,?> task1 = new AsyncTest().getHello(TaskMode.SYNC, "test1");
        container.add(task1);
        assertEquals(
                "Hello test1 !",
                task1.getResult());
    }

    @Test
    public void test_async() throws Exception {
        TaskContainer container = TaskFactory.createTaskContainer();
        container.add(new AsyncTest().getHello(TaskMode.TASK, "test1"));
        container.run();
        Task<?,?> task1 = container.waitFor(WaitMode.ALL);
        assertEquals(
                "Hello test1 !",
                task1.getResult());
    }

    @Test
    public void test_notified_task() throws Exception {
        TaskContainer container = TaskFactory.createTaskContainer();
        container.add(new TaskForTesting(true));
        container.run();
        Task<?,?> task1 = container.waitFor(WaitMode.ALL);
        assertEquals(
                "result",
                task1.getResult());
    }

    @Test
    public void test_unnotified_task() throws Exception {
        TaskContainer container = TaskFactory.createTaskContainer();
        container.add(new TaskForTesting(false));
        container.run();
        Task<?,?> task1 = container.waitFor(WaitMode.ALL);
        assertEquals(
                "result",
                task1.getResult());
    }
}
