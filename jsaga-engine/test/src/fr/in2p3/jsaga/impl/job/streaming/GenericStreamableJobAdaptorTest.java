package fr.in2p3.jsaga.impl.job.streaming;

import static org.junit.Assert.assertEquals;

import java.io.File;

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
    public void submit() {
        m_mockery = new Mockery();
        final StagingJobAdaptor sja = m_mockery.mock(StagingJobAdaptor.class);
//        m_mockery.checking(new Expectations() {{
//            allowing(sja).submit(jobDesc, checkMatch, uniqId); will(returnValue("123")); 
//        }});

        GenericStreamableJobAdaptor adaptor = new GenericStreamableJobAdaptor(sja);
//        adaptor.submit(jobDesc, checkMatch, uniqId, stdin);
    }
}
