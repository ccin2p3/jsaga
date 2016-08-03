package fr.in2p3.jsaga.impl.job.staging;

import org.apache.commons.codec.binary.Base64;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

import java.io.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   OutputDataStagingFromWorker
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   20 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class OutputDataStagingFromWorker extends AbstractDataStagingWorker {
    protected OutputDataStagingFromWorker(URL localURL, String workerPath, boolean append) {
        super(localURL, workerPath, append);
    }

    public void preStaging(PrintStream stdin) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        String WORKER_PATH = m_workerPath;
        String LOCAL_PATH = m_localURL.getPath();
        stdin.println("encode "+WORKER_PATH+" "+LOCAL_PATH);
        stdin.println();
    }

    public void postStaging(Session session, BufferedReader stdout) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // decode
        byte[] decoded = uudecode(stdout);

        // write fully
        int append = (m_append ? Flags.APPEND : Flags.NONE).getValue();
        Buffer buffer = BufferFactory.createBuffer(JSAGA_FACTORY, decoded);
        try {
            org.ogf.saga.file.File localFile = FileFactory.createFile(JSAGA_FACTORY, session, m_localURL, Flags.CREATE.or(append));
            localFile.write(buffer);
            localFile.close();
        } catch (SagaIOException | IncorrectURLException | AlreadyExistsException e) {
            throw new NoSuccessException(e);
        }
    }

    public void cleanup(PrintStream stdin) {
        stdin.println("rm -f "+m_workerPath);
    }

    public boolean isInput() {
        return OUTPUT;
    }

    private static byte[] uudecode(BufferedReader reader) throws NoSuccessException {
        try {
            for (String line; (line=reader.readLine())!=null && !line.startsWith("begin-base64"); );
            StringBuffer encoded = new StringBuffer();
            for (String line; (line=reader.readLine())!=null && !line.equals("===="); ) {
                encoded.append(line);
                encoded.append('\n');
            }
            return Base64.decodeBase64(encoded.toString());
        } catch (IOException e) {
            throw new NoSuccessException(e);
        }
    }
}
