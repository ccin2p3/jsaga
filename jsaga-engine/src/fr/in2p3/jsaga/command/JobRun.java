package fr.in2p3.jsaga.command;

import org.apache.commons.cli.*;
import org.ogf.saga.URL;
import org.ogf.saga.job.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.State;

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
 * -r local://localhost -CandidateHost localhost -Executable job.sh -FileTransfer input>input,output<<output
 */
public class JobRun extends AbstractCommand {
    private static final String OPT_HELP = "h", LONGOPT_HELP = "help";
    // job service
    private static final String OPT_JOB_SERVICE = "s", LONGOPT_JOB_SERVICE = "service";
    // job description
    private static final String EXECUTABLE = "Executable";
    private static final String ARGUMENTS = "Arguments";
    private static final String ENVIRONMENT = "Environment";
    private static final String WORKING_DIRECTORY = "WorkingDirectory";
    private static final String INTERACTIVE = "Interactive";
    private static final String INPUT = "Input";
    private static final String OUTPUT = "Output";
    private static final String ERROR = "Error";
    private static final String JOB_CONTACT = "JobContact";
    private static final String JOB_NAME = "JobName";
    private static final String FILE_TRANSFER = "FileTransfer";
    private static final String CLEANUP = "Cleanup";
    private static final String JOB_START_TIME = "JobStartTime";
    private static final String DEADLINE = "Deadline";
    private static final String CPU_ARCHITECTURE = "CPUArchitecture";
    private static final String OPERATING_SYSTEM_TYPE = "OperatingSystemType";
    private static final String CANDIDATE_HOSTS = "CandidateHosts";
    private static final String QUEUE = "Queue";
    private static final String NUMBER_OF_PROCESSES = "NumberOfProcesses";
    private static final String PROCESSES_PER_HOST = "ProcessesPerHost";
    private static final String THREADS_PER_PROCESS = "ThreadsPerProcess";
    private static final String SPMD_VARIATION = "SPMDVariation";

    protected JobRun() {
        super("jsaga-job-run", null, null);
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
            URL serviceURL = new URL(line.getOptionValue(OPT_JOB_SERVICE));
            JobDescription desc = createJobDescription(line);

            // submit
            Session session = SessionFactory.createSession(true);
            JobService service = JobFactory.createJobService(session, serviceURL);
            Job job = service.createJob(desc);
            job.run();

            // wait
            State previousState = State.NEW;
            boolean isFinished = false;
            while (!isFinished) {
                State currentState = job.getState();
                if (currentState.compareTo(previousState) != 0) {
                    if (State.RUNNING.compareTo(currentState) == 0) {
                        System.out.println("Job is running.");
                    } else if (State.SUSPENDED.compareTo(currentState) == 0) {
                        System.out.println("Job is suspended.");
                    } else if (State.DONE.compareTo(currentState) == 0) {
                        System.out.println("Job completed successfully.");
                        isFinished = true;
                    } else if (State.CANCELED.compareTo(currentState) == 0) {
                        System.out.println("Job canceled.");
                        isFinished = true;
                    } else if (State.FAILED.compareTo(currentState) == 0) {
                        System.out.println("Job failed with "+job.getAttribute("ExitCode"));
                        isFinished = true;
                    } else {
                        throw new Exception("Unexpected state: "+ currentState);
                    }
                }
                Thread.currentThread().sleep(100);
                previousState = currentState;
            }
        }
    }

    protected Options createOptions() {
        Options opt = new Options();

        // command
        opt.addOption(OptionBuilder.withDescription("Display this help and exit")
                .withLongOpt(LONGOPT_HELP)
                .create(OPT_HELP));

        // job service
        opt.addOption(OptionBuilder.withDescription("the URL of the job service")
                .isRequired(true)
                .hasArg()
                .withArgName("URL")
                .withLongOpt(LONGOPT_JOB_SERVICE)
                .create(OPT_JOB_SERVICE));

        // job description
        opt.addOption(OptionBuilder.withDescription("command to execute.").isRequired(true).hasArg().create(EXECUTABLE));
        opt.addOption(OptionBuilder.withDescription("positional parameters for the command.").hasArgs().create(ARGUMENTS));
        opt.addOption(OptionBuilder.withDescription("set of environment variables for the job").hasArgs().withValueSeparator().create(ENVIRONMENT));
        opt.addOption(OptionBuilder.withDescription("working directory for the job").hasArg().create(WORKING_DIRECTORY));
        opt.addOption(OptionBuilder.withDescription("run the job in interactive mode").create(INTERACTIVE));
        opt.addOption(OptionBuilder.withDescription("pathname of the standard input file").hasArg().create(INPUT));
        opt.addOption(OptionBuilder.withDescription("pathname of the standard output file").hasArg().create(OUTPUT));
        opt.addOption(OptionBuilder.withDescription("pathname of the standard error file").hasArg().create(ERROR));
        opt.addOption(OptionBuilder.withDescription("set of endpoints describing where to report").hasArgs().create(JOB_CONTACT));
        opt.addOption(OptionBuilder.withDescription("job name to be attached to the job submission").hasArg().create(JOB_NAME));
        opt.addOption(OptionBuilder.withDescription("a list of file transfer directives").hasArgs().create(FILE_TRANSFER));
        opt.addOption(OptionBuilder.withDescription("defines if output files get removed after the job finishes").hasArg().create(CLEANUP));
        opt.addOption(OptionBuilder.withDescription("time at which a job should be scheduled").hasArg().create(JOB_START_TIME));
        opt.addOption(OptionBuilder.withDescription("hard deadline after which the resource manager should cancel the job").hasArg().create(DEADLINE));
        opt.addOption(OptionBuilder.withDescription("compatible processor for job submission").hasArgs().create(CPU_ARCHITECTURE));
        opt.addOption(OptionBuilder.withDescription("compatible operating system for job submission").hasArgs().create(OPERATING_SYSTEM_TYPE));
        opt.addOption(OptionBuilder.withDescription("list of host names which are to be considered by the resource manager as candidate targets").hasArgs().create(CANDIDATE_HOSTS));
        opt.addOption(OptionBuilder.withDescription("name of a queue to place the job into").hasArg().create(QUEUE));
        opt.addOption(OptionBuilder.withDescription("number of process instances to start").hasArg().create(NUMBER_OF_PROCESSES));
        opt.addOption(OptionBuilder.withDescription("number of processes to start per host").hasArg().create(PROCESSES_PER_HOST));
        opt.addOption(OptionBuilder.withDescription("expected number of threads per process").hasArg().create(THREADS_PER_PROCESS));
        opt.addOption(OptionBuilder.withDescription("SPMD job type and startup mechanism").hasArg().create(SPMD_VARIATION));

        // returns
        return opt;
    }

    private static JobDescription createJobDescription(CommandLine line) throws Exception {
        JobDescription desc = JobFactory.createJobDescription();
        setRequired(desc, line, EXECUTABLE);
        setOptMulti(desc, line, ARGUMENTS);
        setOptMulti(desc, line, ENVIRONMENT);
        setOptional(desc, line, WORKING_DIRECTORY);
        setReqNoArg(desc, line, INTERACTIVE);
        setOptional(desc, line, INPUT);
        setOptional(desc, line, OUTPUT);
        setOptional(desc, line, ERROR);
        setOptMulti(desc, line, JOB_CONTACT);
        setOptional(desc, line, JOB_NAME);
        setOptMulti(desc, line, FILE_TRANSFER);
        setOptional(desc, line, CLEANUP);
        setOptional(desc, line, JOB_START_TIME);
        setOptional(desc, line, DEADLINE);
        setOptMulti(desc, line, CPU_ARCHITECTURE);
        setOptMulti(desc, line, OPERATING_SYSTEM_TYPE);
        setOptMulti(desc, line, CANDIDATE_HOSTS);
        setOptional(desc, line, QUEUE);
        setOptional(desc, line, NUMBER_OF_PROCESSES);
        setOptional(desc, line, PROCESSES_PER_HOST);
        setOptional(desc, line, THREADS_PER_PROCESS);
        setOptional(desc, line, SPMD_VARIATION);
        return desc;
    }
    private static void setRequired(JobDescription desc, CommandLine line, String name) throws Exception {
        desc.setAttribute(name, line.getOptionValue(name));
    }
    private static void setReqNoArg(JobDescription desc, CommandLine line, String name) throws Exception {
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
}
