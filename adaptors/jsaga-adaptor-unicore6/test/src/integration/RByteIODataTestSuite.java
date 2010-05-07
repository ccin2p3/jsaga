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
public class RByteIODataTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new RByteIODataTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(RByteIODataTestSuite.class);}}

    /** test cases */
    public static class RByteIONSEntryTest extends NSEntryTest {
        public RByteIONSEntryTest() throws Exception {super("rbyteio");}
    }
    public static class RByteIODirectoryListTest extends DirectoryListTest {
        public RByteIODirectoryListTest() throws Exception {super("rbyteio");}
    }
    public static class RByteIODirectoryMakeTest extends DirectoryMakeTest {
        public RByteIODirectoryMakeTest() throws Exception {super("rbyteio");}
    }
    public static class RByteIODirectoryTest extends DirectoryTest {
        public RByteIODirectoryTest() throws Exception {super("rbyteio");}
    }
    public static class RByteIOFileReadTest extends FileReadTest {
        public RByteIOFileReadTest() throws Exception {super("rbyteio");}
    }
    public static class RByteIOFileWriteTest extends FileWriteTest {
        public RByteIOFileWriteTest() throws Exception {super("rbyteio");}
        public void test_write_append() { super.ignore("not supported"); }
    }
    public static class RByteIONSCopyTest extends NSCopyTest {
        public RByteIONSCopyTest() throws Exception {super("rbyteio", "rbyteio");}
    }
    public static class RByteIONSCopyRecursiveTest extends NSCopyRecursiveTest {
        public RByteIONSCopyRecursiveTest() throws Exception {super("rbyteio", "rbyteio");}
    }
    public static class RByteIONSMoveTest extends NSMoveTest {
        public RByteIONSMoveTest() throws Exception {super("rbyteio", "rbyteio");}
    }
    public static class RByteIO_to_EmulatorNSCopyTest extends NSCopyTest {
        public RByteIO_to_EmulatorNSCopyTest() throws Exception {super("rbyteio", "test");}
    }
    public static class RByteIO_to_EmulatorNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public RByteIO_to_EmulatorNSCopyRecursiveTest() throws Exception {super("rbyteio", "test");}
    }
    public static class RByteIO_to_EmulatorNSMoveTest extends NSMoveTest {
        public RByteIO_to_EmulatorNSMoveTest() throws Exception {super("rbyteio", "test");}
    }
}