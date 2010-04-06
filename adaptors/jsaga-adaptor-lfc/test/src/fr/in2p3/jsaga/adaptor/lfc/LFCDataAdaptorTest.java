package fr.in2p3.jsaga.adaptor.lfc;

import junit.framework.TestCase;

/**
 * @author Jerome Revillard
 */
public class LFCDataAdaptorTest extends TestCase {
    public void test_getScheme() {
        assertEquals(
                "lfn",
                new LFCDataAdaptor().getType());
    }
}
