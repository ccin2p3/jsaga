package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.attributes.Attributes;
import org.ogf.saga.logicalfile.LogicalFileFactory;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NSLogicalMetaData
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   4 nov. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NSLogicalMetaData extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    private static final String OPT_ATTTRIBUTE = "a", LONGOPT_ATTRIBUTE = "attribute";

    public NSLogicalMetaData() {
        super("jsaga-logical-metadata", new String[]{"URL"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        NSLogicalMetaData command = new NSLogicalMetaData();
        CommandLine line = command.parse(args);
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else
        {
            // get URL and pattern from arguments
            String arg = command.m_nonOptionValues[0];
            URL url = URLFactory.createURL(arg);
            String key;
            if (line.hasOption(OPT_ATTTRIBUTE)) {
                key = line.getOptionValue(OPT_ATTTRIBUTE);
            } else {
                key = null;
            }

            // open connection
            Session session = SessionFactory.createSession(true);
            Attributes entry;
            if (url.getPath().endsWith("/")) {
                entry = LogicalFileFactory.createLogicalDirectory(session, url, Flags.NONE.getValue());
            } else {
                entry = LogicalFileFactory.createLogicalFile(session, url, Flags.NONE.getValue());
            }

            // get meta-data(s)
            if (key != null) {
                System.out.println(entry.getAttribute(key));
            } else {
                String[] keys = entry.listAttributes();
                for (int i=0; i<keys.length; i++) {
                    System.out.println(keys[i]+" = "+entry.getAttribute(keys[i]));
                }
            }

            // close connection
            ((NSEntry)entry).close();
        }
    }

    protected Options createOptions() {
        Options opt = new Options();
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));
        opt.addOption(OptionBuilder.withDescription("Get value of specified attribute <key>")
                .hasArg()
                .withArgName("key")
                .withLongOpt(LONGOPT_ATTRIBUTE)
                .create(OPT_ATTTRIBUTE));
        return opt;
    }
}
