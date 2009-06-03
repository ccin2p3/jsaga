package fr.in2p3.jsaga.impl.job.staging;

import fr.in2p3.jsaga.helpers.StringArray;
import org.ogf.saga.error.*;
import org.ogf.saga.job.Job;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DataStagingList
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   20 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class DataStagingList {
    private List<InputDataStagingToRemote> m_inputToRemote;
    private List<InputDataStagingToWorker> m_inputToWorker;
    private List<OutputDataStagingFromRemote> m_outputFromRemote;
    private List<OutputDataStagingFromWorker> m_outputFromWorker;

    public DataStagingList() throws NotImplementedException, BadParameterException, NoSuccessException {
        m_inputToRemote = new ArrayList<InputDataStagingToRemote>();
        m_inputToWorker = new ArrayList<InputDataStagingToWorker>();
        m_outputFromRemote = new ArrayList<OutputDataStagingFromRemote>();
        m_outputFromWorker = new ArrayList<OutputDataStagingFromWorker>();
    }

    public void add(AbstractDataStaging dataStaging) throws BadParameterException {
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

    public boolean needsStdin() {
        return !m_inputToWorker.isEmpty() || !m_outputFromWorker.isEmpty();
    }

    public boolean needsStdout() {
        return !m_outputFromWorker.isEmpty();
    }

    public void preStaging(Job job, String executable, String[] arguments) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // may create script
        if (this.needsStdin()) {
            // open
            PrintStream stdin = new UnixPrintStream(job.getStdin());

            // copy template to script
            InputStream template = DataStagingList.class.getClassLoader().getResourceAsStream("bash/template.sh");
            byte[] buffer = new byte[1024];
            try {
                for (int len; (len=template.read(buffer)) > -1; ) {
                    stdin.write(buffer, 0, len);
                }
                template.close();
            } catch (IOException e) {
                throw new NoSuccessException(e);
            }

            // for each inputToWorker
            for (int i=0; i<m_inputToWorker.size(); i++) {
                InputDataStagingToWorker staging = m_inputToWorker.get(i);
                staging.preStaging(job.getSession(), stdin, i, executable);
            }

            // invoke command
            stdin.println("set -- "+StringArray.arrayToString(arguments, " "));
            stdin.println(executable+" $*");
            stdin.println();

            // for each outputFromWorker
            for (OutputDataStagingFromWorker staging : m_outputFromWorker) {
                staging.preStaging(stdin);
            }

            // cleanup
            for (InputDataStagingToWorker staging : m_inputToWorker) {
                staging.cleanup(stdin);
            }
            for (OutputDataStagingFromWorker staging : m_outputFromWorker) {
                staging.cleanup(stdin);
            }

            // close
            stdin.close();
        }

        // for each inputToRemote
        for (InputDataStagingToRemote staging : m_inputToRemote) {
            staging.preStaging(job.getSession());
        }
    }

    public void postStaging(Job job) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // may retrieve output files from stdout
        if (this.needsStdout()) {
            // open
            BufferedReader stdout = new BufferedReader(new InputStreamReader(job.getStdout()));

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

    public void cleanup(Job job) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // for each inputToRemote
        for (InputDataStagingToRemote staging : m_inputToRemote) {
            staging.cleanup(job.getSession());
        }

        // for each outputFromRemote
        for (OutputDataStagingFromRemote staging : m_outputFromRemote) {
            staging.cleanup(job.getSession());
        }
    }

    private static boolean isURL(String file) {
        final boolean hasProtocolScheme = file.contains(":/");
        final boolean isLinuxAbsolutePath = file.startsWith("/");
        final boolean isWindowsAbsolutePath = file.indexOf(':')<=1;  // -1 or 1 (none or "_:")
        return hasProtocolScheme && ! (isLinuxAbsolutePath || isWindowsAbsolutePath);
    }
}
