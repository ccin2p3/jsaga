package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.schema.config.ContextInstance;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.apache.commons.cli.*;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.DoesNotExist;
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
    private static final String OPT_USERID = "u", LONGOPT_USERID = "userid";
    private static final String OPT_TIMELEFT = "t", LONGOPT_TIMELEFT = "timeleft";

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
                String type = context.getAttribute(Context.TYPE);
                String indice = context.getAttribute("Indice");
                String name;
                try {
                    name = context.getAttribute("Name");
                } catch (DoesNotExist e) {
                    name = null;
                }
                System.out.println(
                        (type!=null ? type : "???") +
                        (indice!=null ? "["+indice+"]" : "") +
                        (name!=null ? ": "+name : ""));
                // print context
                printContext(context, line, "  ");
            }
            session.close();
        }
        else if (command.m_nonOptionValues.length == 1)
        {
            String id = command.m_nonOptionValues[0];
            ContextInstance[] xmlContext = Configuration.getInstance().getConfigurations().getContextCfg().listContextInstanceArrayById(id);
            for (int i=0; i<xmlContext.length; i++) {
                Context context = ContextFactory.createContext();
                context.setAttribute(Context.TYPE, xmlContext[i].getType());
                context.setAttribute("Indice", ""+xmlContext[i].getIndice());
                context.setDefaults();
                // print context
                printContext(context, line, "");
                // close
                ((ContextImpl) context).close();
            }
        }
    }

    private static void printContext(Context context, CommandLine line, String indent) throws Exception {
        if (line.hasOption(OPT_USERID) || line.hasOption(OPT_TIMELEFT)) {
            try {
                if (line.hasOption(OPT_USERID)) {
                    System.out.println(indent+context.getAttribute("UserID"));
                } else if (line.hasOption(OPT_TIMELEFT)) {
                    System.out.println(indent+context.getAttribute("TimeLeft"));
                }
            } catch (Exception e) {
                System.out.println(indent+"Not initialized: ["+e.getMessage()+"]");
            }
        } else {
            System.out.println(context);
        }
    }

    protected Options createOptions() {
        Options opt = new Options();

        // command
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));

        // options group
        OptionGroup group = new OptionGroup();
        group.setRequired(false);
        {
            group.addOption(OptionBuilder.withDescription("Print UserID attribute only")
                    .withLongOpt(LONGOPT_USERID)
                    .create(OPT_USERID));
            group.addOption(OptionBuilder.withDescription("Print TimeLeft attribute only")
                    .withLongOpt(LONGOPT_TIMELEFT)
                    .create(OPT_TIMELEFT));
        }
        opt.addOptionGroup(group);

        // system properties
        opt.addOption(OptionBuilder.withDescription("Set context instance attribute (e.g. -DVOMS[0].UserVO=dteam)")
                .withArgName("ctxId>.<attr>=<value")
                .hasArg()
                .withValueSeparator()
                .create("D"));
        return opt;
    }
}
