import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.namespace.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   IntegrationTestSuite
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   16 août 2007
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class IntegrationTestSuite extends TestSuite {
    public static class FileNSEntryTest extends NSEntryTest {
        public FileNSEntryTest() throws Exception {super("file");}
    }
    public static class FilePhysicalDirectoryListTest extends PhysicalDirectoryListTest {
        public FilePhysicalDirectoryListTest() throws Exception {super("file");}
    }
    public static class FilePhysicalDirectoryMakeTest extends PhysicalDirectoryMakeTest {
        public FilePhysicalDirectoryMakeTest() throws Exception {super("file");}
    }
    public static class FilePhysicalDirectoryTest extends PhysicalDirectoryTest {
        public FilePhysicalDirectoryTest() throws Exception {super("file");}
    }
    public static class FilePhysicalFileReadTest extends PhysicalFileReadTest {
        public FilePhysicalFileReadTest() throws Exception {super("file");}
    }
    public static class FilePhysicalFileWriteTest extends PhysicalFileWriteTest {
        public FilePhysicalFileWriteTest() throws Exception {super("file");}
    }
    public static class FileNSCopyTest extends NSCopyTest {
        public FileNSCopyTest() throws Exception {super("file", "file");}
    }
    public static class FileNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public FileNSCopyRecursiveTest() throws Exception {super("file", "file");}
    }
    public static class File_to_EmulatorNSCopyTest extends NSCopyTest {
        public File_to_EmulatorNSCopyTest() throws Exception {super("file", "test");}
    }
    public static class File_to_EmulatorNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public File_to_EmulatorNSCopyRecursiveTest() throws Exception {super("file", "test");}
    }

    public IntegrationTestSuite() throws Exception {
        super();
        this.addTestSuite(FileNSEntryTest.class);
        this.addTestSuite(FilePhysicalDirectoryListTest.class);
        this.addTestSuite(FilePhysicalDirectoryMakeTest.class);
        this.addTestSuite(FilePhysicalDirectoryTest.class);
        this.addTestSuite(FilePhysicalFileReadTest.class);
        this.addTestSuite(FilePhysicalFileWriteTest.class);
        this.addTestSuite(FileNSCopyTest.class);
        this.addTestSuite(FileNSCopyRecursiveTest.class);
        this.addTestSuite(File_to_EmulatorNSCopyTest.class);
        this.addTestSuite(File_to_EmulatorNSCopyRecursiveTest.class);
    }

    public static Test suite() throws Exception {
        return new IntegrationTestSuite();
    }
}
