package org.glite.ce.creamapi.jobmanagement.cmdexecutor.policytask;

import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.glite.ce.commonj.certificate.ProxyCertificate;
import org.glite.ce.commonj.certificate.ProxyCertificateStorageInterface;
import org.glite.ce.commonj.certificate.ProxyCertificate.ProxyCertificateType;
import org.glite.ce.commonj.certificate.db.ProxyCertificateDBManager;
import org.glite.ce.creamapi.cmdmanagement.Command;
import org.glite.ce.creamapi.cmdmanagement.CommandException;
import org.glite.ce.creamapi.cmdmanagement.CommandExecutor;
import org.glite.ce.creamapi.cmdmanagement.Policy;
import org.glite.ce.creamapi.cmdmanagement.PolicyException;
import org.glite.ce.creamapi.cmdmanagement.PolicyTask;
import org.glite.ce.creamapi.jobmanagement.JobCommandConstant;
import org.glite.ce.creamapi.jobmanagement.JobStatus;
import org.glite.ce.creamapi.jobmanagement.cmdexecutor.AbstractJobExecutor;

public final class JobCancelTask extends PolicyTask {
    private static final Logger logger = Logger.getLogger(JobCancelTask.class.getName());
    public static final String name = "JOB_CANCEL_TASK";
    
    public JobCancelTask(CommandExecutor executor) {
        super(name, executor);
    }

    public void execute(Policy policy) throws PolicyException {
        if (policy == null || policy.getValue() == null) {
            throw new PolicyException("policy not specified!");
        }

        AbstractJobExecutor executor = (AbstractJobExecutor) getCommandExecutor();

        if (executor == null) {
            throw new PolicyException("executor not found!");
        }
        
        int minutes = 0;

        try {
            minutes = Integer.parseInt(policy.getValue());
        } catch (NumberFormatException e) {
            throw new PolicyException("illegal number format: " + policy.getValue());
        }

        ProxyCertificateStorageInterface proxyStorage = ProxyCertificateDBManager.getInstance();

        try {
            Calendar now = Calendar.getInstance();
            List<ProxyCertificate> proxyList = proxyStorage.getProxyCertificateList(null, null, ProxyCertificateType.DELEGATION);
            int[] cancelCompatibleStatus = new int[] { JobStatus.IDLE, JobStatus.HELD, JobStatus.REALLY_RUNNING, JobStatus.RUNNING };

            for (ProxyCertificate proxy : proxyList) {
                if (proxy.getExpirationTime().after(now)) {
                    long diffTime = (proxy.getExpirationTime().getTimeInMillis() - now.getTimeInMillis()) / 60000;

                    if (diffTime <= minutes) {
                        List<String> jobIdList = executor.getJobDB().retrieveJobId(proxy.getId(), cancelCompatibleStatus, null);
                        
                        logger.info("the delegation proxy \"" + proxy.getId() + "\" is expiring [expiration time = " + proxy.getExpirationTime().getTime() + "]: cancelling " + jobIdList.size() + " jobs");
                        
                        for (String jobId : jobIdList) {
                            Command cancelCmd = new Command(JobCommandConstant.cmdName[JobCommandConstant.JOB_CANCEL], JobCommandConstant.JOB_MANAGEMENT);

                            cancelCmd.setUserId("ADMIN");
                            cancelCmd.setDescription("command executed by CREAM's job cancel task");
                            cancelCmd.setAsynchronous(true);
                            cancelCmd.addParameter("JOB_ID", jobId);
                            cancelCmd.addParameter("IS_ADMIN", "true");
                            cancelCmd.setPriorityLevel(Command.HIGH_PRIORITY);
                            cancelCmd.setCommandGroupId(jobId);

                            try {
                                executor.getCommandManager().insertCommand(cancelCmd);
                            } catch (CommandException e) {
                                logger.error(e.getMessage());
                            }
                        }
                    }
                } else {
                    proxyStorage.deleteProxyCertificate(proxy.getId(), proxy.getDN(), proxy.getFQAN());
                    logger.info("deleted expired delegation proxy \"" + proxy.getId() + "\" [expiration time = " + proxy.getExpirationTime().getTime() + "]");                    
                }
            }
        } catch (Throwable e) {
            throw new PolicyException(e.getMessage());
        }
    }
}
