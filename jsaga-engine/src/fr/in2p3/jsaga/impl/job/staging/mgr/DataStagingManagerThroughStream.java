package fr.in2p3.jsaga.impl.job.staging.mgr;

import fr.in2p3.jsaga.helpers.StringArray;
import fr.in2p3.jsaga.impl.job.instance.AbstractSyncJobImpl;
import fr.in2p3.jsaga.impl.job.staging.*;
import org.ogf.saga.error.*;
import org.ogf.saga.job.JobDescription;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DataStagingManagerThroughStream
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   9 mars 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class DataStagingManagerThroughStream implements DataStagingManager {
    private List<InputDataStagingToRemote> m_inputToRemote;
    private List<InputDataStagingToWorker> m_inputToWorker;
    private List<OutputDataStagingFromRemote> m_outputFromRemote;
    private List<OutputDataStagingFromWorker> m_outputFromWorker;

    // job description
    private String m_executable;
    private String[] m_arguments;
    private StringBuffer m_redirections;

    public DataStagingManagerThroughStream(String[] fileTransfer) throws NotImplementedException, BadParameterException, NoSuccessException {
        // create
        m_inputToRemote = new ArrayList<InputDataStagingToRemote>();
        m_inputToWorker = new ArrayList<InputDataStagingToWorker>();
        m_outputFromRemote = new ArrayList<OutputDataStagingFromRemote>();
        m_outputFromWorker = new ArrayList<OutputDataStagingFromWorker>();
        m_executable = null;
        m_arguments = null;
        m_redirections = null;

        // init
        for (String ft : fileTransfer) {
            AbstractDataStaging dataStaging = DataStagingFactory.create(ft);
            if (dataStaging instanceof InputDataStagingToRemote) {
                m_inputToRemote.add((InputDataStagingToRemote) dataStaging);
            } else if (dataStaging instanceof InputDataStagingToWorker) {
                m_inputToWorker.add((InputDataStagingToWorker) dataStaging);
            } else if (dataStaging instanceof OutputDataStagingFromRemote) {
                m_outputFromRemote.add((OutputDataStagingFromRemote) dataStaging);
            } else if (dataStaging instanceof OutputDataStagingFromWorker) {
                m_outputFromWorker.add((OutputDataStagingFromWorker) dataStaging);
            } else {
                throw new BadParameterException("[INTERNAL ERROR] Unexpected class: "+dataStaging.getClass());
            }
        }
    }

    public JobDescription modifyJobDescription(final JobDescription jobDesc) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        try {
            // clone jobDesc and modify clone
            JobDescription newJobDesc = (JobDescription) jobDesc.clone();

            // remove FileTransfer because it is not supported by plugin
            try {
                newJobDesc.removeAttribute(JobDescription.FILETRANSFER);
            } catch (DoesNotExistException e) {
                // ignore
            }

            // remove options managed by generated script from job description
            m_redirections = new StringBuffer();
            if (this.needsStdin()) {
                // check job is not interactive
                try {
                    if ("true".equalsIgnoreCase(jobDesc.getAttribute(JobDescription.INTERACTIVE))) {
                        throw new BadParameterException("Option "+JobDescription.FILETRANSFER+" can not be used with option "+JobDescription.INTERACTIVE);
                    }
                } catch (DoesNotExistException e) {
                    // ignore
                }

                newJobDesc.setAttribute(JobDescription.INTERACTIVE, "true");

                m_executable = jobDesc.getAttribute(JobDescription.EXECUTABLE);
                newJobDesc.setAttribute(JobDescription.EXECUTABLE, "/bin/sh");

                try {
                    m_arguments = jobDesc.getVectorAttribute(JobDescription.ARGUMENTS);
                    newJobDesc.removeAttribute(JobDescription.ARGUMENTS);
                } catch (DoesNotExistException e) {
                    m_arguments = null;
                }

                try {
                    String input = jobDesc.getAttribute(JobDescription.INPUT);
                    newJobDesc.removeAttribute(JobDescription.INPUT);
                    m_redirections.append(" <");
                    m_redirections.append(input);
                } catch (DoesNotExistException e) {
                    // ignore
                }
                try {
                    String output = jobDesc.getAttribute(JobDescription.OUTPUT);
                    newJobDesc.removeAttribute(JobDescription.OUTPUT);
                    m_redirections.append(" >");
                    m_redirections.append(output);
                } catch (DoesNotExistException e) {
                    // ignore
                }
                try {
                    String error = jobDesc.getAttribute(JobDescription.ERROR);
                    newJobDesc.removeAttribute(JobDescription.ERROR);
                    m_redirections.append(" 2>");
                    m_redirections.append(error);
                } catch (DoesNotExistException e) {
                    // ignore
                }

                // uncomment this to enable debugging job wrapper script
//                newJobDesc.setAttribute(JobDescription.EXECUTABLE, "/usr/bin/cat");
//                newJobDesc.setVectorAttribute(JobDescription.ARGUMENTS, new String[]{"> /tmp/job-wrapper.sh"});
            }

            // returns
            return newJobDesc;
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        } catch (DoesNotExistException e) {
            throw new NoSuccessException(e);
        } catch (CloneNotSupportedException e) {
            throw new NoSuccessException(e);
        }
    }

    public void preStaging(AbstractSyncJobImpl job) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // may create script
        if (this.needsStdin()) {
            // open
            PrintStream stdin = new UnixPrintStream(job.getStdinSync());

            // copy template to script
            InputStream template = this.getClass().getClassLoader().getResourceAsStream("bash/template.sh");
            BufferedReader reader = new BufferedReader(new InputStreamReader(template));
            try {
                for (String line; (line=reader.readLine())!=null; ) {
                    stdin.println(line);
                }
                reader.close();
            } catch (IOException e) {
                throw new NoSuccessException(e);
            }

            // for each inputToWorker
            for (int i=0; i<m_inputToWorker.size(); i++) {
                InputDataStagingToWorker staging = m_inputToWorker.get(i);
                staging.preStaging(job.getSession(), stdin, i, m_executable);
            }

            // invoke command
            stdin.println("PATH=.:$PATH");
            if (m_arguments != null) {
                stdin.println("set -- "+ StringArray.arrayToString(m_arguments, " "));
            }
            stdin.println(m_executable+" $*"+m_redirections.toString());
            stdin.println();

            // for each outputFromWorker
            for (OutputDataStagingFromWorker staging : m_outputFromWorker) {
                staging.preStaging(stdin);
            }

            // cleanup on worker
            if (isCleanup(job.getJobDescriptionSync())) {
                for (InputDataStagingToWorker staging : m_inputToWorker) {
                    staging.cleanup(stdin);
                }
                for (OutputDataStagingFromWorker staging : m_outputFromWorker) {
                    staging.cleanup(stdin);
                }
            }

            // close
            stdin.close();
        }

        // for each inputToRemote
        for (InputDataStagingToRemote staging : m_inputToRemote) {
            staging.preStaging(job.getSession());
        }
    }

    public void postStaging(AbstractSyncJobImpl job, String nativeJobId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // may retrieve output files from stdout
        if (this.needsStdout()) {
            // open
            BufferedReader stdout = new BufferedReader(new InputStreamReader(job.getStdoutSync()));

            // for each outputFromWorker
            for (OutputDataStagingFromWorker staging : m_outputFromWorker) {
                staging.postStaging(job.getSession(), stdout);
            }

            // close
            try{stdout.close();} catch(IOException e){/*ignore*/}
        }

        // for each outputFromRemote
        for (OutputDataStagingFromRemote staging : m_outputFromRemote) {
            staging.postStaging(job.getSession());
        }
    }

    public void cleanup(AbstractSyncJobImpl job, String nativeJobId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // for each inputToRemote
        for (InputDataStagingToRemote staging : m_inputToRemote) {
            staging.cleanup(job.getSession());
        }

        // for each outputFromRemote
        for (OutputDataStagingFromRemote staging : m_outputFromRemote) {
            staging.cleanup(job.getSession());
        }
    }

    private boolean needsStdin() {
        return !m_inputToWorker.isEmpty() || !m_outputFromWorker.isEmpty();
    }

    private boolean needsStdout() {
        return !m_outputFromWorker.isEmpty();
    }

    private static boolean isCleanup(JobDescription jobDesc) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        final boolean CLEANUP_DEFAULT = true;
        try {
            String cleanup = jobDesc.getAttribute(JobDescription.CLEANUP);
            if ("False".equalsIgnoreCase(cleanup)) {
                return false;
            } else if ("True".equalsIgnoreCase(cleanup)) {
                return true;
            } else if ("Default".equalsIgnoreCase(cleanup)) {
                return CLEANUP_DEFAULT;
            } else {
                throw new BadParameterException("Attribute '"+JobDescription.CLEANUP+"' has unexpected value: "+cleanup);
            }
        } catch (DoesNotExistException e) {
            return CLEANUP_DEFAULT;
        }
    }

    private static boolean isURL(String file) {
        final boolean hasProtocolScheme = file.contains(":/");
        final boolean isLinuxAbsolutePath = file.startsWith("/");
        final boolean isWindowsAbsolutePath = file.indexOf(':')<=1;  // -1 or 1 (none or "_:")
        return hasProtocolScheme && ! (isLinuxAbsolutePath || isWindowsAbsolutePath);
    }
}
