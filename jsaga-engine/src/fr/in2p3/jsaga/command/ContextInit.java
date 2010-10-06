package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.impl.context.ContextImpl;
import fr.in2p3.jsaga.impl.session.SessionImpl;
import org.apache.commons.cli.*;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URLFactory;

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
                try {
                    setUserPass(contexts[i]);
                } catch (Exception e) {
                    throw new Exception("Exception occured for context: "+contexts[i].getAttribute(Context.TYPE), e);
                }
            }
            session.close();
        }
        else if (command.m_nonOptionValues.length == 1)
        {
            String id = command.m_nonOptionValues[0];
            SessionImpl session = (SessionImpl) SessionFactory.createSession(true);
            ContextImpl context = session.findContext(URLFactory.createURL(id+"-any://host"));
            if (context != null) {
                setUserPass(context);
                context.close();
            } else {
                throw new Exception("Context not found: "+id);
            }
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
        // trigger initialization of context
        context.getAttribute(Context.USERID);
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
