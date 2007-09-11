package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.InitializableSecurityAdaptorBuilder;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptorBuilder;
import fr.in2p3.jsaga.engine.adaptor.SecurityAdaptorBuilderFactory;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.bean.EngineConfiguration;
import fr.in2p3.jsaga.engine.schema.config.*;
import fr.in2p3.jsaga.helpers.ASCIITableFormatter;
import fr.in2p3.jsaga.helpers.StringArray;
import org.apache.commons.cli.*;

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
    private static final String OPT_SECURITY_INIT = "i", LONGOPT_SECURITY_INIT = "init";
    private static final String OPT_SECURITY = "s", LONGOPT_SECURITY = "security",
            ARG_SECURITY_USAGE = "usage", ARG_SECURITY_DEFAULT = "default", ARG_SECURITY_MISSING = "missing",
            USAGE_OPT_SECURITY = "<mode> = "+ARG_SECURITY_USAGE+" | "+ ARG_SECURITY_DEFAULT +" | "+ARG_SECURITY_MISSING;
    private static final String OPT_DATA = "d", LONGOPT_DATA = "data";
    private static final String OPT_JOB = "j", LONGOPT_JOB = "job";
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
        else if (line.hasOption(OPT_SECURITY_INIT) || line.hasOption(OPT_SECURITY))
        {
            String arg = line.hasOption(OPT_SECURITY_INIT)
                    ? line.getOptionValue(OPT_SECURITY_INIT)
                    : line.getOptionValue(OPT_SECURITY);

            ContextInstance[] ctxArray = config.getContextCfg().toXMLArray();
            String LEGENDE = "\nwhere:\n"+
                    "\t_Attribute_\tcan not be entered from the prompt\n"+
                    "\t*Attribute*\tis a hidden attribute\n"+
                    "\t<Attribute>\tis a path to an existing file or directory\n"+
                    "\t[Attribute]\tis an optional attribute\n";
            if (arg.equals(ARG_SECURITY_USAGE)) {
                ASCIITableFormatter formatter = new ASCIITableFormatter(new String[] {
                        "Type", "Attributes usage"});
                for (int c=0; c<ctxArray.length; c++) {
                    ContextInstance ctx = ctxArray[c];
                    if (ctx.getIndice() == 0) {
                        if (line.hasOption(OPT_SECURITY_INIT)) {
                            if (ctx.getInit() != null) {
                                formatter.append(new String[] {ctx.getType(), ctx.getInit().getUsage()});
                            }
                        } else {
                            formatter.append(new String[] {ctx.getType(), ctx.getUsage()});
                        }
                    }
                }
                formatter.dump(System.out);
                System.out.print(LEGENDE);
            } else if (arg.equals(ARG_SECURITY_DEFAULT)) {
                ASCIITableFormatter formatter = new ASCIITableFormatter(new String[] {
                        "Type", "Indice", "Name", "Default attributes"});
                for (int c=0; c<ctxArray.length; c++) {
                    ContextInstance ctx = ctxArray[c];
                    for (int a=0; a<ctx.getAttributeCount(); a++) {
                        String type = (a==0 ? ctx.getType() : null);
                        String indice = (a==0 ? ""+ctx.getIndice() : null);
                        String name = (a==0 ? ctx.getName() : null);
                        String attribute = ctx.getAttribute(a).getName()+" = "+ctx.getAttribute(a).getValue();
                        if (line.hasOption(OPT_SECURITY_INIT)) {
                            if (ctx.getInit() != null) {
                                formatter.append(new String[] {type, indice, name, attribute});
                            }
                        } else {
                            formatter.append(new String[] {type, indice, name, attribute});
                        }
                    }
                }
                formatter.dump(System.out);
            } else if (arg.equals(ARG_SECURITY_MISSING)) {
                ASCIITableFormatter formatter = new ASCIITableFormatter(new String[] {
                        "Type", "Indice", "Name", "Missing attributes"});
                for (int c=0; c<ctxArray.length; c++) {
                    ContextInstance ctx = ctxArray[c];
                    Map attributes = new HashMap();
                    for (int i=0; i<ctx.getAttributeCount(); i++) {
                        attributes.put(ctx.getAttribute(i).getName(), ctx.getAttribute(i).getValue());
                    }
                    SecurityAdaptorBuilder adaptor = SecurityAdaptorBuilderFactory.getInstance().getSecurityAdaptorBuilder(ctx.getType());
                    if (line.hasOption(OPT_SECURITY_INIT)) {
                        if (ctx.getInit() != null) {
                            Usage missing = ((InitializableSecurityAdaptorBuilder)adaptor).getInitUsage().getMissingValues(attributes);
                            formatter.append(new String[] {ctx.getType(), ""+ctx.getIndice(), ctx.getName(),
                                    (missing!=null ? missing.toString() : null)});
                        }
                    } else {
                        Usage missing = adaptor.getUsage().getMissingValues(attributes);
                        formatter.append(new String[] {ctx.getType(), ""+ctx.getIndice(), ctx.getName(),
                                (missing!=null ? missing.toString() : null)});
                    }
                }
                formatter.dump(System.out);
                System.out.print(LEGENDE);
            } else {
                command.printHelpAndExit("Missing required argument: "+ USAGE_OPT_SECURITY);
            }
        }
        else if (line.hasOption(OPT_DATA))
        {
            Protocol[] protocolArray = config.getProtocolCfg().toXMLArray();
            ASCIITableFormatter formatter = new ASCIITableFormatter(new String[] {
                    "Protocol", "Aliases / Host patterns", "Ambiguous security contexts"});
            for (int p=0; p<protocolArray.length; p++) {
                Protocol protocol = protocolArray[p];
                Set hostnameSet = getHostnames(protocol);
                if (hostnameSet.size() == 1) {
                    ContextInstanceRef[] ctxArray = config.getProtocolCfg().listContextInstanceCandidates(protocol, "*.*", null);
                    formatter.append(new String[] {
                            protocol.getScheme(),
                            StringArray.arrayToString(protocol.getSchemeAlias(), ","),
                            ctxArray.length>1 ? contextArrayToString(ctxArray) : null});
                } else {
                    formatter.append(new String[] {
                            protocol.getScheme(),
                            StringArray.arrayToString(protocol.getSchemeAlias(), ","),
                            null});
                    for (Iterator it= hostnameSet.iterator(); it.hasNext(); ) {
                        String hostname = (String) it.next();
                        ContextInstanceRef[] ctxArray = config.getProtocolCfg().listContextInstanceCandidates(protocol, hostname, null);
                        if (ctxArray.length > 1) {
                            formatter.append(new String[] {
                                    null,
                                    "    "+hostname,
                                    contextArrayToString(ctxArray)});
                        }
                    }
                }
            }
            formatter.dump(System.out);
        }
        else if (line.hasOption(OPT_JOB))
        {
            Jobservice[] jobArray = config.getJobserviceCfg().toXMLArray();
            ASCIITableFormatter formatter = new ASCIITableFormatter(new String[] {
                    "Type", "Path"});
            for (int i=0; i<jobArray.length; i++) {
                Jobservice job = jobArray[i];
                formatter.append(new String[] {
                        job.getType(), job.getPath()});
            }
            formatter.dump(System.out);
        }
        else if (line.hasOption(LONGOPT_EFFECTIVE_CONFIG))
        {
            config.dump(System.out);
        }
    }

    private static Set getHostnames(Protocol protocol) {
        Set hostnameSet = new HashSet();
        for (int d=0; d<protocol.getDomainCount(); d++) {
            Domain domain = protocol.getDomain(d);
            for (int h=0; h<domain.getHostCount(); h++) {
                Host host = domain.getHost(h);
                hostnameSet.add(host.getName()+"*."+domain.getName());
            }
            hostnameSet.add("*."+domain.getName());
        }
        hostnameSet.add("*.*");
        return hostnameSet;
    }

    private static String contextArrayToString(ContextInstanceRef ctxArray[]) {
        // convert to string array
        String[] strArray = new String[ctxArray.length];
        for (int c=0; c<ctxArray.length; c++) {
            ContextInstanceRef ctx = ctxArray[c];
            if (ctx.getName() != null) {
                strArray[c] = ctx.getName();
            } else {
                strArray[c] = ctx.getType()+"["+ctx.getIndice()+"]";
            }
        }
        // string array to comma-separated string
        return StringArray.arrayToString(strArray, ", ");
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
            group.addOption(OptionBuilder.withDescription("Information about security context initialization.\n"+ USAGE_OPT_SECURITY)
                    .withLongOpt(LONGOPT_SECURITY_INIT)
                    .withArgName("mode")
                    .hasArg()
                    .create(OPT_SECURITY_INIT));
            group.addOption(OptionBuilder.withDescription("Information about security context instances.\n"+ USAGE_OPT_SECURITY)
                    .withLongOpt(LONGOPT_SECURITY)
                    .withArgName("mode")
                    .hasArg()
                    .create(OPT_SECURITY));
            group.addOption(OptionBuilder.withDescription("Information about data protocols")
                    .withLongOpt(LONGOPT_DATA)
                    .create(OPT_DATA));
            group.addOption(OptionBuilder.withDescription("Information about job services")
                    .withLongOpt(LONGOPT_JOB)
                    .create(OPT_JOB));
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
