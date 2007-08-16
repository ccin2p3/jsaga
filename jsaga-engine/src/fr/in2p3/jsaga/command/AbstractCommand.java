package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;

import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
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
    protected String[] m_nonOptionValues;

    protected abstract Options createOptions();

    protected AbstractCommand(String appName, String[] nonOptionNames) {
        m_appName = appName;
        m_options = this.createOptions();
        m_nonOptionNames = nonOptionNames;
        m_nonOptionValues = null;
    }

    protected AbstractCommand(String appName) {
        this(appName, null);
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
            } else {
                return line;
            }
        } catch(ParseException e) {
            printHelpAndExit(e.getMessage());
            return null;
        }
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
        for (int i=0; m_nonOptionNames!=null && i<m_nonOptionNames.length; i++) {
            writer.print(" [<"+m_nonOptionNames[i]+">]");
        }
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
}
