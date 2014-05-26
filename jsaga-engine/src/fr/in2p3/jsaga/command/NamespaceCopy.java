package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;
import org.ogf.saga.monitoring.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NamespaceCopy
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NamespaceCopy extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    private static final String OPT_NOT_OVERWRITE = "i", LONGOPT_NOT_OVERWRITE = "interactive";
    private static final String OPT_RECURSIVE = "r", LONGOPT_RECURSIVE = "recursive";
    private static final String OPT_PRESERVE_TIMES = "p", LONGOPT_PRESERVE_TIMES = "preserve";
    private static final String OPT_MONITOR = "m", LONGOPT_MONITOR = "monitor";

    private static final int FLAGS_PRESERVETIMES = 8192;

    public NamespaceCopy() {
        super("jsaga-cp", new String[]{"Source URL", "Target URL"}, new String[]{OPT_HELP, LONGOPT_HELP});
    }

    public static void main(String[] args) throws Exception {
        NamespaceCopy command = new NamespaceCopy();
        CommandLine line = command.parse(args);
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else
        {
            // get arguments
            URL source = URLFactory.createURL(command.m_nonOptionValues[0]);
            URL target = URLFactory.createURL(command.m_nonOptionValues[1]);
            int flags = (line.hasOption(OPT_NOT_OVERWRITE) ? Flags.NONE : Flags.OVERWRITE)
                    .or((line.hasOption(OPT_RECURSIVE) ? Flags.RECURSIVE : Flags.NONE)
                    .or((line.hasOption(OPT_PRESERVE_TIMES) ? FLAGS_PRESERVETIMES : Flags.NONE.getValue())));

            // execute command
            Session session = SessionFactory.createSession(true);
            NSEntry entry = NSFactory.createNSEntry(session, source, Flags.NONE.getValue());
            if (line.hasOption(OPT_MONITOR)) {
                Task task = entry.copy(TaskMode.TASK, target, flags);
                try {
                    Metric metric = task.getMetric("file.copy.progress");
                    metric.addCallback(new Callback(){
                        public boolean cb(Monitorable mt, Metric metric, Context ctx) throws NotImplementedException, AuthorizationFailedException {
                            try {
                                String value = metric.getAttribute(Metric.VALUE);
                                String unit = metric.getAttribute(Metric.UNIT);
                                System.out.println("Progress: "+value+" "+unit);
                            }
                            catch (NotImplementedException e) {throw e;}
                            catch (AuthorizationFailedException e) {throw e;}
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            // callback must stay registered
                            return true;
                        }
                    });
                } catch(DoesNotExistException e) {
                    System.err.println("WARN: Monitoring is not supported for this kind of transfer");
                }
                task.run();
                task.waitFor();
                switch(task.getState()) {
                    case DONE:
                        System.out.println("File successfully copied !");
                        break;
                    default:
                        task.rethrow();
                        break;
                }
            } else {
                entry.copy(target, flags);
            }
            entry.close();
            System.exit(0);
        }
    }

    protected Options createOptions() {
        Options opt = new Options();
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));
        opt.addOption(OptionBuilder.withDescription("Do not overwrite target")
                .isRequired(false)
                .withLongOpt(LONGOPT_NOT_OVERWRITE)
                .create(OPT_NOT_OVERWRITE));
        opt.addOption(OptionBuilder.withDescription("Copy recursively")
                .isRequired(false)
                .withLongOpt(LONGOPT_RECURSIVE)
                .create(OPT_RECURSIVE));
        opt.addOption(OptionBuilder.withDescription("Preserve times")
                .isRequired(false)
                .withLongOpt(LONGOPT_PRESERVE_TIMES)
                .create(OPT_PRESERVE_TIMES));
        opt.addOption(OptionBuilder.withDescription("Monitor transfer")
                .isRequired(false)
                .withLongOpt(LONGOPT_MONITOR)
                .create(OPT_MONITOR));
        return opt;
    }
}
