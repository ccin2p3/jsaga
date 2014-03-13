package fr.in2p3.jsaga.adaptor.ssh3.data;

import org.junit.Assert;
import org.junit.Test;
import org.ogf.saga.JSAGABaseTest;


public class SFTPDataAdaptorTest extends JSAGABaseTest {

    public SFTPDataAdaptorTest() throws Exception {
        super();
    }

    @Test
    public void usage() throws Exception {
        Assert.assertEquals("([KnownHosts]  [FilenameEncoding])", new SFTPDataAdaptor().getUsage().toString());
    }
}
