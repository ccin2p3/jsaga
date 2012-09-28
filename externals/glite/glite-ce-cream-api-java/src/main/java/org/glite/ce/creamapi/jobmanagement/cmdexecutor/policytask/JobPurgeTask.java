package org.glite.ce.creamapi.jobmanagement.cmdexecutor.policytask;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.glite.ce.creamapi.cmdmanagement.Command;
import org.glite.ce.creamapi.cmdmanagement.CommandExecutor;
import org.glite.ce.creamapi.cmdmanagement.Policy;
import org.glite.ce.creamapi.cmdmanagement.PolicyException;
import org.glite.ce.creamapi.cmdmanagement.PolicyTask;
import org.glite.ce.creamapi.jobmanagement.JobCommandConstant;
import org.glite.ce.creamapi.jobmanagement.JobStatus;
import org.glite.ce.creamapi.jobmanagement.cmdexecutor.AbstractJobExecutor;

public class JobPurgeTask extends PolicyTask {
    private static final Logger logger = Logger.getLogger(JobPurgeTask.class.getName());
    public static final String name = "JOB_PURGE_TASK";


    public static enum JOB_STATUS {
        ABORTED("ABORTED", JobStatus.ABORTED), CANCELLED("CANCELLED", JobStatus.CANCELLED), REGISTERED("REGISTERED", JobStatus.REGISTERED), DONE_FAILED("DONE-FAILED",
                JobStatus.DONE_FAILED), DONE_OK("DONE-OK", JobStatus.DONE_OK);

        private String name;
        private int id;

        JOB_STATUS(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }
    }

    public JobPurgeTask(CommandExecutor executor) {
        super(name, executor);
    }

    // private Calendar parseDate(String date) throws IllegalArgumentException {
    // if (date == null) {
    // throw new IllegalArgumentException("date not specified!");
    // }
    //
    // date = date.trim();
    //
    // GregorianCalendar todate = new GregorianCalendar();
    //
    // if (date.endsWith("minutes")) {
    // todate.add(Calendar.MINUTE, -Integer.parseInt(date.substring(0,
    // date.indexOf("minutes")).trim()));
    // } else if (date.endsWith("hours")) {
    // todate.add(Calendar.HOUR_OF_DAY, -Integer.parseInt(date.substring(0,
    // date.indexOf("hours")).trim()));
    // } else if (date.endsWith("days")) {
    // todate.add(Calendar.DAY_OF_YEAR, -Integer.parseInt(date.substring(0,
    // date.indexOf("days")).trim()));
    // } else if (date.endsWith("months")) {
    // todate.add(Calendar.MONTH, -Integer.parseInt(date.substring(0,
    // date.indexOf("months")).trim()));
    // } else if (date.endsWith("years")) {
    // todate.add(Calendar.YEAR, -Integer.parseInt(date.substring(0,
    // date.indexOf("years")).trim()));
    // } else {
    // throw new IllegalArgumentException("illegal date format: " + date +
    // "! use: time_value { days || months || years }");
    // }
    //
    // return todate;
    // }

    // private void parsePolicy(String policy) throws IllegalArgumentException,
    // Exception {
    // if (policy == null) {
    // throw new IllegalArgumentException("policy not specified!");
    // }
    //
    // // String[] op = new String[] { "<=", ">=", "=", "<", ">" };
    // String[] op = new String[] { "<", "=" };
    // for (int x = 0; x < op.length; x++) {
    // int index = policy.indexOf(op[x]);
    // if (index < 0) {
    // continue;
    // }
    //
    // String status = policy.substring(0, index).trim();
    //
    // if (status == null || status.length() == 0) {
    // throw new Exception("job status not specified!");
    // }
    //
    // JOB_STATUS[] jobStatusList = JOB_STATUS.values();
    // for (int i = 0; i < jobStatusList.length; i++) {
    // if (jobStatusList[i].toString().equalsIgnoreCase(status)) {
    // jobStatus = jobStatusList[i];
    // break;
    // }
    // }
    //
    // if (jobStatus == null) {
    // throw new Exception("invalid job status name: " + status +
    // "! use { ABORTED | CANCELLED | DONE-FAILED | DONE-OK | REGISTERED }");
    // }
    //
    // if (index + op[x].length() >= policy.length()) {
    // throw new Exception("date not specified!");
    // }
    //
    // String dateValue = policy.substring(index + op[x].length(),
    // policy.length()).trim();
    //
    // if (dateValue == null || dateValue.length() == 0) {
    // throw new Exception("date not specified!");
    // }
    //
    // date = parseDate(dateValue);
    //
    // this.op = op[x];
    // return;
    // }
    //
    // throw new Exception("invalid operator: use { < | = }");
    // //throw new Exception("invalid operator: use { <= | >= | = | < | > }");
    // }

    public void execute(Policy policy) throws PolicyException {
        if (policy == null || policy.getValue() == null) {
            throw new PolicyException("policy not specified!");
        }

        AbstractJobExecutor executor = (AbstractJobExecutor) getCommandExecutor();

        if (executor == null) {
            throw new PolicyException("executor not found!");
        }

        String policyValue = policy.getValue();

        int index = policyValue.indexOf(" ");
        if (index < 0) {
            throw new PolicyException("malformed policy: " + policy);
        }

        String status = policyValue.substring(0, index).trim();

        if (status == null || status.length() == 0) {
            throw new PolicyException("job status not specified!");
        }

        JOB_STATUS[] jobStatusArray = JOB_STATUS.values();
        JOB_STATUS jobStatus = null;

        for (int i = 0; i < jobStatusArray.length; i++) {
            if (jobStatusArray[i].getName().equalsIgnoreCase(status)) {
                jobStatus = jobStatusArray[i];
                break;
            }
        }

        if (jobStatus == null) {
            throw new PolicyException("invalid job status name: " + status + "! use { ABORTED | CANCELLED | DONE-FAILED | DONE-OK | REGISTERED }");
        }

        if (index >= policyValue.length()) {
            throw new PolicyException("date not specified!");
        }

        String dateValue = policyValue.substring(index, policyValue.length()).trim();

        if (dateValue == null || dateValue.length() == 0) {
            throw new PolicyException("date not specified!");
        }

        GregorianCalendar date = new GregorianCalendar();

        if (dateValue.endsWith("minutes")) {
            date.add(Calendar.MINUTE, -Integer.parseInt(dateValue.substring(0, dateValue.indexOf("minutes")).trim()));
        } else if (dateValue.endsWith("hours")) {
            date.add(Calendar.HOUR_OF_DAY, -Integer.parseInt(dateValue.substring(0, dateValue.indexOf("hours")).trim()));
        } else if (dateValue.endsWith("days")) {
            date.add(Calendar.DAY_OF_YEAR, -Integer.parseInt(dateValue.substring(0, dateValue.indexOf("days")).trim()));
        } else if (dateValue.endsWith("months")) {
            date.add(Calendar.MONTH, -Integer.parseInt(dateValue.substring(0, dateValue.indexOf("months")).trim()));
        } else if (dateValue.endsWith("years")) {
            date.add(Calendar.YEAR, -Integer.parseInt(dateValue.substring(0, dateValue.indexOf("years")).trim()));
        } else {
            throw new PolicyException("illegal date format: " + date + "! use: time_value { days || months || years }");
        }

        try {
            int[] purgeCompatibleStatus = new int[] { jobStatus.getId() };

            List<String> jobIdList = executor.getJobDB().retrieveJobId(null, null, purgeCompatibleStatus, null, date);

            logger.info("purging " + jobIdList.size() + " jobs with status " + jobStatus.getName() + " <= " + date.getTime());

            for (String jobId : jobIdList) {
                Command purgeCmd = new Command(JobCommandConstant.cmdName[JobCommandConstant.JOB_PURGE], JobCommandConstant.JOB_MANAGEMENT);
                purgeCmd.setUserId("ADMIN");
                purgeCmd.setDescription("command executed by CREAM's job purger");
                purgeCmd.setAsynchronous(true);
                purgeCmd.setCommandGroupId(jobId);
                purgeCmd.addParameter("JOB_ID", jobId);
                purgeCmd.addParameter("IS_ADMIN", Boolean.toString(true));
                purgeCmd.setPriorityLevel(Command.LOW_PRIORITY);

                executor.getCommandManager().insertCommand(purgeCmd);
            }
        } catch (Throwable t) {
            throw new PolicyException(t.getMessage());
        }
    }
}