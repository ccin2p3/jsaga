package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.permissions.Permission;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   NamespaceChmod
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   22 mai 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class NamespaceChmod extends AbstractCommand {
    private static final int ARG_MODE = 0;
    private static final int ARG_URL = 1;
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";

    private static final int UNIX_READ = 4;
    private static final int UNIX_WRITE = 2;
    private static final int UNIX_EXEC = 1;

    public NamespaceChmod() {
        super("jsaga-chmod", new String[]{"mode", "URL"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        NamespaceChmod command = new NamespaceChmod();
        CommandLine line = command.parse(args);
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else
        {
            // get arguments
            int mode = Integer.parseInt(command.m_nonOptionValues[ARG_MODE]);
            URL url = URLFactory.createURL(command.m_nonOptionValues[ARG_URL]);

            // execute command
            Session session = SessionFactory.createSession(true);
            NSEntry entry = NSFactory.createNSEntry(session, url);
            setUnixPermissions(entry, "user-"+entry.getOwner(), mode/10/10);
            setUnixPermissions(entry, "group-"+entry.getGroup(), mode/10%10);
            setUnixPermissions(entry, "*", mode%10%10);
            entry.close();
        }
    }

    protected Options createOptions() {
        Options opt = new Options();
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));
        return opt;
    }

    private static void setUnixPermissions(NSEntry entry, String id, int unixPerms) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        // convert to SAGA permissions
        int allowed = Permission.NONE.getValue();
        int denied = Permission.NONE.getValue();
        if ((unixPerms & UNIX_READ) > 0) {
            allowed = Permission.READ.or(allowed);
        } else {
            denied = Permission.READ.or(denied);
        }
        if ((unixPerms & UNIX_WRITE) > 0) {
            allowed = Permission.WRITE.or(allowed);
        } else {
            denied = Permission.WRITE.or(denied);
        }
        if ((unixPerms & UNIX_EXEC) > 0) {
            allowed = Permission.EXEC.or(allowed);
        } else {
            denied = Permission.EXEC.or(denied);
        }

        // set permissions
        if (allowed > Permission.NONE.getValue()) {
            entry.permissionsAllow(id, allowed);
        }
        if (denied > Permission.NONE.getValue()) {
            entry.permissionsDeny(id, allowed);
        }
    }
}
