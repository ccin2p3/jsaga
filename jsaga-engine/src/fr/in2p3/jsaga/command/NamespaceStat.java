package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.file.File;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.permissions.Permission;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.util.Date;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NamespaceStat
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NamespaceStat extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    private static final String OPT_NAME = "n", LONGOPT_NAME = "name";
    private static final String OPT_TYPE = "t", LONGOPT_TYPE = "type";
    private static final String OPT_SIZE = "s", LONGOPT_SIZE = "size";
    private static final String OPT_PERM = "p", LONGOPT_PERM = "perm";
    private static final String OPT_OWNER = "o", LONGOPT_OWNER = "owner";
    private static final String OPT_GROUP = "g", LONGOPT_GROUP = "group";
    private static final String OPT_DATE = "d", LONGOPT_DATE = "date";

    public NamespaceStat() {
        super("jsaga-stat", new String[]{"URL"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        NamespaceStat command = new NamespaceStat();
        CommandLine line = command.parse(args);
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
            NSEntry entry = NSFactory.createNSEntry(session, url);
            if (line.hasOption(OPT_NAME)) {
                System.out.println(entry.getName());
            } else if (line.hasOption(OPT_TYPE)) {
                System.out.println(getType(entry));
            } else if (line.hasOption(OPT_SIZE)) {
                System.out.println(getSize(entry));
            } else if (line.hasOption(OPT_PERM)) {
                System.out.println(getPerm(entry));
            } else if (line.hasOption(OPT_OWNER)) {
                System.out.println(getOwner(entry));
            } else if (line.hasOption(OPT_GROUP)) {
                System.out.println(getGroup(entry));
            } else if (line.hasOption(OPT_DATE)) {
                System.out.println(getMTime(entry));
            } else {
                System.out.println("  File: "+entry.getName());
                System.out.println("  Type: "+getType(entry));
                System.out.println("  Size: "+getSize(entry));
                System.out.println("  Perm: "+getPerm(entry));
                System.out.println(" Owner: "+getOwner(entry));
                System.out.println(" Group: "+getGroup(entry));
                System.out.println("Modify: "+ getMTime(entry));
            }
            entry.close();
            System.exit(0);
        }
    }

    private static String getType(NSEntry entry) throws Exception {
        if (entry.isEntry()) {
            return "file";
        } else if (entry.isDir()) {
            return "directory";
        } else if (entry.isLink()) {
            return "link";
        } else {
            return "unknown";
        }
    }

    private static long getSize(NSEntry entry) throws Exception {
        if (entry instanceof File) {
            return ((File) entry).getSize();
        } else {
            return 0;
        }
    }

    private static String getPerm(NSEntry entry) throws Exception {
        try {
            StringBuffer perms = new StringBuffer();
            perms.append(entry.permissionsCheck(null, Permission.QUERY.getValue()) ? "q" : "-");
            perms.append(entry.permissionsCheck(null, Permission.READ.getValue()) ? "r" : "-");
            perms.append(entry.permissionsCheck(null, Permission.WRITE.getValue()) ? "w" : "-");
            perms.append(entry.permissionsCheck(null, Permission.EXEC.getValue()) ? "x" : "-");
            perms.append(entry.permissionsCheck(null, Permission.OWNER.getValue()) ? "o" : "-");
            return perms.toString();
        } catch(BadParameterException e) {
            return "?";
        } catch(NotImplementedException e) {
            return "?";
        }
    }

    private static String getOwner(NSEntry entry) throws Exception {
        try {
            return entry.getOwner();
        } catch(NotImplementedException e) {
            return "?";
        }
    }

    private static String getGroup(NSEntry entry) throws Exception {
        try {
            return entry.getGroup();
        } catch(NotImplementedException e) {
            return "?";
        }
    }

    private static String getMTime(NSEntry entry) throws Exception {
        try {
            return new Date(entry.getMTime()).toString();
        } catch(NotImplementedException e) {
            return "?";
        }
    }

    protected Options createOptions() {
        Options opt = new Options();
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));

        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.withDescription("Entry name")
                .withLongOpt(LONGOPT_NAME)
                .create(OPT_NAME));
        group.addOption(OptionBuilder.withDescription("Entry type (file | directory | link)")
                .withLongOpt(LONGOPT_TYPE)
                .create(OPT_TYPE));
        group.addOption(OptionBuilder.withDescription("Entry size (or 0 if entry is not a file)")
                .withLongOpt(LONGOPT_SIZE)
                .create(OPT_SIZE));
        group.addOption(OptionBuilder.withDescription("Entry permissions")
                .withLongOpt(LONGOPT_PERM)
                .create(OPT_PERM));
        group.addOption(OptionBuilder.withDescription("Entry owner")
                .withLongOpt(LONGOPT_OWNER)
                .create(OPT_OWNER));
        group.addOption(OptionBuilder.withDescription("Entry group")
                .withLongOpt(LONGOPT_GROUP)
                .create(OPT_GROUP));
        group.addOption(OptionBuilder.withDescription("Entry last modification date")
                .withLongOpt(LONGOPT_DATE)
                .create(OPT_DATE));
        group.setRequired(false);
        opt.addOptionGroup(group);

        return opt;
    }
}
