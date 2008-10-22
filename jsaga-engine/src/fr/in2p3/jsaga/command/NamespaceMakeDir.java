package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.url.URL;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NamespaceMakeDir
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NamespaceMakeDir extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    private static final String OPT_NOT_EXCL = "f", LONGOPT_NOT_EXCL = "force";
    private static final String OPT_CREATEPARENTS = "p", LONGOPT_CREATEPARENTS = "parents";

    public NamespaceMakeDir() {
        super("jsaga-mkdir", new String[]{"URL"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        NamespaceMakeDir command = new NamespaceMakeDir();
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
            int flags =(line.hasOption(OPT_NOT_EXCL) ? Flags.NONE : Flags.EXCL)
                    .or(line.hasOption(OPT_CREATEPARENTS)
                            ? Flags.CREATE.or(Flags.CREATEPARENTS)
                            : Flags.CREATE.getValue());

            // execute command
            Session session = SessionFactory.createSession(true);
            NSDirectory dir = NSFactory.createNSDirectory(session, url, flags);
            dir.close();
        }
    }

    protected Options createOptions() {
        Options opt = new Options();
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));
        opt.addOption(OptionBuilder.withDescription("Do not fail if target already exist")
                .isRequired(false)
                .withLongOpt(LONGOPT_NOT_EXCL)
                .create(OPT_NOT_EXCL));
        opt.addOption(OptionBuilder.withDescription("Make parent directories as needed")
                .isRequired(false)
                .withLongOpt(LONGOPT_CREATEPARENTS)
                .create(OPT_CREATEPARENTS));
        return opt;
    }
}
