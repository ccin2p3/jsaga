package integration;

import junit.framework.Test;

import org.ogf.saga.error.DoesNotExistException;
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
public class UnicoreDataTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new UnicoreDataTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(UnicoreDataTestSuite.class);}}

    /** test cases */
    public static class UnicoreNSEntryTest extends NSEntryTest {
        public UnicoreNSEntryTest() throws Exception {super("unicore");}
    }
    public static class UnicoreDirectoryListTest extends DirectoryListTest {
        public UnicoreDirectoryListTest() throws Exception {super("unicore");}
    }
    public static class UnicoreDirectoryMakeTest extends DirectoryMakeTest {
        public UnicoreDirectoryMakeTest() throws Exception {super("unicore");}
    }
    public static class UnicoreDirectoryTest extends DirectoryTest {
        public UnicoreDirectoryTest() throws Exception {super("unicore");}
    }
    public static class UnicoreFileReadTest extends FileReadTest {
        public UnicoreFileReadTest() throws Exception {super("unicore");}
    }
    public static class UnicoreFileWriteTest extends FileWriteTest {
        public UnicoreFileWriteTest() throws Exception {super("unicore");}
        public void test_write_append() { super.ignore("not supported"); }
    }
    public static class UnicoreNSCopyTest extends NSCopyTest {
        public UnicoreNSCopyTest() throws Exception {super("unicore", "unicore");}
    }
    public static class UnicoreNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public UnicoreNSCopyRecursiveTest() throws Exception {super("unicore", "unicore");}
    }
    public static class UnicoreNSMoveTest extends NSMoveTest {
        public UnicoreNSMoveTest() throws Exception {super("unicore", "unicore");}
    }
    public static class Unicore_to_EmulatorNSCopyTest extends NSCopyTest {
        public Unicore_to_EmulatorNSCopyTest() throws Exception {super("unicore", "test");}
    }
    public static class Unicore_to_EmulatorNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public Unicore_to_EmulatorNSCopyRecursiveTest() throws Exception {super("unicore", "test");}
    }
    public static class Unicore_to_EmulatorNSMoveTest extends NSMoveTest {
        public Unicore_to_EmulatorNSMoveTest() throws Exception {super("unicore", "test");}
    }
}