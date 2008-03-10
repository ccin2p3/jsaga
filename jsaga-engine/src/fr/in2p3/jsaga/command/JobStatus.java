package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.URL;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.job.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.State;

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

    protected JobStatus() {
        super("jsaga-job-status", new String[]{"jobId"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        JobStatus command = new JobStatus();
        CommandLine line = command.parse(args);

        System.setProperty("saga.factory", "fr.in2p3.jsaga.impl.SagaFactoryImpl");
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
                serviceURL = URLFactory.create(matcher.group(1));
                nativeJobId = matcher.group(2);
            } else {
                throw new BadParameter("Job ID does not match regular expression: "+pattern.pattern());
            }

            // get status
            Session session = SessionFactory.createSession(true);
            JobService service = JobFactory.createJobService(session, serviceURL);
            Job job = service.getJob(nativeJobId);
            State state = job.getState();

            // display status
            if (State.RUNNING.compareTo(state) == 0) {
                System.out.println("Job is running.");
            } else if (State.SUSPENDED.compareTo(state) == 0) {
                System.out.println("Job is suspended.");
            } else if (State.DONE.compareTo(state) == 0) {
                System.out.println("Job completed successfully.");
            } else if (State.CANCELED.compareTo(state) == 0) {
                System.out.println("Job canceled.");
            } else if (State.FAILED.compareTo(state) == 0) {
                System.out.println("Job failed with "+job.getAttribute("ExitCode"));
            } else {
                throw new Exception("Unexpected state: "+ state);
            }
        }
    }

    protected Options createOptions() {
        Options opt = new Options();

        // command
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));

        // returns
        return opt;
    }
}
