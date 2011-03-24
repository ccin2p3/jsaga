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
    private static final String SCRIPT_IMPLICIT = "/usr/bin/tr 'ou' 'ui'";
    protected String m_scriptExplicit = "#!/bin/bash\n/bin/cat ${1##file:} | "+SCRIPT_IMPLICIT+" > ${2##file:}";
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

    public void test_remote_input_explicit() throws Exception {
        this.runJobExplicit(getRemote("input"), getWorker("input"), getLocal("output"), getWorker("output"));
    }

    public void test_remote_output_explicit() throws Exception {
        this.runJobExplicit(getLocal("input"), getWorker("input"), getRemote("output"), getWorker("output"));
    }

    public void test_input_output_explicit() throws Exception {
        this.runJobExplicit(getLocal("input"), getWorker("input"), getLocal("output"), getWorker("output"));
    }

    public void test_input_output_implicit() throws Exception {
        this.runJobImplicit(getLocal("input"), getWorker("input"), getLocal("output"), getWorker("output"));
    }

    public void test_output_only_implicit() throws Exception {
        Object localOutput = getLocal("output");
        Object workerOutput = getWorker("output");

        // create job
        JobDescription desc = JobFactory.createJobDescription();
        desc.setAttribute(JobDescription.EXECUTABLE, "/bin/echo");
        desc.setVectorAttribute(JobDescription.ARGUMENTS, new String[]{"cuicui"});
        desc.setVectorAttribute(JobDescription.FILETRANSFER, new String[]{
                localOutput+" < "+workerOutput
        });
        desc.setAttribute(JobDescription.OUTPUT, workerOutput.toString());

        // run job
        String outputContent = this.runAndGetOutput(desc, localOutput);
        assertEquals(OUTPUT_CONTENT, outputContent.trim());

        // cleanup
        this.cleanup(localOutput);
    }

    //////////////////////////////////////////// private methods ////////////////////////////////////////////

    protected File getLocal(String suffix) {
        return new File(TMP, "local-"+m_uuid+"."+suffix);
    }
    protected URI getRemote(String suffix) {
        return new File(TMP, "remote-"+m_uuid+"."+suffix).toURI();
    }
    protected String getWorker(String suffix) {
        return "worker-"+m_uuid+"."+suffix;
    }

    protected void runJobExplicit(Object localInput, Object workerInput, Object localOutput, Object workerOutput) throws Exception {
        this.runJob(true, getLocal("sh"), getWorker("sh"), localInput, workerInput, localOutput, workerOutput);
    }
    protected void runJobImplicit(Object localInput, Object workerInput, Object localOutput, Object workerOutput) throws Exception {
        this.runJob(false, getLocal("sh"), getWorker("sh"), localInput, workerInput, localOutput, workerOutput);
    }
    private void runJob(boolean explicitRedirect, File localScript, String workerScript, Object localInput, Object workerInput, Object localOutput, Object workerOutput) throws Exception {
        // prepare
        this.put(localInput, INPUT_CONTENT.getBytes());

        // create job
        JobDescription desc = JobFactory.createJobDescription();
        desc.setAttribute(JobDescription.EXECUTABLE, workerScript);
        desc.setVectorAttribute(JobDescription.FILETRANSFER, new String[]{
                localScript+" > "+workerScript,
                localInput+" > "+workerInput,
                localOutput+" < "+workerOutput
        });
        if (explicitRedirect) {
            this.put(localScript, m_scriptExplicit.getBytes());
            desc.setVectorAttribute(JobDescription.ARGUMENTS, new String[]{workerInput.toString(), workerOutput.toString()});
        } else {
            this.put(localScript, SCRIPT_IMPLICIT.getBytes());
            desc.setAttribute(JobDescription.INPUT, workerInput.toString());
            desc.setAttribute(JobDescription.OUTPUT, workerOutput.toString());
        }

        // run job
        String outputContent = this.runAndGetOutput(desc, localOutput);
        assertEquals(OUTPUT_CONTENT, outputContent);

        // cleanup
        this.cleanup(localScript);
        this.cleanup(localInput);
        this.cleanup(localOutput);
    }

    private String runAndGetOutput(JobDescription desc, Object localOutput) throws Exception {
        // submit
        JobService service = JobFactory.createJobService(m_session, m_jobservice);
        Job job = service.createJob(desc);
        job.run();
        System.out.println(job.getAttribute(Job.JOBID));

        // wait
        job.waitFor();

        
        // for debugging
        if (State.FAILED.equals(job.getState())) {
            // print stderr
            byte[] buffer = new byte[1024];
            InputStream stderr = job.getStderr();
            for (int len; (len=stderr.read(buffer))>-1; ) {
                System.err.write(buffer, 0, len);
            }
            stderr.close();

            // rethrow exception
            job.rethrow();
        }

        // check job status
        assertEquals(State.DONE.getValue(), job.getState().getValue());

        // return job output content
        return this.get(localOutput);
    }

    private void put(Object location, byte[] content) throws Exception {
        if (location instanceof File) {
            File file = (File) location;
            OutputStream out = new FileOutputStream(file);
            out.write(content);
            out.close();
        } else if (location instanceof URI) {
            URI uri = (URI) location;
            org.ogf.saga.file.File file = FileFactory.createFile(m_session, URLFactory.createURL(uri.toString()), Flags.CREATE.getValue());
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

            // check not empty
            boolean isNotEmpty = (len > -1);
            assertTrue("File is empty: "+file, isNotEmpty);
            return new String(buffer, 0, len);
        } else if (location instanceof URI) {
            URI uri = (URI) location;
            Buffer buffer = BufferFactory.createBuffer(BUFFER_SIZE);
            org.ogf.saga.file.File file = FileFactory.createFile(m_session, URLFactory.createURL(uri.toString()), Flags.READ.getValue());
            int len = file.read(buffer);
            file.close();

            // check not empty
            boolean isNotEmpty = (len > -1);
            assertTrue("File is empty: "+uri, isNotEmpty);
            return new String(buffer.getData(), 0, len);
        } else {
            throw new Exception("Unexpected class: "+location.getClass());
        }
    }

    private void cleanup(Object location) throws Exception {
        if (location instanceof File) {
            File file = (File) location;
            if (! file.delete()) {
                throw new Exception("Failed to remove file: "+file);
            }
        } else if (location instanceof URI) {
            URI uri = (URI) location;
            org.ogf.saga.file.File file = FileFactory.createFile(m_session, URLFactory.createURL(uri.toString()), Flags.NONE.getValue());
            file.remove();
            file.close();
        } else {
            throw new Exception("Unexpected class: "+location.getClass());
        }
    }
}
