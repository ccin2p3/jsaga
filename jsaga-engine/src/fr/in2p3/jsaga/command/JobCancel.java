package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.job.*;
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
* File:   JobCancel
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobCancel extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";

    protected JobCancel() {
        super("jsaga-job-cancel", new String[]{"jobId"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        JobCancel command = new JobCancel();
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

            // cancel job
            Session session = SessionFactory.createSession(true);
            JobService service = JobFactory.createJobService(session, serviceURL);
            Job job = service.getJob(nativeJobId);
            job.cancel();
            job.waitFor();

            // display status
            State state = job.getState();
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
