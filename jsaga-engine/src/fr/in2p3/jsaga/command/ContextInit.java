package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.schema.config.Attribute;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.apache.commons.cli.*;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ContextInit
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   6 avr. 2007
* ***************************************************
* Description:                                      */
/**
 * Initialise context for one or all configured grids
 */
public class ContextInit extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";

    public ContextInit() {
        super("jsaga-context-init", new String[]{"contextId"}, null);
    }
    
    public static void main(String[] args) throws Exception {
        ContextInit command = new ContextInit();
        CommandLine line = command.parse(args);
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else if (command.m_nonOptionValues.length == 0)
        {
            Session session = SessionFactory.createSession(true);
            Context[] contexts = session.listContexts();
            for (int i=0; i<contexts.length; i++) {
                // get context configuration
                fr.in2p3.jsaga.engine.schema.config.Context xmlContext = Configuration.getInstance().getConfigurations().getContextCfg().findContext(
                        contexts[i].getAttribute(Context.TYPE));

                // set UserPass attribute
                setUserPass(contexts[i], xmlContext);

                // trigger initialization of context
                contexts[i].getAttribute(Context.USERID);
            }
            session.close();
        }
        else if (command.m_nonOptionValues.length == 1)
        {
            String id = command.m_nonOptionValues[0];
            fr.in2p3.jsaga.engine.schema.config.Context[] xmlContexts = Configuration.getInstance().getConfigurations().getContextCfg().listContextsArray(id);
            if (xmlContexts.length == 0) {
                throw new BadParameterException("Context type not found: "+id);
            }
            for (int i=0; i<xmlContexts.length; i++) {
                // set context
                Context context = ContextFactory.createContext();
                context.setAttribute(Context.TYPE, xmlContexts[i].getName());
                context.setDefaults();

                // set UserPass attribute
                setUserPass(context, xmlContexts[i]);

                // trigger initialization of context
                context.getAttribute(Context.USERID);

                // close context
                ((ContextImpl) context).close();
            }
        }
    }

    private static void setUserPass(Context context, fr.in2p3.jsaga.engine.schema.config.Context config) throws Exception {
        if (config.getUsage()!=null && config.getUsage().contains(Context.USERPASS) && !containsUserPass(config)) {
            // prompt for UserPass
            System.out.println("Enter UserPass for security context: "+context.getAttribute(Context.TYPE));
            String userPass = getUserInput();

            // set UserPass
            if (userPass != null) {
                context.setAttribute(Context.USERPASS, userPass);
            }
        }
    }

    private static boolean containsUserPass(fr.in2p3.jsaga.engine.schema.config.Context config) {
        for (int i=0; i<config.getAttributeCount(); i++) {
            Attribute attr = config.getAttribute(i);
            if (attr.getName().equals(Context.USERPASS)) {
                return (attr.getValue()!=null);
            }
        }
        return false;
    }

    private static volatile boolean s_stopped;
    private static String getUserInput() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        s_stopped = false;
        new Thread() {
            public void run() {
                while(!s_stopped) {
                    System.out.print("\b ");
                    try {
                        sleep(1);
                    } catch(InterruptedException e) {}
                }
            }
        }.start();
        try {
            String line = in.readLine();
            if (line!=null && line.trim().length() > 0) {
                return line;
            } else {
                return null;
            }
        } catch(IOException e) {
            throw e;
        } finally {
            s_stopped = true;
        }
    }

    protected Options createOptions() {
        Options opt = new Options();

        // command
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));

        // system properties
        opt.addOption(OptionBuilder.withDescription("Set context instance attribute (e.g. -DVOMS[0].UserVO=dteam)")
                .withArgName("ctxId>.<attr>=<value")
                .hasArg()
                .withValueSeparator()
                .create("D"));
        return opt;
    }
}
