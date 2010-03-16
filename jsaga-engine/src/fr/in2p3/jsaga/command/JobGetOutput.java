package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.job.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import fr.in2p3.jsaga.impl.job.instance.JobImpl;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   JobGetOutput
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   1 oct. 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class JobGetOutput extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";

    protected JobGetOutput() {
        super("jsaga-job-get-output", new String[]{"jobId"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        JobGetOutput command = new JobGetOutput();
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

            // get job
            Session session = SessionFactory.createSession(true);
            JobService service = JobFactory.createJobService(session, serviceURL);
            Job job = service.getJob(nativeJobId);

            // execute post-staging and cleanup
            ((JobImpl)job).postStagingAndCleanup();
            System.out.println("Job output have been retrieved successfully (if it exists)");
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
