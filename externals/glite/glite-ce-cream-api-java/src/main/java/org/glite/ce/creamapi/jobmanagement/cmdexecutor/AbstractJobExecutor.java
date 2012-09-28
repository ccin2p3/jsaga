package org.glite.ce.creamapi.jobmanagement.cmdexecutor;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.glite.ce.common.db.DatabaseException;
import org.glite.ce.commonj.certificate.ProxyCertificate;
import org.glite.ce.commonj.certificate.ProxyCertificateException;
import org.glite.ce.commonj.certificate.ProxyCertificateStorageInterface;
import org.glite.ce.commonj.certificate.ProxyCertificate.ProxyCertificateType;
import org.glite.ce.commonj.certificate.db.ProxyCertificateDBManager;
import org.glite.ce.commonj.utils.CEUtils;
import org.glite.ce.creamapi.cmdmanagement.AbstractCommandExecutor;
import org.glite.ce.creamapi.cmdmanagement.Command;
import org.glite.ce.creamapi.cmdmanagement.CommandException;
import org.glite.ce.creamapi.cmdmanagement.CommandExecutorException;
import org.glite.ce.creamapi.cmdmanagement.CommandResult;
import org.glite.ce.creamapi.cmdmanagement.Policy;
import org.glite.ce.creamapi.cmdmanagement.PolicyManagerException;
import org.glite.ce.creamapi.jobmanagement.Job;
import org.glite.ce.creamapi.jobmanagement.JobCommand;
import org.glite.ce.creamapi.jobmanagement.JobCommandConstant;
import org.glite.ce.creamapi.jobmanagement.JobEnumeration;
import org.glite.ce.creamapi.jobmanagement.JobManagementException;
import org.glite.ce.creamapi.jobmanagement.JobStatus;
import org.glite.ce.creamapi.jobmanagement.JobStatusChangeListener;
import org.glite.ce.creamapi.jobmanagement.JobWrapper;
import org.glite.ce.creamapi.jobmanagement.Lease;
import org.glite.ce.creamapi.jobmanagement.cmdexecutor.command.JobIdFilterFailure;
import org.glite.ce.creamapi.jobmanagement.cmdexecutor.command.JobIdFilterResult;
import org.glite.ce.creamapi.jobmanagement.cmdexecutor.policytask.GetJobSTDTask;
import org.glite.ce.creamapi.jobmanagement.cmdexecutor.policytask.JobCancelTask;
import org.glite.ce.creamapi.jobmanagement.cmdexecutor.policytask.JobPurgeTask;
import org.glite.ce.creamapi.jobmanagement.cmdexecutor.policytask.JobSubmissionTask;
import org.glite.ce.creamapi.jobmanagement.cmdexecutor.policytask.LeaseManager;
import org.glite.ce.creamapi.jobmanagement.db.JobDBInterface;
import org.glite.ce.creamapi.jobmanagement.jdl.JobFactory;

public abstract class AbstractJobExecutor extends AbstractCommandExecutor implements JobStatusChangeListener {
    private final static Logger logger = Logger.getLogger(AbstractJobExecutor.class.getName());
    /** Labels for JobWrapper */
    public final static String JOB_WRAPPER_DELEGATION_TIME_SLOT = "JOB_WRAPPER_DELEGATION_TIME_SLOT";
    public final static String JOB_WRAPPER_COPY_PROXY_MIN_RETRY_WAIT = "JOB_WRAPPER_COPY_PROXY_MIN_RETRY_WAIT";
    public final static String DELEGATION_PROXY_CERT_SANDBOX_URI = "DELEGATION_PROXY_CERT_SANDBOX_URI";
    /** Label for Cream job sensor host */
    public final static String CREAM_JOB_SENSOR_HOST = "CREAM_JOB_SENSOR_HOST";

    /** Label for Cream job sensor port */
    public final static String CREAM_JOB_SENSOR_PORT = "CREAM_JOB_SENSOR_PORT";

    private JobDBInterface jobDB;
    private LeaseManager leaseManager;
    private Socket socket = null;
    private ObjectOutputStream oos = null;
    private boolean acceptNewJobs = true;

    protected AbstractJobExecutor(String name) {
        super(name, JobCommandConstant.JOB_MANAGEMENT);
        setCommands(JobCommandConstant.cmdName);
    }

    public void initExecutor() throws CommandException {
        String proxyPath = getParameterValueAsString("CREAM_USER_PROXY_PATH");
        if (proxyPath == null) {
            throw new CommandException("parameter \"CREAM_USER_PROXY_PATH\" not defined!");
        }

        String sandboxPath = getParameterValueAsString("CREAM_USER_SANDBOX_PATH");
        if (sandboxPath == null) {
            throw new CommandException("parameter \"CREAM_USER_SANDBOX_PATH\" not defined!");
        }

        Process proc = null;

        try {
            CEUtils.makeDir(proxyPath);

            Runtime runtime = Runtime.getRuntime();
            proc = runtime.exec("chmod 700 " + proxyPath);
            proc.waitFor();
            proc.getInputStream().close();
            proc.getErrorStream().close();
            proc.getOutputStream().close();

            CEUtils.makeDir(sandboxPath);

            proc = runtime.exec("chmod 775 " + sandboxPath);
            proc.waitFor();
            proc.getInputStream().close();
            proc.getErrorStream().close();
            proc.getOutputStream().close();

            proc = runtime.exec("groups");

            InputStreamReader isr = new InputStreamReader(proc.getInputStream());
            BufferedReader in = new BufferedReader(isr);
            String inputLine = "";
            String groups = "";

            while ((inputLine = in.readLine()) != null) {
                groups += inputLine + " ";
            }

            in.close();

            proc.waitFor();
            proc.getInputStream().close();
            proc.getErrorStream().close();
            proc.getOutputStream().close();

            String[] groupArray = groups.split(" ");

            for (int i = 0; i < groupArray.length; i++) {
                String subDirPath = sandboxPath + "/" + groupArray[i];

                File subDir = new File(subDirPath);
                if (!subDir.exists()) {
                    subDir.mkdir();

                    proc = runtime.exec("chmod 770 " + subDirPath);
                    proc.waitFor();
                    proc.getInputStream().close();
                    proc.getErrorStream().close();
                    proc.getOutputStream().close();

                    proc = runtime.exec("chgrp " + groupArray[i] + " " + subDirPath);
                    proc.waitFor();
                    proc.getInputStream().close();
                    proc.getErrorStream().close();
                    proc.getOutputStream().close();
                }
            }
        } catch (Exception e) {
            throw new CommandException(e.getMessage());
        } finally {
            if (proc != null) {
                try {
                    proc.getInputStream().close();
                    proc.getErrorStream().close();
                    proc.getOutputStream().close();
                } catch (IOException e) {
                }
            }
        }
    }

    public synchronized boolean doesAcceptNewJobs() {
        return acceptNewJobs;
    }

    public synchronized void setAcceptNewJobs(boolean acceptNewJobs) {
        this.acceptNewJobs = acceptNewJobs;
    }

    public void destroy() {
        logger.info("destroy invoked!");

        super.destroy();

        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e1) {
            }
        }
        socket = null;

        if (oos != null) {
            try {
                oos.close();
            } catch (IOException e1) {
            }
        }

        logger.info("destroyed!");
    }

    public Job retrieveJob(String jobId) throws CommandException {
        logger.debug("Begin retrieveJob for jobId = " + jobId);
        Job job = null;
        try {
            job = jobDB.retrieveJob(jobId, null);
        } catch (Exception e) {
            throw new CommandException(e.getMessage());
        }
        logger.debug("End retrieveJob for jobId = " + jobId);
        return job;
    }

    public void updateJob(Job job) throws CommandException {
        try {
            jobDB.update(job);
        } catch (Exception e) {
            throw new CommandException(e.getMessage());
        }
    }

    private JobEnumeration getJobList(final Command cmd) throws IllegalArgumentException, CommandException {
        logger.debug("Begin getJobList");
        if (cmd == null) {
            throw new IllegalArgumentException("cmd not defined!");
        }

        List<Integer> compatibleStatusList = new ArrayList<Integer>(0);

        int cmdType = getCommandType(cmd.getName());

        switch (cmdType) {
        case JobCommandConstant.JOB_START:
            compatibleStatusList.add(JobStatus.REGISTERED);
            break;
        case JobCommandConstant.JOB_CANCEL:
            compatibleStatusList.add(JobStatus.IDLE);
            compatibleStatusList.add(JobStatus.HELD);
            compatibleStatusList.add(JobStatus.REALLY_RUNNING);
            compatibleStatusList.add(JobStatus.RUNNING);
            break;
        case JobCommandConstant.JOB_SET_LEASEID:
            compatibleStatusList.add(JobStatus.REGISTERED);
            compatibleStatusList.add(JobStatus.PENDING);
            compatibleStatusList.add(JobStatus.IDLE);
            compatibleStatusList.add(JobStatus.HELD);
            compatibleStatusList.add(JobStatus.REALLY_RUNNING);
            compatibleStatusList.add(JobStatus.RUNNING);
            break;
        case JobCommandConstant.JOB_PURGE:
            compatibleStatusList.add(JobStatus.ABORTED);
            compatibleStatusList.add(JobStatus.REGISTERED);
            compatibleStatusList.add(JobStatus.CANCELLED);
            compatibleStatusList.add(JobStatus.DONE_OK);
            compatibleStatusList.add(JobStatus.DONE_FAILED);
            break;
        case JobCommandConstant.JOB_RESUME:
            compatibleStatusList.add(JobStatus.HELD);
            break;
        case JobCommandConstant.JOB_SUSPEND:
            compatibleStatusList.add(JobStatus.IDLE);
            compatibleStatusList.add(JobStatus.REALLY_RUNNING);
            compatibleStatusList.add(JobStatus.RUNNING);
            break;
        case JobCommandConstant.PROXY_RENEW:
            compatibleStatusList.add(JobStatus.PENDING);
            compatibleStatusList.add(JobStatus.IDLE);
            compatibleStatusList.add(JobStatus.HELD);
            compatibleStatusList.add(JobStatus.REALLY_RUNNING);
            compatibleStatusList.add(JobStatus.RUNNING);
            break;
        default:
            compatibleStatusList.add(JobStatus.REGISTERED);
            compatibleStatusList.add(JobStatus.PENDING);
            compatibleStatusList.add(JobStatus.IDLE);
            compatibleStatusList.add(JobStatus.RUNNING);
            compatibleStatusList.add(JobStatus.REALLY_RUNNING);
            compatibleStatusList.add(JobStatus.CANCELLED);
            compatibleStatusList.add(JobStatus.HELD);
            compatibleStatusList.add(JobStatus.DONE_OK);
            compatibleStatusList.add(JobStatus.DONE_FAILED);
            compatibleStatusList.add(JobStatus.PURGED);
            compatibleStatusList.add(JobStatus.ABORTED);
            break;
        }

        String userId = null;

        if (cmd.getParameterAsString("IS_ADMIN") == null || cmd.getParameterAsString("IS_ADMIN").equalsIgnoreCase("false")) {
            userId = cmd.getUserId();
        }

        // Retrieving jobFilter parameters.
        List<String> jobList = null;
        Calendar fromDate = null;
        Calendar toDate = null;
        Calendar fromStatusDate = null;
        Calendar toStatusDate = null;
        String leaseId = null;
        String delegationId = null;

        // jobId list parameter
        if (cmd.containsParameterKey("JOB_ID")) {
            jobList = new ArrayList<String>(0);
            jobList.add(cmd.getParameterAsString("JOB_ID"));
        } else if (cmd.containsParameterKey("JOB_ID_LIST")) {
            jobList = cmd.getParameterMultivalue("JOB_ID_LIST");
        }

        // fromDate parameter
        fromDate = makeDate(cmd.getParameterAsString("FROM_DATE"));
        // toDate parameter
        toDate = makeDate(cmd.getParameterAsString("TO_DATE"));
        // leaseId parameter
        leaseId = cmd.getParameterAsString("LEASE_ID");
        // delegationId parameter
        delegationId = cmd.getParameterAsString("DELEGATION_PROXY_ID");

        // fromStatusDate parameter
        fromStatusDate = makeDate(cmd.getParameterAsString("FROM_STATUS_DATE"));
        // toStatusDate parameter
        toStatusDate = makeDate(cmd.getParameterAsString("TO_STATUS_DATE"));

        // statusList parameter
        List<String> statusList = cmd.getParameterMultivalue("JOB_STATUS_LIST");

        if (statusList == null) {
            logger.debug("statusList is null!");
        }
        if ((statusList != null) && (statusList.size() == 0)) {
            logger.debug("statusList is empty!");
        }

        List<Integer> compatibleStatusToFindList = new ArrayList<Integer>(0);

        if (statusList != null) {
            for (String status : statusList) {
                for (int x = 0; x < JobStatus.statusName.length; x++) {
                    if (compatibleStatusList.contains(x) && status.equals(JobStatus.getNameByType(x))) {
                        compatibleStatusToFindList.add(x);
                        continue;
                    }
                }
            }
        } else {
            compatibleStatusToFindList = compatibleStatusList;
        }

        if ((compatibleStatusToFindList == null) || (compatibleStatusToFindList.size() == 0)) {
            logger.error("Status specified not compatible for the request command.");
            throw new CommandException("Status specified not compatible for the request command.");
        }

        int[] compatibleStatusToFind = new int[compatibleStatusToFindList.size()];
        for (int i = 0; i < compatibleStatusToFindList.size(); i++) {
            compatibleStatusToFind[i] = compatibleStatusToFindList.get(i);
        }

        List<String> jobIdFound = null;
        List<String> helpJobList = new ArrayList<String>(0);
        List<JobIdFilterResult> jobIdFilterResultList = new ArrayList<JobIdFilterResult>();
        JobIdFilterResult jobIdFilterResult = null;

        try {
            if ((jobList != null) && (jobList.size() > 0)) {
                logger.debug("JobList parameter is not null in jobFilter.");
                // find job not existing.
                helpJobList.addAll(jobList);
                jobIdFound = jobDB.retrieveJobId(jobList, userId);

                helpJobList.removeAll(jobIdFound);

                for (String jobId : helpJobList) {
                    jobIdFilterResult = new JobIdFilterResult();
                    jobIdFilterResult.setJobId(jobId);
                    jobIdFilterResult.setErrorCode(JobIdFilterFailure.JOBID_ERRORCODE);
                    jobIdFilterResult.setFailureReason(JobIdFilterFailure.failureReason[JobIdFilterFailure.JOBID_ERRORCODE]);
                    jobIdFilterResultList.add(jobIdFilterResult);
                    logger.debug("JobId = " + jobId + " FailureReason = " + JobIdFilterFailure.failureReason[JobIdFilterFailure.JOBID_ERRORCODE]);
                }
                helpJobList.clear();
                helpJobList.addAll(jobIdFound);

                // find job by status.
                if (jobIdFound != null && jobIdFound.size() > 0) {
                    jobIdFound = jobDB.retrieveJobId(jobIdFound, userId, compatibleStatusToFind, fromStatusDate, toStatusDate);

                    helpJobList.removeAll(jobIdFound);

                    for (String jobId : helpJobList) {
                        jobIdFilterResult = new JobIdFilterResult();
                        jobIdFilterResult.setJobId(jobId);
                        jobIdFilterResult.setErrorCode(JobIdFilterFailure.STATUS_ERRORCODE);
                        jobIdFilterResult.setFailureReason(JobIdFilterFailure.failureReason[JobIdFilterFailure.STATUS_ERRORCODE]);
                        jobIdFilterResultList.add(jobIdFilterResult);
                        logger.debug("JobId = " + jobId + " FailureReason = " + JobIdFilterFailure.failureReason[JobIdFilterFailure.STATUS_ERRORCODE]);
                    }
                    helpJobList.clear();
                    helpJobList.addAll(jobIdFound);
                }

                // find job by date.
                if ((jobIdFound != null) && jobIdFound.size() > 0 && (fromDate != null || toDate != null)) {
                    jobIdFound = jobDB.retrieveByDate(jobIdFound, userId, fromDate, toDate);

                    helpJobList.removeAll(jobIdFound);

                    for (String jobId : helpJobList) {
                        jobIdFilterResult = new JobIdFilterResult();
                        jobIdFilterResult.setJobId(jobId);
                        jobIdFilterResult.setErrorCode(JobIdFilterFailure.DATE_ERRORCODE);
                        jobIdFilterResult.setFailureReason(JobIdFilterFailure.failureReason[JobIdFilterFailure.DATE_ERRORCODE]);
                        jobIdFilterResultList.add(jobIdFilterResult);
                        logger.debug("JobId = " + jobId + " FailureReason = " + JobIdFilterFailure.failureReason[JobIdFilterFailure.DATE_ERRORCODE]);
                    }
                    helpJobList.clear();
                    helpJobList.addAll(jobIdFound);
                }

                // find job by delegationId.
                if ((jobIdFound != null) && jobIdFound.size() > 0 && (delegationId != null)) {
                    jobIdFound = jobDB.retrieveJobId(jobIdFound, delegationId, null, userId);

                    helpJobList.removeAll(jobIdFound);

                    for (String jobId : helpJobList) {
                        jobIdFilterResult = new JobIdFilterResult();
                        jobIdFilterResult.setJobId(jobId);
                        jobIdFilterResult.setErrorCode(JobIdFilterFailure.DELEGATIONID_ERRORCODE);
                        jobIdFilterResult.setFailureReason(JobIdFilterFailure.failureReason[JobIdFilterFailure.DELEGATIONID_ERRORCODE]);
                        jobIdFilterResultList.add(jobIdFilterResult);
                        logger.debug("JobId = " + jobId + " FailureReason = " + JobIdFilterFailure.failureReason[JobIdFilterFailure.DELEGATIONID_ERRORCODE]);
                    }
                    helpJobList.clear();
                    helpJobList.addAll(jobIdFound);
                }

                // find job by leaseId.
                if ((jobIdFound != null) && jobIdFound.size() > 0 && (leaseId != null)) {
                    jobIdFound = jobDB.retrieveJobId(jobIdFound, null, leaseId, userId);

                    helpJobList.removeAll(jobIdFound);

                    for (String jobId : helpJobList) {
                        jobIdFilterResult = new JobIdFilterResult();
                        jobIdFilterResult.setJobId(jobId);
                        jobIdFilterResult.setErrorCode(JobIdFilterFailure.LEASEID_ERRORCODE);
                        jobIdFilterResult.setFailureReason(JobIdFilterFailure.failureReason[JobIdFilterFailure.LEASEID_ERRORCODE]);
                        jobIdFilterResultList.add(jobIdFilterResult);
                        logger.debug("JobId = " + jobId + " FailureReason = " + JobIdFilterFailure.failureReason[JobIdFilterFailure.LEASEID_ERRORCODE]);
                    }
                }
            } else { // jobList is null or empty.
                logger.debug("JobList parameter is null in jobFilter.");
                /*
                 * jobIdFound = jobDB.retrieveJobId(null, userId,
                 * compatibleStatusToFind, fromStatusDate, toStatusDate);
                 * 
                 * if ((jobIdFound != null) && (jobIdFound.size() > 0) &&
                 * (fromDate != null || toDate != null)) { jobIdFound =
                 * jobDB.retrieveByDate(jobIdFound, userId, fromDate, toDate); }
                 * 
                 * if ((jobIdFound != null) && (jobIdFound.size() > 0) &&
                 * ((delegationId != null) || (leaseId != null))) { jobIdFound =
                 * jobDB.retrieveJobId(jobIdFound, delegationId, leaseId,
                 * userId); }
                 */
                jobIdFound = jobDB.retrieveJobId(userId, delegationId, compatibleStatusToFind, fromDate, toDate, leaseId, fromStatusDate, toStatusDate);
            }
            // job found.
            if (jobIdFound != null) {
                for (String jobId : jobIdFound) {
                    jobIdFilterResult = new JobIdFilterResult();
                    jobIdFilterResult.setJobId(jobId);
                    jobIdFilterResult.setErrorCode(JobIdFilterFailure.OK_ERRORCODE);
                    jobIdFilterResult.setFailureReason(JobIdFilterFailure.failureReason[JobIdFilterFailure.OK_ERRORCODE]);
                    jobIdFilterResultList.add(jobIdFilterResult);
                }
            }
        } catch (DatabaseException e) {
            logger.error(e.getMessage());
            throw new CommandException(e.getMessage());
        }

        JobEnumeration jobEnum = new JobEnumeration(jobIdFound, jobDB);
        cmd.getResult().addParameter("JOBID_FILTER_RESULT_LIST", jobIdFilterResultList);
        cmd.getResult().addParameter("JOB_ENUM", jobEnum);

        return jobEnum;
    }

    private Calendar makeDate(String timestamp) {
        if (timestamp == null) {
            return null;
        }
        Calendar date = null;
        try {
            Long time = Long.parseLong(timestamp);
            date = Calendar.getInstance();
            date.setTimeInMillis(time);
        } catch (Throwable e) {
        }

        return date;
    }

    private int getCommandType(String cmdName) {
        if (cmdName == null) {
            return -1;
        }

        for (int i = 0; i < JobCommandConstant.cmdName.length; i++) {
            if (cmdName.equals(JobCommand.getCommandName(i))) {
                return i;
            }
        }

        return -1;
    }

    public void execute(final Command command) throws CommandExecutorException, CommandException, IllegalArgumentException {
        logger.debug("BEGIN execute");

        if (command == null) {
            throw new IllegalArgumentException("command not defined!");
        }

        logger.debug("executing command: " + command.toString());

        if (!command.getCategory().equalsIgnoreCase(getCategory())) {
            throw new CommandException("command category mismatch: found \"" + command.getCategory() + "\" required \"" + getCategory() + "\"");
        }

        if (command.isAsynchronous() && command.getCommandGroupId() != null && "COMPOUND".equals(command.getCommandGroupId())) {
            List<String> jobIdList = null;

            if (command.containsParameterKey("JOB_ID_LIST")) {
                jobIdList = command.getParameterMultivalue("JOB_ID_LIST");

                command.deleteParameter("JOB_ID_LIST");
            } else {
                jobIdList = getJobList(command).getJobIdList();
            }

            if (command.containsParameterKey("EXECUTION_MODE")) {
                if (Command.EXECUTION_MODE_SERIAL.equals(command.getParameterAsString("EXECUTION_MODE"))) {
                    command.setExecutionMode(Command.ExecutionModeValues.SERIAL);
                } else {
                    command.setExecutionMode(Command.ExecutionModeValues.PARALLEL);
                }
            }

            if (command.containsParameterKey("PRIORITY_LEVEL")) {
                command.setPriorityLevel(Integer.parseInt(command.getParameterAsString("PRIORITY_LEVEL")));
            }

            if (jobIdList != null) {
                for (String jobId : jobIdList) {
                    command.addParameter("JOB_ID", jobId);
                    command.setCommandGroupId(jobId);

                    getCommandManager().insertCommand(command);
                }
            }

            if (command.getExecutionCompletedTime() == null) {
                command.setExecutionCompletedTime(Calendar.getInstance());
            }

            command.setStatus(Command.SUCCESSFULL);

            logger.debug("END execute");
            return;
        }

        String userId = command.getUserId();

        if (userId == null) {
            throw new CommandException("userId not defined!");
        }

        String logInfo = getLogInfo(command);
        logger.info(logInfo);

        boolean isAdmin = command.getParameterAsString("IS_ADMIN") != null && command.getParameterAsString("IS_ADMIN").equalsIgnoreCase("true");

        int cmdType = getCommandType(command.getName());

        if (cmdType == JobCommandConstant.GET_SERVICE_INFO) {
            int[] jobStatus = new int[1];
            for (int i = 0; i < JobStatus.statusName.length; i++) {
                jobStatus[0] = i;
                try {
                    command.getResult().addParameter(JobStatus.getNameByType(i), new Long(getJobDB().jobCountByStatus(jobStatus, null)));
                } catch (DatabaseException e) {
                    logger.error("execute() - GET_SERVICE_INFO error: " + e.getMessage());
                }
            }
            command.getResult().addParameter("ACCEPT_NEW_JOBS", "" + doesAcceptNewJobs());
        } else if (cmdType == JobCommandConstant.DOES_ACCEPT_NEW_JOBS) {
            command.getResult().addParameter("ACCEPT_NEW_JOBS", "" + doesAcceptNewJobs());
        } else if (cmdType == JobCommandConstant.SET_ACCEPT_NEW_JOBS) {
            String accept = command.getParameterAsString("ACCEPT_NEW_JOBS");
            if (accept == null) {
                throw new CommandException("ACCEPT_NEW_JOBS value not specified!");
            }
            if (!isAdmin) {
                throw new CommandException("Operation reserved only to administrator!");
            }

            setAcceptNewJobs(Boolean.parseBoolean(accept));

            if (doesAcceptNewJobs()) {
                try {
                    getPolicyManager().addPolicyTask(new JobSubmissionTask(this));
                } catch (PolicyManagerException e) {
                    logger.error(e.getMessage());
                }
            } else {
                getPolicyManager().removePolicyTask(JobSubmissionTask.name);
            }
        } else if (cmdType == JobCommandConstant.JOB_REGISTER) {
            logger.debug("Calling jobRegister.");
            jobRegister(command);
        } else if (cmdType == JobCommandConstant.JOB_SET_LEASEID) {
            logger.debug("Calling jobSetLeaseId.");
            jobSetLeaseId(command);
        } else if (cmdType == JobCommandConstant.DELETE_LEASE) {
            logger.debug("Calling deleteLease.");
            deleteLease(command);
        } else if (cmdType == JobCommandConstant.SET_LEASE) {
            logger.debug("Calling setLease.");
            setLease(command);
        } else if (cmdType == JobCommandConstant.GET_LEASE) {
            logger.debug("Calling getLease.");
            getLease(command);
        } else if (cmdType == JobCommandConstant.JOB_INFO) {
            logger.debug("Calling jobInfo.");
            getJobList(command);
        } else if (cmdType == JobCommandConstant.JOB_STATUS) {
            JobEnumeration jobEnum = getJobList(command);
            String user = null;

            if (!isAdmin) {
                user = userId;
            }

            try {
                List<JobStatus> list = jobDB.retrieveLastJobStatus(jobEnum.getJobIdList(), user);
                command.getResult().addParameter("JOB_STATUS_LIST", list);
            } catch (DatabaseException e) {
                throw new CommandException(e.getMessage());
            }
        } else if (cmdType == JobCommandConstant.JOB_LIST) {
            logger.debug("Calling jobList.");
            try {
                String user = null;

                if (!isAdmin) {
                    user = userId;
                }

                List<String> jobIdFound = jobDB.retrieveJobId(user);
                JobEnumeration jobEnum = new JobEnumeration(jobIdFound, jobDB);
                command.getResult().addParameter("JOB_ENUM", jobEnum);
            } catch (DatabaseException e) {
                logger.error(e.getMessage());
                throw new CommandException(e.getMessage());
            }
        } else {
            try {
                if (cmdType == JobCommandConstant.PROXY_RENEW) {
                    try {
                        String delegId = command.getParameterAsString("DELEGATION_PROXY_ID");
                        if (delegId == null) {
                            throw new JobManagementException("DELEGATION_PROXY_ID not specified!");
                        }

                        ProxyCertificate proxyCertificate = makeLocalCopyProxyCertificateFile(delegId, command.getUserId(), null, true);
                        command.addParameter("DELEGATION_PROXY_INFO", proxyCertificate.getDescription());

                        logger.info("PROXY RENEW: [delegId=" + delegId + "] [proxyInfo=" + proxyCertificate.getDescription() + "]");
                    } catch (Exception e) {
                        logger.error("PROXY RENEW error: " + e.getMessage());
                        throw new JobManagementException("PROXY RENEW error: " + e.getMessage());
                    }
                }

                JobEnumeration jobEnum = getJobList(command);

                List<Job> jobList = new ArrayList<Job>(0);
                Calendar now = Calendar.getInstance();

                while (jobEnum.hasMoreJobs()) {
                    Job job = jobEnum.nextJob();

                    JobCommand jobCmd = new JobCommand();
                    jobCmd.setJobId(job.getId());
                    jobCmd.setCreationTime(command.getCreationTime());
                    jobCmd.setDescription(command.getDescription());
                    jobCmd.setStartSchedulingTime(command.getStartProcessingTime());
                    jobCmd.setStartProcessingTime(now);
                    jobCmd.setType(cmdType);
                    jobCmd.setCommandExecutorName(command.getCommandExecutorName());

                    if (!isAdmin) {
                        jobCmd.setUserId(command.getUserId());
                    }

                    logger.debug("Calling jobDB.insertJobCommand.");
                    try {
                        jobDB.insertJobCommand(jobCmd);
                    } catch (Throwable e) {
                        logger.error(e.getMessage());
                        continue;
                    }

                    logger.debug("jobDB.insertJobCommand has been executed.");

                    if (jobCmd.getStatus() != JobCommand.ERROR) {
                        job.addCommandHistory(jobCmd);
                        jobList.add(job);
                    }
                }

                for (Job j : jobList) {
                    JobCommand jobCmd = j.getLastCommand();
                    if (jobCmd == null) {
                        continue;
                    }

                    jobCmd.setStatus(JobCommand.PROCESSING);

                    try {
                        switch (cmdType) {
                        case JobCommandConstant.JOB_CANCEL:
                            logger.info(logInfo + jobCmd.toString());
                            cancel(j);
                            break;
                        case JobCommandConstant.JOB_PURGE:
                            logger.info(logInfo + jobCmd.toString());
                            purge(j);
                            break;
                        case JobCommandConstant.JOB_SUSPEND:
                            logger.info(logInfo + jobCmd.toString());
                            suspend(j);
                            break;
                        case JobCommandConstant.JOB_RESUME:
                            logger.info(logInfo + jobCmd.toString());
                            resume(j);
                            break;
                        case JobCommandConstant.JOB_START:
                            logger.info(logInfo + jobCmd.toString());
                            jobStart(j, command.getUserId());

                            StringBuffer sb = new StringBuffer(logInfo + jobCmd.toString());

                            if (j.getLRMSAbsLayerJobId() != null) {
                                sb.append(" lrmsAbsJobId=").append(j.getLRMSAbsLayerJobId()).append(";");
                            }

                            if (j.getLRMSJobId() != null) {
                                sb.append(" lrmsJobId=").append(j.getLRMSJobId()).append(";");
                            }

                            logger.info(sb.toString());
                            sb = null;
                            break;
                        case JobCommandConstant.PROXY_RENEW:
                            logger.info(logInfo + jobCmd.toString() + " delegationId=" + j.getDelegationProxyId() + ";");

							//old proxy renewal
                            renewProxy(j);

                            String delegationProxyInfo = command.getParameterAsString("DELEGATION_PROXY_INFO");
                            if (delegationProxyInfo != null) {
                                j.setDelegationProxyInfo(delegationProxyInfo);
                                jobDB.update(j);
                            }
                        }

                        if (cmdType != JobCommandConstant.JOB_PURGE) {
                            jobCmd.setStatus(JobCommand.SUCCESSFULL);

                            jobDB.updateJobCommand(jobCmd);
                        }
                    } catch (CommandException ce) {
                        jobCmd.setStatus(JobCommand.ERROR);
                        jobCmd.setFailureReason(ce.getMessage());

                        jobDB.updateJobCommand(jobCmd);

                        if (cmdType == JobCommandConstant.PROXY_RENEW) {
                            int retryCount = 0;

                            if (command.containsParameterKey("_RETRY_COUNT_")) {
                                String value = command.getParameterAsString("_RETRY_COUNT_");
                                try {
                                    retryCount = Integer.parseInt(value);
                                } catch (Throwable t) {
                                }
                            }

                            retryCount++;

                            if (retryCount <= 3) {
                                Command newCmd = new Command(command.getName(), command.getCategory());
                                newCmd.setCommandExecutorName(command.getCommandExecutorName());
                                newCmd.setStatus(Command.RESCHEDULED);
                                newCmd.setDescription("command executed again by CREAM (up to 3 times max): retry count = " + retryCount + "/3");
                                newCmd.setAsynchronous(true);
                                newCmd.setUserId(command.getUserId());
                                newCmd.addParameter("JOB_ID", j.getId());
                                newCmd.addParameter("_RETRY_COUNT_", "" + retryCount);
                                newCmd.setPriorityLevel(Command.HIGH_PRIORITY);
                                newCmd.setExecutionMode(Command.ExecutionModeValues.PARALLEL);
                                newCmd.setCommandGroupId(j.getId());

                                try {
                                    Thread.sleep(10000); // wait 10 sec.
                                } catch (Throwable t) {
                                }

                                getCommandManager().insertCommand(newCmd);
                            }
                        }
                    } finally {
                        logger.info(logInfo + jobCmd.toString());
                    }
                }

                List<String> invalidJobIdlist = (List<String>) command.getResult().getParameter("JOB_ID_LIST_STATUS_NOT_COMPATIBLE_FOUND");

                if (invalidJobIdlist != null) {
                    for (String jobId : invalidJobIdlist) {
                        try {
                            JobCommand jobCmd = new JobCommand(cmdType, jobId);
                            jobCmd.setCreationTime(command.getCreationTime());
                            jobCmd.setDescription(command.getDescription());
                            jobCmd.setStartSchedulingTime(command.getStartProcessingTime());
                            jobCmd.setStatus(JobCommand.ERROR);
                            jobCmd.setFailureReason("status not compatible with the specified command!");
                            
                            if (!isAdmin) {
                                jobCmd.setUserId(command.getUserId());
                            }

                            jobDB.insertJobCommand(jobCmd);

                            logger.info(logInfo + jobCmd.toString());
                        } catch (DatabaseException e) {
                            logger.error(e.getMessage());
                        }
                    }
                }
                command.setStatus(Command.SUCCESSFULL);
            } catch (DatabaseException e) {
                throw new CommandException(e.getMessage());
            } catch (JobManagementException e) {
                throw new CommandException(e.getMessage());
            }
        }

        if (command.getExecutionCompletedTime() == null) {
            command.setExecutionCompletedTime(Calendar.getInstance());
        }

        command.setStatus(Command.SUCCESSFULL);

        logger.info(getLogInfo(command));
        logger.debug("END execute");
    }

    private String getLogInfo(Command command) {
        StringBuffer logInfo = new StringBuffer();

        if (command != null) {
            if (command.containsParameterKey("REMOTE_REQUEST_ADDRESS")) {
                logInfo.append("REMOTE_REQUEST_ADDRESS=").append(command.getParameter("REMOTE_REQUEST_ADDRESS")).append("; ");
            }

            if (command.containsParameterKey("USER_DN")) {
                logInfo.append("USER_DN=").append(command.getParameter("USER_DN")).append("; ");
            }

            if (command.containsParameterKey("USER_FQAN")) {
                List<String> fqanList = command.getParameterMultivalue("USER_FQAN");

                logInfo.append("USER_FQAN={ ");
                for (String fqan : fqanList) {
                    logInfo.append(fqan).append("; ");
                }

                logInfo.append("}; ");
            }

            logInfo.append("CMD_NAME=").append(command.getName()).append("; ");
            logInfo.append("CMD_CATEGORY=").append(command.getCategory()).append("; ");
            logInfo.append("CMD_STATUS=").append(command.getStatusName()).append("; ");
        }
        return logInfo.toString();
    }

    private void jobStart(Job job, String userId) throws CommandException {
        if (job == null || job.getId() == null || userId == null) {
            return;
        }

        logger.debug("Begin jobStart for job " + job.getId());
        JobStatus status = null;

        try {
            status = new JobStatus(JobStatus.PENDING, job.getId());
            job.addStatus(status);

            logger.debug("pending status for job " + job.getId());

            doOnJobStatusChanged(status);

            logger.debug("pending status for job " + job.getId() + " done");
        } catch (Throwable e) {
            throw new CommandException(e.getMessage());
        }

        Calendar now = Calendar.getInstance();
        CommandResult cr = null;
        try {
            cr = submit(job);
        } catch (CommandException e) {
            status = new JobStatus(JobStatus.ABORTED, job.getId());
            status.setFailureReason(e.getMessage());

            job.addStatus(status);

            try {
                doOnJobStatusChanged(status);
            } catch (Throwable te) {
                throw new CommandException(te.getMessage());
            }

            setLeaseExpired(job);

            throw e;
        }

        job.setLRMSJobId(cr.getParameterAsString("LRMS_JOB_ID"));
        job.setLRMSAbsLayerJobId(cr.getParameterAsString("LRMS_ABS_JOB_ID"));

        try {
            if (isEmptyField(job.getLRMSAbsLayerJobId())) {
                status = new JobStatus(JobStatus.ABORTED, job.getId());
                status.setFailureReason("LRMSAbsLayerJobId not found!");

                job.addStatus(status);

                doOnJobStatusChanged(status);

                setLeaseExpired(job);
            } else {
                jobDB.update(job);

                JobStatus lastStatus = jobDB.retrieveLastJobStatus(job.getId(), userId);
                if (lastStatus.getType() == JobStatus.PENDING) {
                    status = new JobStatus(JobStatus.IDLE, job.getId(), now);

                    job.addStatus(status);

                    doOnJobStatusChanged(status);
                }
            }
        } catch (Throwable te) {
            throw new CommandException(te.getMessage());
        }

        logger.debug("End jobStart for job " + job.getId());
    }

    private void jobSetLeaseId(final Command command) throws CommandException {
        logger.debug("Begin setJobLeaseId");
        List<String> jobIdList = null;
        String leaseId = command.getParameterAsString("NEW_LEASE_ID");
        String userId = command.getUserId();

        if (userId == null) {
            throw new CommandException("userId not specified!");
        }

        JobEnumeration jobEnum = getJobList(command);
        jobIdList = jobEnum.getJobIdList();

        for (String jobId : jobIdList) {
            JobCommand jobCmd = new JobCommand(JobCommandConstant.JOB_SET_LEASEID, jobId);
            jobCmd.setCreationTime(command.getCreationTime());
            // jobCmd.setDescription(msg);
            jobCmd.setUserId(userId);
            jobCmd.setStartSchedulingTime(command.getStartSchedulingTime());
            jobCmd.setStartProcessingTime(command.getStartProcessingTime());
            jobCmd.setStatus(JobCommand.PROCESSING);

            try {
                jobDB.insertJobCommand(jobCmd);

                logger.info(getLogInfo(command) + jobCmd.toString() + " leaseId=" + leaseId + ";");

                leaseManager.setJobLeaseId(jobId, leaseId, userId);

                jobCmd.setStatus(JobCommand.SUCCESSFULL);

                jobDB.updateJobCommand(jobCmd);
            } catch (Exception e) {
                logger.error(e.getMessage());
                jobCmd.setStatus(JobCommand.ERROR);
                jobCmd.setFailureReason(e.getMessage());
                try {
                    jobDB.updateJobCommand(jobCmd);
                } catch (DatabaseException de) {
                    logger.error(e.getMessage());
                }
            } finally {
                logger.info(getLogInfo(command) + jobCmd.toString() + " leaseId=" + leaseId + ";");
            }
        }// for
        logger.debug("End setJobLeaseId");
    }

    private void getLease(final Command command) throws CommandException {
        logger.debug("Begin getLease");
        List<Lease> leaseList = new ArrayList<Lease>(0);

        try {
            if (command.containsParameterKey("LEASE_ID")) {
                Lease lease = jobDB.retrieveJobLease(command.getParameterAsString("LEASE_ID"), command.getUserId());
                if (lease != null) {
                    leaseList.add(lease);
                }
            } else {
                List<Lease> lease = jobDB.retrieveJobLease(command.getUserId());
                if (lease != null) {
                    leaseList = lease;
                }
            }

            command.getResult().addParameter("LEASE_LIST", leaseList);
        } catch (DatabaseException e) {
            throw new CommandException(e.getMessage());
        }
        logger.debug("End getLease");
    }

    private void deleteLease(final Command command) throws CommandException {
        logger.debug("Begin deleteLease");
        if (!command.containsParameterKey("LEASE_ID")) {
            throw new CommandException("lease id not specified!");
        }

        String leaseId = command.getParameterAsString("LEASE_ID");
        String userId = command.getUserId();

        logger.info(getLogInfo(command) + command.toString() + " leaseId=" + leaseId + ";");

        if (userId == null) {
            throw new CommandException("userId not specified!");
        }

        leaseManager.deleteLease(leaseId, userId);
        logger.debug("End deleteLease");
    }

    private void setLease(final Command command) throws CommandException {
        logger.debug("BEGIN setLease");
        if (!command.containsParameterKey("LEASE_ID")) {
            throw new CommandException("lease id not specified!");
        }
        if (!command.containsParameterKey("LEASE_TIME")) {
            throw new CommandException("lease time not specified!");
        }

        String userId = command.getUserId();
        String leaseId = command.getParameterAsString("LEASE_ID");

        logger.info(getLogInfo(command) + command.toString() + " leaseId=" + leaseId + ";");

        Calendar leaseTime = null;
        Calendar boundedLeaseTime = null;
        try {
            Long timestamp = Long.parseLong(command.getParameterAsString("LEASE_TIME"));
            leaseTime = Calendar.getInstance();
            leaseTime.setTimeInMillis(timestamp);

            logger.debug("leaseTime = " + leaseTime.getTime());

            Lease lease = new Lease();
            lease.setLeaseId(leaseId);
            lease.setUserId(userId);
            lease.setLeaseTime(leaseTime);

            boundedLeaseTime = leaseManager.setLease(lease);

            command.getResult().addParameter("LEASE_TIME", boundedLeaseTime);
            logger.debug("boundedLeaseTime = " + boundedLeaseTime);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            throw new CommandException(e.getMessage());
        } catch (CommandException e) {
            logger.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new CommandException(e.getMessage());
        }

        logger.debug("END setLease");
    }

    private boolean isEmptyField(String field) {
        return (field == null || Job.NOT_AVAILABLE_VALUE.equals(field) || field.length() == 0);
    }

    protected void createJobSandboxDir(Job job, String gsiFTPcreamURL) throws CommandException, InterruptedException, IOException {
        Runtime runtime = Runtime.getRuntime();
        BufferedReader in = null;
        BufferedOutputStream os = null;
        BufferedReader readErr = null;
        Process proc = null;

        String cream_sandbox_dir = getParameterValueAsString("CREAM_USER_SANDBOX_PATH");
        if (cream_sandbox_dir == null) {
            throw new CommandException("parameter \"CREAM_USER_SANDBOX_PATH\" not defined!");
        }

        job.setCREAMSandboxBasePath(cream_sandbox_dir);

        String glexec_cream_script_path = getParameterValueAsString("CREAM_CREATE_SANDBOX_BIN_PATH");
        if (glexec_cream_script_path == null) {
            throw new CommandException("parameter \"CREAM_CREATE_SANDBOX_BIN_PATH\" not defined!");
        }

        String[] cmd = null;
        String[] envp = null;
        String su_exec_path = getParameterValueAsString("GLEXEC_BIN_PATH");

        if (su_exec_path != null) {
            if (isEmptyField(job.getAuthNProxyCertPath())) {
                throw new CommandException("authN proxy path not defined!");
            }

            try {
                makeLocalCopyProxyCertificateFile(ProxyCertificateType.AUTHENTICATION.getName(), job.getUserId(), job.getAuthNProxyCertPath(), false);
            } catch (Throwable t) {
                throw new CommandException(t.getMessage());
            }

            envp = new String[] { "GLEXEC_MODE=lcmaps_get_account", "GLEXEC_CLIENT_CERT=" + job.getAuthNProxyCertPath() };
            cmd = new String[] { su_exec_path, glexec_cream_script_path, cream_sandbox_dir, job.getUserId(), job.getId(), "true" };
        } else {
            su_exec_path = getParameterValueAsString("SUDO_BIN_PATH");

            if (su_exec_path == null) {
                throw new CommandException("parameter \"GLEXEC_BIN_PATH\" or \"SUDO_BIN_PATH\" not defined!");
            }

            cmd = new String[] { su_exec_path, "-u", job.getLocalUser(), glexec_cream_script_path, cream_sandbox_dir, job.getUserId(), job.getId(), "true" };
        }

        /*
         * String glexec_path = getParameterValueAsString("GLEXEC_BIN_PATH"); if
         * (glexec_path == null) { throw new
         * CommandException("parameter \"GLEXEC_BIN_PATH\" not defined!"); }
         * 
         * String glexec_cream_script_path =
         * getParameterValueAsString("CREAM_CREATE_SANDBOX_BIN_PATH"); if
         * (glexec_cream_script_path == null) { throw newCommandException(
         * "parameter \"CREAM_CREATE_SANDBOX_BIN_PATH\" not defined!"); }
         * 
         * if (isEmptyField(job.getAuthNProxyCertPath())) { throw new
         * CommandException("authN proxy path not defined!"); }
         * 
         * try { checkLocalProxyCertificateFile(ProxyCertificate.AUTHN_PROXY,
         * jobGetUserId(), job.getAuthNProxyCertPath()); } catch (Throwable t) {
         * throw new CommandException(t.getMessage()); }
         * 
         * try { String[] envp = new String[] {
         * "GLEXEC_MODE=lcmaps_get_account", "GLEXEC_CLIENT_CERT=" +
         * job.getAuthNProxyCertPath() }; String[] cmd = new String[] {
         * glexec_path, glexec_cream_script_path, cream_sandbox_dir,
         * job.getUserId(), job.getId(), "true" };
         */

        try {
            try {
                proc = runtime.exec(cmd, envp);
            } catch (Throwable e) {
                logger.error("createJobSandboxDir: " + e.getMessage(), e);
                throw (new IOException("The problem seems to be related to glexec"));
            }

            in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            os = new BufferedOutputStream(proc.getOutputStream());
            readErr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            try {
                job.setWorkingDirectory(in.readLine());

                if (job.getWorkingDirectory() == null) {
                    throw new IOException();
                }
            } catch (IOException e) {
                throw new IOException("cannot create the job's working directory! The problem seems to be related to glexec");
            }

            try {
                job.setLocalUser(in.readLine());
            } catch (IOException e) {
                throw new IOException("cannot retrieve the name of the local user! The problem seems to be related to glexec");
            }

            if (gsiFTPcreamURL != null && job.getWorkingDirectory() != null) {
                job.setCREAMInputSandboxURI(gsiFTPcreamURL + job.getWorkingDirectory() + "/ISB");
                job.setCREAMOutputSandboxURI(gsiFTPcreamURL + job.getWorkingDirectory() + "/OSB");

                if (job.getOutputSandboxBaseDestURI() != null && job.getOutputSandboxBaseDestURI().equalsIgnoreCase("gsiftp://localhost")) {
                    job.setOutputSandboxBaseDestURI(job.getCREAMOutputSandboxURI());
                }
                if (job.getOutputSandboxDestURI() != null) {
                    String[] file = job.getOutputSandboxDestURI();
                    for (int x = 0; x < file.length; x++) {
                        if (file[x].startsWith("gsiftp://localhost")) {
                            file[x] = job.getCREAMOutputSandboxURI() + file[x].substring("gsiftp://localhost".length());
                        }
                    }
                }
            } else {
                job.setCREAMInputSandboxURI(Job.NOT_AVAILABLE_VALUE);
                job.setCREAMOutputSandboxURI(Job.NOT_AVAILABLE_VALUE);
            }

            String id = "" + StrictMath.random();

            if (id.charAt(1) == '.') {
                id = id.substring(2);
            }

            if (id.length() > 20) {
                id = id.substring(0, 20);
            }

            job.addExtraAttribute(Job.DELEGATION_PROXY_CERT_SANDBOX_PATH, job.getWorkingDirectory() + "/Proxy/" + id);

            if (gsiFTPcreamURL != null && (isEmptyField(job.getICEId()) || (!isEmptyField(job.getICEId()) && !isEmptyField(job.getMyProxyServer())))) {
                //new proxy renewal
              //  String proxyPath = job.getWorkingDirectory().substring(0, job.getWorkingDirectory().length()-17) + "proxy" + File.separator + job.getDelegationProxyId();
              //  job.putVolatileProperty(DELEGATION_PROXY_CERT_SANDBOX_URI, gsiFTPcreamURL + proxyPath);

                //old proxy renewal
                job.putVolatileProperty(DELEGATION_PROXY_CERT_SANDBOX_URI, gsiFTPcreamURL + job.getExtraAttribute(Job.DELEGATION_PROXY_CERT_SANDBOX_PATH));

                if (containsParameterKey(JOB_WRAPPER_DELEGATION_TIME_SLOT)) {
                    job.putVolatileProperty(JOB_WRAPPER_DELEGATION_TIME_SLOT, (String) getParameterValue(JOB_WRAPPER_DELEGATION_TIME_SLOT));
                }
            } 

            if (containsParameterKey(JOB_WRAPPER_COPY_PROXY_MIN_RETRY_WAIT)) {
                job.putVolatileProperty(JOB_WRAPPER_COPY_PROXY_MIN_RETRY_WAIT, (String) getParameterValue(JOB_WRAPPER_COPY_PROXY_MIN_RETRY_WAIT));
            }

            String tmpWrapper = null;

            try {
                tmpWrapper = JobWrapper.buildWrapper(job);
            } catch (Throwable e) {
                proc.destroy();
                throw new CommandException("cannot generate the job wrapper! the problem seems to be related to the jdl: " + e.getMessage());
            }

            if (tmpWrapper == null) {
                throw new CommandException("cannot generate the job wrapper!");
            }

            try {
                os.write(tmpWrapper.getBytes());
                os.flush();
                os.close();
                os = null;
                proc.waitFor();
            } catch (Throwable e) {
                throw new CommandException("cannot write the job wrapper (jobId = " + job.getId() + ")! The problem seems to be related to glexec");
            }
        } catch (CommandException ce) {
            throw ce;
        } catch (Throwable exx) {
            if (proc != null) {
                proc.waitFor();

                if(proc.exitValue() != 0) {            
                    String procErrorMessage = null;

                    if (readErr.ready()) {
                        procErrorMessage = " [glexec reported = \"" + readErr.readLine() + "\"]";
                    } else {
                        procErrorMessage = " [error = Glexec policy violation: see glexec log for more details. (ExitCode = " + proc.exitValue() + ")]";
                    }

                  //  logger.error(procErrorMessage);
                    throw new CommandException(exx.getMessage() + procErrorMessage);
                }
            } else {
                throw new CommandException(exx.getMessage());
            }
        } finally {
            if (proc != null) {
                proc.waitFor();
                
                try {
                    proc.getInputStream().close();
                    proc.getErrorStream().close();
                    proc.getOutputStream().close();          
                } catch (IOException ex) {}
            }
        }
    }

    protected Job makeJobFromCmd(Command cmd) throws CommandException {
        String jdl = cmd.getParameterAsString("JDL");
        if (jdl == null) {
            throw new CommandException("JDL not defined!");
        }
        try {
            return JobFactory.makeJob(jdl);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            throw new CommandException(e.getMessage());
        }
    }

    private void jobRegister(final Command cmd) throws CommandException, CommandExecutorException, IllegalArgumentException {
        logger.debug("BEGIN jobRegister");
        if (cmd == null) {
            throw new IllegalArgumentException("command not defined!");
        }

        String userId = cmd.getUserId();
        Job job = null;

        JobCommand jobCmd = new JobCommand(JobCommandConstant.JOB_REGISTER);
        jobCmd.setStatus(cmd.getStatus());
        jobCmd.setCreationTime(cmd.getCreationTime());
        jobCmd.setDescription(cmd.getDescription());
        jobCmd.setUserId(cmd.getUserId());
        jobCmd.setStartSchedulingTime(cmd.getStartProcessingTime());

        StringBuffer logInfo = new StringBuffer(getLogInfo(cmd));
        logInfo.append(jobCmd.toString());

        logger.info(logInfo.toString());

        try {
            if (userId == null) {
                throw new CommandException("userId not defined!");
            }

            /*
             * if (job.getGridJobId() != null &&
             * !job.getGridJobId().equals("N/A")) { List<String> list = new
             * ArrayList<String>(1); list.add(job.getGridJobId()); list =
             * jobDB.retrieveJobIdByGridJobId(list, userId);
             * 
             * if (list.size() > 0) { // throw new
             * CommandException("a job with the following gridJobId \"" +
             * job.getGridJobId() + "\" already exists!");
             * 
             * cmd.getResult().addParameter("JOB",
             * jobDB.retrieveJob(list.get(0), userId)); return; } }
             */

            job = makeJobFromCmd(cmd);
            
            JobStatus status = new JobStatus(JobStatus.REGISTERED, job.getId());

            job.addStatus(status);
            job.setUserId(userId);
            job.setLocalUser(Job.NOT_AVAILABLE_VALUE);
            job.setJDL(cmd.getParameterAsString("JDL"));
            job.setICEId(cmd.getParameterAsString("ICE_ID"));
            job.addCommandHistory(jobCmd);

            if (cmd.containsParameterKey("USER_VO")) {
                job.setVirtualOrganization(cmd.getParameterAsString("USER_VO"));
            }

            if (isEmptyField(job.getBatchSystem())) {
                throw new CommandException("\"BatchSystem\" attribute not defined into the JDL");
            }

            if (isEmptyField(job.getQueue())) {
                throw new CommandException("\"QueueName\" attribute not defined into the JDL");
            }

            if (!isBatchSystemSupported(job.getBatchSystem())) {
                throw new CommandException("Batch System " + job.getBatchSystem() + " not supported!");
            }

            job.setCreamURL(cmd.getParameterAsString("CREAM_URL"));
            // job.setAuthNProxyCertPath(cmd.getParameterAsString("AUTHN_PROXY_PATH"));
            job.setAuthNProxyCertPath(getParameterValueAsString("CREAM_USER_PROXY_PATH") + File.separator + job.getUserId() + File.separator + ProxyCertificateType.AUTHENTICATION.getName());
            job.setDelegationProxyId(cmd.getParameterAsString("DELEGATION_PROXY_ID"));
            job.setDelegationProxyInfo(cmd.getParameterAsString("DELEGATION_PROXY_INFO"));
            // job.setDelegationProxyCertPath(cmd.getParameterAsString("DELEGATION_PROXY_PATH"));
            job.setDelegationProxyCertPath(getParameterValueAsString("CREAM_USER_PROXY_PATH") + File.separator + job.getUserId() + File.separator + job.getDelegationProxyId());
            job.setLRMSAbsLayerJobId(Job.NOT_AVAILABLE_VALUE);
            job.setLRMSJobId(Job.NOT_AVAILABLE_VALUE);
            job.setWorkerNode(Job.NOT_AVAILABLE_VALUE);
            job.setWorkingDirectory(Job.NOT_AVAILABLE_VALUE);
            job.setLocalUser(Job.NOT_AVAILABLE_VALUE);

            if (this.containsParameterKey("LRMS_EVENT_LISTENER_PORT")) {
                job.setLoggerDestURI(InetAddress.getLocalHost().getHostAddress() + ":" + getParameterValueAsString("LRMS_EVENT_LISTENER_PORT"));
            }

            if (job.getCreamURL() != null) {
                try {
                    URL url = new URL(job.getCreamURL());
                    job.setCeId(url.getHost() + ":" + url.getPort() + "/cream-" + job.getBatchSystem() + "-" + job.getQueue());
                } catch (MalformedURLException e) {
                }
            }

            if (cmd.containsParameterKey("LEASE_ID")) {
                String leaseId = cmd.getParameterAsString("LEASE_ID");

                if (leaseId != null && leaseId.length() > 0) {
                    Lease lease = jobDB.retrieveJobLease(leaseId, userId);
                    if (lease != null) {
                        logger.debug("found lease \"" + leaseId + "\" = " + lease.getLeaseTime().getTime());
                        job.setLease(lease);
                    } else {
                        throw new CommandException("lease id \"" + leaseId + "\" not found!");
                    }
                }
            }

            boolean jobInserted = false;
            int count = 0;

            while (!jobInserted && count < 5) {
                try {
                    jobDB.insert(job);
                    jobInserted = true;
                } catch (DatabaseException de) {
                    if (de.getMessage().indexOf("Duplicate entry") > -1) {
                        job.setId(job.generateJobId());
                        count++;
                    } else {
                        throw new CommandException(de.getMessage());
                    }
                } catch (IllegalArgumentException ie) {
                    throw new CommandException(ie.getMessage());
                }
            }

            if (!jobInserted) {
                throw new CommandException("Duplicate jobId error: cannot insert the new job (" + job.getId() + ") into the database");
            }

            jobCmd.setJobId(job.getId());
            jobCmd.setStatus(JobCommand.SUCCESSFULL);
            
            try {
                createJobSandboxDir(job, cmd.getParameterAsString("GSI_FTP_CREAM_URL"));
            } catch (Exception e) {
                jobCmd.setStatus(JobCommand.ERROR);
                jobCmd.setFailureReason(e.getMessage());

                status.setType(JobStatus.ABORTED);
                status.setFailureReason(e.getMessage());

                jobDB.updateStatus(status, null);
                
                throw new CommandException(e.getMessage());
            } finally {
                jobDB.update(job);
                jobDB.updateJobCommand(jobCmd);
            }

            StringBuffer logStatusInfo = new StringBuffer("JOB ");
            logStatusInfo.append(status.getJobId()).append(" STATUS CHANGED: -- => ").append(status.getName());
        	logger.info(logStatusInfo.toString());
        	
            try {
                sendNotification(job);
            } catch (JobManagementException e) {
                logger.error(e);
            }

            String autostart = cmd.getParameterAsString("AUTOSTART");
            Boolean autostart_b = autostart != null ? new Boolean(autostart) : new Boolean(Boolean.FALSE);

            if (autostart_b.booleanValue()) {
                jobStart(job, userId);
            }

            logInfo.append(" localUser=").append(job.getLocalUser());
            logInfo.append("; jobId=").append(job.getId());

            if (!isEmptyField(job.getGridJobId())) {
                logInfo.append("; gridJobId=").append(job.getGridJobId());
            }
            if (!isEmptyField(job.getDelegationProxyId())) {
                logInfo.append("; delegationId=").append(job.getDelegationProxyId());
            }
            logInfo.append(";");

            logger.info(logInfo.toString());
            cmd.getResult().addParameter("JOB", job);
        } catch (CommandException e) {
            logger.error(e.getMessage());
            throw e;
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            throw new CommandExecutorException(e);
        }

        logger.debug("END jobRegister");
    }

    private void purge(Job job) throws CommandException, IllegalArgumentException {
        if (job == null) {
            throw new IllegalArgumentException("job not defined!");
        }

        if (isEmptyField(job.getAuthNProxyCertPath())) {
            throw new CommandException("authN proxy path not defined!");
        }

        StringBuffer errorMessage = null;
        
        if (!isEmptyField(job.getWorkingDirectory())) {
            if (!containsParameterKey("CREAM_PURGE_SANDBOX_BIN_PATH")) {
                throw new CommandException("CREAM_PURGE_SANDBOX_BIN_PATH parameter not found!");
            }

            String cream_sandbox_dir = getParameterValueAsString("CREAM_USER_SANDBOX_PATH");
            if (cream_sandbox_dir == null) {
                throw new CommandException("parameter \"CREAM_USER_SANDBOX_PATH\" not defined!");
            }

            String workingDir = job.getWorkingDirectory();

            if (!workingDir.startsWith(cream_sandbox_dir)) {
                throw new CommandException("the job's working directory path is wrong!");
            }

            int index = cream_sandbox_dir.length();
            if (!cream_sandbox_dir.endsWith(File.separator)) {
                index++;
            }

            String localUserGroup = workingDir.substring(index);
            localUserGroup = localUserGroup.substring(0, localUserGroup.indexOf(File.separator));

            String userId = workingDir.substring(index + localUserGroup.length() + 1);
            userId = userId.substring(0, userId.indexOf(File.separator));

            Process proc = null;

            try {
                String[] cmd = new String[] { getParameterValueAsString("CREAM_PURGE_SANDBOX_BIN_PATH"), localUserGroup, userId, job.getId() };

                proc = Runtime.getRuntime().exec(cmd);
            } catch (Throwable e) {
                logger.error(e.getMessage());
            } finally {
                if (proc != null) {
                    try {
                        proc.waitFor();
                    } catch (InterruptedException e) {
                    }
                    
                    if (proc.exitValue() != 0) {
                        BufferedReader readErr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

                        errorMessage = new StringBuffer();
                        String inputLine = null;

                        try {
                            while ((inputLine = readErr.readLine()) != null) {
                                errorMessage.append(inputLine);
                            }
                        } catch (IOException ioe) {}
                    }

                    try {
                        proc.getInputStream().close();
                        proc.getErrorStream().close();
                        proc.getOutputStream().close();
                    } catch (IOException ioe) {}
                }
            }
        }
        
        if (errorMessage == null || errorMessage.toString().trim().endsWith("No such file or directory")) {
            try {
                jobDB.delete(job.getId(), job.getUserId());
                logger.info("purge: purged job " + job.getId());
            } catch (DatabaseException e) {
                logger.error(e.getMessage());
                throw new CommandException(e.getMessage());
            }
        } else {
            throw new CommandException(errorMessage.toString().trim());
        }
    }

    public void execute(Command[] cmd) throws CommandExecutorException, CommandException, IllegalArgumentException {
        if (cmd == null) {
            return;
        }

        for (int i = 0; i < cmd.length; i++) {
            execute(cmd[i]);
        }
    }

    public abstract CommandResult submit(Job job) throws CommandException;

    public abstract void cancel(Job job) throws CommandException;

    public abstract void suspend(Job job) throws CommandException;

    public abstract void resume(Job job) throws CommandException;

    public abstract void renewProxy(Job job) throws CommandException;

    public abstract void renewProxy(Job job, boolean sendToWN) throws CommandException;

    public abstract boolean isBatchSystemSupported(String bs);

    public synchronized void doOnJobStatusChanged(JobStatus status) throws IllegalArgumentException, JobManagementException {
        if (status == null) {
            throw new IllegalArgumentException("job status not defined!");
        }

        if (status.getType() == JobStatus.PURGED) {
            return;
        }

        JobStatus lastStatus;
        try {
            lastStatus = jobDB.retrieveLastJobStatus(status.getJobId(), null);
        } catch (DatabaseException e) {
            throw new JobManagementException(e.getMessage());
        }

        if (lastStatus == null) {
            throw new JobManagementException("job " + status.getJobId() + " not found!");
        }

        if (status.getType() == lastStatus.getType()) {
            if (lastStatus.getName().startsWith("DONE")) {
                try {
                    if (!lastStatus.getExitCode().equalsIgnoreCase("W")) {
                        status.setExitCode(lastStatus.getExitCode());
                    }
                    jobDB.updateStatus(status, null);
                    logger.info("JOB " + status.getJobId() + " STATUS UPDATED: " + status.getName());

                    try {
                        sendNotification(status.getJobId());
                    } catch (Throwable e) {
                        logger.error(e.getMessage());
                    }
                } catch (IllegalArgumentException e) {
                    logger.error(e);
                    throw new JobManagementException(e);
                } catch (DatabaseException e) {
                    logger.error(e);
                    throw new JobManagementException(e);
                }
            }
        } else {
            if (lastStatus.getType() == JobStatus.ABORTED || lastStatus.getType() == JobStatus.CANCELLED || lastStatus.getType() == JobStatus.DONE_OK
                    || lastStatus.getType() == JobStatus.DONE_FAILED) {
                return;
            }

            Job job = null;
            try {
                job = jobDB.retrieveJob(status.getJobId(), null);
            } catch (Exception e) {
                logger.error(e.getMessage());
                return;
            }

            if (job == null) {
                logger.warn("doOnJobStatusChanged warn: job " + status.getJobId() + " not found!");
                return;
            }

            switch (status.getType()) {
            case JobStatus.ABORTED:
                setLeaseExpired(job);
                break;

            case JobStatus.CANCELLED:
                boolean found = false;
                for (int i = job.getCommandHistoryCount() - 1; i >= 0; i--) {
                    if (job.getCommandHistoryAt(i).getType() == JobCommandConstant.JOB_CANCEL) {
                        status.setDescription(job.getCommandHistoryAt(i).getDescription());
                        
                        if(status.getDescription() == null || status.getDescription().length() == 0) {
                            status.setDescription("job cancelled on user request!");                                    
                        }
                        
                        found = true;
                        
                        break;
                    }
                }
                
                setLeaseExpired(job);
                
                if(!found) {
                    status.setDescription("job cancelled by the sysadmin!");      
                    
                    Calendar time = Calendar.getInstance();
                    time.add(Calendar.MINUTE, 1);
                    
                    try {
                        getPolicyManager().addPolicy(new Policy(status.getJobId(), GetJobSTDTask.name, status.getJobId(), time));
                    } catch (PolicyManagerException e) {
                        logger.error(e.getMessage());
                    }                    
                }
                
                break;

            case JobStatus.DONE_OK:
            case JobStatus.DONE_FAILED:
                if (status.getExitCode().equalsIgnoreCase("W")) {
                    Calendar time = Calendar.getInstance();
                    time.add(Calendar.MINUTE, 1);

                    try {
                        getPolicyManager().addPolicy(new Policy(status.getJobId(), GetJobSTDTask.name, status.getJobId(), time));
                    } catch (PolicyManagerException e) {
                        logger.error(e.getMessage());
                    }
                }

                setLeaseExpired(job);
                break;

            case JobStatus.REALLY_RUNNING:
                if (lastStatus.getType() == JobStatus.ABORTED || lastStatus.getType() == JobStatus.CANCELLED || lastStatus.getType() == JobStatus.DONE_OK
                        || lastStatus.getType() == JobStatus.DONE_FAILED) {
                    return;
                }
                break;

            case JobStatus.RUNNING:
                if (status.getTimestamp().compareTo(lastStatus.getTimestamp()) <= 0) {
                    return;
                }

                if (lastStatus.getType() == JobStatus.REALLY_RUNNING || lastStatus.getType() == JobStatus.ABORTED || lastStatus.getType() == JobStatus.CANCELLED
                        || lastStatus.getType() == JobStatus.DONE_OK || lastStatus.getType() == JobStatus.DONE_FAILED) {
                    return;
                }
                try {
                    List<JobStatus> statusList = jobDB.retrieveJobStatusHistory(status.getJobId(), null);

                    if (statusList != null && statusList.size() > 2) {
                        JobStatus oldStatus = statusList.get(statusList.size() - 2);
                        status.setType(oldStatus.getType() == JobStatus.REALLY_RUNNING ? JobStatus.REALLY_RUNNING : JobStatus.RUNNING);
                    }
                } catch (DatabaseException e) {
                    throw new JobManagementException(e);
                }
                break;

            case JobStatus.HELD:
            case JobStatus.IDLE:
            case JobStatus.PENDING:
            case JobStatus.REGISTERED:
                if (status.getTimestamp().compareTo(lastStatus.getTimestamp()) < 0) {
                    logger.warn("the timestamp of the new status " + status.getName() + " for the JOB " + status.getJobId() + " is older than the last one");
                    return;
                }

                break;
            }

            try {
                job.addStatus(status);

                logger.debug("inserting new status " + status.getName() + " for the JOB " + status.getJobId());
                jobDB.insertStatus(status, null);

                StringBuffer logInfo = new StringBuffer("JOB ");
                logInfo.append(status.getJobId()).append(" STATUS CHANGED: ").append(lastStatus.getName()).append(" => ").append(status.getName());

                if (status.getType() == JobStatus.IDLE && job.getLRMSJobId() != null) {
                    logInfo.append(" [lrmsJobId=").append(job.getLRMSJobId()).append("]");
                } else if ((status.getType() == JobStatus.RUNNING || status.getType() == JobStatus.REALLY_RUNNING) && job.getWorkerNode() != null) {
                    logInfo.append(" [workerNode=").append(job.getWorkerNode()).append("]");
                }

                logger.info(logInfo.toString());
            } catch (IllegalArgumentException e) {
                logger.error(e);
                throw new JobManagementException(e);
            } catch (DatabaseException e) {
                logger.error(e);
                throw new JobManagementException(e);
            }

            try {
                sendNotification(job);
            } catch (Throwable e) {
                logger.error(e.getMessage());
            }
        }
    }

    public JobDBInterface getJobDB() {
        return jobDB;
    }

    public void setJobDB(JobDBInterface jobDB) {
        this.jobDB = jobDB;
        if (jobDB != null) {
            try {
                Job job = null;
                List<String> jobPendingList = jobDB.retrieveJobId(new int[] { JobStatus.PENDING }, null, null, null);

                for (String jobId : jobPendingList) {
                    job = jobDB.retrieveJob(jobId, null);

                    if (job != null && (job.getLRMSAbsLayerJobId() == null || Job.NOT_AVAILABLE_VALUE.equals(job.getLRMSAbsLayerJobId()))) {
                        if (job.getCommandHistoryCount() > 1) {
                            JobCommand cmd = job.getCommandHistoryAt(1);
                            cmd.setStatus(JobCommand.ABORTED);
                            cmd.setFailureReason("command aborted because its execution has been interrupted by the CREAM shutdown");

                            jobDB.updateJobCommand(cmd);
                            logger.info(cmd.toString());
                        }

                        JobStatus status = job.getLastStatus();
                        status.setType(JobStatus.ABORTED);
                        status.setFailureReason("job aborted because the execution of the JOB_START command has been interrupted by the CREAM shutdown");
                        status.setTimestamp(job.getLastCommand().getExecutionCompletedTime());

                        jobDB.updateStatus(status, null);
                        logger.info("job " + job.getId() + " aborted because the execution of the JOB_START command has been interrupted by the CREAM shutdown");

                        try {
                            sendNotification(job);
                        } catch (JobManagementException e) {
                            logger.error(e.getMessage());
                        }
                    } 
                }

                jobDB.updateAllUnterminatedJobCommand();
            } catch (DatabaseException e) {
                logger.error(e.getMessage());
            }

            leaseManager = new LeaseManager(this);
            try {
                getPolicyManager().addPolicyTask(leaseManager);
            } catch (Throwable e) {
                logger.error(e.getMessage());
            }
            try {
                getPolicyManager().addPolicyTask(new JobCancelTask(this));
            } catch (Throwable e) {
                logger.error(e.getMessage());
            }
            try {
                getPolicyManager().addPolicyTask(new JobPurgeTask(this));
            } catch (Throwable e) {
                logger.error(e.getMessage());
            }
            try {
                getPolicyManager().addPolicyTask(new JobSubmissionTask(this));
            } catch (Throwable e) {
                logger.error(e.getMessage());
            }
            try {
                getPolicyManager().addPolicyTask(new GetJobSTDTask(this));
            } catch (Throwable e) {
                logger.error(e.getMessage());
            }
        }
    }

    private void setLeaseExpired(Job job) {
        if (job == null || job.getLease() == null) {
            return;
        }

        try {
            jobDB.setLeaseExpired(job.getId(), job.getLease());
        } catch (Throwable e) {
            logger.error("setLeaseExpired: " + e.getMessage());
        }
    }

    /**
     * Initialises the socket connection.
     * 
     * @throws JobManagementException
     */
    private void initSocket() throws JobManagementException {
        if (socket == null) {
            // Create a socket object for communicating
            String sensorHost = getParameterValueAsString(CREAM_JOB_SENSOR_HOST);
            String sensorPort = getParameterValueAsString(CREAM_JOB_SENSOR_PORT);

            if (sensorHost == null) {
                throw new JobManagementException("CREAM_JOB_SENSOR_HOST parameter not specified!");
            }

            logger.debug("initSocket: CREAM_JOB_SENSOR_HOST = " + sensorHost + " CREAM_JOB_SENSOR_PORT = " + sensorPort);

            try {
                int sensorPortNumber = sensorPort != null ? Integer.parseInt(sensorPort) : 9909;

                socket = new Socket();
                socket.connect(new InetSocketAddress(sensorHost, sensorPortNumber), 500);

                oos = new ObjectOutputStream(socket.getOutputStream());
            } catch (Exception e) {
                throw new JobManagementException(e.getMessage());
            }
            logger.info("initSocket: created socket for host=" + sensorHost + ":" + sensorPort);
        }
    }

    /**
     * Sends a notification to the server serializing current <code>Job</code>
     * object.
     * 
     * @param jobId
     *            The Job id for current job.
     * @throws JobManagementException
     */
    protected void sendNotification(String jobId) throws JobManagementException {
        if (jobId == null && jobDB == null) {
            return;
        }

        try {
            sendNotification(jobDB.retrieveJob(jobId, null));
        } catch (Exception e) {
            logger.error("sendNotification error: " + e.getMessage());
            throw new JobManagementException(e.getMessage());
        }
    }

    /**
     * Sends a notification to the server serializing current <code>Job</code>
     * object.
     * 
     * @param job
     *            The Job to be notified.
     * @throws JobManagementException
     */
    protected void sendNotification(Job job) throws JobManagementException {
        if (job == null) {
            return;
        }

        if (socket == null) {
            initSocket();
        }

        if (socket.isConnected()) {
            synchronized (socket) {
                try {
                    logger.debug("sendNotification: Retrieved job for jobId=" + job.getId());
                    job.writeExternal(oos);
                    logger.debug("sendNotification: writeExternal perfomed");
                    oos.flush();
                } catch (Throwable e) {
                    logger.error("sendNotification error: " + e.getMessage());
                    if (socket != null && !socket.isClosed()) {
                        try {
                            socket.close();
                        } catch (IOException e1) {
                            throw new JobManagementException(e1.getMessage());
                        }
                    }
                    socket = null;

                    if (oos != null) {
                        try {
                            oos.close();
                        } catch (IOException e1) {
                            throw new JobManagementException(e1.getMessage());
                        }
                    }
                } finally {
                    if (socket != null) {
                        socket.notifyAll();
                    }
                }
            }
        } else {
            logger.warn("sendNotification: the socket is NOT connected");
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    throw new JobManagementException(e.getMessage());
                }
            }
            socket = null;

            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    throw new JobManagementException("socket is not connected");
                }
            }
        }
    }

    protected ProxyCertificate makeLocalCopyProxyCertificateFile(String id, String userId, String destProxyCertPath, boolean copyToSandbox) throws IllegalArgumentException, CommandException {
        if (id == null) {
            throw new IllegalArgumentException("certificate id not specified!");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId not specified!");
        }
//        if (destProxyCertPath == null) {
//            throw new IllegalArgumentException("destProxyCertPath not specified!");
//        }

        ProxyCertificate proxyCertificate = null;

        if (copyToSandbox) {
            ProxyCertificateStorageInterface proxyStorage = ProxyCertificateDBManager.getInstance();
            try {
                proxyCertificate = proxyStorage.getProxyCertificate(id, userId);
            } catch (ProxyCertificateException e) {
                throw new CommandException(e.getMessage());
            }

            if (proxyCertificate == null) {
                throw new CommandException("proxy certificate [id=" + id + "] [userId=" + userId + "] not found!");
            }

            if (proxyCertificate.getCertificate() == null || proxyCertificate.getCertificate().length() == 0) {
                throw new CommandException("the proxy certificate [id=" + id + "] [userId=" + userId + "] is empty!");
            }

            String proxyPath = getParameterValueAsString("CREAM_USER_PROXY_PATH");
            if (proxyPath == null) {
                throw new CommandException("parameter \"CREAM_USER_PROXY_PATH\" not defined!");
            }

            proxyPath += File.separator + userId + File.separator + ProxyCertificateType.AUTHENTICATION.getName();

            String[] envp = new String[] { "GLEXEC_MODE=lcmaps_get_account", "GLEXEC_CLIENT_CERT=" + proxyPath };
            String[] cmd = new String[] { getParameterValueAsString("GLEXEC_BIN_PATH"), getParameterValueAsString("CREAM_MAKE_CERTIFICATE_FILE_BIN_PATH"), getParameterValueAsString("CREAM_USER_SANDBOX_PATH"), userId, id };

            Process proc = null;
            try {
                proc = Runtime.getRuntime().exec(cmd, envp);

                BufferedOutputStream os = new BufferedOutputStream(proc.getOutputStream());
                os.write(proxyCertificate.getCertificate().getBytes());
                os.flush();
                os.close();
            } catch (Throwable e) {
                logger.error(e);
                throw new CommandException(e.getMessage());
            } finally {
                if (proc != null) {
                    try {
                        proc.waitFor();
                    } catch (InterruptedException e) {
                    }
                    
                    StringBuffer errorMessage = null;

                    if (proc.exitValue() != 0) {
                        BufferedReader readErr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

                        errorMessage = new StringBuffer();
                        String inputLine = null;

                        try {
                            while ((inputLine = readErr.readLine()) != null) {
                                errorMessage.append(inputLine);
                            }
                        } catch (IOException ioe) {}
                    }

                    try {
                        proc.getInputStream().close();
                        proc.getErrorStream().close();
                        proc.getOutputStream().close();
                    } catch (IOException e) {
                    }

                    if (errorMessage != null && errorMessage.length() > 0) {
                        throw new CommandException(errorMessage.toString());
                    }
                }
            }
        } else {
            if (destProxyCertPath == null) {
                throw new IllegalArgumentException("destProxyCertPath not specified!");
            }
            
            File proxyCertFile = new File(destProxyCertPath);
            if (!proxyCertFile.exists() || (Calendar.getInstance().getTimeInMillis() - proxyCertFile.lastModified() > 600000)) {
                ProxyCertificateStorageInterface proxyStorage = ProxyCertificateDBManager.getInstance();
                try {
                    proxyCertificate = proxyStorage.getProxyCertificate(id, userId);
                } catch (ProxyCertificateException e) {
                    throw new CommandException(e.getMessage());
                }

                if (proxyCertificate == null) {
                    throw new CommandException("proxy certificate [id=" + id + "] [userId=" + userId + "] not found!");
                }

                if (proxyCertificate.getCertificate() == null || proxyCertificate.getCertificate().length() == 0) {
                    throw new CommandException("the proxy certificate [id=" + id + "] [userId=" + userId + "] is empty!");
                }

                try {
                    proxyCertFile.getParentFile().mkdirs();
                } catch (Exception e) {
                    throw new CommandException("system error: cannot create the authN proxy dir (" + destProxyCertPath + ")! Reason = " + e.toString());
                }

                try {
                    CEUtils.makeFile(destProxyCertPath, proxyCertificate.getCertificate(), false, true);

                    logger.debug("Written proxy: " + destProxyCertPath + "  " + Calendar.getInstance().getTime());
                } catch (Exception e) {
                    throw new CommandException("cannot write the authN cert to file: " + e.getMessage());
                }
            }
        }

        return proxyCertificate;
    }
}
