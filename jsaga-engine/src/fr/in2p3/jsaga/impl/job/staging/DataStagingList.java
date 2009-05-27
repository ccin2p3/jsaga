package fr.in2p3.jsaga.impl.job.staging;

import fr.in2p3.jsaga.helpers.StringArray;
import org.ogf.saga.error.*;
import org.ogf.saga.job.Job;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Pattern PATTERN = Pattern.compile("([^<>]*) *(>>|>|<<|<) *([^<>]*)");

    private List<InputDataStagingToRemote> m_inputToRemote;
    private List<InputDataStagingToWorker> m_inputToWorker;
    private List<OutputDataStagingFromRemote> m_outputFromRemote;
    private List<OutputDataStagingFromWorker> m_outputFromWorker;

    public DataStagingList(String[] fileTransferArray) throws NotImplementedException, BadParameterException, NoSuccessException {
        m_inputToRemote = new ArrayList<InputDataStagingToRemote>();
        m_inputToWorker = new ArrayList<InputDataStagingToWorker>();
        m_outputFromRemote = new ArrayList<OutputDataStagingFromRemote>();
        m_outputFromWorker = new ArrayList<OutputDataStagingFromWorker>();
        for (String fileTransfer : fileTransferArray) {
            Matcher m = PATTERN.matcher(fileTransfer);
            if (m.matches() && m.groupCount()==3) {
                String local = m.group(1).trim();
                String operator = m.group(2).trim();
                String worker = m.group(3).trim();

                // set localURL
                URL localURL;
                if (isURL(local)) {
                    localURL = URLFactory.createURL(local);
                } else if (new File(local).isAbsolute()) {
                    localURL = URLFactory.createURL(new File(local).toURI().toString());
                } else {
                    localURL = URLFactory.createURL("file://./" + local.replaceAll("\\\\", "/"));
                }

                // create DataStaging
                if (">>".equals(operator) || ">".equals(operator)) {
                    boolean append = ">>".equals(operator);
                    if (isURL(worker)) {
                        m_inputToRemote.add(new InputDataStagingToRemote(localURL, URLFactory.createURL(worker), append));
                    } else {
                        m_inputToWorker.add(new InputDataStagingToWorker(localURL, worker, append));
                    }
                } else if ("<<".equals(operator) || "<".equals(operator)) {
                    boolean append = "<<".equals(operator);
                    if (isURL(worker)) {
                        m_outputFromRemote.add(new OutputDataStagingFromRemote(localURL, URLFactory.createURL(worker), append));
                    } else {
                        m_outputFromWorker.add(new OutputDataStagingFromWorker(localURL, worker, append));
                    }
                } else {
                    throw new BadParameterException("[INTERNAL ERROR] Unexpected operator: " + operator);
                }
            } else {
                throw new BadParameterException("Syntax error in attribute " + JobDescription.FILETRANSFER + ": " + fileTransfer);
            }
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
