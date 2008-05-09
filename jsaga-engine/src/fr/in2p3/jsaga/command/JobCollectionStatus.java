package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.jobcollection.*;
import org.apache.commons.cli.*;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

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

        System.setProperty("saga.factory", "fr.in2p3.jsaga.impl.SagaFactoryImpl");
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
                File statusGraph = new File(Base.JSAGA_VAR, "status.gif");
                generateGraph(
                        xmlStatus,
                        "xsl/graphviz/jsaga-status-graph.xsl",
                        statusGraph);
                File stagingGraph = new File(Base.JSAGA_VAR, "staging.gif");
                generateGraph(
                        xmlStatus,
                        "xsl/graphviz/jsaga-staging-graph.xsl",
                        stagingGraph);
                System.out.println("Graphs successfully generated:");
                System.out.println("  "+statusGraph.getAbsolutePath());
                System.out.println("  "+stagingGraph.getAbsolutePath());
            } else {
                TransformerFactory.newInstance().newTransformer().transform(
                        new DOMSource(jobCollection.getStatesAsXML()),
                        new StreamResult(System.out));
            }
        }
    }

    private static void generateGraph(Document xmlStatus, String stylesheet, File graph) throws Exception {
        ByteArrayOutputStream dotStream = new ByteArrayOutputStream();
        TransformerFactory.newInstance().newTransformer(
                new StreamSource(JobCollectionStatus.class.getClassLoader().getResourceAsStream(stylesheet))
        ).transform(
                new DOMSource(xmlStatus),
                new StreamResult(dotStream));
        File dot;
        try {
            dot = getDot(Base.JSAGA_HOME);
        } catch(FileNotFoundException e) {
            dot = getDot(new File("externals/graphviz/config"));
        }
        //Process p = Runtime.getRuntime().exec("f:/cygwin/bin/cat.exe");
        Process p = Runtime.getRuntime().exec(dot.getAbsolutePath()+" -Tgif -o"+graph.getAbsolutePath());
        OutputStream stdin = p.getOutputStream();
        dotStream.writeTo(stdin);
        stdin.close();
        copy(p.getInputStream(), System.out);
        if (p.waitFor() != 0) {
            ByteArrayOutputStream error = new ByteArrayOutputStream();
            copy(p.getErrorStream(), error);
            throw new Exception(error.toString());
        }
    }

    private static File getDot(File baseDir) throws FileNotFoundException {
        File dotFile;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("windows") != -1) {
            dotFile = new File(baseDir, "lib/win32/dot.exe");
        } else {
            dotFile = new File(baseDir, "lib/linux/dot");
        }
        if (dotFile.exists()) {
            return dotFile;
        } else {
            throw new FileNotFoundException("You must install the Graphviz module to use this option");
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        for (int len; (len=in.read(buffer))>-1; ) {
            out.write(buffer, 0, len);
        }
    }

    protected Options createOptions() {
        Options opt = new Options();

        // command
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));

        // optional arguments
        opt.addOption(OptionBuilder.withDescription("Generate status and staging graphs")
                .withLongOpt(LONGOPT_GRAPHVIZ)
                .create(OPT_GRAPHVIZ));

        // returns
        return opt;
    }
}
