package integration;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   FileIntegrationTestSuite
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   16 aout 2007
 * ***************************************************
 * Description:                                      */
/**
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
    FileIntegrationTestSuite.FileNSEntryTest.class,
    FileIntegrationTestSuite.FileDirectoryTest.class,
    FileIntegrationTestSuite.FileDirectoryMakeTest.class,
    FileIntegrationTestSuite.FileFileReadTest.class,
    FileIntegrationTestSuite.FileFileWriteTest.class,
    FileIntegrationTestSuite.FileMovementTest.class,
    FileIntegrationTestSuite.EmulatorMovementTest.class
})
public class FileIntegrationTestSuite {

    protected static String TYPE;

    @BeforeClass
    public static void setType() {
        TYPE = "file";
    }
    /** test cases */
    public static class FileNSEntryTest extends EntryTest {
        public FileNSEntryTest() throws Exception {super("file");}
    }
    public static class FileDirectoryTest extends DirTest {
        public FileDirectoryTest() throws Exception {super("file");}
    }
    public static class FileDirectoryMakeTest extends MakeDirTest {
        public FileDirectoryMakeTest() throws Exception {super("file");}
    }
    public static class FileFileReadTest extends ReadTest {
        public FileFileReadTest() throws Exception {super("file");}
    }
    public static class FileFileWriteTest extends WriteTest {
        public FileFileWriteTest() throws Exception {super("file");}
    }
    public static class FileMovementTest extends DataMovementTest {
        public FileMovementTest() throws Exception {super("file", "file");}
    }
    public static class EmulatorMovementTest extends DataMovementTest {
        public EmulatorMovementTest() throws Exception {super("file", "test");}
    }
}
