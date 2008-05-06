package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.apache.commons.cli.*;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

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
                ((ContextImpl) contexts[i]).destroy();
            }
            session.close();
        }
        else if (command.m_nonOptionValues.length == 1)
        {
            String id = command.m_nonOptionValues[0];
            fr.in2p3.jsaga.engine.schema.config.Context[] xmlContexts = Configuration.getInstance().getConfigurations().getContextCfg().listContextsArray(id);
            if (xmlContexts.length == 0) {
                throw new BadParameter("Context type not found: "+id);
            }
            for (int i=0; i<xmlContexts.length; i++) {
                Context context = ContextFactory.createContext();
                context.setAttribute(Context.TYPE, xmlContexts[i].getName());
                context.setDefaults();
                ((ContextImpl) context).destroy();
                ((ContextImpl) context).close();
            }
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
