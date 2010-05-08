package fr.in2p3.jsaga.adaptor.wms.job;

import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOSetter;
import org.globus.ftp.*;
import org.ogf.saga.error.*;

import java.io.OutputStream;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WMSJobIOHandler
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   13 mai 2008
* ***************************************************/

public class WMSJobIOHandler implements JobIOSetter {
    public static final String INPUT_SUFFIX = "input.txt";
    public static final String OUTPUT_SUFFIX = "output.txt";
    public static final String ERROR_SUFFIX = "error.txt";

    private GridFTPClient m_stagingClient;
    private String m_stagingPrefix;
    private String m_jobId;

    public WMSJobIOHandler(GridFTPClient stagingClient, String stagingPrefix, String jobId) {
        m_stagingClient = stagingClient;
        m_stagingPrefix = stagingPrefix;
        m_jobId = jobId;
    }

    public void setStdout(OutputStream out) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        this.getToStream(m_stagingPrefix+"-"+OUTPUT_SUFFIX, out);
    }

    public void setStderr(OutputStream err) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        // workaround: sleep between setStdout and setStderr
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new NoSuccessException(e);
        }
        this.getToStream(m_stagingPrefix+"-"+ERROR_SUFFIX, err);
    }

    public String getJobId() {
        return m_jobId;
    }

    private void getToStream(String absolutePath, OutputStream stream) throws NoSuccessException {
        final boolean autoFlush = false;
        final boolean ignoreOffset = true;
        try {
            m_stagingClient.setType(GridFTPSession.TYPE_IMAGE);
            m_stagingClient.setMode(GridFTPSession.MODE_STREAM);
            m_stagingClient.setPassive();
            m_stagingClient.setLocalActive();
            m_stagingClient.get(
                    absolutePath,
                    new DataSinkStream(stream, autoFlush, ignoreOffset),
                    null);
        } catch (Exception e) {
            throw new NoSuccessException("Failed to read file: "+absolutePath, e);
        }
    }

    //Not used...
//    private void putFromStream(String absolutePath, boolean append, InputStream stream) throws NoSuccessException {
//        final int DEFAULT_BUFFER_SIZE = 16384;
//        try {
//            m_stagingClient.setType(GridFTPSession.TYPE_IMAGE);
//            m_stagingClient.setMode(GridFTPSession.MODE_EBLOCK);
//            m_stagingClient.setPassive();
//            m_stagingClient.setLocalActive();
//            m_stagingClient.put(
//                absolutePath,
//                new DataSourceStream(stream, DEFAULT_BUFFER_SIZE),
//                    null,
//                    append);
//        } catch (Exception e) {
//            throw new NoSuccessException("Failed to write file: "+absolutePath, e);
//        }
//    }
}