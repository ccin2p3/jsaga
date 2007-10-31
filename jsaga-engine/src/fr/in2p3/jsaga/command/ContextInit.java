package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.schema.config.ContextInstance;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.apache.commons.cli.*;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

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
                ((ContextImpl) contexts[i]).init();
            }
            session.close();
        }
        else if (command.m_nonOptionValues.length == 1)
        {
            String id = command.m_nonOptionValues[0];
            ContextInstance[] xmlContext = Configuration.getInstance().getConfigurations().getContextCfg().listContextInstanceArrayById(id);
            for (int i=0; i<xmlContext.length; i++) {
                Context context = ContextFactory.createContext();
                context.setAttribute("Type", xmlContext[i].getType());
                context.setAttribute("Indice", ""+xmlContext[i].getIndice());
                context.setDefaults();
                ((ContextImpl) context).init();
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
