package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   SrbIntegrationTestSuite
 * Author: Pascal Calvat (pcalvat@cc.in2p3.fr)
 * Date:   13 may 2008
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class SrbIntegrationTestSuite extends TestSuite {
    // test SRB as a protocol for physical entries
    public static class SrbNSEntryTest extends NSEntryTest {
        public SrbNSEntryTest() throws Exception {super("srb");}
    }
	public static class SrbDirectoryListTest extends DirectoryListTest {
        public SrbDirectoryListTest() throws Exception {super("srb");}
    }
    public static class SrbDirectoryMakeTest extends DirectoryMakeTest {
        public SrbDirectoryMakeTest() throws Exception {super("srb");}
    }
    public static class SrbDirectoryTest extends DirectoryTest {
        public SrbDirectoryTest() throws Exception {super("srb");}
    }
    public static class SrbFileReadTest extends FileReadTest {
        public SrbFileReadTest() throws Exception {super("srb");}
    }
    public static class SrbFileWriteTest extends FileWriteTest {
        public SrbFileWriteTest() throws Exception {super("srb");}
    }
    public static class Srb_to_EmulatorNSCopyTest extends NSCopyTest {
        public Srb_to_EmulatorNSCopyTest() throws Exception {super("srb", "test");}
    }
    public static class Srb_to_EmulatorNSCopyFromTest extends NSCopyFromTest {
        public Srb_to_EmulatorNSCopyFromTest() throws Exception {super("srb", "test");}
    }
    public static class Srb_to_EmulatorNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public Srb_to_EmulatorNSCopyRecursiveTest() throws Exception {super("srb", "test");}
    }
    public static class Srb_to_EmulatorNSMoveTest extends NSMoveTest {
        public Srb_to_EmulatorNSMoveTest() throws Exception {super("srb", "test");}
    }

/*
    // test SRB as a protocol for logical entries
    public static class SrbLogicalDirectoryListTest extends LogicalDirectoryListTest {
        public SrbLogicalDirectoryListTest() throws Exception {super("srb");}
    }
    public static class SrbLogicalDirectoryMakeTest extends LogicalDirectoryMakeTest {
        public SrbLogicalDirectoryMakeTest() throws Exception {super("srb");}
    }
    public static class SrbLogicalDirectoryMetaDataTest extends LogicalDirectoryMetaDataTest {
        public SrbLogicalDirectoryMetaDataTest() throws Exception {super("srb");}
    }
    public static class SrbLogicalDirectoryTest extends LogicalDirectoryTest {
        public SrbLogicalDirectoryTest() throws Exception {super("srb");}
    }
    public static class SrbLogicalFileReadTest extends LogicalFileReadTest {
        public SrbLogicalFileReadTest() throws Exception {super("srb");}
    }
    public static class SrbLogicalFileWriteTest extends LogicalFileWriteTest {
        public SrbLogicalFileWriteTest() throws Exception {super("srb");}
    }
*/

    public SrbIntegrationTestSuite() throws Exception {
        super();
        // test SRB as a protocol for physical entries
        this.addTestSuite(SrbNSEntryTest.class);
		this.addTestSuite(SrbDirectoryListTest.class);
        this.addTestSuite(SrbDirectoryMakeTest.class);
        this.addTestSuite(SrbDirectoryTest.class);
        this.addTestSuite(SrbFileReadTest.class);
        this.addTestSuite(SrbFileWriteTest.class);
        this.addTestSuite(Srb_to_EmulatorNSCopyTest.class);
        this.addTestSuite(Srb_to_EmulatorNSCopyFromTest.class);
        this.addTestSuite(Srb_to_EmulatorNSCopyRecursiveTest.class);
        this.addTestSuite(Srb_to_EmulatorNSMoveTest.class);

/*
        // test SRB as a protocol for logical entries
        this.addTestSuite(SrbLogicalDirectoryListTest.class);
        this.addTestSuite(SrbLogicalDirectoryMakeTest.class);
        this.addTestSuite(SrbLogicalDirectoryMetaDataTest.class);
        this.addTestSuite(SrbLogicalDirectoryTest.class);
        this.addTestSuite(SrbLogicalFileReadTest.class);
        this.addTestSuite(SrbLogicalFileWriteTest.class);
*/
    }

    public static Test suite() throws Exception {
        return new SrbIntegrationTestSuite();
    }
}
