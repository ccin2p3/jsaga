package integration;

import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusDataTestSuite
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   31 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
//    GlobusDataTestSuite.GsiftpNSSetUpTest.class,
    GlobusDataTestSuite.GsiftpNSEntryTest.class,
    GlobusDataTestSuite.GsiftpDirTest.class,
    GlobusDataTestSuite.GsiftpDirectoryMakeTest.class,
    GlobusDataTestSuite.GsiftpFileReadTest.class,
    GlobusDataTestSuite.GsiftpFileWriteTest.class,
    GlobusDataTestSuite.GsiftpDataMovementTest.class,
    GlobusDataTestSuite.GsiftpEmulatorDataMovementTest.class
})
public class GlobusDataTestSuite {
    /** test cases */
//    public static class GsiftpNSSetUpTest extends NSSetUpTest {
//        public GsiftpNSSetUpTest() throws Exception {super("gsiftp");}
//    }
    public static class GsiftpNSEntryTest extends EntryTest {
        public GsiftpNSEntryTest() throws Exception {super("gsiftp");}
    }
    public static class GsiftpDirTest extends DirTest {
        public GsiftpDirTest() throws Exception {super("gsiftp");}
    }
    public static class GsiftpDirectoryMakeTest extends MakeDirTest {
        public GsiftpDirectoryMakeTest() throws Exception {super("gsiftp");}
    }
    public static class GsiftpFileReadTest extends ReadTest {
        public GsiftpFileReadTest() throws Exception {super("gsiftp");}
    }
    public static class GsiftpFileWriteTest extends WriteTest {
    	// test_write_append: DPM ignores the append flag so the adaptor sends a BadParameter
    	// test_read_and_write: does not work on DPM servers for unknown reason
        public GsiftpFileWriteTest() throws Exception {super("gsiftp");}
    }
    public static class GsiftpDataMovementTest extends DataMovementTest {
        public GsiftpDataMovementTest() throws Exception {super("gsiftp", "gsiftp");}
    }
    public static class GsiftpEmulatorDataMovementTest extends DataMovementTest {
        public GsiftpEmulatorDataMovementTest() throws Exception {super("gsiftp", "test");}
        @Test
        public void test_copy_1MB() throws Exception {
            String File1MBName = "file1MB.txt";
            String bufferString = "01234567";
            for (int i=0; i<17; i++) {
                bufferString += bufferString;
            }
            NSEntry m_file_1MB = m_dir.open(createURL(m_subDirUrl, File1MBName), FLAGS_FILE);
            Buffer buffer = BufferFactory.createBuffer(bufferString.getBytes());
            ((File)m_file_1MB).write(buffer);
            m_file_1MB.close();
   	
            URL target = createURL(m_dirUrl2, File1MBName);
            m_file_1MB.copy(m_dirUrl2, Flags.NONE.getValue());
            checkCopied(target, bufferString, 1024*1024);
            m_file_1MB.remove();
            
        }
    }
}