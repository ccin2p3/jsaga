package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NSLogicalLocation
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NSLogicalLocation extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    private static final String OPT_CREATE = "c", LONGOPT_CREATE = "create";
    private static final String OPT_REGISTER = "r", LONGOPT_REGISTER = "register";
    private static final String OPT_UNREGISTER = "u", LONGOPT_UNREGISTER = "unregister";
    private static final String OPT_LIST = "l", LONGOPT_LIST = "list";

    public NSLogicalLocation() {
        super("jsaga-logical-location", new String[]{"Logical URL"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        NSLogicalLocation command = new NSLogicalLocation();
        CommandLine line = command.parse(args);
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else if (line.hasOption(OPT_REGISTER))
        {
            LogicalFile file;
            if (line.hasOption(OPT_CREATE)) {
                file = command.getLogicalFile(Flags.CREATE.or(Flags.EXCL));
            } else {
                file = command.getLogicalFile();
            }
            file.addLocation(URLFactory.createURL(line.getOptionValue(OPT_REGISTER)));
            file.close();
        }
        else if (line.hasOption(OPT_UNREGISTER))
        {
            LogicalFile file = command.getLogicalFile();
            file.removeLocation(URLFactory.createURL(line.getOptionValue(OPT_UNREGISTER)));
            if (file.listLocations().size() == 0) {
                file.remove();
            }
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

    private LogicalFile getLogicalFile() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectStateException, AlreadyExistsException {
        return this.getLogicalFile(Flags.NONE.getValue());
    }
    private LogicalFile getLogicalFile(int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectStateException, AlreadyExistsException {
        URL logicalUrl = URLFactory.createURL(m_nonOptionValues[0]);
        Session session = SessionFactory.createSession(true);
        NSEntry entry = NSFactory.createNSEntry(session, logicalUrl, flags);
        if (entry instanceof LogicalFile) {
            LogicalFile file = (LogicalFile) entry;
            return file;
        } else {
            throw new BadParameterException("Provided URL is not a logical file: "+logicalUrl);
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

        // optionan
        opt.addOption(OptionBuilder.withDescription("Create logical file")
                .withLongOpt(LONGOPT_CREATE)
                .create(OPT_CREATE));

        return opt;
    }
}
