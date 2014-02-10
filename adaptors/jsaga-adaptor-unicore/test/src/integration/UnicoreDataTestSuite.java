package integration;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.file.DirTest;
import org.ogf.saga.file.MakeDirTest;
import org.ogf.saga.file.ReadTest;
import org.ogf.saga.file.WriteTest;
import org.ogf.saga.namespace.DataMovementTest;
import org.ogf.saga.namespace.EntryTest;


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
@RunWith(Suite.class)
@SuiteClasses({
    UnicoreDataTestSuite.UnicoreNSEntryTest.class,
    UnicoreDataTestSuite.UnicoreDirectoryTest.class,
    UnicoreDataTestSuite.UnicoreDirectoryMakeTest.class,
    UnicoreDataTestSuite.UnicoreFileReadTest.class,
    UnicoreDataTestSuite.UnicoreFileWriteTest.class,
    UnicoreDataTestSuite.UnicoreDataMovementTest.class,
    UnicoreDataTestSuite.Unicore_to_EmulatorDataMovementTest.class
})
 public class UnicoreDataTestSuite {

    /** test cases */
    public static class UnicoreNSEntryTest extends EntryTest {
        public UnicoreNSEntryTest() throws Exception {super("unicore");}
    }
    public static class UnicoreDirectoryMakeTest extends MakeDirTest {
        public UnicoreDirectoryMakeTest() throws Exception {super("unicore");}
    }
    public static class UnicoreDirectoryTest extends DirTest {
        public UnicoreDirectoryTest() throws Exception {super("unicore");}
    }
    public static class UnicoreFileReadTest extends ReadTest {
        public UnicoreFileReadTest() throws Exception {super("unicore");}
    }
    public static class UnicoreFileWriteTest extends WriteTest {
        public UnicoreFileWriteTest() throws Exception {super("unicore");}
        @Override @Test @Ignore("Not supported")
        public void test_write_append() { }
    }
    public static class UnicoreDataMovementTest extends DataMovementTest {
        public UnicoreDataMovementTest() throws Exception {super("unicore", "unicore");}
    }
    public static class Unicore_to_EmulatorDataMovementTest extends DataMovementTest {
        public Unicore_to_EmulatorDataMovementTest() throws Exception {super("unicore", "test");}
    }
}