package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.helpers.XMLFileParser;
import fr.in2p3.jsaga.jobcollection.*;
import org.apache.commons.cli.*;
import org.ogf.saga.error.BadParameter;
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
    private static final String OPT_LANGUAGE = "l", LONGOPT_LANGUAGE = "language";
    private static final String OPT_RESOURCES = "r", LONGOPT_RESOURCES = "resources";
    // options group
    private static final String OPT_TRANSLATE = "t", LONGOPT_TRANSLATE = "translate";
    private static final String OPT_PREPROCESS = "p", LONGOPT_PREPROCESS = "preprocess";

    protected JobCollectionRun() {
        super("jsaga-jobcollection-run", new String[]{"jobCollection"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        JobCollectionRun command = new JobCollectionRun();
        CommandLine line = command.parse(args);

        System.setProperty("saga.factory", "fr.in2p3.jsaga.impl.SagaFactoryImpl");
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
            JobCollectionDescription desc = JobCollectionFactory.createJobCollectionDescription(language, jobCollectionFile);

            if (line.hasOption(OPT_TRANSLATE)) {
                XMLFileParser.dump(desc.getAsDocument(), System.out);
            } else {
                // create job collection
                Session session = SessionFactory.createSession(true);
                JobCollectionManager manager = JobCollectionFactory.createJobCollectionManager(session);
                JobCollection jobCollection = manager.createJobCollection(desc);

                if (line.hasOption(OPT_PREPROCESS)) {
                    if (resourcesFile != null) {
                        jobCollection.allocateResources(resourcesFile);
                    } else {
                        throw new BadParameter("Option --"+LONGOPT_PREPROCESS+" requires option --"+LONGOPT_RESOURCES);
                    }
                } else {
                    // submit job collection
                    jobCollection.run();
                    if (resourcesFile != null) {
                        jobCollection.allocateResources(resourcesFile);
                    }

                    // wait
                    while (jobCollection.size() > 0) {
                        Task finishedTask = jobCollection.waitFor(WaitMode.ANY);
                        System.out.println("Job finished with state: "+finishedTask.getState().name());
                    }
                }
            }
        }
    }

    protected Options createOptions() {
        Options opt = new Options();

        // command
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));

        // optional
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

        // options group
        OptionGroup group = new OptionGroup();
        group.setRequired(false);
        {
            group.addOption(OptionBuilder.withDescription("Translate job collection to JSDL and exit")
                    .withLongOpt(LONGOPT_TRANSLATE)
                    .create(OPT_TRANSLATE));
            group.addOption(OptionBuilder.withDescription("Preprocess job collection and exit")
                    .withLongOpt(LONGOPT_PREPROCESS)
                    .create(OPT_PREPROCESS));
        }
        opt.addOptionGroup(group);
        
        // returns
        return opt;
    }
}
