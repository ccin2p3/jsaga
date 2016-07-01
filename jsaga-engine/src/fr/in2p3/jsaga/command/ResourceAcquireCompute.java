package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.job.JobFactory;
import org.ogf.saga.job.JobService;
import org.ogf.saga.resource.ResourceFactory;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.instance.Compute;
import org.ogf.saga.resource.manager.ResourceManager;
import org.ogf.saga.resource.task.State;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   ResourceAcquireCompute
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   24 feb 2016
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class ResourceAcquireCompute extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    // required arguments
    private static final String OPT_RESOURCE = "r", LONGOPT_RESOURCE = "resource";
    // optional arguments
    private static final String OPT_TEMPLATE = "t", LONGOPT_TEMPLATE = "template";

    protected ResourceAcquireCompute() {
        super("jsaga-resource-acquire-compute", null, null, new GnuParser());
    }

    public static void main(String[] args) throws Exception {
        ResourceAcquireCompute command = new ResourceAcquireCompute();
        CommandLine line = command.parse(args);
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else
        {
            // get arguments
            URL serviceURL = URLFactory.createURL(line.getOptionValue(OPT_RESOURCE));
            String templateName = line.getOptionValue(OPT_TEMPLATE);
            Session session = SessionFactory.createSession(true);
            ResourceManager rm = ResourceFactory.createResourceManager(session, serviceURL);
            // Create compute description
            ComputeDescription desc = (ComputeDescription) ResourceFactory.createResourceDescription(Type.COMPUTE);
            desc.setVectorAttribute(ComputeDescription.TEMPLATE, new String[]{templateName});
            // Acquire resource
            Compute computeNode = rm.acquireCompute(desc);
            // Wait until ACTIVE
            computeNode.waitFor(State.ACTIVE);
            // Now we can submit a job
            String computeNodeAccess = computeNode.getAccess()[0];
            URL jobServiceURL = URLFactory.createURL(computeNodeAccess);
            JobService jobService = JobFactory.createJobService(session, jobServiceURL);
            // ...
            // Release the resource
            computeNode.release();
            System.exit(0);
        }
    }
    @SuppressWarnings("static-access")
    protected Options createOptions() {
        Options opt = new Options();

        // command
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));

        // required arguments
        opt.addOption(OptionBuilder.withDescription("the URL of the job service")
                .isRequired(true)
                .hasArg()
                .withArgName("URL")
                .withLongOpt(LONGOPT_RESOURCE)
                .create(OPT_RESOURCE));

        opt.addOption(OptionBuilder.withDescription("The image to use ")
                .isRequired(true)
                .hasArg()
                .withArgName("TEMPLATE")
                .withLongOpt(LONGOPT_TEMPLATE)
                .create(OPT_TEMPLATE));

        // returns
        return opt;
    }
}
