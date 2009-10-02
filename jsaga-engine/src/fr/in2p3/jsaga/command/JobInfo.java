package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.error.*;
import org.ogf.saga.job.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   JobInfo
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   2 oct. 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class JobInfo extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";

    protected JobInfo() {
        super("jsaga-job-info", new String[]{"jobId"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        JobInfo command = new JobInfo();
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

            // dump info
            System.out.println("State:         "+job.getState().toString());
            System.out.println("Exit code:     "+getAttribute(job, Job.EXITCODE));
            System.out.println("State reason:  "+getCause(job));
            System.out.println("Created time:  "+getAttribute(job, Job.CREATED));
            System.out.println("Started time:  "+getAttribute(job, Job.STARTED));
            System.out.println("Finished time: "+getAttribute(job, Job.FINISHED));
            System.out.println("Execution hosts:");
            String[] hosts = getVectorAttribute(job, Job.EXECUTIONHOSTS);
            for (int i=0; i<hosts.length; i++) {
                System.out.println("\t"+hosts[i]);
            }
        }
    }
    private static String getAttribute(Job job, String key) throws SagaException {
        try {
            return job.getAttribute(key);
        } catch (IncorrectStateException e) {
            return "[not initialized yet]";
        } catch (NotImplementedException e) {
            return "[not supported for this backend]";
        }
    }
    private static String[] getVectorAttribute(Job job, String key) throws SagaException {
        try {
            return job.getVectorAttribute(key);
        } catch (IncorrectStateException e) {
            return new String[]{"[not initialized yet]"};
        } catch (NotImplementedException e) {
            return new String[]{"[not supported for this backend]"};
        }
    }
    private static String getCause(Job job) {
        try {
            job.rethrow();
            return null;
        } catch (SagaException e) {
            return e.getMessage();
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
