package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.impl.namespace.AbstractNSDirectoryImpl;
import org.apache.commons.cli.*;
import org.ogf.saga.URL;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

import java.util.Iterator;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileList
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   4 avr. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NamespaceList extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    private static final String OPT_LONG = "l", LONGOPT_LONG = "long";
    private static final String OPT_PATTERN = "p", LONGOPT_PATTERN = "pattern";

    public NamespaceList() {
        super("jsaga-ls", new String[]{"URL"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        NamespaceList command = new NamespaceList();
        CommandLine line = command.parse(args);

        System.setProperty("saga.factory", "fr.in2p3.jsaga.impl.SagaFactoryImpl");
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else
        {
            // get arguments
            URL url = new URL(command.m_nonOptionValues[0].replaceAll(" ", "%20"));
            String pattern = line.getOptionValue(OPT_PATTERN);

            // get list
            Session session = SessionFactory.createSession(true);
            NSDirectory dir = NSFactory.createNSDirectory(session, url, Flags.NONE.getValue());
            List list;
            if (line.hasOption(OPT_LONG)) {
                list = ((AbstractNSDirectoryImpl)dir).listWithLongFormat(pattern, Flags.NONE.getValue());
            } else {
                list = dir.list(pattern, Flags.NONE.getValue());
            }
            dir.close();

            // display list
            for (Iterator it=list.iterator(); it.hasNext(); ) {
                Object file = it.next();
                String fileCorrected = (file instanceof URL ? file.toString().replaceAll("%20", " ") : (String) file);
                System.out.println(fileCorrected);
            }
        }
    }

    protected Options createOptions() {
        Options opt = new Options();
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));
        opt.addOption(OptionBuilder.withDescription("Use a long listing format")
                .isRequired(false)
                .withLongOpt(LONGOPT_LONG)
                .create(OPT_LONG));
        opt.addOption(OptionBuilder.withDescription("Filter with names matching pattern <value>")
                .isRequired(false)
                .hasArg()
                .withArgName("value")
                .withLongOpt(LONGOPT_PATTERN)
                .create(OPT_PATTERN));
        return opt;
    }
}
