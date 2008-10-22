package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.url.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URLFactory;

import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NamespaceLogical
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NamespaceLogical extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    private static final String OPT_REGISTER = "r", LONGOPT_REGISTER = "register";
    private static final String OPT_UNREGISTER = "u", LONGOPT_UNREGISTER = "unregister";
    private static final String OPT_LIST = "l", LONGOPT_LIST = "list";

    public NamespaceLogical() {
        super("jsaga-logical", new String[]{"Logical URL"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        NamespaceLogical command = new NamespaceLogical();
        CommandLine line = command.parse(args);

        System.setProperty("saga.factory", "fr.in2p3.jsaga.impl.SagaFactoryImpl");
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else if (line.hasOption(OPT_REGISTER))
        {
            LogicalFile file = command.getLogicalFile();
            file.addLocation(URLFactory.createURL(line.getOptionValue(OPT_REGISTER)));
            file.close();
        }
        else if (line.hasOption(OPT_UNREGISTER))
        {
            LogicalFile file = command.getLogicalFile();
            file.removeLocation(URLFactory.createURL(line.getOptionValue(OPT_REGISTER)));
            file.close();
        }
        else if (line.hasOption(OPT_LIST))
        {
            LogicalFile file = command.getLogicalFile();
            for (URL location : file.listLocations()) {
                System.out.println(location);
            }
            file.close();
        }
    }

    private LogicalFile getLogicalFile() throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess, IncorrectState, AlreadyExists {
        URL logicalUrl = URLFactory.createURL(m_nonOptionValues[0]);
        Session session = SessionFactory.createSession(true);
        NSEntry entry = NSFactory.createNSEntry(session, logicalUrl, Flags.NONE.getValue());
        if (entry instanceof LogicalFile) {
            LogicalFile file = (LogicalFile) entry;
            return file;
        } else {
            throw new BadParameter("Provided URL is not a logical file: "+logicalUrl);
        }
    }

    protected Options createOptions() {
        Options opt = new Options();

        // command group
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        {
            group.addOption(OptionBuilder.withDescription("Display this help and exit")
                    .withLongOpt(LONGOPT_HELP)
                    .create(OPT_HELP));
            group.addOption(OptionBuilder.withDescription("Register replica <Physical URL>")
                    .hasArg()
                    .withArgName("Physical URL")
                    .withLongOpt(LONGOPT_REGISTER)
                    .create(OPT_REGISTER));
            group.addOption(OptionBuilder.withDescription("Unregister replica <Physical URL>")
                    .hasArg()
                    .withArgName("Physical URL")
                    .withLongOpt(LONGOPT_UNREGISTER)
                    .create(OPT_UNREGISTER));
            group.addOption(OptionBuilder.withDescription("List replicas <Physical URL>")
                    .withLongOpt(LONGOPT_LIST)
                    .create(OPT_LIST));
        }
        opt.addOptionGroup(group);

        return opt;
    }
}
