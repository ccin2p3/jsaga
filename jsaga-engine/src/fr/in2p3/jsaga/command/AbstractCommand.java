package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractCommand
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractCommand {
    private String m_appName;
    private Options m_options;
    private String[] m_nonOptionNames;
    private String[] m_standaloneOptionNames;
    protected String[] m_nonOptionValues;

    protected abstract Options createOptions();

    /**
     * @param appName the application name
     * @param nonOptionNames the names of non-options (required if <code>standaloneOptionNames</code> not null)
     * @param standaloneOptionNames the names of standalone options (i.e. options that do not require non-options)
     */
    protected AbstractCommand(String appName, String[] nonOptionNames, String[] standaloneOptionNames) {
        m_appName = appName;
        m_options = this.createOptions();
        m_nonOptionNames = nonOptionNames;
        m_standaloneOptionNames = standaloneOptionNames;
        m_nonOptionValues = null;
    }

    protected AbstractCommand(String appName) {
        this(appName, null, null);
    }

    protected CommandLine parse(String[] args) {
        // update args
        List list = new ArrayList();
        for (int i=0; args!=null && i<args.length; i++) {
            String[] array;
            if (args[i]!=null && args[i].startsWith("-D") && (array=args[i].split("=")).length<=2) {
                String name = array[0].substring(2);
                String value = array.length==2 ? array[1] : "";
                System.setProperty(name, value);
            } else {
                list.add(args[i]);
            }
        }
        String[] newArgs = (args!=null && list.size()!=args.length
                ? (String[]) list.toArray(new String[list.size()])
                : args);

        // parse
        try {
            boolean stopAtNonOption = (m_nonOptionNames==null || m_nonOptionNames.length==0);
            CommandLine line = new PosixParser().parse(m_options, newArgs, stopAtNonOption);
            m_nonOptionValues = line.getArgs();
            if (m_nonOptionNames!=null && m_nonOptionValues.length>m_nonOptionNames.length) {
                throw new UnrecognizedOptionException("Unexpected option: "+ m_nonOptionValues[m_nonOptionNames.length]);
            } else if (m_standaloneOptionNames!=null && m_nonOptionValues.length<m_nonOptionNames.length && !this.hasStandaloneOption(line)) {
                throw new MissingOptionException("Missing option: "+m_nonOptionNames[m_nonOptionValues.length]);
            } else {
                return line;
            }
        } catch(ParseException e) {
            printHelpAndExit(e.getMessage());
            return null;
        }
    }
    private boolean hasStandaloneOption(CommandLine line) {
        for (int i=0; m_standaloneOptionNames!=null && i<m_standaloneOptionNames.length; i++) {
            if (line.hasOption(m_standaloneOptionNames[i])) {
                return true;
            }
        }
        return false;
    }

    protected void printHelpAndExit(String errorMessage) {
        if (errorMessage != null) {
            System.err.println(errorMessage);
            System.err.println();
            this.printHelp();
            System.exit(1);
        } else {
            this.printHelp();
            System.exit(0);
        }
    }
    private void printHelp() {
        HelpFormatter help = new HelpFormatter();

        // build usage
        ByteArrayOutputStream usage = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(usage);
        help.printUsage(
                writer,
                HelpFormatter.DEFAULT_WIDTH,
                m_appName,
                m_options);
        writer.flush();

        // print usage and help
        writer = new PrintWriter(System.out);
        writer.write(usage.toString(), 0, usage.size()-2);
        printNonOptionNames(writer, 0);
        writer.println();
        writer.println();
        writer.println("where:");
        help.printOptions(
                writer,
                HelpFormatter.DEFAULT_WIDTH,
                m_options,
                HelpFormatter.DEFAULT_LEFT_PAD,
                HelpFormatter.DEFAULT_DESC_PAD);
        writer.flush();
    }
    private void printNonOptionNames(PrintWriter writer, int current) {
        if (m_nonOptionNames!=null && current<m_nonOptionNames.length) {
            if (m_standaloneOptionNames != null) {
                writer.print(" <"+m_nonOptionNames[current]+">");
            } else {
                writer.print(" [<"+m_nonOptionNames[current]+">");
                printNonOptionNames(writer, current+1);
                writer.print("]");
            }
        }
    }
}
