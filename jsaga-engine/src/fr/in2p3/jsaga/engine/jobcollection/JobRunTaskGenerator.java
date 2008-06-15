package fr.in2p3.jsaga.engine.jobcollection;

import fr.in2p3.jsaga.engine.workflow.StartTask;
import fr.in2p3.jsaga.engine.workflow.task.JobRunTask;
import fr.in2p3.jsaga.engine.workflow.task.StagedTask;
import fr.in2p3.jsaga.impl.job.instance.JobHandle;
import fr.in2p3.jsaga.workflow.Workflow;
import org.ggf.schemas.jsdl.*;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobRunTaskGenerator
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   7 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobRunTaskGenerator {
    private String m_jobName;
    private JobDescription m_jobDesc;
    private JobRunTask m_jobRunTask;

    public JobRunTaskGenerator(String jobName, JobDefinition jobDesc, JobHandle jobHandle) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        m_jobName = jobName;
        m_jobDesc = jobDesc.getJobDescription();
        m_jobRunTask = new JobRunTask(m_jobName, jobHandle);
    }

    public void updateWorkflow(Workflow workflow) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        boolean hasStagedTask = false;
        workflow.add(m_jobRunTask, null, null);
        for (int i=0; i<m_jobDesc.getDataStagingCount(); i++) {
            DataStaging dataStaging = m_jobDesc.getDataStaging(i);
            if (dataStaging.getSource()!=null && dataStaging.getSource().getURI()!=null) {
                StagedTask inputTask = new StagedTask(m_jobName, dataStaging.getName(), true);
                workflow.add(inputTask, null, m_jobRunTask.getName());
                hasStagedTask = true;
            }
            if (dataStaging.getTarget()!=null && dataStaging.getTarget().getURI()!=null) {
                StagedTask outputTask = new StagedTask(m_jobName, dataStaging.getName(), true);
                workflow.add(outputTask, null, m_jobRunTask.getName());
                hasStagedTask = true;
            }
        }
        if (! hasStagedTask) {
            // connect jobRunTask to startTask
            workflow.add(m_jobRunTask, StartTask.name(), null);
        }
    }

    public JobRunTask getTask() {
        return m_jobRunTask;
    }
}
