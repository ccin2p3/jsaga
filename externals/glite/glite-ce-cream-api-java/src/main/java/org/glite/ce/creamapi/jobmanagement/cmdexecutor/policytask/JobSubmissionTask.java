package org.glite.ce.creamapi.jobmanagement.cmdexecutor.policytask;

import java.io.StringReader;

import org.apache.log4j.Logger;
import org.glite.ce.creamapi.cmdmanagement.CommandExecutor;
import org.glite.ce.creamapi.cmdmanagement.Policy;
import org.glite.ce.creamapi.cmdmanagement.PolicyException;
import org.glite.ce.creamapi.cmdmanagement.PolicyTask;
import org.glite.ce.creamapi.jobmanagement.JobStatus;
import org.glite.ce.creamapi.jobmanagement.cmdexecutor.AbstractJobExecutor;

import condor.classad.ClassAd;
import condor.classad.ClassAdParser;
import condor.classad.Expr;
import condor.classad.RecordExpr;

public final class JobSubmissionTask extends PolicyTask {
    private static final Logger logger = Logger.getLogger(JobSubmissionTask.class.getName());
    public static final String name = "JOB_SUBMISSION_TASK";
    
    public JobSubmissionTask(CommandExecutor executor) {
        super(name, executor);
    }

    public void execute(Policy policy) throws PolicyException {
        if (policy == null || policy.getValue() == null) {
            throw new PolicyException("policy not specified!");
        }

        System.gc();

        AbstractJobExecutor executor = (AbstractJobExecutor) getCommandExecutor();

        if (executor == null) {
            throw new PolicyException("executor not found!");
        }

        try {
            int[] jobStatus = new int[1];
            jobStatus[0] = JobStatus.PENDING;
            Long jobPending = executor.getJobDB().jobCountByStatus(jobStatus, null);

            jobStatus = new int[2];
            jobStatus[0] = JobStatus.IDLE;
            jobStatus[1] = JobStatus.HELD;
            Long jobIdle = executor.getJobDB().jobCountByStatus(jobStatus, null);

            jobStatus[0] = JobStatus.RUNNING;
            jobStatus[1] = JobStatus.REALLY_RUNNING;
            Long jobRunning = executor.getJobDB().jobCountByStatus(jobStatus, null);

            StringReader input = new StringReader("[jobPending = " + jobPending + ";\njobIdle = " + jobIdle + ";\njobRunning = " + jobRunning + "\n];");
            StringReader input2 = new StringReader("[test = " + policy.getValue() + " ];");

            ClassAdParser parser = new ClassAdParser(input2, ClassAdParser.TEXT);
            parser.enableTracing(false);

            Expr p = parser.parse();
            parser.reset(input);
            parser.enableTracing(false);

            RecordExpr prova = (RecordExpr) parser.parse();

            prova.insertAttribute("test", p.selectExpr("test"));

            Expr val = ClassAd.eval(prova, new String[] { "test" });

            boolean acceptNewJobs = !val.isTrue();
            executor.setAcceptNewJobs(acceptNewJobs);

            logger.info("policy [" + policy.getValue() + "] evaluated: acceptNewJobs = " + acceptNewJobs);

            if (logger.isDebugEnabled()) {
                String status = "\n---------------------------";
                status += "\npolicy = " + policy.getValue();
                status += "\nclassad =\n[\n\tjobPending = " + jobPending + ";\n\tjobIdle = " + jobIdle + ";\n\tjobRunning = " + jobRunning + "\n];";
                status += "\nresult = setAcceptJobSubmissions? " + acceptNewJobs;
                status += "\n---------------------------";
                logger.debug(status);
            }
        } catch (Throwable t) {
            throw new PolicyException(t.getMessage());
        }
    }
}
