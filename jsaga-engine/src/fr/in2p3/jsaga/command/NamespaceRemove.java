package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NamespaceRemove
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NamespaceRemove extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    private static final String OPT_RECURSIVE = "r", LONGOPT_RECURSIVE = "recursive";

    public NamespaceRemove() {
        super("jsaga-rm", new String[]{"URL"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        NamespaceRemove command = new NamespaceRemove();
        CommandLine line = command.parse(args);
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else
        {
            // get arguments
            URL url = URLFactory.createURL(command.m_nonOptionValues[0]);
            Flags flags = (line.hasOption(OPT_RECURSIVE) ? Flags.RECURSIVE : Flags.NONE);

            // execute command
            Session session = SessionFactory.createSession(true);
            NSEntry entry = NSFactory.createNSEntry(session, url, Flags.NONE.getValue());
            entry.remove(flags.getValue());
            entry.close();
        }
    }

    protected Options createOptions() {
        Options opt = new Options();
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));
        opt.addOption(OptionBuilder.withDescription("Remove recursively")
                .isRequired(false)
                .withLongOpt(LONGOPT_RECURSIVE)
                .create(OPT_RECURSIVE));
        return opt;
    }
}
