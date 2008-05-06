package fr.in2p3.jsaga.impl.job.instance;

import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.impl.monitoring.*;
import fr.in2p3.jsaga.impl.task.TaskStateMetricImpl;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.job.Job;
import org.ogf.saga.task.State;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobMetrics
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobMetrics {
    TaskStateMetricImpl m_State;
    MetricImpl<String> m_StateDetail;
    /** deviation from SAGA specification */
    MetricImpl<String> m_SubState;

    /** constructor */
    JobMetrics(JobImpl job) throws NoSuccess {
        m_State = (TaskStateMetricImpl) job._addMetric(new TaskStateMetricImpl(
                job,
                Job.JOB_STATE,
                "fires on state changes of the job, and has the literal value of the job state enum.",
                MetricMode.ReadOnly,
                "1",
                MetricType.Enum,
                State.NEW));
        m_StateDetail = job._addMetric(new MetricImpl<String>(
                job,
                Job.JOB_STATEDETAIL,
                "fires as a job changes its state detail",
                MetricMode.ReadOnly,
                "1",
                MetricType.String,
                "Unknown:Unknown"));
        m_SubState = job._addMetric(new MetricImpl<String>(
                job,
                JobImpl.JOB_SUBSTATE,
                "fires on sub-state changes of the job (deviation from SAGA specification)",
                MetricMode.ReadOnly,
                "1",
                MetricType.String,
                SubState.SUBMITTED.toString()));
    }

    /** clone */
    public JobMetrics clone() throws CloneNotSupportedException {
        JobMetrics clone = (JobMetrics) super.clone();
        clone.m_State = m_State.clone();
        clone.m_StateDetail = m_StateDetail.clone();
        clone.m_SubState = m_SubState.clone();
        return clone;
    }
}
