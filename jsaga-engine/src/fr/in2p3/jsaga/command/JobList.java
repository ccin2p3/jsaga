package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.job.JobFactory;
import org.ogf.saga.job.JobService;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.util.List;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   JobList
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   1 oct. 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class JobList extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";

    protected JobList() {
        super("jsaga-job-list", new String[]{"resource"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        JobList command = new JobList();
        CommandLine line = command.parse(args);
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else
        {
            // get arguments
            URL serviceURL = URLFactory.createURL(command.m_nonOptionValues[0]);

            // get status
            Session session = SessionFactory.createSession(true);
            JobService service = JobFactory.createJobService(session, serviceURL);

            // dump list
            List<String> list = service.list();
            for (String jobid : list) {
                System.out.println(jobid);
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

        // returns
        return opt;
    }
}
