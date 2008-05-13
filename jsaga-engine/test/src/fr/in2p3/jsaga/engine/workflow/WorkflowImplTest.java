package fr.in2p3.jsaga.engine.workflow;

import fr.in2p3.jsaga.engine.workflow.task.DummyTask;
import fr.in2p3.jsaga.workflow.*;
import junit.framework.TestCase;
import org.ogf.saga.error.*;
import org.ogf.saga.task.State;
import org.ogf.saga.task.WaitMode;

import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WorkflowImplTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class WorkflowImplTest extends TestCase {
    public void test_workflow() throws Exception {
        Workflow workflow = WorkflowFactory.createWorkflow();
        WorkflowTask endTask = new TestTask("endTask");         workflow.add(endTask, null, null);
        WorkflowTask task1 = new TestTask("task1");             workflow.add(task1, null, endTask.getName());
        WorkflowTask task2 = new TestTask("task2");             workflow.add(task2, null, endTask.getName());
        WorkflowTask startTask1 = new TestTask("startTask1");   workflow.add(startTask1, "start", task1.getName());
        workflow.run();
        Thread.sleep(100);
        assertEquals(State.DONE, task1.getState());
        assertEquals(State.NEW, task2.getState());
        assertEquals(State.NEW, endTask.getState());

        WorkflowTask startTask2 = new TestTask("startTask2");   workflow.add(startTask2, "start", task2.getName());
        workflow.add(endTask);  // waitFor() requires to add the final tasks to the task container
        workflow.waitFor(WaitMode.ALL);
        assertEquals(State.DONE, task2.getState());
        assertEquals(State.DONE, endTask.getState());
    }

    class TestTask extends DummyTask {
        public TestTask(String name) throws NotImplemented, BadParameter, Timeout, NoSuccess {
            super(name);
        }
    }
}
