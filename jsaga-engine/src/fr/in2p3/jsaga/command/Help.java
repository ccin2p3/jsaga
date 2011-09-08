package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.engine.session.SessionConfiguration;
import fr.in2p3.jsaga.engine.descriptors.*;
import fr.in2p3.jsaga.engine.schema.config.Execution;
import fr.in2p3.jsaga.engine.schema.config.Protocol;
import fr.in2p3.jsaga.helpers.ASCIITableFormatter;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.apache.commons.cli.*;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

import java.net.URL;
import java.util.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   Help
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   6 avr. 2007
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class Help extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    private static final String OPT_VERSION = "v", LONGOPT_VERSION = "version";
    private static final String OPT_SECURITY = "s", LONGOPT_SECURITY = "security",
            ARG_SECURITY_USAGE = "usage", ARG_SECURITY_DEFAULT = "default", ARG_SECURITY_MISSING = "missing",
            USAGE_OPT_SECURITY = "<mode> = "+ARG_SECURITY_USAGE+" | "+ ARG_SECURITY_DEFAULT +" | "+ARG_SECURITY_MISSING;
    private static final String OPT_DATA = "d", LONGOPT_DATA = "data";
    private static final String OPT_JOB = "j", LONGOPT_JOB = "job";
    private static final String LONGOPT_DUMP_ADAPTORS = "adaptors";
    private static final String LONGOPT_DUMP_SESSION = "session";
    private static final String LONGOPT_DUMP_CONFIG = "config";

    public Help() {
        super("jsaga-help");
    }

    public static void main(String[] args) throws Exception {
        Help command = new Help();
        CommandLine line = command.parse(args);
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else if (line.hasOption(OPT_VERSION))
        {
            Package p = Help.class.getPackage();
            System.out.println("JSAGA Engine");
            System.out.println(p.getImplementationVendor());
            System.out.println();
            System.out.println("SAGA  specification  version: "+p.getSpecificationVersion());
            System.out.println("JSAGA implementation version: "+p.getImplementationVersion());

        }
        else if (line.hasOption(OPT_SECURITY))
        {
            String arg = line.getOptionValue(OPT_SECURITY);

            Session session = SessionFactory.createSession(true);
            String LEGENDE = "\nwhere:\n"+
                    "\t_Attribute_\tcan not be entered from the prompt\n"+
                    "\t*Attribute*\tis a hidden attribute\n"+
                    "\t<Attribute>\tis a path to an existing file or directory\n"+
                    "\t[Attribute]\tis an optional attribute\n";
            if (arg.equals(ARG_SECURITY_USAGE)) {
                ASCIITableFormatter formatter = new ASCIITableFormatter(new String[] {
                        "Type", "Attributes usage"});
                for (Context context : session.listContexts()) {
                    String type = context.getAttribute(Context.TYPE);
                    String usage = ((ContextImpl) context).getUsage();
                    formatter.append(new String[] {type, usage});
                }
                formatter.dump(System.out);
                System.out.print(LEGENDE);
            } else if (arg.equals(ARG_SECURITY_DEFAULT)) {
                ASCIITableFormatter formatter = new ASCIITableFormatter(new String[] {
                        "Type", "Default attributes"});
                Set<String> ignored = new HashSet<String>();
                ignored.addAll(Arrays.asList(Context.TYPE, ContextImpl.URL_PREFIX,
                        ContextImpl.BASE_URL_INCLUDES, ContextImpl.BASE_URL_EXCLUDES,
                        ContextImpl.JOB_SERVICE_ATTRIBUTES, ContextImpl.DATA_SERVICE_ATTRIBUTES));
                for (Context context : session.listContexts()) {
                    String type = context.getAttribute(Context.TYPE);
                    boolean first = true;
                    for (String key : context.listAttributes()) {
                        if (! ignored.contains(key)) {
                            formatter.append(new String[] {
                                    (first ? type : null),
                                    key+"="+((ContextImpl)context).getDefault(key)});
                            first = false;
                        }
                    }
                }
                formatter.dump(System.out);
            } else if (arg.equals(ARG_SECURITY_MISSING)) {
                ASCIITableFormatter formatter = new ASCIITableFormatter(new String[] {
                        "Type", "Missing attributes"});
                for (Context context : session.listContexts()) {
                    String type = context.getAttribute(Context.TYPE);
                    String missing = ((ContextImpl) context).getMissings();
                    formatter.append(new String[] {
                            type,
                            (missing!=null ? missing.toString() : null)});
                }
                formatter.dump(System.out);
                System.out.print(LEGENDE);
            } else {
                command.printHelpAndExit("Missing required argument: "+ USAGE_OPT_SECURITY);
            }
        }
        else if (line.hasOption(OPT_DATA))
        {
            DataAdaptorDescriptor descriptor = AdaptorDescriptors.getInstance().getDataDesc();
            ASCIITableFormatter formatter = new ASCIITableFormatter(new String[] {
                    "Scheme", "Supported contexts"});
            for (Protocol protocol : descriptor.getXML()) {
                formatter.append(new String[] {
                        protocol.getType(),
                        Arrays.toString(protocol.getSupportedContextType())});
            }
            formatter.dump(System.out);
        }
        else if (line.hasOption(OPT_JOB))
        {
            JobAdaptorDescriptor descriptor = AdaptorDescriptors.getInstance().getJobDesc();
            ASCIITableFormatter formatter = new ASCIITableFormatter(new String[] {
                    "Scheme", "Supported contexts"});
            for (Execution execution : descriptor.getXML()) {
                formatter.append(new String[] {
                        execution.getType(),
                        Arrays.toString(execution.getSupportedContextType())});
            }
            formatter.dump(System.out);
        }
        else if (line.hasOption(LONGOPT_DUMP_ADAPTORS))
        {
            System.out.println(new String(AdaptorDescriptors.getInstance().toByteArray()));
        }
        else if (line.hasOption(LONGOPT_DUMP_SESSION))
        {
            Session session = SessionFactory.createSession(true);
            for (org.ogf.saga.context.Context context : session.listContexts()) {
                System.out.println("-------------------------");
                for (String key : context.listAttributes()) {
                    try {
                        if (context.isVectorAttribute(key)) {
                            System.out.println(key+"="+Arrays.toString(context.getVectorAttribute(key)));
                        } else {
                            System.out.println(key+"="+context.getAttribute(key));
                        }
                    } catch (DoesNotExistException e) {
                        System.out.println(key+"=[NOT INITIALIZED]");
                    }
                }
            }
        }
        else if (line.hasOption(LONGOPT_DUMP_CONFIG))
        {
            // WARNING: this code is JSAGA specific
            URL url = EngineProperties.getURL(EngineProperties.JSAGA_DEFAULT_CONTEXTS);
            SessionConfiguration cfg = new SessionConfiguration(url);
            System.out.println(cfg.toXML());
        }
    }

    protected Options createOptions() {
        Options opt = new Options();

        // command group
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        {
            group.addOption(OptionBuilder.withDescription("Display this help and exit")
                    .withLongOpt(LONGOPT_HELP)
                    .create(OPT_HELP));
            group.addOption(OptionBuilder.withDescription("Output version information and exit")
                    .withLongOpt(LONGOPT_VERSION)
                    .create(OPT_VERSION));
            group.addOption(OptionBuilder.withDescription("Information about security context instances.\n"+ USAGE_OPT_SECURITY)
                    .withLongOpt(LONGOPT_SECURITY)
                    .withArgName("mode")
                    .hasArg()
                    .create(OPT_SECURITY));
            group.addOption(OptionBuilder.withDescription("Information about data protocols.")
                    .withLongOpt(LONGOPT_DATA)
                    .create(OPT_DATA));
            group.addOption(OptionBuilder.withDescription("Information about job services.")
                    .withLongOpt(LONGOPT_JOB)
                    .create(OPT_JOB));
            group.addOption(OptionBuilder.withDescription("Dump information about adaptors as XML")
                    .withLongOpt(LONGOPT_DUMP_ADAPTORS)
                    .create());
            group.addOption(OptionBuilder.withDescription("Dump the default session")
                    .withLongOpt(LONGOPT_DUMP_SESSION)
                    .create());
            group.addOption(OptionBuilder.withDescription("Dump the effective XML configuration")
                    .withLongOpt(LONGOPT_DUMP_CONFIG)
                    .create());
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
