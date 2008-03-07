/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package org.globus.exec.client;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.addressing.EndpointReferenceType;

import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.oasis.wsrf.faults.BaseFaultType;
import org.w3c.dom.Element;

import org.globus.util.I18n;

import org.globus.exec.generated.StateEnumeration;
import org.globus.exec.utils.FaultUtils;
import org.globus.exec.utils.ManagedJobFactoryConstants;
import org.globus.exec.utils.Resources;
import org.globus.exec.utils.client.ManagedJobFactoryClientHelper;
import org.globus.exec.utils.rsl.RSLHelper;
import org.globus.wsrf.client.ServiceURL;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.globus.wsrf.impl.security.authorization.HostAuthorization;
import org.globus.wsrf.impl.security.authorization.IdentityAuthorization;
import org.globus.wsrf.impl.security.authorization.SelfAuthorization;
import org.globus.wsrf.utils.XmlUtils;

/**
 * <p>
 * This command-line tool requests and submits jobs to a GT4 GRAM server.
 * <p>
 * <b>Job Service Destruction</b>
 * <p>
 * Execution errors and user interrupt events are handled by automatically
 * destroying the requested job service(s), unless the -batch option
 * is on the command-line. The -batch option prevents the tool from
 * listening to job state changes and from waiting for the job to finish.
 * If -batch is selected, the command will return as soon as the remote
 * job has been submitted.
 * <p>
 * The behavior of the tool with respect to job service destruction will vary
 * in response to several kinds of events:
 * <ul>
 * <li> The command exits normally after the job(s) finish(es), and destroys
 *      the job service(s) it requested. In batch mode, the requested job
 *      is never destroyed.
 *
 * <li> The command is terminated in response to a user interrupt, such as
 *      typing <em>Ctrl</em> + <em>C</em>, or a system-wide event,
 *      such as user logoff or system shutdown. If the -no-interrupt option
 *      is on the command-line and the command-line has been successfully parsed
 *      when the interrupt occurs, the tool does not destroy any job service(s)
 *      it requested. Otherwise the tool destroys the requested job service(s).
 *
 * <li> In case of any error of execution, the command will exit and
 *      destroy the job(s) it successfully requested.
 * </ul>
 * <p>
 * <p>
 * If the virtual machine aborts, that is, stops running without shutting down
 * cleanly, for instance because it received a SIGKILL signal on Unix, then no
 * guarantee can be made about whether or not the job service(s) will be
 * destroyed.
 * <p>
 * Note: the shutdown behavior explained above cannot be guaranteed if the
 * JVM option -Xrs is entered.
 * The recommended way to disable service destruction is to specify
 * the -batch option on the command-line.
 *
 * Use -help for more help and a list of available options.
 */
public class GlobusRun implements GramJobListener {

    private static final long STATE_CHANGE_BASE_TIMEOUT_MILLIS = 60000;

    private static final String GLOBUS_VERSION = "GT 3.9.4";

    private static Log logger = LogFactory.getLog(GlobusRun.class.getName());

    /**
     * Job submission member variables.
     */

    private GramJob job;

    private boolean jobCompleted = false; //completed if Done or Failed

    private boolean batch;

    private boolean limitedDelegation = true;

    private boolean delegationEnabled = true;

    private boolean quiet = false; //by default, print all messages

    /**
     * Constants for termination time options.
     */
    private static final String DURATION_FORMAT = "HH:mm";
    private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm";

    /**
     * Bit masks for command-line options.
     */
    private static final int GLOBUSRUN_ARG_QUIET             = 2;
    private static final int GLOBUSRUN_ARG_DRYRUN            = 4;
    private static final int GLOBUSRUN_ARG_PARSE_ONLY        = 8;
    private static final int GLOBUSRUN_ARG_AUTHENTICATE_ONLY = 16;
    private static final int GLOBUSRUN_ARG_BATCH             = 512;
    private static final int GLOBUSRUN_ARG_FULL_DELEGATION   = 8192;
    private static final int GLOBUSRUN_ARG_LIST              = 16384;

    private String submissionID = null;

    private static final String usageDesc =
        "ARGUMENTS: \n" +
        "         [options] [<factory>] <job description>\n" +
        "         -p -file <job description file name>\n" +
        "         (-state | -release | -kill) <job handle>\n" +
        "         -help | -usage | -version\n" +
        "\n" +
        "with\n" +
        "     <job description> = -file <file name> | <command line>\n" +
        "     <factory>         = -factory <contact> [-type <type>]\n" +
        "     <contact>         = [<protocol>://]<host>[:[port]][/<service>]\n"+
        "     [options]         = [-q] [-n]\n" +
        "                         [-b] [-duration] [-terminate-at]\n" +
        "                         [-auth <auth>] [-xmlsec <sec>] [-personal]\n"+
        "                         [-submission-id <ID>]" +
        "\n" +
        "\n";

    private static final String descriptionDesc =
        "DESCRIPTION:\n" +
        "This command is used to submit jobs to globus resources. The job\n" +
        "startup is done by submitting a client-side provided job\n" +
        "description to the GRAM services.\n" +
        "In addition to starting jobs, it is possible to query the state of\n" +
        "a previously started job and parse a job description file without\n" +
        "making any submission.\n" +
        "The existence of a valid proxy is required for essentially all\n" +
        "supported operations but job description file parsing (option -p).\n" +
        "\n";

    private static final String optionsDesc =
        "OPTIONS:\n" +
        "\n"+
        "Help:\n" +
        " -help                 display help.\n" +
        " -usage                display usage.\n" +
        " -v, -version          display version.\n" +
        "\n" +
        "Job Factory Contact:\n" +
        " -factory <contact>    specify the URL of the Job Factory Service\n" +
        "                       to contact when submitting or listing jobs.\n" +
        "                       A factory contact string can be specified in\n"+
        "                       the following ways:\n" +
        "                       host\n" +
        "                       host:\n" +
        "                       host:port\n" +
        "                       host:port/service\n" +
        "                       host/service\n" +
        "                       host:/service\n" +
        "                       It is also possible to specify the protocol\n" +
        "                       by prepending   protocol://  to each of the\n" +
        "                       previous possibilities, bringing the total\n" +
        "                       number of supported syntaxes to 12.\n" +
        "                       For those factory contacts which omit the\n"+
        "                       protocol, port or service field, default\n" +
        "                       values are used, as summarized in the\n" +
        "                       following table:\n" +
        "                       URL part | default value\n"+
        "                       port     | " +
                                Integer.toString(ServiceURL.getDefaultPort()) + "\n" +
        "                       protocol | " +
                                ServiceURL.getDefaultProtocol() + "\n" +
        "                       service  | " +
        ManagedJobFactoryConstants.DEFAULT_SERVICE_URL.getFullServicePath()
        + "\n\n" +
        "                       Omitting altogether the -factory option is\n" +
        "                       equivalent to specifying the local host as\n" +
        "                       the contact string (with the implied default\n"+
        "                       protocol, port and service).\n" +
        " -type <factory type>  the type of factory resource to use. This is\n"+
        "                       the name of the local resource manager.\n" +
        "                       The default is " +
        ManagedJobFactoryConstants.DEFAULT_FACTORY_TYPE + ".\n" +
        "Job Specification:\n" +
        " <command line>        create a simple job description that only\n" +
        "                       consists of a command line of the form:\n" +
        "                           'executable (argument)*'\n" +
        "                       Quotes must be used if there is one or more\n" +
        "                       arguments.\n" +
        " -file <RSL filename>  read RSL from the local file <RSL filename>.\n"+
        "                       The RSL must be a single job request.\n" +
        " -p                    only parse the RSL, and then print either a\n" +
        "                       success message or a parser failure. No job\n" +
        "                       will be submitted to any factory service.\n" +
        "                       The RSL must be a single job request. \n" +
        "\n" +
        "Batch Operations:\n" +
        " -b, -batch            do not wait for started job to complete (and\n"+
        "                       do not destroy started job service on exit).\n"+
        "                       The handle of the job service will be\n" +
        "                       printed on the standard output.\n" +
        "                       incompatible with multi-request jobs.\n" +
        "                       Implies -quiet.\n" +
        " -l, -list             NOT FUNCTIONAL YET (see below)\n" +
        " -state <handle>       printout the state of the specified job.\n" +
        "                       For a list of valid states, see the GRAM\n" +
        "                       documentation; the current valid states are\n" +
        "                       Pending, Active, Done, Suspended, and Failed."+
        "\n"+
        " -r, -release <handle> release the specified job from hold.\n" +
        "\n" +
        " -k, -kill <handle>    kill the specified job.\n" +
        "\n" +
        "                       Note: The <handle> argument is printed out\n"+
        "                       when executing in batch mode or when using\n" +
        "                       the -list option.\n" +
        "\n" +

        "Job Resource Lifetime:\n" +
        " -duration <duration>  specify duration of job resource. The job\n"+
        "                       resource will destroy itself automatically\n" +
        "                       after the specified duration starting from\n" +
        "                       service creation.\n" +
        "                       Format: " + DURATION_FORMAT + "\n" +
        "                       Default: " + GramJob.DEFAULT_DURATION_HOURS +
                                                               " hours.\n" +
        "                       Incompatible with -date-time.\n"+
        "                       Useful with -batch.\n" +
        " -terminate-at <date>  specify termination date/time of job.\n" +
        "                       Same as -duration but with an absolute\n"+
        "                       date/time value.\n" +
        "                       Format: " + DATE_FORMAT + "\n" +
        "                       Default: see -duration.\n" +
        "                       The date expression may need to be quoted,\n" +
        "                       as in:     -terminate-at '08/15/2005 11:30'\n" +
        "                       Incompatible with -duration.\n"+
        "                       Useful with -batch.\n" +
        "\n" +

        "Security:\n" +
        " -auth <auth>          set authorization type. <auth> can be:\n" +
        "                           'host' for host authorization (default),\n" +
        "                           'self' for self authorization\n"+
        "                           <id> for identity authorization.\n" +
        " -xmlsec <sec>         set message protection level. <sec> can be:\n"+
        "                           'sig' for XML Signature (default),\n" +
        "                           'enc' for XML Encryption.\n" +
        " -personal             shortcut for -auth self.\n" +
        " -proxy <proxy file>   use <proxy file> instead of the default\n" +
        "                       proxy credential file.\n"+
        " -deleg <deleg>        set delegation type. <deleg> can be:\n" +
        "                           'full' for full delegation,\n" +
        "                           'limited' for limited delegation " +
                                                                "(default),\n" +
        "                           or 'none' for no delegation " +
        "\n" +

        "Miscellaneous:\n" +
        " -q, -quiet            set quiet mode on (do not print diagnostic\n" +
        "                       messages when job state changes, in\n" +
        "                       non-batch mode). Useful when job output is\n" +
        "                       redirected to the local process and parsed.\n" +
        " -n, -no-interrupt     disable interrupt handling. By default,\n" +
        "                       interrupt signals (typically generated by\n" +
        "                       Ctrl + C) cause the program to terminate the\n"+
        "                       currently submitted job. This flag disables\n" +
        "                       that behavior.\n" +
        " -timeout <integer>    set timeout for HTTP socket, in milliseconds.\n"
        +
        "                       Applies to job submission only.\n" +
        "                       Default is " + GramJob.DEFAULT_TIMEOUT + ".\n" +
        " -submission-id <ID>   set the submission ID of a previous job\n" +
        "                       submission for which no server response was\n" +
        "                       received.\n"+
        "                       The ID can be used after an attempted job\n" +
        "                       submission in order to recover the handle to\n" +
        "                       the job.\n"+
        "\n" +
        "GT2 globusrun options not functional (yet):\n" +
        " -l, -list             NOT IMPLEMENTED ON SERVER SIDE YET.\n" +
        "                       list previously started and not destroyed\n" +
        "                       job services for this user. The output of\n" +
        "                       this command consists of the handles and RSL\n"+
        "                       of the submitted jobs.\n" +
        "                       Requires the -factory <URL> argument.\n" +
        " -dryrun               NOT IMPLEMENTED ON SERVER SIDE YET.\n" +
        "                       augment the RSL in order to mark this job as\n"+
        "                       a dry run, if the RSL does not already say\n" +
        "                       so. This causes the job manager to stop\n" +
        "                       short of starting the job, but still detect\n" +
        "                       other RSL errors (such as bad directory,\n" +
        "                       bad executable, etc). An error message will\n" +
        "                       be displayed if the dry run fails.\n" +
        "                       Otherwise, a message will be displayed\n" +
        "                       indicating that the dryrun was successful.\n" +
        " -authenticate-only    NOT IMPLEMENTED ON SERVER SIDE YET.\n" +
        "";

    private static final String ERROR_MESSAGE_PREFIX = "Error: ";
    private static final String JOB_FAILED = "Job failed: ";
    private static final String JOB_STATE_PREFIX = "Job State: ";

   /**
    * Application error state.
    */
    private boolean noInterruptHandling = false;
    private boolean isInterrupted = true;
    private boolean normalApplicationEnd = false;

    private String proxyPath = null;


    /**
     * Must be used instead of <code>System.exit</code>. Marks the application
     * as <bold>not</bold> being interrupted.  In this way it is possible to
     * determine when the application exits normally or because of an error
     * as opposed to being interrupted for instance with Ctrl+C.
     *
     * @param exitCode the exit code of the application. If <=0 then
     *        the application will be marked as ending normally (i.e. no error).
     */
    private void exit(int exitCode) {
        //if this method is not called, we have an interrupt
        //(or an exception not handled)
        //so by default, isInterrupted is true and normalApplicationEnd is false
        if (exitCode <= 0) {
            normalApplicationEnd = true; //to detect if error/exception or not
        }
        isInterrupted = false; //message to shutdown hook
        System.exit(exitCode);
    }

    public static void main(String args[]) {
        new GlobusRun(args);
    }

    public GlobusRun(String args[]) {
        processArguments(args);
    }

    private void processArguments(String args[]) {

        String simpleJobCommandLine = null;
        File rslFile = null;
        String contactString = null;
        EndpointReferenceType factoryEndpoint = null;
        String factoryType = ManagedJobFactoryConstants.DEFAULT_FACTORY_TYPE;
        Integer xmlSecurity = Constants.SIGNATURE;
        Authorization authorization = null;
        int options = 0;
        boolean commandLineError = false;
        Date serviceDuration = null;
        Date serviceTerminationDate = null;
        int timeout = GramJob.DEFAULT_TIMEOUT;

        if (args.length == 0) {
            commandLineError = true;
        }
        else {

            /**
             * Todo use option package from Globus to cleanup code/design
             */
            for (int i = 0; i < args.length && !commandLineError; i++) {

                if (args[i].startsWith("-")) {

                    if (args[i].equalsIgnoreCase("-help")) {
                        System.out.println();
                        System.out.println(usageDesc);
                        System.out.println(descriptionDesc);
                        System.out.println(optionsDesc);
                        exit(0);
                    }
                    else if (args[i].equalsIgnoreCase("-usage")) {
                        System.out.println(usageDesc);
                        exit(0);
                    }
                    else if (args[i].equals("-v") ||
                             args[i].equalsIgnoreCase("-version")) {
                        System.out.println(GLOBUS_VERSION);
                        exit(0);
                    }
                    else if (args[i].equals("-n") ||
                             args[i].equalsIgnoreCase("-no-interrupt")) {
                        noInterruptHandling = true;
                    }
                    else if (args[i].equals("-p") ||
                             args[i].equalsIgnoreCase("-parse")) {
                        // parse only
                        options |= GLOBUSRUN_ARG_PARSE_ONLY;

                    }
                    else if (args[i].equalsIgnoreCase("-auth")) {
                        if (i + 1 >= args.length) {
                            printError("-auth requires argument");
                            commandLineError = true;
                        }
                        i++;
                        if (args[i].equalsIgnoreCase("host")) {
                            authorization = HostAuthorization.getInstance();
                        }
                        else if (args[i].equalsIgnoreCase("self")) {
                            authorization = SelfAuthorization.getInstance();
                        }
                        else {
                            authorization = new IdentityAuthorization(args[i]);
                        }
                    }
                    else if (args[i].equalsIgnoreCase("-xmlsec")) {
                        if (i + 1 >= args.length) {
                            printError("-xmlsec requires argument");
                            commandLineError = true;
                        }
                        i++;
                        if (args[i].equalsIgnoreCase("sig")) {
                            xmlSecurity = Constants.SIGNATURE;
                        }
                        else if (args[i].equalsIgnoreCase("enc")) {
                            xmlSecurity = Constants.ENCRYPTION;
                        }
                        else {
                            printError("unsupported -xmlsec argument: " +
                                       args[i]);
                            commandLineError = true;
                        }
                    }
                    else if (args[i].equalsIgnoreCase("-personal")) {
                        authorization = SelfAuthorization.getInstance();
                    }
                    else if (args[i].equals("-l") ||
                             args[i].equalsIgnoreCase("-list")) {
                        options |= GLOBUSRUN_ARG_LIST;

                    }
                    else if (args[i].equalsIgnoreCase("-state")) {

                        // job state
                        ++i;
                        if (i == args.length) {
                            commandLineError = true;
                            printError("-state requires a job handle");
                        }
                        else {
                            state(args[i]);
                        }
                    }
                    else if (args[i].equals("-r") ||
                             args[i].equalsIgnoreCase("-release")) {

                        // release job
                        ++i;
                        if (i == args.length) {
                            commandLineError = true;
                            printError("-release requires a job handle");
                        }
                        else {
                            release(args[i]);
                        }
                    }
                    else if (args[i].equals("-k") ||
                             args[i].equalsIgnoreCase("-kill")) {

                        // kill job
                        ++i;
                        if (i == args.length) {
                            commandLineError = true;
                            printError("-kill requires a job handle");
                        }
                        else {
                            kill(args[i]);
                        }
                    }
                    else if (args[i].equals("-b") ||
                             args[i].equalsIgnoreCase("-batch")) {

                        // batch job
                        options |= GLOBUSRUN_ARG_BATCH;
                        //no need to explicitly set QUIET since
                        //will not wait for state notifications anyway.

                    }
                    else if (args[i].equalsIgnoreCase("-duration")) {

                        // set termination time of job
                        ++i;
                        if (i == args.length) {
                            commandLineError = true;
                            printError("-duration requires a duration");
                        }
                        else {
                            DateFormat dateFormat =
                                    new SimpleDateFormat(DURATION_FORMAT);

                            try {
                                serviceDuration = dateFormat.parse(args[i]);
                            }
                            catch (Exception e) {
                                printError("service duration '" +
                                           args[i] + "' is malformed.\n" +
                                           "Format is " + DURATION_FORMAT);
                                commandLineError = true;
                            }
                        }
                    }
                    else if (args[i].equalsIgnoreCase("-terminate-at")) {

                        // set termination time of job
                        ++i;
                        if (i == args.length) {
                            commandLineError = true;
                            printError("-terminate-at requires a date");
                        }
                        else {

                            DateFormat dateFormat =
                                    new SimpleDateFormat(DATE_FORMAT);

                            try {
                                serviceTerminationDate = dateFormat.parse(
                                        args[i]);
                            }
                            catch (Exception e) {
                                printError("termination date/time '" +
                                           args[i] + "' is malformed.\n" +
                                           "Format is " + DATE_FORMAT);
                                commandLineError = true;
                            }
                        }
                    }
                    else if (args[i].equals("-d") ||
                             args[i].equalsIgnoreCase("-dryrun")) {

                        // dryrun
                        options |= GLOBUSRUN_ARG_DRYRUN;

                    }
                    //quiet no impl yet
                    else if (args[i].equals("-q") ||
                             args[i].equalsIgnoreCase("-quiet")) {

                        // quiet mode
                        options |= GLOBUSRUN_ARG_QUIET;

                    }
                    else if (args[i].equals("-f") ||
                             args[i].equalsIgnoreCase("-file")) {

                        // read from RSL file
                        i++;
                        if (i == args.length) {
                            commandLineError = true;
                            printError("-file requires a filename");
                        }
                        else {
                            rslFile = new File(args[i]);
                            //will exit if error
                        }

                    }
                    else if (args[i].equals("-factory")) {

                        // MMJFS
                        i++;
                        if (i == args.length) {
                            commandLineError = true;
                            printError("-factory requires a factory " +
                                       "contact string");
                            break;
                        }
                        else {
                            contactString = args[i];
                        }
                    }
                    else if (args[i].equals("-type")) {

                        i++;
                        if (i == args.length) {
                            commandLineError = true;
                            printError("-type requires a factory type. " +
                                       "For example: Fork, PBS, LSF, Condor,\n"+
                                       "Multi (for multijobs).");
                            break;
                        }
                        else {
                            factoryType = args[i];

                            //provide a full delegated proxy for multi-jobs
                            //since it has to re-delegate to single-jobs
                            if (factoryType.equals(
                                ManagedJobFactoryConstants.FACTORY_TYPE.MULTI))
                            {
                                this.limitedDelegation = false;
                            }
                        }
                    }
                    else if (args[i].equalsIgnoreCase("-timeout")) {

                        // set timeout for underlying distributed communication
                        ++i;
                        if (i == args.length) {
                            commandLineError = true;
                            printError("-timeout requires a integer value");
                        }
                        else {

                            try {
                                timeout = Integer.parseInt(args[i]);
                            }
                            catch (NumberFormatException e) {
                                printError("timeout '" +
                                           args[i] +
                                           "' is not an integer value.\n");
                                commandLineError = true;
                            }
                        }
                    }

                    else if (args[i].equals("-proxy")) {

                        // Use instead of default proxy
                        i++;
                        if (i == args.length) {
                            commandLineError = true;
                            printError("-proxy requires a path string");
                            break;
                        }
                        else {
                            this.proxyPath = args[i];
                        }
                    }

                    else if (args[i].equals("-deleg")) {

                        // Set delegation type
                        i++;
                        if (i == args.length) {
                            commandLineError = true;
                            printError(
                                "-deleg requires \"full\" "
                                + ", \"limited\", or \"none\"");
                            break;
                        }
                        else {
                            String delegArg = args[i].toLowerCase();
                            if (delegArg.equals("full")) {
                                this.limitedDelegation = false;
                            } else if (delegArg.equals("limited")) {
                                //default
                            } else if (delegArg.equals("none")) {
                                this.delegationEnabled = false;
                            } else {
                                printError(
                                    "-deleg requires either \"full\" "
                                    + ", \"limited\", or \"none\"");
                            }
                        }
                    }
                    else if (args[i].equals("-submission-id")) {
                        // Set the submission message ID
                        i++;
                        if (i == args.length) {
                            commandLineError = true;
                            printError(
                                "-submission-id requires an ID");
                            break;
                        } else {
                            submissionID = args[i];
                        }
                    }

                    else {
                        printError("unknown argument " + args[i]);
                        commandLineError = true;
                    }

                }
                else { //arg doesn't start with '-'

                    if (i + 1 == args.length) { //last argument: rsl string
                        if (rslFile != null) {
                            commandLineError = true;
                            printError("RSL already specified as a file");
                        }
                        logger.debug("Reading command line to execute: " +
                                     args[i]);
                        simpleJobCommandLine = args[i];
                    }
                    else {
                        printError("unknown argument " + args[i]);
                        commandLineError = true;
                    }
                }
            } //end for

            //at this point state/release/kill have already branched out
            //Remains list (need factory), parse (need RSL),
            //submit job (need both)

            boolean rslParsingOnly = (options & GLOBUSRUN_ARG_PARSE_ONLY) != 0;
            boolean jobListing = (options & GLOBUSRUN_ARG_LIST) != 0;
            boolean jobSubmission = !rslParsingOnly && !jobListing;

            //parameter checking
            if (jobSubmission) {
                if (serviceDuration != null &&
                    serviceTerminationDate != null){
                    printError(
                    "service termination time options are incompatible");
                    commandLineError = true;
                }
            }

            if (rslParsingOnly || jobSubmission) {
                if (simpleJobCommandLine == null && rslFile == null) {
                    printError("No RSL specified");
                    commandLineError = true;
                }
            }

            if (jobListing || jobSubmission) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Factory contact string: " + contactString +
                                 "\n" +
                                 "Factory type: " + factoryType);
                }

                try {
                    URL factoryURL = ManagedJobFactoryClientHelper.
                        getServiceURL(contactString).getURL();
                    factoryEndpoint =
                        ManagedJobFactoryClientHelper.getFactoryEndpoint(
                        factoryURL,
                        factoryType);

                    if (logger.isDebugEnabled()) {
                        Element eprElement = null;
                        try {
                            eprElement = ObjectSerializer.toElement(
                                factoryEndpoint,
                                RSLHelper.FACTORY_ENDPOINT_ATTRIBUTE_QNAME);
                        } catch (Exception e) {
                            logger.debug("ERROR: could not serialize EPR", e);
                        }
                        logger.debug("Factory EPR: "
                                    + XmlUtils.toString(eprElement));
                    }
                }
                catch (Exception e) {
                    commandLineError = true;
                    printError("Could not create factory endpoint reference");
                }
            }

            //actual execution of command
            if (!commandLineError) {

                if (jobListing) {
                    listUserJobs(factoryEndpoint);
                }
                else
                if (rslParsingOnly) {
                   try {
                       if (rslFile != null) {
                           RSLHelper.readRSL(rslFile);
                       }
                   }
                   catch (Exception e)
                   {
                       String errorMessage = "Exception while parsing RSL file";
                       logger.debug(errorMessage, e);
                       printError("Exception while parsing RSL file" +
                                  e.getMessage());
                       exit(1);
                   }
                   printMessage("RSL file successfully parsed.");
                }
                else
                if (jobSubmission) {

                    submitRSL(
                            factoryEndpoint,
                            simpleJobCommandLine,
                            rslFile,
                            authorization,
                            xmlSecurity,
                            (options & GLOBUSRUN_ARG_BATCH) != 0,
                            (options & GLOBUSRUN_ARG_DRYRUN) != 0,
                            (options & GLOBUSRUN_ARG_QUIET) != 0,
                            serviceDuration,
                            serviceTerminationDate,
                            timeout);
                }
            } //end if !commandLineError

        } //end else at least one argument

        if (commandLineError) {
            System.err.println();
            System.err.println(usageDesc);
            exit(1);
        }

    }

    private void listUserJobs(EndpointReferenceType factoryEndpoint)
    {
        List jobHandles = null;
        try {
            jobHandles = GramJob.getJobs(factoryEndpoint);
        }
        catch (Exception e) {
            logger.debug("Exception retrieving list of submitted jobs", e);
            printError("could not obtain list of submitted jobs: "+
                               e.getMessage());
            exit(1);
        }

        if (jobHandles == null || jobHandles.size() == 0) {
            System.out.println("No managed jobs for user.");
            exit(0);
        }

        boolean error = false;
        Iterator it = jobHandles.iterator();
        while (it.hasNext()) {
            String jobHandle = (String)it.next();
            System.out.println();
            System.out.println("job handle:\n");
            System.out.println(jobHandle);

            GramJob job = getExistingJob(jobHandle);
            try {
                System.out.println();
                System.out.println("job RSL:\n" + RSLHelper.convertToString(
                    job.getDescription()));
            }
            catch (Exception e) {
                String errorMessage =
                    i18n.getMessage(Resources.FETCH_JOB_DESCRIPTION_ERROR);
                logger.debug(errorMessage, e);
                printError("could not obtain RSL from Managed Job Service");
                error = true;
            }
        } //end while

        if (error) {
            exit(1);
        }
        exit(0);
    }

    private void release(String jobHandle) {

        GramJob job = getExistingJob(jobHandle);
        try {
            job.release();
        }
        catch (Exception e) {
            logger.debug("Error while releasing job", e);
            printError("could not release job " + jobHandle + ": " +
                       e.getMessage());
            exit(1);
        }
        exit(0);

    }

    private void kill(String jobHandle) {

        GramJob job = getExistingJob(jobHandle);
        try {
            logger.debug("destroyJob() called in kill()");
            destroyJob(job);
        }
        catch (Exception e) {
            logger.debug("Error while destroying job", e);
            printError("could not kill job " + jobHandle + ": " +
                       e.getMessage());
            exit(1);
        }
        exit(0);

    }

    private void state(String jobHandle) {

        GramJob job = getExistingJob(jobHandle);

        refreshJobStatus();
        //print it
        StateEnumeration jobState = job.getState();
        printJobState(jobState, job.isHolding());
        if (jobState.equals(StateEnumeration.Failed)) {
            printJobFault(job);
            //state query feature is not failing, so exit code is still 0.
        }
        exit(0);

    }

    private void refreshJobStatus() {
        try {
            job.refreshStatus();
        }
        catch (Exception e) {
            logger.debug("Exception while refreshing job state", e);
            printError("could not refresh job state: " + e.getMessage());
            exit(1);
        }
    }


    private GramJob getExistingJob(String jobHandle) {
        GramJob job = new GramJob();
        try {
            job.setHandle(jobHandle);
        }
        catch (Exception e) {
            logger.debug("Exception while setting URL of job service", e);
            printError("could not find job with endpoint: " + jobHandle);
            /**
             * Todo: print endpoint in XML
             */
            //exception message is no good here.
            exit(1);
        }
        return job;
    }

    private void submitRSL(EndpointReferenceType factoryEndpoint,
                           String simpleJobCommandLine,
                           File rslFile,
                           Authorization authorization,
                           Integer xmlSecurity,
                           boolean batchMode,
                           boolean dryRunMode,
                           boolean quiet,
                           Date duration,
                           Date terminationDate,
                           int timeout)
    {
        this.quiet = quiet;
        this.batch = batchMode || dryRunMode; //in single job only.
        //In multi-job, -batch is not allowed. Dryrun is.

        if (batchMode) {
            printMessage("Warning: Will not wait for job completion, " +
                         "and will not destroy job service.");
        }

        if (rslFile != null) {
            try {
                this.job = new GramJob(rslFile);
            } catch (Exception e) {
                String errorMessage =
                    "Unable to parse RSL from file " + rslFile;
                logger.debug(errorMessage, e);
                printError(errorMessage + " - " + e.getMessage());
                exit(2);
            }
        }
        else {
            this.job = new GramJob(RSLHelper.makeSimpleJob(simpleJobCommandLine));
        }
        job.setTimeOut(timeout);

        job.setAuthorization(authorization);
        job.setMessageProtectionType(xmlSecurity);
        job.setDelegationEnabled(this.delegationEnabled);
        job.setDuration(duration);
        job.setTerminationTime(terminationDate);

        try {
            ShutdownHook shutdownHook = new ShutdownHook();
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            logger.debug("Added shutdown hook");
        }
        catch (Exception e) {
            //maybe already shuttingdown, or hooks disabled, etc...
            logger.warn("Exception while registering shutdown hook: ", e);
        }

        //now must ensure JVM will exit if uncaught exception:
        try {
            //this.job.setDryRun(dryRunMode);

            this.processJob(job, factoryEndpoint, batch);

        }
        catch (Exception e) {
            logger.debug("Caught exception: ", e);
            printError(e.getMessage());
            exit(1);
        }
    }

    private void processJob(GramJob job,
                            EndpointReferenceType factoryEndpoint,
                            boolean batch)
    {

        if (proxyPath != null) {
            try {
                ExtendedGSSManager manager = (ExtendedGSSManager)
                        ExtendedGSSManager.getInstance();
                String handle = "X509_USER_PROXY=" + proxyPath.toString();

                GSSCredential proxy
                    = manager.createCredential(handle.getBytes(),
                        ExtendedGSSCredential.IMPEXP_MECH_SPECIFIC,
                        GSSCredential.DEFAULT_LIFETIME, null,
                        GSSCredential.INITIATE_AND_ACCEPT);
                job.setCredentials(proxy);
            } catch (Exception e) {
                logger.debug("Exception while obtaining user proxy: ", e);
                printError("error obtaining user proxy: " + e.getMessage());
                //don't exit, but resume using default proxy instead
            }
        }

        if (submissionID == null) {
            // New job submission (not an attempt to recover)
            UUIDGen uuidgen = UUIDGenFactory.getUUIDGen();
            submissionID = "uuid:" + uuidgen.nextUUID();
        }

        printMessage("Submission ID: " + submissionID);

        if (!batch) {
            job.addListener(this);
            /*
            refreshJobStatus();
            */
        }

        boolean submitted = false;
        int tries = 0;

        while (!submitted) {
            tries++;

            try {
                job.submit(factoryEndpoint, batch, this.limitedDelegation,
                    submissionID);
                submitted = true;
            /* Incorrectly attempts repeated submissions for fatal errors.
            } catch (AxisFault toe) {
                logger.debug("create timed out: " + toe.getMessage());
                if (tries < 10) {
                    logger.info("trying again");
                } else {
                    printError(
                            "In spite of repeated attempts, I could not " +
                            "obtain a response from\nthe ManagedJobFactory " +
                            "service when submitting the job.");
                    exit(1);
                }
                */
            } catch (Exception e) {
                logger.debug("Exception while submitting the job request: ", e);
                printError("error submitting job request: " + e.getMessage());
                exit(1);
            }
        }

        if (batch) {
            printMessage("CREATED MANAGED JOB SERVICE WITH HANDLE:");
            printMessage(job.getHandle());
        }

        if (logger.isDebugEnabled()) {
            long millis = System.currentTimeMillis();
            BigDecimal seconds = new BigDecimal(((double)millis)/1000);
            seconds = seconds.setScale(3, BigDecimal.ROUND_HALF_DOWN);
            logger.debug( "submission time, in seconds from the Epoch:"
                          + "\nafter: " + seconds.toString());
            logger.debug( "\nafter, in milliseconds: " + millis);
        }


        if (!batch) {
            printMessage("WAITING FOR JOB TO FINISH");

            //we should print the current job state here ?

            waitForJobCompletion(STATE_CHANGE_BASE_TIMEOUT_MILLIS);
            try {
                this.destroyJob(this.job); //TEST
            }
            catch (Exception e) { printError("coudl not destroy");}

            if (this.job.getState().equals(StateEnumeration.Failed)) {
                printJobFault(this.job);
                //non-batch, so job submission should be part of command success
                exit(1);
            }
        }

        exit(0);
    }

    /**
     * Since messaging is assumed to be unreliable
     * (i.e. a notification could very well be lost), we
     * implement policy of pulling the remote state when a given
     * waited-for notification has not has been received after a timeout.
     * Note: this could however have the side-effect of hiding bugs in
     * the service-side notification implementation.
     *
     * The base delay in parameter is doubled each time the wait times out
     * (binary exponential backoff).
     * When a state change notification is received, the time out delay is
     * reset to the base value.
     *
     * @param maxWaitPerStateNotificationMillis long base timeout for each
     *                                               state transition before
     *                                               pulling the state from the
     *                                               service
     */
    private synchronized void waitForJobCompletion(
        long maxWaitPerStateNotificationMillis)
    {

        long durationToWait = maxWaitPerStateNotificationMillis;
        long startTime;
        StateEnumeration oldState = job.getState();
        /*
        if (oldState == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Local job state is null - " +
                             "forcing query of remote state");
            }
            refreshJobStatus(); //may update this.jobCompleted
            oldState = job.getState();
        }*/
           //prints one more state initially (Unsubmitted)
           //but cost extra remote call for sure. Null test below instead

        while (!this.jobCompleted) {

            if (logger.isDebugEnabled()) {
                logger.debug(
                    "job not completed - waiting for state change " +
                    "(timeout before pulling: " + durationToWait +
                    " ms).");
            }

            startTime = System.currentTimeMillis(); //(re)set start time
            try {
                wait(durationToWait); //wait for a state change notif
            }
            catch (InterruptedException ie) {
                String errorMessage =
                    "interrupted thread waiting for job to finish";
                logger.debug(errorMessage, ie);
                printError(errorMessage); //no exiting...
            }

            //now let's determine what stopped the wait():

            StateEnumeration currentState = job.getState();
            // A) New job state change notification (good!)
            if (currentState != null && !currentState.equals(oldState)) {
                oldState = currentState; //wait for next state notif
                durationToWait = maxWaitPerStateNotificationMillis; //reset
            }
            else {
                long now = System.currentTimeMillis();
                long durationWaited = now - startTime;
                // B) Timeout when waiting for a notification (bad)
                if (durationWaited >= durationToWait) {
                    if (logger.isWarnEnabled()) {
                        logger.warn(
                            "Did not receive any new notification of " +
                            "job state change after a delay of " +
                            durationToWait + " ms.\nPulling job state.");
                    }
                    //pull state from remote job and print the
                    //state only if it is a new state
                    refreshJobStatus();
                    //Little issue: the printing of state
                    //says "State Notification" even if it is not
                    //really one...

                    //binary exponential backoff
                    durationToWait = 2 * durationToWait;
                }
                // C) Some other reason
                else {
                    //wait but only for remainder of timeout duration
                    durationToWait = durationToWait - durationWaited;
                }
            }

        } //end loop

        /*
         OLD CODE: wait until job completes, no timeout + refresh...
            while (!this.jobCompleted) {
                try {
                    wait();
                }
                catch (InterruptedException ie) {
                    String errorMessage =
                        "interrupted thread waiting for job to finish";
                    logger.debug(errorMessage, ie);
                    printError(errorMessage); //no exit...
                }
            }
         */

    }

    /**
     * Callback as a GramJobListener.
     * Will not be called in batch mode.
     */
    public void stateChanged(GramJob job) {
        StateEnumeration jobState = job.getState();
        boolean holding = job.isHolding();
        printMessage("========== State Notification ==========");
        printJobState(jobState, holding);
        printMessage("========================================");

        synchronized (this) {
            if (   jobState.equals(StateEnumeration.Done)
                   //this class should be hidden from this layer?
                || jobState.equals(StateEnumeration.Failed)) {

                printMessage("Exit Code: "
                    + Integer.toString(job.getExitCode()));

                this.jobCompleted = true;
                //notifyAll();
            }

            notifyAll();

            //if we a running an interractive job,
            //prevent a hold from hanging the client
            if (holding && !batch) {
                logger.debug(
                    "Automatically releasing hold for interactive job");
                try {
                    job.release();
                } catch (Exception e) {
                   String errorMessage = "Unable to release job from hold";
                   logger.debug(errorMessage, e);
                   printError(errorMessage + " - " + e.getMessage());
                   exit(1);
                }
            }
        }
    }

    /**
     * Print message to user if not in quiet mode.
     *
     * @param message the message to send to stdout.
     */
    private void printMessage(String message) {
        if (!this.quiet) {
            System.out.println(message);
        }
    }

    /**
     * Print error message with prefix.
     */
    private void printError(String message) {
        System.err.println(ERROR_MESSAGE_PREFIX + message);
    }

    private void printJobState(StateEnumeration jobState, boolean holding) {
        String holdString = "";
        if (holding) holdString = "HOLD ";
        printMessage(JOB_STATE_PREFIX + holdString + jobState.getValue());
    }

    private void printJobFault(GramJob job) {
        BaseFaultType fault = job.getFault();
        if (fault != null) {
            printMessage("Fault:\n" + FaultUtils.faultToString(fault));
        }
    }

    private String convertEPRtoString(EndpointReferenceType endpoint)
        throws Exception
    {
        return ObjectSerializer.toString(
            endpoint,
            org.apache.axis.message.addressing.Constants.
                QNAME_ENDPOINT_REFERENCE);
    }


    /**
     * destroys the job WSRF resource
     * Precondition: job ! =null && job.isRequested() && !job.isLocallyDestroyed()
     */
    private void destroyJob(GramJob job) throws Exception
    {
        printMessage("DESTROYING JOB RESOURCE");
        job.destroy();
        printMessage("JOB RESOURCE DESTROYED");
    }

    private class ShutdownHook extends Thread
    {

      private boolean destroyRequestedJob() {

          return (isInterrupted && !noInterruptHandling) || //handled interrupt
                 (normalApplicationEnd && !batch)        || //no error
                 (!normalApplicationEnd && !isInterrupted); //exception
                 //should we disable last one unless exception
                 //happens when start()?
      }

      public void run() {

          if (logger.isDebugEnabled() &&
              isInterrupted && !noInterruptHandling) {
              logger.debug("Handling shutdown signal.");
          }

          //code defensively since interrupt can happen at any time
          if (job != null && job.isRequested() && !job.isLocallyDestroyed()) {

              if (destroyRequestedJob()) {
                  try {
                        logger.debug(
                              "destroying job service from shutdown hook");
                        logger.debug("destroyJob() called in run()");
                        destroyJob(job);
                  }
                  catch (Exception e) {
                      logger.debug(
                                "Exception while destroying job service: ",
                                e);
                      printError("could not destroy the job service: " +
                                 e.getMessage());
                  }
              }

          }

        } //end run


    } //end class

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

} //end class
