package fr.in2p3.jsaga.adaptor.batchssh.job

import org.ogf.saga.error.NoSuccessException
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus
import fr.in2p3.jsaga.adaptor.job.SubState

class BatchSSHJobStatus(jobId: String, state: Object, retCode: Int) extends JobStatus(jobId, state, state.toString) {

    def getModel = "pbs-ssh"
    
    def getSubState = {
      m_nativeStateCode match {
        case "C" | "E" =>
          retCode match {
            case 0 => SubState.DONE
            case 153 => SubState.DONE
            case 271 => SubState.CANCELED
            case _ => SubState.FAILED_ERROR
          }
        case "H" => SubState.SUSPENDED_ACTIVE
        case "Q" => SubState.RUNNING_QUEUED
        case "S" | "W" => SubState.SUSPENDED_QUEUED
        case "R" => SubState.RUNNING_ACTIVE
        case _ => throw new NoSuccessException("Unreconized state "+ m_nativeStateCode)
      }

    }
}
