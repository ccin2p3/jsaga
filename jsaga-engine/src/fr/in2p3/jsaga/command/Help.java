package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptorBuilder;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.bean.EngineConfiguration;
import fr.in2p3.jsaga.engine.factories.SecurityAdaptorBuilderFactory;
import fr.in2p3.jsaga.engine.schema.config.*;
import fr.in2p3.jsaga.helpers.ASCIITableFormatter;
import fr.in2p3.jsaga.helpers.StringArray;
import fr.in2p3.jsaga.introspector.Introspector;
import fr.in2p3.jsaga.introspector.IntrospectorFactory;
import org.apache.commons.cli.*;

import java.util.HashMap;
import java.util.Map;

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
    private static final String OPT_DATA = "d", LONGOPT_DATA = "data",
            ARG_DATA_SERVICE = "service", ARG_DATA_CONTEXT = "context",
            USAGE_OPT_DATA = "<mode> = "+ARG_DATA_SERVICE+" | "+ARG_DATA_CONTEXT;
    private static final String OPT_JOB = "j", LONGOPT_JOB = "job",
            ARG_JOB_SERVICE = "service", ARG_JOB_CONTEXT = "context",
            USAGE_OPT_JOB = "<mode> = "+ARG_JOB_SERVICE+" | "+ARG_JOB_CONTEXT;
    private static final String OPT_SECURITY_ATTRIBUTE = "a", LONGOPT_SECURITY_ATTRIBUTE = "attribute";
    private static final String LONGOPT_EFFECTIVE_CONFIG = "config";

    public Help() {
        super("jsaga-help");
    }

    public static void main(String[] args) throws Exception {
        Help command = new Help();
        CommandLine line = command.parse(args);
        EngineConfiguration config = Configuration.getInstance().getConfigurations();
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

            Context[] ctxArray = config.getContextCfg().toXMLArray();
            String LEGENDE = "\nwhere:\n"+
                    "\t_Attribute_\tcan not be entered from the prompt\n"+
                    "\t*Attribute*\tis a hidden attribute\n"+
                    "\t<Attribute>\tis a path to an existing file or directory\n"+
                    "\t[Attribute]\tis an optional attribute\n";
            if (arg.equals(ARG_SECURITY_USAGE)) {
                ASCIITableFormatter formatter = new ASCIITableFormatter(new String[] {
                        "Type", "Attributes usage"});
                for (int c=0; c<ctxArray.length; c++) {
                    Context ctx = ctxArray[c];
                    formatter.append(new String[] {ctx.getName(), ctx.getUsage()});
                }
                formatter.dump(System.out);
                System.out.print(LEGENDE);
            } else if (arg.equals(ARG_SECURITY_DEFAULT)) {
                ASCIITableFormatter formatter = new ASCIITableFormatter(new String[] {
                        "Type", "Default attributes"});
                for (int c=0; c<ctxArray.length; c++) {
                    Context ctx = ctxArray[c];
                    for (int a=0; a<ctx.getAttributeCount(); a++) {
                        String attribute = ctx.getAttribute(a).getName()+" = "+ctx.getAttribute(a).getValue();
                        formatter.append(new String[] {(a==0 ? ctx.getName() : null), attribute});
                    }
                }
                formatter.dump(System.out);
            } else if (arg.equals(ARG_SECURITY_MISSING)) {
                ASCIITableFormatter formatter = new ASCIITableFormatter(new String[] {
                        "Type", "Missing attributes"});
                for (int c=0; c<ctxArray.length; c++) {
                    Context ctx = ctxArray[c];
                    Map attributes = new HashMap();
                    for (int i=0; i<ctx.getAttributeCount(); i++) {
                        attributes.put(ctx.getAttribute(i).getName(), ctx.getAttribute(i).getValue());
                    }
                    SecurityAdaptorBuilder adaptor = SecurityAdaptorBuilderFactory.getInstance().getSecurityAdaptorBuilder(ctx.getName());
                    Usage usage = adaptor.getUsage();
                    Usage missing = (usage!=null ? usage.getMissingValues(attributes) : null);
                    formatter.append(new String[] {ctx.getName(), (missing!=null ? missing.toString() : null)});
                }
                formatter.dump(System.out);
                System.out.print(LEGENDE);
            } else {
                command.printHelpAndExit("Missing required argument: "+ USAGE_OPT_SECURITY);
            }
        }
        else if (line.hasOption(OPT_DATA))
        {
            String arg = line.getOptionValue(OPT_DATA);
            if (arg.equals(ARG_DATA_SERVICE)) {
                dumpServices(IntrospectorFactory.createNSIntrospector());
            } else if (arg.equals(ARG_DATA_CONTEXT)) {
                dumpContexts(IntrospectorFactory.createNSIntrospector());
            } else {
                command.printHelpAndExit("Missing required argument: "+USAGE_OPT_DATA);
            }
        }
        else if (line.hasOption(OPT_JOB))
        {
            String arg = line.getOptionValue(OPT_JOB);
            if (arg.equals(ARG_JOB_SERVICE)) {
                dumpServices(IntrospectorFactory.createJobIntrospector());
            } else if (arg.equals(ARG_JOB_CONTEXT)) {
                dumpContexts(IntrospectorFactory.createJobIntrospector());
            } else {
                command.printHelpAndExit("Missing required argument: "+USAGE_OPT_JOB);
            }
        }
        else if (line.hasOption(OPT_SECURITY_ATTRIBUTE))
        {
            String arg = line.getOptionValue(OPT_SECURITY_ATTRIBUTE);
            if (arg.contains(".")) {
                String ctxId = arg.substring(0, arg.indexOf("."));
                String attrName = arg.substring(arg.indexOf(".")+1);
                String attrValue = getAttribute(config.getContextCfg().findContextByName(ctxId), attrName);
                System.out.println(attrValue);
            } else {
                command.printHelpAndExit("Bad argument: "+arg);
            }
        }
        else if (line.hasOption(LONGOPT_EFFECTIVE_CONFIG))
        {
            config.dump(System.out);
        }
    }

    private static void dumpServices(Introspector introspector) throws Exception {
        ASCIITableFormatter formatter = new ASCIITableFormatter(new String[] {
                "Scheme", "Host pattern", "Service name"});
        for (String scheme : introspector.getVectorAttribute(Introspector.SCHEME)) {
            Introspector nsScheme = introspector.getChildIntrospector(scheme);
            boolean isFirst = true;
            for (String hostPattern : nsScheme.getVectorAttribute(Introspector.HOST_PATTERN)) {
                Introspector nsHostPattern = nsScheme.getChildIntrospector(hostPattern);
                formatter.append(new String[] {
                        isFirst ? scheme : null,
                        hostPattern,
                        StringArray.arrayToString(nsHostPattern.getVectorAttribute(Introspector.SERVICE), ",")});
                isFirst = false;
            }
        }
        formatter.dump(System.out);
    }

    private static void dumpContexts(Introspector introspector) throws Exception {
        ASCIITableFormatter formatter = new ASCIITableFormatter(new String[] {
                "Scheme", "Context name"});
        for (String scheme : introspector.getVectorAttribute(Introspector.SCHEME)) {
            Introspector nsScheme = introspector.getChildIntrospector(scheme);
            formatter.append(new String[] {
                    scheme,
                    StringArray.arrayToString(nsScheme.getVectorAttribute(Introspector.CONTEXT), ",")});
        }
        formatter.dump(System.out);
    }

    private static String getAttribute(ObjectType object, String name) throws Exception {
        for (int a=0; object!=null && a<object.getAttributeCount(); a++) {
            Attribute attribute = object.getAttribute(a);
            if (attribute.getName().equals(name)) {
                return attribute.getValue();
            }
        }
        throw new Exception("Attribute not found: "+name);
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
            group.addOption(OptionBuilder.withDescription("Information about data protocols.\n"+ USAGE_OPT_DATA)
                    .withLongOpt(LONGOPT_DATA)
                    .withArgName("mode")
                    .hasArg()
                    .create(OPT_DATA));
            group.addOption(OptionBuilder.withDescription("Information about job services.\n"+ USAGE_OPT_JOB)
                    .withLongOpt(LONGOPT_JOB)
                    .withArgName("mode")
                    .hasArg()
                    .create(OPT_JOB));
            group.addOption(OptionBuilder.withDescription("Output the value of security context attribute")
                    .withLongOpt(LONGOPT_SECURITY_ATTRIBUTE)
                    .withArgName("ctxId>.<attr")
                    .hasArg()
                    .create(OPT_SECURITY_ATTRIBUTE));
            group.addOption(OptionBuilder.withDescription("Output the effective configuration")
                    .withLongOpt(LONGOPT_EFFECTIVE_CONFIG)
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
