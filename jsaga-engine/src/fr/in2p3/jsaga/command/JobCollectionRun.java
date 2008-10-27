package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.helpers.XMLFileParser;
import fr.in2p3.jsaga.jobcollection.*;
import org.apache.commons.cli.*;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.WaitMode;

import java.io.File;
import java.io.FileNotFoundException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobCollectionRun
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobCollectionRun extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    // optional
    private static final String OPT_NAME = "n", LONGOPT_NAME = "name";
    private static final String OPT_LANGUAGE = "l", LONGOPT_LANGUAGE = "language";
    private static final String OPT_RESOURCES = "r", LONGOPT_RESOURCES = "resources";
    private static final String OPT_BATCH = "b", LONGOPT_BATCH = "batch";
    private static final String OPT_CLEANUP = "c", LONGOPT_CLEANUP = "cleanup";
    // options group
    private static final String OPT_DUMP_JSDL = "j", LONGOPT_DUMP_JSDL = "dump-jsdl";
    private static final String OPT_DUMP_STAGING = "s", LONGOPT_DUMP_STAGING = "dump-staging";
    private static final String OPT_DUMP_WRAPPER = "w", LONGOPT_DUMP_WRAPPER = "dump-wrapper";
    private static final String OPT_GRAPHVIZ = "g", LONGOPT_GRAPHVIZ = "graph";

    protected JobCollectionRun() {
        super("jsaga-jobcollection-run", new String[]{"jobCollection"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        JobCollectionRun command = new JobCollectionRun();
        CommandLine line = command.parse(args);
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else
        {
            // get arguments
            String language = (line.hasOption(OPT_LANGUAGE) ? line.getOptionValue(OPT_LANGUAGE) : "JSDL");
            File jobCollectionFile = new File(command.m_nonOptionValues[0]);
            if (!jobCollectionFile.exists()) {
                throw new FileNotFoundException("File not found: "+jobCollectionFile.getAbsolutePath());
            }
            File resourcesFile = null;
            if (line.hasOption(OPT_RESOURCES)) {
                resourcesFile = new File(line.getOptionValue(OPT_RESOURCES));
                if (!resourcesFile.exists()) {
                    throw new FileNotFoundException("File not found: "+resourcesFile.getAbsolutePath());
                }
            }

            // create job collection description
            JobCollectionDescription desc;
            if (line.hasOption(OPT_NAME)) {
                desc = JobCollectionFactory.createJobCollectionDescription(language, jobCollectionFile, line.getOptionValue(OPT_NAME));
            } else {
                desc = JobCollectionFactory.createJobCollectionDescription(language, jobCollectionFile);
            }

            if (line.hasOption(OPT_DUMP_JSDL)) {
                XMLFileParser.dump(desc.getAsDocument(), System.out);
            } else {
                // create job collection
                Session session = SessionFactory.createSession(true);
                JobCollectionManager manager = JobCollectionFactory.createJobCollectionManager(session);
                boolean force = line.hasOption(OPT_CLEANUP);
                JobCollection jobCollection = manager.createJobCollection(desc, force);
                if (resourcesFile != null) {
                    jobCollection.allocateResources(resourcesFile);
                }

                if (line.hasOption(OPT_DUMP_STAGING)) {
                    XMLFileParser.dump(jobCollection.getStatesAsXML(), System.out);
                } else if (line.hasOption(OPT_DUMP_WRAPPER)) {
                    String jobName = line.getOptionValue(OPT_DUMP_WRAPPER);
                    JobWithStaging job = findJob(jobCollection, jobName);
                    System.out.println(job.getWrapper());
                } else if (line.hasOption(OPT_GRAPHVIZ)) {
                    GraphGenerator generator = new GraphGenerator(jobCollection.getJobCollectionName(), jobCollection.getStatesAsXML());
                    File statusGraph = generator.generateStatusGraph();
                    File stagingGraph = generator.generateStagingGraph();
                    System.out.println("Graphs successfully generated:");
                    System.out.println("  "+statusGraph.getAbsolutePath());
                    System.out.println("  "+stagingGraph.getAbsolutePath());
                } else {
                    // submit job collection
                    jobCollection.run();

                    if (line.hasOption(OPT_BATCH)) {
                        System.out.println(jobCollection.getJobCollectionName());
                    } else {
                        // wait
                        while (jobCollection.size() > 0) {
                            Task finishedTask = jobCollection.waitFor(WaitMode.ANY);
                            System.out.println("Job finished with state: "+finishedTask.getState().name());
                        }
                    }
                }

                // cleanup job collection
                jobCollection.cleanup();
            }
        }
    }
    private static JobWithStaging findJob(JobCollection jobCollection, String jobName) throws Exception {
        Task[] array = jobCollection.getTasks();
        for (int i=0; i<array.length; i++) {
            JobWithStaging job = (JobWithStaging) array[i];
            if (jobName.equals(job.getJobDescription().getAttribute("JobName"))) {
                return job;
            }
        }
        throw new DoesNotExistException("Job not found in collection: "+jobName);
    }

    protected Options createOptions() {
        Options opt = new Options();

        // command
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));

        // optional
        opt.addOption(OptionBuilder.withDescription("The name of the job collection (override name in job description)")
                .hasArg()
                .withArgName("name")
                .withLongOpt(LONGOPT_NAME)
                .create(OPT_NAME));
        opt.addOption(OptionBuilder.withDescription("The language used for job description (default=JSDL)")
                .hasArg()
                .withArgName("id")
                .withLongOpt(LONGOPT_LANGUAGE)
                .create(OPT_LANGUAGE));
        opt.addOption(OptionBuilder.withDescription("Selected resources file")
                .hasArg()
                .withArgName("file")
                .withLongOpt(LONGOPT_RESOURCES)
                .create(OPT_RESOURCES));
        opt.addOption(OptionBuilder.withDescription("Exit immediatly after having submitted the job collection, " +
                "and print the collection name on the standard output.")
                .withLongOpt(LONGOPT_BATCH)
                .create(OPT_BATCH));
        opt.addOption(OptionBuilder.withDescription("Cleanup previous execution of job collection")
                .withLongOpt(LONGOPT_CLEANUP)
                .create(OPT_CLEANUP));

        // options group
        OptionGroup group = new OptionGroup();
        group.setRequired(false);
        {
            group.addOption(OptionBuilder.withDescription("Dump generated JSDL document and exit")
                    .withLongOpt(LONGOPT_DUMP_JSDL)
                    .create(OPT_DUMP_JSDL));
            group.addOption(OptionBuilder.withDescription("Dump generated staging graph and exit")
                    .withLongOpt(LONGOPT_DUMP_STAGING)
                    .create(OPT_DUMP_STAGING));
            group.addOption(OptionBuilder.withDescription("Dump generated wrapper script and exit")
                    .hasArg()
                    .withArgName("JobName")
                    .withLongOpt(LONGOPT_DUMP_WRAPPER)
                    .create(OPT_DUMP_WRAPPER));
            group.addOption(OptionBuilder.withDescription("Generate status and staging graphs")
                    .withLongOpt(LONGOPT_GRAPHVIZ)
                    .create(OPT_GRAPHVIZ));
        }
        opt.addOptionGroup(group);
        
        // returns
        return opt;
    }
}
