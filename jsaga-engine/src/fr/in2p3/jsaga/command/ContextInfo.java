package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.impl.context.*;
import org.apache.commons.cli.*;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.SagaException;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ContextInfo
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ContextInfo extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    private static final String OPT_ATTRIBUTE = "a", LONGOPT_ATTRIBUTE = "attribute";

    public ContextInfo() {
        super("jsaga-context-info", new String[]{"contextId"}, null);        
    }

    public static void main(String[] args) throws Exception {
        ContextInfo command = new ContextInfo();
        CommandLine line = command.parse(args);
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else {
            // create empty session
            Session session = SessionFactory.createSession(false);
            ConfiguredContext[] configContexts = ConfigurableContextFactory.listConfiguredContext();
            for (int i=0; i<configContexts.length; i++) {
                if (command.m_nonOptionValues.length==0
                    || command.m_nonOptionValues[0].equals(configContexts[i].getUrlPrefix())
                    || command.m_nonOptionValues[0].equals(configContexts[i].getType()))
                {
                    Context context = ConfigurableContextFactory.createContext(configContexts[i]);
                    // print title
                    System.out.println("Security context: "+getLabel(context));
                    // print context
                    print(session, context, line);
                }
            }
            session.close();
        }
    }

    public static String getLabel(Context context) throws SagaException {
        return context.getAttribute(ContextImpl.URL_PREFIX)+" ("+context.getAttribute(Context.TYPE)+")";
    }
    private static void print(Session session, Context context, CommandLine line) {
        try {
            if (line.hasOption(OPT_ATTRIBUTE)) {
                try {
                    System.out.println("  "+context.getAttribute(line.getOptionValue(OPT_ATTRIBUTE)));
                } catch(NotImplementedException e) {
                    System.out.println("  Attribute not supported ["+e.getMessage()+"]");
                }
            } else {
                session.addContext(context); // this triggers initialization of context
                System.out.print(context);
            }
        } catch(Exception e) {
            System.out.println("  Context not initialized ["+e.getMessage()+"]");
        }
        System.out.println();
    }

    protected Options createOptions() {
        Options opt = new Options();

        // command
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));

        // options
        opt.addOption(OptionBuilder.withDescription("Query context instance(s) for attribute <attr> only")
                .withArgName("attr")
                .hasArg()
                .withLongOpt(LONGOPT_ATTRIBUTE)
                .create(OPT_ATTRIBUTE));

        // system properties
        opt.addOption(OptionBuilder.withDescription("Set context instance attribute (e.g. -DVOMS[0].UserVO=dteam)")
                .withArgName("ctxId>.<attr>=<value")
                .hasArg()
                .withValueSeparator()
                .create("D"));
        return opt;
    }
}
