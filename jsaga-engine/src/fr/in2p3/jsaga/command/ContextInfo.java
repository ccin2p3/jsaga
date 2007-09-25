package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.schema.config.ContextInstance;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.apache.commons.cli.*;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

import java.util.Iterator;

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
            for (Iterator it=session.listContexts().iterator(); it.hasNext(); ) {
                Context context = (Context) it.next();
                System.out.println(context);
            }
            session.close(0.0f);
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
                System.out.println(context);
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
