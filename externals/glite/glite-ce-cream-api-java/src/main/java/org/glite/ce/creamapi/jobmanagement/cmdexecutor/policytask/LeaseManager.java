package org.glite.ce.creamapi.jobmanagement.cmdexecutor.policytask;

import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.glite.ce.common.db.DatabaseException;
import org.glite.ce.creamapi.cmdmanagement.Command;
import org.glite.ce.creamapi.cmdmanagement.CommandException;
import org.glite.ce.creamapi.cmdmanagement.CommandExecutor;
import org.glite.ce.creamapi.cmdmanagement.Policy;
import org.glite.ce.creamapi.cmdmanagement.PolicyException;
import org.glite.ce.creamapi.cmdmanagement.PolicyTask;
import org.glite.ce.creamapi.jobmanagement.Job;
import org.glite.ce.creamapi.jobmanagement.JobCommand;
import org.glite.ce.creamapi.jobmanagement.JobCommandConstant;
import org.glite.ce.creamapi.jobmanagement.JobStatus;
import org.glite.ce.creamapi.jobmanagement.Lease;
import org.glite.ce.creamapi.jobmanagement.cmdexecutor.AbstractJobExecutor;

public final class LeaseManager extends PolicyTask {
    private static final Logger logger = Logger.getLogger(LeaseManager.class);
    private static final Object mutex = new Object();
    private static final int[] status = new int[] { JobStatus.HELD, JobStatus.IDLE, JobStatus.PENDING, JobStatus.RUNNING, JobStatus.REALLY_RUNNING };
    public static final String name = "LEASE_MANAGER";
    
    private int maxLeaseTime = 36000; //minutes
    private boolean isLeaseManagerConfigured = true;
    private boolean isLeaseManagerWorking = true;

    public LeaseManager(CommandExecutor executor) {
        super(name, executor);
    }

    public boolean isLeaseManagerConfigured() {
        return isLeaseManagerConfigured;
    }

    public boolean isLeaseManagerWorking() {
        return isLeaseManagerWorking;
    }

    public Calendar getBoundedLeaseTime(Calendar leaseTime) throws CommandException, IllegalArgumentException {
        if (leaseTime == null) {
            throw new IllegalArgumentException("lease time not specified!");
        }

        Calendar now = Calendar.getInstance();

        if (leaseTime.before(now)) {
            throw new CommandException("lease time (" + leaseTime.getTime() + ") expired!");
        }

        Long nowInMillis = now.getTimeInMillis();
        Long leaseInMills = leaseTime.getTimeInMillis();
        Long diffTime = (leaseInMills - nowInMillis) / 1000; // diff in sec

        now.add(Calendar.SECOND, (int) ((diffTime < 0 || diffTime > maxLeaseTime) ? maxLeaseTime : diffTime));

        // int minutes = now.get(Calendar.MINUTE);
        // now.set(Calendar.MINUTE, (minutes < 10) ? 10 : (minutes - (minutes %
        // 10) + 10));
        // now.set(Calendar.SECOND, 0);
        // now.set(Calendar.MILLISECOND, 0);

        return now;
    }

    public void deleteLease(String leaseId, String userId) throws CommandException, IllegalArgumentException {
        logger.debug("Begin deleteLease");
        if (userId == null) {
            throw new IllegalArgumentException("userId not specified!");
        }
        if (leaseId == null) {
            throw new IllegalArgumentException("leaseId not specified!");
        }

        logger.debug("First synchronized (mutex)");
        synchronized (mutex) {
            try {
                AbstractJobExecutor executor = (AbstractJobExecutor) getCommandExecutor();
                executor.getJobDB().deleteJobLease(leaseId, userId);       
                
                logger.info("lease deleted: leaseId = " + leaseId + " userId = " + userId);
                
            } catch (DatabaseException e) {
                logger.error("Problem to delete the lease [leaseId = " + leaseId + "] [userId = " + userId + "]: " + e.getMessage());
                
                throw new CommandException(e.getMessage());
            }
        }
        logger.debug("After synchronized (mutex)");
        logger.debug("End deleteLease");
    }

    public Calendar setLease(Lease lease) throws CommandException, IllegalArgumentException {
        logger.debug("Begin setLease");
        Calendar boundedLeaseTime = null;
        if (lease == null) {
            logger.error("jobLease not specified!");
            throw new IllegalArgumentException("jobLease not specified!");
        }
        if (lease.getUserId() == null) {
            logger.error("userId not specified!");
            throw new IllegalArgumentException("userId not specified!");
        }
        if (lease.getLeaseId() == null) {
            logger.error("leaseId not specified!");
            throw new IllegalArgumentException("leaseId not specified!");
        }
        if (lease.getLeaseTime() == null) {
            logger.error("leaseTime not specified!");
            throw new IllegalArgumentException("leaseTime not specified!");
        }

        boundedLeaseTime = getBoundedLeaseTime(lease.getLeaseTime());
        lease.setLeaseTime(boundedLeaseTime);
        logger.debug("First synchronized (mutex)");
        
        synchronized (mutex) {
            AbstractJobExecutor executor = (AbstractJobExecutor) getCommandExecutor();
            try {
                executor.getJobDB().insertJobLease(lease);
                logger.info("lease created: leaseId = " + lease.getLeaseId() + " leaseTime = " + lease.getLeaseTime().getTime() + " userId = " + lease.getUserId());
            } catch (DatabaseException de) {
                logger.debug("insertJobLease is failed. Now attempting to update ...");
                try {
                    executor.getJobDB().updateJobLease(lease);
                    logger.info("lease updated: leaseId = " + lease.getLeaseId() + " leaseTime = " + lease.getLeaseTime().getTime() + " userId = " + lease.getUserId());
                } catch (DatabaseException de2) {
                    logger.error("Problem to update/insert the lease [leaseId = " + lease.getLeaseId() + "] [leaseTime = " + lease.getLeaseTime().getTime() + "] [userId = " + lease.getUserId() + "]: " + de2.getMessage());
                    throw new CommandException("Problem to update/insert jobLease. " + de2.getMessage());
                }
            }
        }
        
        logger.debug("After synchronized (mutex)");
        logger.debug("End setLease");
        return boundedLeaseTime;
    }

    public void setJobLeaseId(String jobId, String leaseId, String userId) throws IllegalArgumentException, CommandException {
        logger.debug("Begin setJobLeaseId. jobId = " + jobId + " leaseId = " + leaseId + " userId = " + userId);
        if (jobId == null) {
            throw new IllegalArgumentException("jobId not specified!");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId not specified!");
        }

        logger.debug("First synchronized (mutex)");
        synchronized (mutex) {
            try {
                AbstractJobExecutor executor = (AbstractJobExecutor) getCommandExecutor();
                executor.getJobDB().setLeaseId(leaseId, jobId, userId);
            } catch (DatabaseException e) {
                throw new CommandException(e.getMessage());
            }
        }
        logger.debug("After synchronized (mutex)");
        logger.info("LeaseId field has been set for  jobId = " + jobId + " leaseId = " + leaseId + " userId = " + userId);
        logger.debug("End setJobLeaseId. jobId = " + jobId + " leaseId = " + leaseId + " userId = " + userId);
    }

    private int checkCommand(List<JobCommand> commandList) {
        if (commandList == null) {
            return 0;
        }
        int count = 1;
        
        for (JobCommand jobCmd : commandList) {
            if (jobCmd != null && jobCmd.getType() == JobCommandConstant.JOB_CANCEL && jobCmd.getDescription() != null && jobCmd.getDescription().indexOf("Lease") > 0) {
                logger.debug("jobCmd.getExecutionCompletedTime() == null ? " + (jobCmd.getExecutionCompletedTime() == null));
                if (jobCmd.getExecutionCompletedTime() == null) {
                    return -1;
                }
                count++;
            }
        }
        return count;
    }

    private boolean jobCancelByLeaseManager(Job job, Calendar now) {
        boolean ok = false;
        int count = checkCommand(job.getCommandHistory());
        
        if ((count != -1) && (count <= 3)) {
            Command cancelCmd = new Command(JobCommandConstant.cmdName[JobCommandConstant.JOB_CANCEL], JobCommandConstant.JOB_MANAGEMENT);

            cancelCmd.setUserId("ADMIN");
            cancelCmd.setDescription("Job cancelled by Lease Manager! (try " + count + "/3)!");
            cancelCmd.setAsynchronous(true);
            cancelCmd.addParameter("JOB_ID", job.getId());
            cancelCmd.addParameter("IS_ADMIN", "true");
            cancelCmd.setPriorityLevel(Command.HIGH_PRIORITY);
            cancelCmd.setCommandGroupId(job.getId());

            try {
                AbstractJobExecutor executor = (AbstractJobExecutor) getCommandExecutor();
                executor.getCommandManager().insertCommand(cancelCmd);
                ok = true;
            } catch (CommandException e) {
                logger.error(e.getMessage());
            }
        } else {
            ok = true;
        }
        return ok;
    }

    public void execute(Policy policy) throws PolicyException {
        if(policy == null || policy.getValue() == null) {
            throw new PolicyException("policy not specified!");
        }
        
        AbstractJobExecutor executor = (AbstractJobExecutor) getCommandExecutor();

        if (executor == null) {
            isLeaseManagerConfigured = false;
            throw new PolicyException("executor not specified!");
        }

        if (executor.getJobDB() == null) {
            isLeaseManagerConfigured = false;
            throw new PolicyException("jobDb reference not specified!");
        }

        try {
            maxLeaseTime = Integer.parseInt(policy.getValue());
        } catch(NumberFormatException e) {
            logger.warn("illegal number format: " + policy.getValue() + " using the default value: " + maxLeaseTime + " minutes");
        }

        isLeaseManagerConfigured = false;
        isLeaseManagerWorking  = true;
        Calendar now = Calendar.getInstance();

        logger.debug("First synchronized (mutex)");
        
        synchronized (mutex) {
            try {
                // retrieving lease obj expired.
                List<Lease> leaseList = executor.getJobDB().retrieveJobLease(now, null);

                for (Lease lease : leaseList) {
                    try {
                        executor.getJobDB().setLeaseExpired(lease);
                        deleteLease(lease.getLeaseId(), lease.getUserId());
                    } catch (Exception e) {
                        throw new PolicyException("setLeaseExpired / delete job lease is failed! " + e.getMessage());
                    }
                } // for
            } catch (DatabaseException e) {
                logger.error("retrieveJobLease for leaseId expired is failed! " + e.getMessage());
            }
        } // synchronized

        logger.debug("First synchronized (mutex)");

        // cancelling job expired.
        try {
            List<String> jobIdListToCancel = executor.getJobDB().retrieveJobIdLeaseTimeExpired(status, null, null);

            Job job = null;

            for (String jobIdList : jobIdListToCancel) {
                logger.debug("jobIdListToCancel = " + jobIdList);
                
                try {
                    job = executor.getJobDB().retrieveJob(jobIdList, null);
                    
                    if (jobCancelByLeaseManager(job, now)) {
                        logger.info("Job has been cancelled. jobId = " + job.getId());
                    } else {
                        logger.warn("Problem to cancel job by LeaseManager. jobId = " + job.getId());
                    }
                } catch (Throwable e) {
                    logger.error(e.getMessage());
                    continue;
                }
            }
        } catch (DatabaseException de) {
            throw new PolicyException("Retrieving jobs expired by LeaseManager is failed! " + de.getMessage());
        }

        isLeaseManagerWorking = false;
    }
}
