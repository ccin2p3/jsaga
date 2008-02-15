package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.schema.config.ContextInstance;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.apache.commons.cli.*;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.NotImplemented;
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

        System.setProperty("saga.factory", "fr.in2p3.jsaga.impl.SagaFactoryImpl");
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else if (command.m_nonOptionValues.length == 0)
        {
            Session session = SessionFactory.createSession(true);
            Context[] contexts = session.listContexts();
            for (int i=0; i<contexts.length; i++) {
                Context context = contexts[i];

                // print title
                System.out.println("Security context: "+context.getAttribute(Context.TYPE));

                // print context
                print(context, line);
            }
            session.close();
        }
        else if (command.m_nonOptionValues.length == 1)
        {
            String contextId = command.m_nonOptionValues[0];
            ContextInstance[] xmlContext = Configuration.getInstance().getConfigurations().getContextCfg().listContextInstanceArray(contextId);
            for (int i=0; i<xmlContext.length; i++) {
                // set context
                Context context = ContextFactory.createContext();
                context.setAttribute(Context.TYPE, xmlContext[i].getName());
                context.setDefaults();

                // print title
                if (xmlContext.length > 1) {
                    System.out.println("Security context: "+context.getAttribute(Context.TYPE));
                }

                // print context
                print(context, line);

                // close context
                ((ContextImpl) context).close();
            }
        }
    }

    private static void print(Context context, CommandLine line) {
        try {
            if (line.hasOption(OPT_ATTRIBUTE)) {
                try {
                    System.out.println("  "+context.getAttribute(line.getOptionValue(OPT_ATTRIBUTE)));
                } catch(NotImplemented e) {
                    System.out.println("  Attribute not supported ["+e.getMessage()+"]");
                }
            } else {
                context.getAttribute(Context.USERID);   // this triggers initialization of context
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
