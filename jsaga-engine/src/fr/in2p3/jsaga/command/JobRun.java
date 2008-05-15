package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.URL;
import org.ogf.saga.job.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.State;

import java.io.*;

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
    private static final String OPT_BATCH = "b", LONGOPT_BATCH = "batch";
    // attribute names missing in interface JobDescription
    private static final String JOBNAME = "JobName";

    protected JobRun() {
        super("jsaga-job-run", null, null, new GnuParser());
    }

    public static void main(String[] args) throws Exception {
        JobRun command = new JobRun();
        CommandLine line = command.parse(args);

        System.setProperty("saga.factory", "fr.in2p3.jsaga.impl.SagaFactoryImpl");
        if (line.hasOption(OPT_HELP))
        {
            command.printHelpAndExit(null);
        }
        else
        {
            // get arguments
            URL serviceURL = URLFactory.create(line.getOptionValue(OPT_RESOURCE));
            JobDescription desc = createJobDescription(line);
            if (!line.hasOption(OPT_BATCH)) {
                desc.setAttribute(JobDescription.INTERACTIVE, "true");
            }

            // submit
            Session session = SessionFactory.createSession(true);
            JobService service = JobFactory.createJobService(session, serviceURL);
            Job job = service.createJob(desc);
            job.run();

            if (line.hasOption(OPT_BATCH)) {
                String jobId = job.getAttribute(Job.JOBID);
                System.out.println(jobId);
            } else {
                // wait
                job.waitFor();

                // display final state
                State state = job.getState();
                if (State.DONE.compareTo(state) == 0) {
                    copyStream(job.getStdout(), System.out);
                } else {
                    System.err.println("Job did not complete successfully, final state is: "+state);
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
        opt.addOption(OptionBuilder.withDescription("exit immediatly after having submitted the job, " +
                "and print the job ID on the standard output.")
                .withLongOpt(LONGOPT_BATCH)
                .create(OPT_BATCH));

        // job description
        opt.addOption(o("job name to be attached to the job submission").hasArg().create(JOBNAME));
        opt.addOption(o("command to execute").isRequired(true).hasArg().create(JobDescription.EXECUTABLE));
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

    private static JobDescription createJobDescription(CommandLine line) throws Exception {
        JobDescription desc = JobFactory.createJobDescription();
        setOptional(desc, line, JOBNAME);
        setRequired(desc, line, JobDescription.EXECUTABLE);
        setOptMulti(desc, line, JobDescription.ARGUMENTS);
        setOptional(desc, line, JobDescription.SPMDVARIATION);
        setOptional(desc, line, JobDescription.TOTALCPUCOUNT);
        setOptional(desc, line, JobDescription.NUMBEROFPROCESSES);
        setOptional(desc, line, JobDescription.PROCESSESPERHOST);
        setOptional(desc, line, JobDescription.THREADSPERPROCESS);
        setOptMulti(desc, line, JobDescription.ENVIRONMENT);
        setOptional(desc, line, JobDescription.WORKINGDIRECTORY);
        setOptNoArg(desc, line, JobDescription.INTERACTIVE);
        setOptional(desc, line, JobDescription.INPUT);
        setOptional(desc, line, JobDescription.OUTPUT);
        setOptional(desc, line, JobDescription.ERROR);
        setOptMulti(desc, line, JobDescription.FILETRANSFER);
        setOptional(desc, line, JobDescription.CLEANUP);
        setOptional(desc, line, JobDescription.JOBSTARTTIME);
        setOptional(desc, line, JobDescription.TOTALCPUTIME);
        setOptional(desc, line, JobDescription.TOTALPHYSICALMEMORY);
        setOptional(desc, line, JobDescription.CPUARCHITECTURE);
        setOptional(desc, line, JobDescription.OPERATINGSYSTEMTYPE);
        setOptMulti(desc, line, JobDescription.CANDIDATEHOSTS);
        setOptional(desc, line, JobDescription.QUEUE);
        setOptMulti(desc, line, JobDescription.JOBCONTACT);
        return desc;
    }
    private static void setRequired(JobDescription desc, CommandLine line, String name) throws Exception {
        desc.setAttribute(name, line.getOptionValue(name));
    }
    private static void setOptNoArg(JobDescription desc, CommandLine line, String name) throws Exception {
        desc.setAttribute(name, Boolean.toString(line.hasOption(name)));
    }
    private static void setOptional(JobDescription desc, CommandLine line, String name) throws Exception {
        if (line.hasOption(name)) {
            desc.setAttribute(name, line.getOptionValue(name));
        }
    }
    private static void setOptMulti(JobDescription desc, CommandLine line, String name) throws Exception {
        if (line.hasOption(name)) {
            desc.setVectorAttribute(name, line.getOptionValues(name));
        }
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        for (int len; (len=in.read(buffer))>0; ) {
            out.write(buffer, 0, len);
        }
    }
}
