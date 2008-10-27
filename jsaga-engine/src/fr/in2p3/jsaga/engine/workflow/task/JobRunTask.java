package fr.in2p3.jsaga.engine.workflow.task;

import fr.in2p3.jsaga.engine.schema.status.Task;
import fr.in2p3.jsaga.engine.schema.status.types.TaskTypeType;
import fr.in2p3.jsaga.engine.workflow.AbstractWorkflowTaskImpl;
import fr.in2p3.jsaga.impl.job.instance.JobHandle;
import org.ogf.saga.error.*;
import org.ogf.saga.task.State;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobRunTask
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   29 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobRunTask extends AbstractWorkflowTaskImpl {
    private JobHandle m_jobHandle;

    /** constructor */
    public JobRunTask(String jobName, JobHandle jobHandle) throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        super(null, name(jobName));
        m_jobHandle = jobHandle;
        // update XML status
        Task xmlStatus = super.getStateAsXML();
        xmlStatus.setType(TaskTypeType.JOB);
        xmlStatus.setGroup(name(jobName));
        xmlStatus.setLabel(jobName);
    }

    //////////////////////////////////////////// abstract methods ////////////////////////////////////////////

    protected void doSubmit() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_jobHandle.run();
    }

    protected void doCancel() {
        try {
            m_jobHandle.cancel();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected State queryState() throws NotImplementedException, TimeoutException, NoSuccessException {
        return m_jobHandle.getState();
    }

    public boolean startListening() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not supported");
    }

    public void stopListening() throws NotImplementedException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not supported");
    }

    public static String name(String jobName) {
        return "run_"+jobName;
    }
}
