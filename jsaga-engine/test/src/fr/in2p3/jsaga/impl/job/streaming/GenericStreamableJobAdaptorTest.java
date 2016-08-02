package fr.in2p3.jsaga.impl.job.streaming;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptor;

public class GenericStreamableJobAdaptorTest {

    private Mockery m_mockery = null;

    @Test
    public void localFileFactory() {
        String uniqId = "123";
        File f;
        String prefix = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator");
        
        f = LocalFileFactory.getLocalInputFile(uniqId);
        assertEquals(prefix + "local-123.input", f.getAbsolutePath());
        f = LocalFileFactory.getLocalOutputFile(uniqId);
        assertEquals(prefix + "local-123.output", f.getAbsolutePath());
        f = LocalFileFactory.getLocalErrorFile(uniqId);
        assertEquals(prefix + "local-123.error", f.getAbsolutePath());
    }
    
    @Test 
    public void submit() throws Exception {
        m_mockery = new Mockery();
        final StagingJobAdaptor sja = m_mockery.mock(StagingJobAdaptor.class);
        m_mockery.checking(new Expectations() {{
            allowing(sja).submit(
                    with(any(String.class)), 
                    with(any(Boolean.class)), 
                    with(any(String.class))); will(returnValue("123")); 
        }});

        GenericStreamableJobAdaptor adaptor = new GenericStreamableJobAdaptor(sja);
        GenericJobIOHandler gjioh = (GenericJobIOHandler) adaptor.submit("", false, "123", new ByteArrayInputStream("INPUT_STRING".getBytes()));
        assertEquals(gjioh.getJobId(), "123");
        // check that input file contains "INPUT_STRING"
        FileInputStream fis = new FileInputStream(LocalFileFactory.getLocalInputFile("123"));
        byte[] content = new byte[1024];
        int bytesRead = fis.read(content);
        assertEquals("INPUT_STRING", new String(content, 0, bytesRead));
        // delete input file
        LocalFileFactory.getLocalInputFile("123").delete();
    }
}
