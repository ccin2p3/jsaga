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
        command.execute(line);
    }

    public void execute(CommandLine line) throws Exception {
        if (line.hasOption(OPT_HELP))
        {
            super.printHelpAndExit(null);
        }
        else
        {
            // get arguments
            URL source = URLFactory.createURL(super.m_nonOptionValues[0]);
            URL target = URLFactory.createURL(super.m_nonOptionValues[1]);
            int flags =(line.hasOption(OPT_NOT_OVERWRITE) ? Flags.NONE : Flags.OVERWRITE)
                    .or(line.hasOption(OPT_RECURSIVE) ? Flags.RECURSIVE : Flags.NONE);

            // execute command
            Session session = SessionFactory.createSession(true);
            NSEntry entry = NSFactory.createNSEntry(session, source, Flags.NONE.getValue());
            this.changeBehavior(session, target);
            entry.move(target, flags);
            entry.close();
        }
    }

    protected void changeBehavior(Session session, URL target) throws Exception {
        // do nothing
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
