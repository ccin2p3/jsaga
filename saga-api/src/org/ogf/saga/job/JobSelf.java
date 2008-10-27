package org.ogf.saga.job;

import org.ogf.saga.monitoring.AsyncSteerable;

/**
 * A JobSelf is a Job that represents the current application, and is steerable.
 */
public interface JobSelf extends Job, AsyncSteerable<JobSelf> {
}
