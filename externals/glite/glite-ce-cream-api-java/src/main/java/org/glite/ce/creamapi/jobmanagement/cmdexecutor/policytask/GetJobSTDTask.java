package org.glite.ce.creamapi.jobmanagement.cmdexecutor.policytask;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.glite.ce.creamapi.cmdmanagement.CommandExecutor;
import org.glite.ce.creamapi.cmdmanagement.Policy;
import org.glite.ce.creamapi.cmdmanagement.PolicyException;
import org.glite.ce.creamapi.cmdmanagement.PolicyTask;
import org.glite.ce.creamapi.jobmanagement.Job;
import org.glite.ce.creamapi.jobmanagement.JobStatus;
import org.glite.ce.creamapi.jobmanagement.cmdexecutor.AbstractJobExecutor;

public class GetJobSTDTask extends PolicyTask {
    private static final Logger logger = Logger.getLogger(GetJobSTDTask.class.getName());
    public static final String name = "GET_JOB_STD_TASK";
    
    public GetJobSTDTask(CommandExecutor executor) {
        super(name, executor);
    }


    public void execute(Policy policy) throws PolicyException {
        if(policy == null || policy.getValue() == null) {
            throw new PolicyException("policy not specified!");
        }

        AbstractJobExecutor executor = (AbstractJobExecutor) getCommandExecutor();

        if (executor == null) {
            throw new PolicyException("executor not found!");
        }
                
        String jobId = policy.getValue();
                
        Job job = null;
        try {
            job = executor.getJobDB().retrieveJob(jobId, null);
        } catch (Throwable e) {
            throw new PolicyException(e.getMessage());
        }
        
        if (job == null) {
            throw new PolicyException("job [jobId=" + jobId + "] not found!");
        }

        JobStatus lastStatus = job.getLastStatus();
        if (lastStatus == null) {
            throw new PolicyException("last status not found for the job " + jobId);
        }

        boolean update = false;

        if (lastStatus.getExitCode() != null && lastStatus.getExitCode().equals("W")) {
            try {
                String exitCode = getExitCode(job.getWorkingDirectory() + "/StandardOutput", job.getAuthNProxyCertPath());
                lastStatus.setExitCode(exitCode);                
            } catch (Exception e) {
                lastStatus.setExitCode(Job.NOT_AVAILABLE_VALUE);
            } finally {
                update = true;
            }
        }

        if (lastStatus.getType() == JobStatus.DONE_FAILED || lastStatus.getType() == JobStatus.CANCELLED) {
            try {
                String errorMessage = lastStatus.getFailureReason();
                if (errorMessage != null) {
                    errorMessage += "; ";
                }

                errorMessage += readFile(job.getWorkingDirectory() + "/StandardError", job.getAuthNProxyCertPath());
                lastStatus.setFailureReason(errorMessage);
            } catch (Exception e) {
                if (lastStatus.getFailureReason() == null || lastStatus.getFailureReason().length() == 0) {
                    lastStatus.setFailureReason(Job.NOT_AVAILABLE_VALUE);
                }
            } finally {
                update = true;
            }
        }

        if (update) {
            try {
                executor.doOnJobStatusChanged(lastStatus);
            } catch (Throwable e) {
                throw new PolicyException(e.getMessage());
            }
        }
    }

    private String getExitCode(String filePath, String proxyPath) throws IllegalArgumentException, Exception {
        logger.debug("retrieving the exit code from file [path=" + filePath + "]");
        
        String exitCode = readFile(filePath, proxyPath);
        
        if (exitCode == null) {
            logger.debug("exit code not found! [path=" + filePath + "]");
            return Job.NOT_AVAILABLE_VALUE;
        }

        logger.debug("found exit code \"" + exitCode + " [path=" + filePath + "]");
        
        String pattern = "job exit status = ";
        
        int index = exitCode.indexOf(pattern);
        if (index > -1) {
            exitCode = exitCode.substring(index + pattern.length());
            exitCode = exitCode.substring(0, exitCode.indexOf(" "));
            exitCode = exitCode.trim();
        } else {
            exitCode = Job.NOT_AVAILABLE_VALUE;
        }
        
        return exitCode;
    }

    private String readFile(String stdErrorFilePath, String proxyPath) throws IllegalArgumentException, InterruptedException, IOException, FileNotFoundException {
        String message = "";

        if (stdErrorFilePath == null) {
            throw (new IllegalArgumentException("stdErrorFilePath is null"));
        }

        if (proxyPath == null) {
            throw (new IllegalArgumentException("proxyPath is null"));
        }

        AbstractJobExecutor executor = (AbstractJobExecutor) getCommandExecutor();
        
        String[] envp = new String[] { "GLEXEC_MODE=lcmaps_get_account", "GLEXEC_CLIENT_CERT=" + proxyPath };
        String[] cmd = new String[] { executor.getParameterValueAsString("GLEXEC_BIN_PATH"), executor.getParameterValueAsString("GLEXEC_CAT_CMD_PATH"), stdErrorFilePath };

        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec(cmd, envp);

            BufferedReader readIn = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            
            String strLine = null;
            while ((strLine = readIn.readLine()) != null) {
                message += strLine + " ";
            }
            proc.waitFor();
        } catch (Exception e) {
            if (proc != null) {
                proc.destroy();
            }
        } finally {
            if (proc != null) {
                proc.getErrorStream().close();
                proc.getInputStream().close();
                proc.getOutputStream().close();
            }
        }

        if (message.length() == 0) {
            throw (new FileNotFoundException("file \"" + stdErrorFilePath + " not found!"));
        }

        return message.trim();
    }
}
