package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.impl.context.ConfigurableContextFactory;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import fr.in2p3.jsaga.impl.session.SessionImpl;
import org.apache.commons.cli.*;
import org.ogf.saga.context.Context;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ContextDestroy
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ContextDestroy extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";

    public ContextDestroy() {
        super("jsaga-context-destroy", new String[]{"contextId"}, null);        
    }

    public static void main(String[] args) throws Exception {
        ContextDestroy command = new ContextDestroy();
        CommandLine line = command.parse(args);
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else {
            // create empty session
            Session session = SessionFactory.createSession(false);
            String[] contextUrlPrefix = ConfigurableContextFactory.listContextUrlPrefix();

            for (int i=0; i<contextUrlPrefix.length; i++) {
                if (command.m_nonOptionValues.length==0
                    || command.m_nonOptionValues[0].equals(contextUrlPrefix[i])
                    /*|| command.m_nonOptionValues[0].equals(ConfigurableContextFactory.getType(contextIds[i])*/)
                {
                    Context context = ConfigurableContextFactory.createContext(contextUrlPrefix[i]);
                    ((ContextImpl) context).destroy();
                    ((ContextImpl) context).close();
                }
            }
            session.close();
        }
        /*
        else if (command.m_nonOptionValues.length == 0)
        {
            Session session = SessionFactory.createSession(true);
            Context[] contexts = session.listContexts();
            for (int i=0; i<contexts.length; i++) {
                ((ContextImpl) contexts[i]).destroy();
            }
            session.close();
        }
        else if (command.m_nonOptionValues.length == 1)
        {
            String id = command.m_nonOptionValues[0];
            SessionImpl session = (SessionImpl) SessionFactory.createSession(true);
            ContextImpl context = session.findContext(URLFactory.createURL(id+"-any://host"));
            if (context != null) {
                context.destroy();
                context.close();
            } else {
                throw new Exception("Context not found: "+id);
            }
        }
        */
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
