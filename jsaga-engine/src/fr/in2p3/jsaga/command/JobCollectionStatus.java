package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.jobcollection.*;
import org.apache.commons.cli.*;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobCollectionStatus
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobCollectionStatus extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    // optional arguments
    private static final String OPT_GRAPHVIZ = "g", LONGOPT_GRAPHVIZ = "graph";

    protected JobCollectionStatus() {
        super("jsaga-jobcollection-status", new String[]{"collectionName"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        JobCollectionStatus command = new JobCollectionStatus();
        CommandLine line = command.parse(args);
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else
        {
            // get arguments
            String collectionName = command.m_nonOptionValues[0];

            // get status
            JobCollectionManager manager = JobCollectionFactory.createJobCollectionManager(null);
            JobCollection jobCollection = manager.getJobCollection(collectionName);
            Document xmlStatus = jobCollection.getStatesAsXML();
            if (line.hasOption(OPT_GRAPHVIZ)) {
                GraphGenerator generator = new GraphGenerator(collectionName, xmlStatus);
                File statusGraph = generator.generateStatusGraph();
                System.out.println("Graph successfully generated: "+statusGraph.getAbsolutePath());
            } else {
                TransformerFactory.newInstance().newTransformer().transform(
                        new DOMSource(xmlStatus),
                        new StreamResult(System.out));
            }
        }
    }

    protected Options createOptions() {
        Options opt = new Options();

        // command
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));

        // optional arguments
        opt.addOption(OptionBuilder.withDescription("Generate status graph")
                .withLongOpt(LONGOPT_GRAPHVIZ)
                .create(OPT_GRAPHVIZ));

        // returns
        return opt;
    }
}
