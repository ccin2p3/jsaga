package fr.in2p3.jsaga.engine.jobcollection;

import fr.in2p3.jsaga.engine.workflow.task.*;
import fr.in2p3.jsaga.workflow.Workflow;
import org.ggf.schemas.jsdl.*;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobEndTaskGenerator
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   7 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobEndTaskGenerator {
    private String m_jobName;
    private JobDescription m_jobDesc;
    private JobEndTask m_jobEndTask;

    public JobEndTaskGenerator(String jobName, JobDefinition jobDesc) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        m_jobName = jobName;
        m_jobDesc = jobDesc.getJobDescription();
        m_jobEndTask = new JobEndTask(m_jobName);
    }

    public void updateWorkflow(Workflow workflow) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        if (m_jobDesc.getDataStagingCount() > 0) {
            workflow.add(m_jobEndTask, null, null);
            for (int i=0; i<m_jobDesc.getDataStagingCount(); i++) {
                DataStaging dataStaging = m_jobDesc.getDataStaging(i);
                if (dataStaging.getTarget()!=null && dataStaging.getTarget().getURI()!=null) {
                    StagedTask outputTask = new StagedTask(m_jobName, dataStaging.getName(), false);
                    workflow.add(outputTask, null, m_jobEndTask.getName());
                }
            }
        } else {
            workflow.add(m_jobEndTask, JobRunTask.name(m_jobName), null);
        }
    }

    public DummyTask getTask() {
        return m_jobEndTask;
    }
}
