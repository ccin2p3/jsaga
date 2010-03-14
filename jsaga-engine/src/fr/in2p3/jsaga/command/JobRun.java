package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.SagaException;
import org.ogf.saga.job.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.State;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.io.*;
import java.util.Iterator;
import java.util.Properties;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobRun
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   4 avr. 2007
* ***************************************************
* Description:                                      */
/**
 * -r local://localhost -Executable job.sh -FileTransfer input>input,output<<output
 */
public class JobRun extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    // required arguments
    private static final String OPT_RESOURCE = "r", LONGOPT_RESOURCE = "resource";
    // optional arguments
    private static final String OPT_FILE = "f", LONGOPT_FILE = "file";
    private static final String OPT_DESCRIPTION = "d", LONGOPT_DESCRIPTION = "description";
    private static final String OPT_BATCH = "b", LONGOPT_BATCH = "batch";
    // attribute names missing in interface JobDescription
    private static final String JOBNAME = "JobName";

    protected JobRun() {
        super("jsaga-job-run", null, null, new GnuParser());
    }

    public static void main(String[] args) throws Exception {
        JobRun command = new JobRun();
        CommandLine line = command.parse(args);
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else
        {
            // get arguments
            URL serviceURL = URLFactory.createURL(line.getOptionValue(OPT_RESOURCE));
            String file = line.getOptionValue(OPT_FILE);

            // create the job description
            Properties prop = new Properties();
            if (file != null) {
                prop.load(new FileInputStream(file));
            }
            for (Iterator it=line.iterator(); it.hasNext(); ) {
                Option opt = (Option) it.next();
                if (opt.getValue() != null) {
                    prop.setProperty(opt.getOpt(), opt.getValue());
                } else {
                    prop.setProperty(opt.getOpt(), Boolean.toString(true));
                }
            }
            JobDescription desc = createJobDescription(prop);
            boolean isStreamRedirected = prop.containsKey(JobDescription.INPUT) || prop.containsKey(JobDescription.OUTPUT) || prop.containsKey(JobDescription.ERROR);
            if (!line.hasOption(OPT_BATCH) && !isStreamRedirected) {
                desc.setAttribute(JobDescription.INTERACTIVE, "true");
            }

            // create the job
            Session session = SessionFactory.createSession(true);
            JobService service = JobFactory.createJobService(session, serviceURL);
            final Job job = service.createJob(desc);

            if (line.hasOption(OPT_DESCRIPTION)) {
                // dump job description
                String nativeDesc = job.getAttribute("NativeJobDescription");
                System.out.println(nativeDesc);
            } else {
                // submit
                job.run();

                if (line.hasOption(OPT_BATCH)) {
                    String jobId = job.getAttribute(Job.JOBID);
                    System.out.println(jobId);
                } else {
                    // add shutdown hook
                    Thread hook = new Thread(){
                        public void run() {
                            // cancel the job
                            try {
                                System.out.println("Canceling job: "+job.getAttribute(Job.JOBID));
                                job.cancel();
                            } catch (SagaException e) {
                                e.printStackTrace();
                            }
                            // give it a change to display final job state
                            try {sleep(1000);} catch(InterruptedException e){e.printStackTrace();}
                        }
                    };
                    Runtime.getRuntime().addShutdownHook(hook);

                    // wait
                    job.waitFor();

                    // display final state
                    State state = job.getState();
                    if (State.CANCELED.compareTo(state) == 0) {
                        System.out.println("Job canceled.");
                    } else {
                        Runtime.getRuntime().removeShutdownHook(hook);
                        if (State.DONE.compareTo(state) == 0) {
                            try {
                                if ("true".equalsIgnoreCase(desc.getAttribute(JobDescription.INTERACTIVE))) {
                                    copyStream(job.getStdout(), System.out);
                                } else {
                                    System.out.println("Job done.");
                                }
                            } catch(SagaException e) {
                                System.out.println("Job done.");
                            }
                        } else if (State.FAILED.compareTo(state) == 0) {
                            try {
                                String exitCode = job.getAttribute(Job.EXITCODE);
                                System.out.println("Job failed with exit code: "+exitCode);
                            } catch(SagaException e) {
                                System.out.println("Job failed.");
                                job.rethrow();
                            }
                        } else {
                            throw new Exception("Unexpected state: "+ state);
                        }
                    }
                }
            }
            System.exit(0);
        }
    }

    protected Options createOptions() {
        Options opt = new Options();

        // command
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));

        // required arguments
        opt.addOption(OptionBuilder.withDescription("the URL of the job service")
                .isRequired(true)
                .hasArg()
                .withArgName("URL")
                .withLongOpt(LONGOPT_RESOURCE)
                .create(OPT_RESOURCE));

        // optional arguments
        opt.addOption(OptionBuilder.withDescription("generate the job description in the targeted grid language " +
                "and exit (do not submit the job)")
                .withLongOpt(LONGOPT_DESCRIPTION)
                .create(OPT_DESCRIPTION));
        opt.addOption(OptionBuilder.withDescription("exit immediatly after having submitted the job, " +
                "and print the job ID on the standard output.")
                .withLongOpt(LONGOPT_BATCH)
                .create(OPT_BATCH));

        // required group
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.withDescription("read job description from file <path>")
                .hasArg()
                .withArgName("path")
                .withLongOpt(LONGOPT_FILE)
                .create(OPT_FILE));
        group.addOption(o("command to execute").hasArg().create(JobDescription.EXECUTABLE));
        group.setRequired(true);
        opt.addOptionGroup(group);

        // job description
        opt.addOption(o("job name to be attached to the job submission").hasArg().create(JOBNAME));
        opt.addOption(o("positional parameters for the command").hasArgs().create(JobDescription.ARGUMENTS));
        opt.addOption(o("SPMD job type and startup mechanism").hasArg().create(JobDescription.SPMDVARIATION));
        opt.addOption(o("total number of cpus requested for this job").hasArg().create(JobDescription.TOTALCPUCOUNT));
        opt.addOption(o("number of process instances to start").hasArg().create(JobDescription.NUMBEROFPROCESSES));
        opt.addOption(o("number of processes to start per host").hasArg().create(JobDescription.PROCESSESPERHOST));
        opt.addOption(o("expected number of threads per process").hasArg().create(JobDescription.THREADSPERPROCESS));
        opt.addOption(o("set of environment variables for the job").hasArgs().withValueSeparator().create(JobDescription.ENVIRONMENT));
        opt.addOption(o("working directory for the job").hasArg().create(JobDescription.WORKINGDIRECTORY));
        opt.addOption(o("run the job in interactive mode").create(JobDescription.INTERACTIVE));
        opt.addOption(o("pathname of the standard input file").hasArg().create(JobDescription.INPUT));
        opt.addOption(o("pathname of the standard output file").hasArg().create(JobDescription.OUTPUT));
        opt.addOption(o("pathname of the standard error file").hasArg().create(JobDescription.ERROR));
        opt.addOption(o("a list of file transfer directives").hasArgs().create(JobDescription.FILETRANSFER));
        opt.addOption(o("defines if output files get removed after the job finishes").hasArg().create(JobDescription.CLEANUP));
        opt.addOption(o("time at which a job should be scheduled").hasArg().create(JobDescription.JOBSTARTTIME));
        opt.addOption(o("estimated total number of CPU seconds which the job will require").hasArg().create(JobDescription.TOTALCPUTIME));
        opt.addOption(o("estimated amount of memory the job requires").hasArg().create(JobDescription.TOTALPHYSICALMEMORY));
        opt.addOption(o("compatible processor for job submission").hasArg().create(JobDescription.CPUARCHITECTURE));
        opt.addOption(o("compatible operating system for job submission").hasArg().create(JobDescription.OPERATINGSYSTEMTYPE));
        opt.addOption(o("list of host names which are to be considered by the resource manager as candidate targets").hasArgs().create(JobDescription.CANDIDATEHOSTS));
        opt.addOption(o("name of a queue to place the job into").hasArg().create(JobDescription.QUEUE));
        opt.addOption(o("set of endpoints describing where to report").hasArgs().create(JobDescription.JOBCONTACT));

        // returns
        return opt;
    }
    private static OptionBuilder o(String description) {
        return OptionBuilder.withDescription(description);
    }

    private static JobDescription createJobDescription(Properties prop) throws Exception {
        JobDescription desc = JobFactory.createJobDescription();
        setOptional(desc, prop, JOBNAME);
        setRequired(desc, prop, JobDescription.EXECUTABLE);
        setOptMulti(desc, prop, JobDescription.ARGUMENTS);
        setOptional(desc, prop, JobDescription.SPMDVARIATION);
        setOptional(desc, prop, JobDescription.TOTALCPUCOUNT);
        setOptional(desc, prop, JobDescription.NUMBEROFPROCESSES);
        setOptional(desc, prop, JobDescription.PROCESSESPERHOST);
        setOptional(desc, prop, JobDescription.THREADSPERPROCESS);
        setOptMulti(desc, prop, JobDescription.ENVIRONMENT);
        setOptional(desc, prop, JobDescription.WORKINGDIRECTORY);
        setOptional(desc, prop, JobDescription.INTERACTIVE);
        setOptional(desc, prop, JobDescription.INPUT);
        setOptional(desc, prop, JobDescription.OUTPUT);
        setOptional(desc, prop, JobDescription.ERROR);
        setOptMulti(desc, prop, JobDescription.FILETRANSFER);
        setOptional(desc, prop, JobDescription.CLEANUP);
        setOptional(desc, prop, JobDescription.JOBSTARTTIME);
        setOptional(desc, prop, JobDescription.TOTALCPUTIME);
        setOptional(desc, prop, JobDescription.TOTALPHYSICALMEMORY);
        setOptional(desc, prop, JobDescription.CPUARCHITECTURE);
        setOptional(desc, prop, JobDescription.OPERATINGSYSTEMTYPE);
        setOptMulti(desc, prop, JobDescription.CANDIDATEHOSTS);
        setOptional(desc, prop, JobDescription.QUEUE);
        setOptMulti(desc, prop, JobDescription.JOBCONTACT);
        return desc;
    }
    private static void setRequired(JobDescription desc, Properties prop, String name) throws Exception {
        String value = prop.getProperty(name);
        if (value != null) {
            desc.setAttribute(name, value);
        } else {
            throw new BadParameterException("Missing required attribute: "+name);
        }
    }
    private static void setOptional(JobDescription desc, Properties prop, String name) throws Exception {
        String value = prop.getProperty(name);
        if (value != null) {
            desc.setAttribute(name, value);
        }
    }
    private static void setOptMulti(JobDescription desc, Properties prop, String name) throws Exception {
        String values = prop.getProperty(name);
        if (values != null) {
            desc.setVectorAttribute(name, values.split(" "));
        }
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        for (int len; (len=in.read(buffer))>0; ) {
            out.write(buffer, 0, len);
        }
    }
}
