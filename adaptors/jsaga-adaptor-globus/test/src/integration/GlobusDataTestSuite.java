package integration;

import junit.framework.Test;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusIntegrationTestSuite
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   31 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GlobusDataTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new GlobusDataTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(GlobusDataTestSuite.class);}}

    /** test cases */
    public static class GsiftpNSSetUpTest extends NSSetUpTest {
        public GsiftpNSSetUpTest() throws Exception {super("gsiftp");}
    }
    public static class GsiftpNSEntryTest extends NSEntryTest {
        public GsiftpNSEntryTest() throws Exception {super("gsiftp");}
    }
    public static class GsiftpDirectoryListTest extends DirectoryListTest {
        public GsiftpDirectoryListTest() throws Exception {super("gsiftp");}
    }
    public static class GsiftpDirectoryMakeTest extends DirectoryMakeTest {
        public GsiftpDirectoryMakeTest() throws Exception {super("gsiftp");}
    }
    public static class GsiftpDirectoryTest extends DirectoryTest {
        public GsiftpDirectoryTest() throws Exception {super("gsiftp");}
    }
    public static class GsiftpFileReadTest extends FileReadTest {
        public GsiftpFileReadTest() throws Exception {super("gsiftp");}
    }
    public static class GsiftpFileWriteTest extends FileWriteTest {
    	// test_write_append: DPM ignores the append flag so the adaptor sends a BadParameter
    	// test_read_and_write: does not work on DPM servers for unknown reason
        public GsiftpFileWriteTest() throws Exception {super("gsiftp");}
    }
    public static class GsiftpNSCopyTest extends NSCopyTest {
        public GsiftpNSCopyTest() throws Exception {super("gsiftp", "gsiftp");}
    }
    public static class GsiftpNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public GsiftpNSCopyRecursiveTest() throws Exception {super("gsiftp", "gsiftp");}
    }
    public static class GsiftpNSMoveTest extends NSMoveTest {
        public GsiftpNSMoveTest() throws Exception {super("gsiftp", "gsiftp");}
    }
    public static class Gsiftp_to_EmulatorNSCopyTest extends NSCopyTest {
        public Gsiftp_to_EmulatorNSCopyTest() throws Exception {super("gsiftp", "test");}
    }
    public static class Gsiftp_to_EmulatorNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public Gsiftp_to_EmulatorNSCopyRecursiveTest() throws Exception {super("gsiftp", "test");}
    }
    public static class Gsiftp_to_EmulatorNSMoveTest extends NSMoveTest {
        public Gsiftp_to_EmulatorNSMoveTest() throws Exception {super("gsiftp", "test");}
    }
}