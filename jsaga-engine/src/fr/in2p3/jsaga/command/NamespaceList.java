package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.helpers.SAGAPattern;
import org.apache.commons.cli.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileList
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   4 avr. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NamespaceList extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    private static final String OPT_LONG = "l", LONGOPT_LONG = "long";
    private static final String OPT_NOWILDCARD = "n", LONGOPT_NOWILDCARD = "no-wildcard";

    public NamespaceList() {
        super("jsaga-ls", new String[]{"URL"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        NamespaceList command = new NamespaceList();
        CommandLine line = command.parse(args);
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else
        {
            // get URL and pattern from arguments
            String arg = command.m_nonOptionValues[0];
            URL url;
            String pattern;
            if (SAGAPattern.hasWildcard(arg) && !line.hasOption(OPT_NOWILDCARD)) {
                Matcher matcher = Pattern.compile("((.*)/)*(.*/)/*").matcher(arg);
                if (matcher.matches() && matcher.groupCount()>1) {
                    url = URLFactory.createURL(matcher.group(1));
                    pattern = matcher.group(3);
                } else {
                    url = URLFactory.createURL(arg.substring(0, arg.lastIndexOf('/')+1));
                    pattern = arg.substring(arg.lastIndexOf('/')+1);
                }
            } else {
                url = URLFactory.createURL(arg);
                pattern = null;
            }

            // get list
            Session session = SessionFactory.createSession(true);
            NSDirectory dir = NSFactory.createNSDirectory(session, url, Flags.NONE.getValue());
            List<URL> list = dir.list(pattern, Flags.NONE.getValue());

            if (line.hasOption(OPT_LONG)) {
                // display list
                EntryLongFormat formatter = new EntryLongFormat(dir);
                for (URL entry : list) {
                    System.out.println(formatter.toString(entry));
                }
                // close connection
                dir.close();
            } else {
                // close connection
                dir.close();
                // display list
                for (URL entry : list) {
                    // getString() decodes the URL, while toString() does not
                    System.out.println(entry.getString());
                }
            }
            System.exit(0);
        }
    }

    protected Options createOptions() {
        Options opt = new Options();
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));
        opt.addOption(OptionBuilder.withDescription("Use a long listing format")
                .isRequired(false)
                .withLongOpt(LONGOPT_LONG)
                .create(OPT_LONG));
        opt.addOption(OptionBuilder.withDescription("Disable interpretation of wildcards")
                .isRequired(false)
                .withLongOpt(LONGOPT_NOWILDCARD)
                .create(OPT_NOWILDCARD));
        return opt;
    }
}
