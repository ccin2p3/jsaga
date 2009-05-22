package org.ogf.saga.job;

import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.job.abstracts.AbstractJobTest;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.task.State;
import org.ogf.saga.url.URLFactory;

import java.io.*;
import java.net.URI;
import java.util.UUID;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobRunSandboxTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class JobRunSandboxTest extends AbstractJobTest {
    private static final String SCRIPT_CONTENT = "#!/bin/sh\n/usr/bin/cat $1 | /usr/bin/tr 'ou' 'ui' $2";
    private static final String INPUT_CONTENT = "coucou";
    private static final String OUTPUT_CONTENT = "cuicui";
    private static final File TMP = new File(System.getProperty("java.io.tmpdir"));

    private UUID m_uuid;

    protected JobRunSandboxTest(String jobprotocol) throws Exception {
        super(jobprotocol);
    }

    public void setUp() {
        m_uuid = UUID.randomUUID();
    }

    public void tearDown() {
        m_uuid = null;
    }

    public void test_input_local_to_worker() throws Exception {
        this.runJobInput(getLocal("input"), getWorker("input"));
    }
    public void test_input_remote_to_worker() throws Exception {
        this.runJobInput(getRemote("input"), getWorker("input"));
    }
    public void test_input_local_to_remote() throws Exception {
        this.runJobInput(getLocal("input"), getRemote("input"));
    }
    public void test_input_remote_to_remote() throws Exception {
        this.runJobInput(getRemote("input_source"), getRemote("input_target"));
    }

    public void test_output_local_from_worker() throws Exception {
        this.runJobOutput(getLocal("output"), getWorker("output"));
    }
    public void test_output_remote_from_worker() throws Exception {
        this.runJobOutput(getRemote("output"), getWorker("output"));
    }
    public void test_output_local_from_remote() throws Exception {
        this.runJobOutput(getLocal("output"), getRemote("output"));
    }
    public void test_output_remote_from_remote() throws Exception {
        this.runJobOutput(getRemote("output"), getRemote("output"));
    }

    //////////////////////////////////////////// private methods ////////////////////////////////////////////

    private File getLocal(String suffix) {
        return new File(TMP, "local-"+m_uuid+"."+suffix);
    }
    private URI getRemote(String suffix) {
        return new File(TMP, "remote-"+m_uuid+"."+suffix).toURI();
    }
    private String getWorker(String suffix) {
        return new File(TMP, "worker-"+m_uuid+"."+suffix).toURI().getPath();
    }

    private void runJobInput(Object localInput, Object workerInput) throws Exception {
        this.runJob(getLocal("sh"), getWorker("sh"), localInput, workerInput, getLocal("output"), getWorker("output"));
    }
    private void runJobOutput(Object localOutput, Object workerOutput) throws Exception {
        this.runJob(getLocal("sh"), getWorker("sh"), getLocal("input"), getWorker("input"), localOutput, workerOutput);
    }
    private void runJob(File localScript, String workerScript, Object localInput, Object workerInput, Object localOutput, Object workerOutput) throws Exception {
        // prepare
        this.put(localScript, SCRIPT_CONTENT.getBytes());
        this.put(localInput, INPUT_CONTENT.getBytes());
        String scriptPath = toWorkerPath(workerScript);
        String inputPath = toWorkerPath(workerInput);
        String outputPath = toWorkerPath(workerOutput);

        // create job
        JobDescription desc = JobFactory.createJobDescription();
        desc.setAttribute(JobDescription.EXECUTABLE, scriptPath);
        desc.setVectorAttribute(JobDescription.ARGUMENTS, new String[]{inputPath, outputPath});
        desc.setVectorAttribute(JobDescription.FILETRANSFER, new String[]{
                localScript+" > "+workerScript,
                localInput+" > "+workerInput,
                localOutput+" < "+workerOutput
        });

        // submit
        JobService service = JobFactory.createJobService(m_session, m_jobservice);
        Job job = service.createJob(desc);
        job.run();

        // wait
        job.waitFor();

        // check job status
        assertEquals(State.DONE.getValue(), job.getState().getValue());

        // check job output
        String outputContent = this.get(localOutput);
        assertEquals(OUTPUT_CONTENT, outputContent);
    }

    private void put(Object location, byte[] content) throws Exception {
        if (location instanceof File) {
            File file = (File) location;
            OutputStream out = new FileOutputStream(file);
            out.write(content);
            out.close();
        } else if (location instanceof URI) {
            URI uri = (URI) location;
            org.ogf.saga.file.File file = FileFactory.createFile(m_session, URLFactory.createURL(uri.toString()), Flags.WRITE.getValue());
            file.write(BufferFactory.createBuffer(content));
            file.close();
        } else {
            throw new Exception("Unexpected class: "+location.getClass());
        }
    }

    private String get(Object location) throws Exception {
        final int BUFFER_SIZE = 1024;
        if (location instanceof File) {
            File file = (File) location;
            byte[] buffer = new byte[BUFFER_SIZE];
            InputStream in = new FileInputStream(file);
            int len = in.read(buffer);
            in.close();
            return new String(buffer, 0, len);
        } else if (location instanceof  URI) {
            URI uri = (URI) location;
            Buffer buffer = BufferFactory.createBuffer(BUFFER_SIZE);
            org.ogf.saga.file.File file = FileFactory.createFile(m_session, URLFactory.createURL(uri.toString()), Flags.READ.getValue());
            int len = file.read(buffer);
            file.close();
            return new String(buffer.getData(), 0, len);
        } else {
            throw new Exception("Unexpected class: "+location.getClass());
        }
    }

    private static String toWorkerPath(Object location) throws Exception {
        if (location instanceof String) {
            String path = (String) location;
            return path;
        } else if (location instanceof URI) {
            URI uri = (URI) location;
            return uri.getPath();
        } else {
            throw new Exception("Unexpected class: "+location.getClass());
        }
    }
}
