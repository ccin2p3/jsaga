package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.impl.logicalfile.LogicalDirectoryImpl;
import org.apache.commons.cli.*;
import org.ogf.saga.attributes.Attributes;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.logicalfile.LogicalFileFactory;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.util.HashMap;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NSLogicalMetaData
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   4 nov. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NSLogicalMetaData extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    private static final String OPT_GET = "g", LONGOPT_GET = "get";
    private static final String OPT_SET = "s", LONGOPT_SET = "set";
    private static final String OPT_REMOVE = "r", LONGOPT_REMOVE = "remove";
    private static final String OPT_LIST = "l", LONGOPT_LIST = "list";
    private static final String OPT_LIST_ALL_KEYS = "k", LONGOPT_LIST_ALL_KEYS = "list-all-keys";
    private static final String OPT_LIST_ALL_VALUES = "v", LONGOPT_LIST_ALL_VALUES = "list-all-values";

    public NSLogicalMetaData() {
        super("jsaga-logical-metadata", new String[]{"URL"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        NSLogicalMetaData command = new NSLogicalMetaData();
        CommandLine line = command.parse(args);
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else
        {
            // get URL and pattern from arguments
            String arg = command.m_nonOptionValues[0];
            URL url = URLFactory.createURL(arg);

            // open connection
            Session session = SessionFactory.createSession(true);
            Attributes entry;
            if (url.getPath().endsWith("/")) {
                entry = LogicalFileFactory.createLogicalDirectory(session, url, Flags.NONE.getValue());
            } else {
                entry = LogicalFileFactory.createLogicalFile(session, url, Flags.NONE.getValue());
            }

            // operation
            if (line.hasOption(OPT_GET)) {
                String key = line.getOptionValue(OPT_GET);
                if (entry.isVectorAttribute(key)) {
                    String[] values = entry.getVectorAttribute(key);
                    for (int v=0; v<values.length; v++) {
                        System.out.println(values[v]);
                    }
                } else {
                    System.out.println(entry.getAttribute(key));
                }
            } else if (line.hasOption(OPT_SET)) {
                String[] array = line.getOptionValues(OPT_SET);
                String key = array[0];
                String[] values = array[1].split(",");
                switch (values.length) {
                    case 0:
                        throw new Exception("Option "+OPT_SET+" requires at least 2 arguments: <key> <value>*");
                    case 1:
                        entry.setAttribute(key, values[0]);
                        break;
                    default:
                        entry.setVectorAttribute(key, values);
                        break;
                }
            } else if (line.hasOption(OPT_REMOVE)) {
                String key = line.getOptionValue(OPT_REMOVE);
                entry.removeAttribute(key);
            } else if (line.hasOption(OPT_LIST)) {
                String[] keys = entry.listAttributes();
                for (int i=0; i<keys.length; i++) {
                    System.out.print(keys[i]+" = ");
                    if (entry.isVectorAttribute(keys[i])) {
                        String[] values = entry.getVectorAttribute(keys[i]);
                        for (int v=0; v<values.length; v++) {
                            System.out.println(indent(v==0 ? 0 : keys[i].length()+3) + values[v]);
                        }
                    } else {
                        System.out.println(entry.getAttribute(keys[i]));
                    }
                }
            } else if (line.hasOption(OPT_LIST_ALL_KEYS)) {
                if (entry instanceof LogicalFile) {
                    throw new BadParameterException("Option -"+OPT_LIST_ALL_KEYS+" requires path to end with a '/'");
                }
                String[] keys = ((LogicalDirectoryImpl)entry).listAttributesRecursive(new HashMap<String,String>());
                for (int i=0; i<keys.length; i++) {
                    System.out.println(keys[i]);
                }
            } else if (line.hasOption(OPT_LIST_ALL_VALUES)) {
                String key = line.getOptionValue(OPT_LIST_ALL_VALUES);
                if (entry instanceof LogicalFile) {
                    throw new BadParameterException("Option -"+OPT_LIST_ALL_VALUES+" requires path to end with a '/'");
                }
                String[] values = ((LogicalDirectoryImpl)entry).listAttributeValuesRecursive(key, new HashMap<String,String>());
                for (int i=0; i<values.length; i++) {
                    System.out.println(values[i]);
                }
            }

            // close connection
            ((NSEntry)entry).close();
        }
    }

    private static String indent(int indent) {
        StringBuffer buffer = new StringBuffer();
        for (int i=0; i<indent; i++) {
            buffer.append(' ');
        }
        return buffer.toString();
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
            group.addOption(OptionBuilder.withDescription("Get meta-data <key>")
                    .hasArg()
                    .withArgName("key")
                    .withLongOpt(LONGOPT_GET)
                    .create(OPT_GET));
            group.addOption(OptionBuilder.withDescription("Set meta-data <key> with comma-separated values (spaces not allowed)")
                    .hasArgs(2)
                    .withArgName("key values")
                    .withLongOpt(LONGOPT_SET)
                    .create(OPT_SET));
            group.addOption(OptionBuilder.withDescription("Remove meta-data <key>")
                    .hasArg()
                    .withArgName("key")
                    .withLongOpt(LONGOPT_REMOVE)
                    .create(OPT_REMOVE));
            group.addOption(OptionBuilder.withDescription("List meta-data <key>-<value> pairs")
                    .withLongOpt(LONGOPT_LIST)
                    .create(OPT_LIST));
            group.addOption(OptionBuilder.withDescription("List all meta-data keys")
                    .withLongOpt(LONGOPT_LIST_ALL_KEYS)
                    .create(OPT_LIST_ALL_KEYS));
            group.addOption(OptionBuilder.withDescription("List all meta-data values for <key>")
                    .hasArg()
                    .withArgName("key")
                    .withLongOpt(LONGOPT_LIST_ALL_VALUES)
                    .create(OPT_LIST_ALL_VALUES));
        }
        opt.addOptionGroup(group);
        
        return opt;
    }
}
