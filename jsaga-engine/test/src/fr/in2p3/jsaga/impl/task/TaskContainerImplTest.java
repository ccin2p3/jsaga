package fr.in2p3.jsaga.impl.task;

import junit.framework.TestCase;
import org.ogf.saga.task.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   TaskContainerImplTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class TaskContainerImplTest extends TestCase {
    public void test_sync() throws Exception {
        TaskContainer container = new TaskContainerImpl(null);
        int cookie1 = container.add(new AsyncTest().getHello(TaskMode.SYNC, "test1"));
        Task<?,?> task1 = container.getTask(cookie1);
        assertEquals(
                "Hello test1 !",
                task1.getResult());
    }

    public void test_async() throws Exception {
        TaskContainer container = new TaskContainerImpl(null);
        container.add(new AsyncTest().getHello(TaskMode.TASK, "test1"));
        container.run();
        Task<?,?> task1 = container.waitFor(WaitMode.ALL);
        assertEquals(
                "Hello test1 !",
                task1.getResult());
    }

    public void test_notified_task() throws Exception {
        TaskContainer container = new TaskContainerImpl(null);
        container.add(new TaskForTesting(true));
        container.run();
        Task<?,?> task1 = container.waitFor(WaitMode.ALL);
        assertEquals(
                "result",
                task1.getResult());
    }

    public void test_unnotified_task() throws Exception {
        TaskContainer container = new TaskContainerImpl(null);
        container.add(new TaskForTesting(false));
        container.run();
        Task<?,?> task1 = container.waitFor(WaitMode.ALL);
        assertEquals(
                "result",
                task1.getResult());
    }
}
