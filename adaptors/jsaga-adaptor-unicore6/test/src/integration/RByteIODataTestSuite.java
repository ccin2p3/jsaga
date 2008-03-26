package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
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
public class RByteIODataTestSuite extends TestSuite {
    // test cases
    public static class RByteIONSEntryTest extends NSEntryTest {
        public RByteIONSEntryTest() throws Exception {super("rbyteio");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    public static class RByteIODirectoryListTest extends DirectoryListTest {
        public RByteIODirectoryListTest() throws Exception {super("rbyteio");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    public static class RByteIODirectoryMakeTest extends DirectoryMakeTest {
        public RByteIODirectoryMakeTest() throws Exception {super("rbyteio");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    public static class RByteIODirectoryTest extends DirectoryTest {
        public RByteIODirectoryTest() throws Exception {super("rbyteio");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    public static class RByteIOFileReadTest extends FileReadTest {
        public RByteIOFileReadTest() throws Exception {super("rbyteio");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    public static class RByteIOFileWriteTest extends FileWriteTest {
        public RByteIOFileWriteTest() throws Exception {super("rbyteio");}
        public void test_write_append() { System.out.println("the test_write_append ignored : not supported"); }
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    public static class RByteIONSCopyTest extends NSCopyTest {
        public RByteIONSCopyTest() throws Exception {super("rbyteio", "rbyteio");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    public static class RByteIONSCopyFromTest extends NSCopyFromTest {
        public RByteIONSCopyFromTest() throws Exception {super("rbyteio", "rbyteio");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    public static class RByteIONSCopyRecursiveTest extends NSCopyRecursiveTest {
        public RByteIONSCopyRecursiveTest() throws Exception {super("rbyteio", "rbyteio");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }
    public static class RByteIONSMoveTest extends NSMoveTest {
        public RByteIONSMoveTest() throws Exception {super("rbyteio", "rbyteio");}
        public void setUp() throws Exception {System.out.println(this.getClass()); super.setUp();}
    }

    public RByteIODataTestSuite() throws Exception {
        super();
        // test cases
        this.addTestSuite(RByteIONSEntryTest.class);
        this.addTestSuite(RByteIODirectoryListTest.class);
        this.addTestSuite(RByteIODirectoryMakeTest.class);
        this.addTestSuite(RByteIODirectoryTest.class);
        this.addTestSuite(RByteIOFileReadTest.class);
        this.addTestSuite(RByteIOFileWriteTest.class);
        this.addTestSuite(RByteIONSCopyTest.class);
        this.addTestSuite(RByteIONSCopyFromTest.class);
        this.addTestSuite(RByteIONSCopyRecursiveTest.class);
        this.addTestSuite(RByteIONSMoveTest.class);
    }

    public static Test suite() throws Exception {
        return new RByteIODataTestSuite();
    }
}