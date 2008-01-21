package org.ogf.saga.job;

import org.ogf.saga.SagaObject;
import org.ogf.saga.attributes.Attributes;

/**
 * Contents of a job description is defined by its attributes.
 * Should we have separate methods for each of the attributes???
 */
public interface JobDescription extends SagaObject, Attributes {

    // Required attributes:
    
    /** Attribute name, command to execute. */
    public static final String EXECUTABLE = "Executable";

    // Optional attributes:

    /** Attribute name, positional parameters for the command. */
    public static final String ARGUMENTS = "Arguments";

    /** Attribute name, SPMD job type and startup mechanism. */
    public static final String SPMDVARIATION = "SPMDVariation";

    /** Attribute name, total number of cpus requested for this job. */
    public static final String TOTALCPUCOUNT = "TotalCPUCount";

    /** Attribute name, total number of processes to be started. */
    public static final String NUMBEROFPROCESSES = "NumberOfProcesses";

    /** Attribute name, total number of processes to be started per host. */
    public static final String PROCESSESPERHOST = "ProcessesPerHost";

    /** Attribute name, number of threads to start per process. */
    public static final String THREADSPERPROCESS = "ThreadsPerProcess";

    /** Attribute name, set of environment variables for the job. */
    public static final String ENVIRONMENT = "Environment";

    /** Attribute name, working directory for the job. */
    public static final String WORKINGDIRECTORY = "WorkingDirectory";

    /** Attribute name, run the job in interactive mode. */
    public static final String INTERACTIVE = "Interactive";

    /** Attribute name, pathname of the standard input file. */
    public static final String INPUT = "Input";

    /** Attribute name, pathname of the standard output file. */
    public static final String OUTPUT = "Output";

    /** Attribute name, pathname of the standard error file. */
    public static final String ERROR = "Error";

    /** Attribute name, a list of file transfer directives. */
    public static final String FILETRANSFER = "FileTransfer";

    /**
     * Attribute name, defines whether output files get removed after the
     * job finishes.
     */
    public static final String CLEANUP = "Cleanup";

    /** Attribute name, time at which the job should be scheduled. */
    public static final String JOBSTARTTIME = "JobStartTime";

    /**
     * Attribute name, estimate of total number of CPU seconds the job will
     * require.
     */
    public static final String TOTALCPUTIME = "TotalCPUTime";

    /** Attribute name, estimate of the amount of memory the job requires. */
    public static final String TOTALPHYSICALMEMORY = "TotalPhysicalMemory";

    /** Attribute name, compatible processor for job submission. */
    public static final String CPUARCHITECTURE = "CPUArchitecture";

    /** Attribute name, compatible operating system for job submission. */
    public static final String OPERATINGSYSTEMTYPE = "OperatingSystemType";

    /**
     * Attribute name, list of host names which are to be considered by the
     * resource manager as candidate targets.
     */
    public static final String CANDIDATEHOSTS = "CandidateHosts";

    /** Attribute name, name of queue to place the job into. */
    public static final String QUEUE = "Queue";

    /**
     * Attribute name, set of end points where to report job state transitions.
     */
    public static final String JOBCONTACT = "JobContact";
}
