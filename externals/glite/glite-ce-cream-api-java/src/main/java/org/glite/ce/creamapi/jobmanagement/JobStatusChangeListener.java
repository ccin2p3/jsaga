package org.glite.ce.creamapi.jobmanagement;

public interface JobStatusChangeListener {

   public void doOnJobStatusChanged(JobStatus status) throws IllegalArgumentException, JobManagementException;
}
