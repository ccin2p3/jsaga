package integration;

import org.ogf.saga.logicalfile.LogicalFileReadTest;
import org.ogf.saga.namespace.NSSetUpTest;

import junit.framework.Test;


/**
 * @author Jerome Revillard
 */
public class LFNIntegrationTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new LFNIntegrationTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(LFNIntegrationTestSuite.class);}}

    
    /** test cases */
    public static class LFNNSSetUpTest extends NSSetUpTest {
        public LFNNSSetUpTest() throws Exception {super("lfn");}
    }
    public static class LFNLogicalFileReadTest extends LogicalFileReadTest {
        public LFNLogicalFileReadTest() throws Exception {super("lfn");}
    }
}
