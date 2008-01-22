package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.URL;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NamespaceMove
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NamespaceMove extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    private static final String OPT_NOT_OVERWRITE = "i", LONGOPT_NOT_OVERWRITE = "interactive";
    private static final String OPT_RECURSIVE = "r", LONGOPT_RECURSIVE = "recursive";

    public NamespaceMove() {
        super("jsaga-mv", new String[]{"Source URL", "Target URL"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        NamespaceMove command = new NamespaceMove();
        CommandLine line = command.parse(args);

        System.setProperty("saga.factory", "fr.in2p3.jsaga.impl.SagaFactoryImpl");
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else
        {
            // get arguments
            URL source = new URL(command.m_nonOptionValues[0].replaceAll(" ", "%20"));
            URL target = new URL(command.m_nonOptionValues[1].replaceAll(" ", "%20"));
            int flags =(line.hasOption(OPT_NOT_OVERWRITE) ? Flags.NONE : Flags.OVERWRITE)
                    .or(line.hasOption(OPT_RECURSIVE) ? Flags.RECURSIVE : Flags.NONE);

            // execute command
            Session session = SessionFactory.createSession(true);
            NSEntry entry;
            if (source.getPath().endsWith("/")) {
                entry = NSFactory.createNSDirectory(session, source, Flags.NONE.getValue());
            } else {
                entry = NSFactory.createNSEntry(session, source, Flags.NONE.getValue());
            }
            entry.move(target, flags);
            entry.close();
        }
    }

    protected Options createOptions() {
        Options opt = new Options();
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));
        opt.addOption(OptionBuilder.withDescription("Do not overwrite target")
                .isRequired(false)
                .withLongOpt(LONGOPT_NOT_OVERWRITE)
                .create(OPT_NOT_OVERWRITE));
        opt.addOption(OptionBuilder.withDescription("Move recursively")
                .isRequired(false)
                .withLongOpt(LONGOPT_RECURSIVE)
                .create(OPT_RECURSIVE));
        return opt;
    }
}
