package fr.in2p3.jsaga.impl.job.instance;

import fr.in2p3.jsaga.impl.attributes.AttributeImpl;
import fr.in2p3.jsaga.impl.monitoring.MetricMode;
import fr.in2p3.jsaga.impl.monitoring.MetricType;
import org.ogf.saga.job.Job;

import java.util.Date;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobAttributes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobAttributes implements Cloneable {
    AttributeImpl<String> m_JobId;
    AttributeImpl<Date> m_Created;
    AttributeImpl<Date> m_Started;
    AttributeImpl<Date> m_Finished;
    /** deviation from SAGA specification */
    AttributeImpl<String> m_NativeJobDescription;

    /** constructor */
    JobAttributes(JobImpl job) {
        m_JobId = job._addAttribute(new AttributeImpl<String>(
                Job.JOBID,
                "SAGA representation of the job identifier",
                MetricMode.ReadOnly,
                MetricType.String,
                null));
        m_Created = job._addAttribute(new AttributeImpl<Date>(
                Job.CREATED,
                "time stamp of the job creation in the resource manager",
                MetricMode.ReadOnly,
                MetricType.Time,
                new Date(System.currentTimeMillis())));
        m_Started = job._addAttribute(new AttributeImpl<Date>(
                Job.STARTED,
                "time stamp indicating when the job started running",
                MetricMode.ReadOnly,
                MetricType.Time,
                null));
        m_Finished = job._addAttribute(new AttributeImpl<Date>(
                Job.FINISHED,
                "time stamp indicating when the job completed",
                MetricMode.ReadOnly,
                MetricType.Time,
                null));
        m_NativeJobDescription = job._addAttribute(new AttributeImpl<String>(
                JobImpl.NATIVEJOBDESCRIPTION,
                "job description understood by the job service (deviation from SAGA specification)",
                MetricMode.ReadOnly,
                MetricType.String,
                null));
    }

    /** clone */
    public JobAttributes clone() throws CloneNotSupportedException {
        JobAttributes clone = (JobAttributes) super.clone();
        clone.m_JobId = m_JobId.clone();
        clone.m_Created = m_Created.clone();
        clone.m_Started = m_Started.clone();
        clone.m_Finished = m_Finished.clone();
        clone.m_NativeJobDescription = m_NativeJobDescription.clone();
        return clone;
    }
}
