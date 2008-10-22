package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import org.apache.commons.cli.*;
import org.ogf.saga.url.URL;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NamespaceTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NamespaceTest extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    private static final String OPT_EXISTS = "e", LONGOPT_EXISTS = "exist";
    private static final String OPT_ISFILE = "f", LONGOPT_ISFILE = "file";
    private static final String OPT_ISDIRECTORY = "d", LONGOPT_ISDIRECTORY = "directory";
    private static final String OPT_ISLINK = "L", LONGOPT_ISLINK = "link";

    private static final int FLAGS_BYPASSEXIST = 4096;

    public NamespaceTest() {
        super("jsaga-test", new String[]{"URL"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        NamespaceTest command = new NamespaceTest();
        CommandLine line = command.parse(args);

        System.setProperty("saga.factory", "fr.in2p3.jsaga.impl.SagaFactoryImpl");
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else
        {
            // get arguments
            URL url = URLFactory.createURL(command.m_nonOptionValues[0]);

            // execute command
            Session session = SessionFactory.createSession(true);
            NSEntry entry = NSFactory.createNSEntry(session, url, FLAGS_BYPASSEXIST);
            boolean success;
            if (line.hasOption(OPT_EXISTS)) {
                success = ((AbstractNSEntryImpl)entry).exists();
            } else if (line.hasOption(OPT_ISFILE)) {
                try {
                    success = entry.isEntry();
                } catch(IncorrectState e) {
                    success = false;
                }
            } else if (line.hasOption(OPT_ISDIRECTORY)) {
                try {
                    success = entry.isDir();
                } catch(IncorrectState e) {
                    success = false;
                }
            } else if (line.hasOption(OPT_ISLINK)) {
                try {
                    success = entry.isLink();
                } catch(IncorrectState e) {
                    success = false;
                }
            } else {
                throw new Exception("Unexpected exception");
            }
            entry.close();

            // return
            System.exit(success ? 0 : 1);
        }
    }

    protected Options createOptions() {
        Options opt = new Options();
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));

        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.withDescription("File exists")
                .withLongOpt(LONGOPT_EXISTS)
                .create(OPT_EXISTS));
        group.addOption(OptionBuilder.withDescription("File exists and is a regular file")
                .withLongOpt(LONGOPT_ISFILE)
                .create(OPT_ISFILE));
        group.addOption(OptionBuilder.withDescription("File exists and is a directory")
                .withLongOpt(LONGOPT_ISDIRECTORY)
                .create(OPT_ISDIRECTORY));
        group.addOption(OptionBuilder.withDescription("File exists and is a symbolic link")
                .withLongOpt(LONGOPT_ISLINK)
                .create(OPT_ISLINK));
        group.setRequired(true);
        opt.addOptionGroup(group);

        return opt;
    }
}
