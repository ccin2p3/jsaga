package integration;

import junit.framework.Test;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   FileIntegrationTestSuite
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   16 août 2007
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class FileIntegrationTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new FileIntegrationTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(FileIntegrationTestSuite.class);}}

    /** test cases */
    public static class FileNSEntryTest extends NSEntryTest {
        public FileNSEntryTest() throws Exception {super("file");}
    }
    public static class FileDirectoryListTest extends DirectoryListTest {
        public FileDirectoryListTest() throws Exception {super("file");}
    }
    public static class FileDirectoryMakeTest extends DirectoryMakeTest {
        public FileDirectoryMakeTest() throws Exception {super("file");}
    }
    public static class FileDirectoryTest extends DirectoryTest {
        public FileDirectoryTest() throws Exception {super("file");}
    }
    public static class FileFileReadTest extends FileReadTest {
        public FileFileReadTest() throws Exception {super("file");}
    }
    public static class FileFileWriteTest extends FileWriteTest {
        public FileFileWriteTest() throws Exception {super("file");}
    }
    public static class FileNSCopyTest extends NSCopyTest {
        public FileNSCopyTest() throws Exception {super("file", "file");}
    }
    public static class FileNSCopyFromTest extends NSCopyFromTest {
        public FileNSCopyFromTest() throws Exception {super("file", "file");}
    }
    public static class FileNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public FileNSCopyRecursiveTest() throws Exception {super("file", "file");}
    }
    public static class FileNSMoveTest extends NSMoveTest {
        public FileNSMoveTest() throws Exception {super("file", "file");}
    }
    public static class File_to_EmulatorNSCopyTest extends NSCopyTest {
        public File_to_EmulatorNSCopyTest() throws Exception {super("file", "test");}
    }
    public static class File_to_EmulatorNSCopyFromTest extends NSCopyFromTest {
        public File_to_EmulatorNSCopyFromTest() throws Exception {super("file", "test");}
    }
    public static class File_to_EmulatorNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public File_to_EmulatorNSCopyRecursiveTest() throws Exception {super("file", "test");}
    }
    public static class File_to_EmulatorNSMoveTest extends NSMoveTest {
        public File_to_EmulatorNSMoveTest() throws Exception {super("file", "test");}
    }
}
