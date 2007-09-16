package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

import java.lang.Exception;
import java.net.URISyntaxException;

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
        super("jsaga-logical", new String[]{"Logical URI"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        NamespaceLogical command = new NamespaceLogical();
        CommandLine line = command.parse(args);

        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else if (line.hasOption(OPT_REGISTER))
        {
            LogicalFile file = command.getLogicalFile();
            file.addLocation(line.getOptionValue(OPT_REGISTER));
            file.close(0.0f);
        }
        else if (line.hasOption(OPT_UNREGISTER))
        {
            LogicalFile file = command.getLogicalFile();
            file.removeLocation(line.getOptionValue(OPT_REGISTER));
            file.close(0.0f);
        }
        else if (line.hasOption(OPT_LIST))
        {
            LogicalFile file = command.getLogicalFile();
            String[] locations = file.listLocations();
            for (int i=0; i<locations.length; i++) {
                System.out.println(locations[i]);
            }
            file.close(0.0f);
        }
    }

    private LogicalFile getLogicalFile() throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess, IncorrectState, AlreadyExists {
        URI logicalUri;
        try {
            logicalUri = new URI(m_nonOptionValues[0]);
        } catch (URISyntaxException e) {
            throw new IncorrectURL(e);
        }
        Session session = SessionFactory.createSession(true);
        NamespaceEntry entry = NamespaceFactory.createNamespaceEntry(session, logicalUri, Flags.NONE);
        if (entry instanceof LogicalFile) {
            LogicalFile file = (LogicalFile) entry;
            return file;
        } else {
            throw new BadParameter("Provided URI is not a logical file: "+logicalUri);
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
            group.addOption(OptionBuilder.withDescription("Register replica <Physical URI>")
                    .hasArg()
                    .withArgName("Physical URI")
                    .withLongOpt(LONGOPT_REGISTER)
                    .create(OPT_REGISTER));
            group.addOption(OptionBuilder.withDescription("Unregister replica <Physical URI>")
                    .hasArg()
                    .withArgName("Physical URI")
                    .withLongOpt(LONGOPT_UNREGISTER)
                    .create(OPT_UNREGISTER));
            group.addOption(OptionBuilder.withDescription("List replicas <Physical URI>")
                    .withLongOpt(LONGOPT_LIST)
                    .create(OPT_LIST));
        }
        opt.addOptionGroup(group);

        return opt;
    }
}
