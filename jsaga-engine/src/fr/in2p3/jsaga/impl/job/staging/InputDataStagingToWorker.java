package fr.in2p3.jsaga.impl.job.staging;

import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.file.File;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.io.PrintStream;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   InputDataStagingToWorker
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   20 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class InputDataStagingToWorker extends AbstractDataStagingWorker {
    protected InputDataStagingToWorker(URL localURL, String workerPath, boolean append) {
        super(localURL, workerPath, append);
    }

    public void preStaging(Session session, PrintStream stdin, int position, String executable) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // set variables
        String WORKER_PATH = m_workerPath;
        String FUNCTION = "input_"+position;

        // read fully
        Buffer buffer;
        try {
            File localFile = FileFactory.createFile(session, m_localURL, Flags.READ.getValue());
            long size = localFile.getSize();
            if (size > Integer.MAX_VALUE) {
                throw new NotImplementedException("Attribute "+ JobDescription.FILETRANSFER+" is not supported for large files: "+size);
            }
            buffer = BufferFactory.createBuffer((int) size);
            localFile.read(buffer, (int) size);
            localFile.close();
        } catch (SagaIOException e) {
            throw new NoSuccessException(e);
        } catch (IncorrectURLException e) {
            throw new NoSuccessException(e);
        } catch (AlreadyExistsException e) {
            throw new NoSuccessException(e);
        }

        // generate script
        stdin.println("function "+FUNCTION+" () {");
        stdin.println("/bin/cat << ====");
        stdin.print(encode(buffer.getData()));
        stdin.println("====");
        stdin.println("}");
        if (m_append) {
            stdin.println("decode_append "+WORKER_PATH+" "+FUNCTION);
        } else {
            stdin.println("decode "+WORKER_PATH+" "+FUNCTION);
        }
        if (WORKER_PATH.equals(executable)) {
            stdin.println("chmod u+x "+WORKER_PATH);
        }
        stdin.println();
    }

    public void cleanup(PrintStream stdin) {
        stdin.println("rm -f "+m_workerPath);
    }

    private static String encode(byte[] decoded) throws NoSuccessException {
        String encoded = new BASE64Encoder().encodeBuffer(decoded);
        return encoded.replaceAll("\r\n", "\n");
    }

    /** must not be used on Windows platform */
    private static void encode(byte[] decoded, PrintStream stdin) throws NoSuccessException {
        try {
            new BASE64Encoder().encodeBuffer(decoded, stdin);
        } catch (IOException e) {
            throw new NoSuccessException(e);
        }
    }
}
