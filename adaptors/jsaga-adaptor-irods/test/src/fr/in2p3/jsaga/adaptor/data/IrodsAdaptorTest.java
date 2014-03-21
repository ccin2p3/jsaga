package fr.in2p3.jsaga.adaptor.data;

import org.junit.Assert;
import org.junit.Test;

public class IrodsAdaptorTest {

    @Test
    public void usage() throws Exception {
        String u = "(UserID  zone  defaultresource  [domain]  [metadatavalue])";
        Assert.assertEquals(u, new IrodsDataAdaptor().toString());
    }

}
