package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;
import org.ogf.saga.job.*;
import org.ogf.saga.monitoring.Callback;
import org.ogf.saga.monitoring.Metric;
import org.ogf.saga.monitoring.Monitorable;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.State;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobStatus
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   8 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobStatus extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    private static final String OPT_MONITOR = "m", LONGOPT_MONITOR = "monitor";

    protected JobStatus() {
        super("jsaga-job-status", new String[]{"jobId"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        JobStatus command = new JobStatus();
        CommandLine line = command.parse(args);
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else
        {
            // get arguments
            URL serviceURL;
            String nativeJobId;
            Pattern pattern = Pattern.compile("\\[(.*)\\]-\\[(.*)\\]");
            Matcher matcher = pattern.matcher(command.m_nonOptionValues[0]);
            if (matcher.find()) {
                serviceURL = URLFactory.createURL(matcher.group(1));
                nativeJobId = matcher.group(2);
            } else {
                throw new BadParameterException("Job ID does not match regular expression: "+pattern.pattern());
            }

            // get status
            Session session = SessionFactory.createSession(true);
            JobService service = JobFactory.createJobService(session, serviceURL);
            Job job = service.getJob(nativeJobId);

            if (line.hasOption(OPT_MONITOR)) {
                Metric metric = job.getMetric(Job.JOB_STATE);
                metric.addCallback(new Callback(){
                    public boolean cb(Monitorable mt, Metric metric, Context ctx) throws NotImplementedException, AuthorizationFailedException {
                        try {
                            String value = metric.getAttribute(Metric.VALUE);
                            System.out.println("Current state: "+value);
                        } catch (NotImplementedException | AuthorizationFailedException e) {
                            throw e;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // callback must stay registered
                        return true;
                    }
                });
                job.waitFor();
            }
            
            State state = job.getState();

            // display status
            if (State.RUNNING.compareTo(state) == 0) {
                System.out.println("Job is running.");
            } else if (State.SUSPENDED.compareTo(state) == 0) {
                System.out.println("Job is suspended.");
            } else if (State.DONE.compareTo(state) == 0) {
                System.out.println("Job done.");
            } else if (State.CANCELED.compareTo(state) == 0) {
                System.out.println("Job canceled.");
            } else if (State.FAILED.compareTo(state) == 0) {
                try {
                    String exitCode = job.getAttribute(Job.EXITCODE);
                    System.out.println("Job failed with exit code: "+exitCode);
                } catch(NotImplementedException e) {
                    System.out.println("Job failed.");
                    job.rethrow();
                }
            } else {
                throw new Exception("Unexpected state: "+ state);
            }
            System.exit(0);
        }
    }

    protected Options createOptions() {
        Options opt = new Options();

        // command
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));
        opt.addOption(OptionBuilder.withDescription("Monitor job")
                .isRequired(false)
                .withLongOpt(LONGOPT_MONITOR)
                .create(OPT_MONITOR));

        // returns
        return opt;
    }
}
