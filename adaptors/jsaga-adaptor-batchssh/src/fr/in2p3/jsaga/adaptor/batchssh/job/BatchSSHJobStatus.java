package fr.in2p3.jsaga.adaptor.batchssh.job;

import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

/* ***************************************************
* File:   BatchSSHJobStatus
* Author: Yassine BACHAR & Taha BENYEZZA
* Date:   12 Fevrier 2011
* ***************************************************/

public class BatchSSHJobStatus extends JobStatus {
   
    public BatchSSHJobStatus(String jobId, SubState status) {
        super(jobId,status, status.toString());
    }

    public String getModel() {
        return "pbs+ssh";
    }

    public SubState getSubState() {
            return (SubState) m_nativeStateCode;
    }

}
