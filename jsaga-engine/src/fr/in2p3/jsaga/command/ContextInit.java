package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.engine.session.SessionConfiguration;
import fr.in2p3.jsaga.impl.context.ConfiguredContext;
import fr.in2p3.jsaga.impl.context.ConfigurableContextFactory;

import org.apache.commons.cli.*;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import java.io.*;
import java.util.regex.Pattern;

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
        else {
            // create an empty SAGA session
            Session session = SessionFactory.createSession(false);
            // use JSAGA specific classes: lis of ConfigurableContext extracted by the JSAGA configuration
            // by ConfigurableContextFactory
            ConfiguredContext[] configContexts = ConfigurableContextFactory.listConfiguredContext();
            for (int i=0; i<configContexts.length; i++) {
                if (command.m_nonOptionValues.length==0
                    || command.m_nonOptionValues[0].equals(configContexts[i].getUrlPrefix())
                    || command.m_nonOptionValues[0].equals(configContexts[i].getType()))
                {
                	// Build the SAGA context
                    Context context = ConfigurableContextFactory.createContext(configContexts[i]);
                    // set password
                    setUserPass(context);
                    // add context to session (and init context)
                    session.addContext(context);
                }
            }
            session.close();
        }
    }

    private static void setUserPass(Context context) throws Exception {
        // todo: introspect context object to know whether UserPass is supported or not
        try {
            context.getAttribute(Context.USERPASS);
        } catch (DoesNotExistException e) {
            // prompt for UserPass
            System.out.println("Enter UserPass for security context: "+ContextInfo.getLabel(context));
            String userPass = getUserInput();

            // set UserPass
            if (userPass != null) {
                context.setAttribute(Context.USERPASS, userPass);
            }
        }
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

        return opt;
    }
}
