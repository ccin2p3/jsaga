package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.file.File;
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
* File:   NamespaceCat
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NamespaceCat extends AbstractCommand {
    private static final int BUFFER_SIZE = 1024;
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";

    public NamespaceCat() {
        super("jsaga-cat", new String[]{"Source URL"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        NamespaceCat command = new NamespaceCat();
        CommandLine line = command.parse(args);
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else
        {
            // get arguments
            URL source = URLFactory.createURL(command.m_nonOptionValues[0]);

            // execute command
            Session session = SessionFactory.createSession(true);
            NSEntry entry = NSFactory.createNSEntry(session, source, Flags.READ.getValue());
            try {
                while (entry instanceof LogicalFile) {
                    URL effectiveSource = entry.readLink();
                    NSEntry effectiveEntry = NSFactory.createNSEntry(session, effectiveSource, Flags.READ.getValue());
                    entry.close();
                    entry = effectiveEntry;
                }
                if (entry instanceof File) {
                    File file = (File) entry;
                    Buffer buffer = BufferFactory.createBuffer(BUFFER_SIZE);
                    for (int len; (len=file.read(buffer)) > -1; ) {
                        System.out.write(buffer.getData(), 0, len);
                    }
                    buffer.close();
                } else if (entry instanceof NSDirectory) {
                    throw new Exception("Entry is a directory: "+entry.getURL());
                }
            } finally {
                entry.close();
            }
            System.exit(0);
        }
    }

    protected Options createOptions() {
        Options opt = new Options();
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));
        return opt;
    }
}
