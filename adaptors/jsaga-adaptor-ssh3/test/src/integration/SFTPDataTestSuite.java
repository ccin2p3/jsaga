package integration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.ogf.saga.file.DirTest;
import org.ogf.saga.file.File;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.file.MakeDirTest;
import org.ogf.saga.file.ReadTest;
import org.ogf.saga.file.WriteTest;
import org.ogf.saga.namespace.DataCleanUp;
import org.ogf.saga.namespace.DataMovementTest;
import org.ogf.saga.namespace.EntryTest;
import org.ogf.saga.permissions.Permission;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SFTPDataTestSuite
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   5 NOV 2013
****************************************************/

@RunWith(Suite.class)
@SuiteClasses({
	SFTPDataTestSuite.SFTPNSEntryTest.class, 
	SFTPDataTestSuite.SFTPDirTest.class, 
	SFTPDataTestSuite.SFTPDirectoryMakeTest.class, 
	SFTPDataTestSuite.SFTPFileReadTest.class, 
	SFTPDataTestSuite.SFTPFileWriteTest.class, 
	SFTPDataTestSuite.SFTPDataMovementTest.class, 
	SFTPDataTestSuite.SFTPEmulatorDataMovementTest.class})
public class SFTPDataTestSuite {
	
	private final static String TYPE = "sftp";
	
    /** test cases */
    public static class SFTPNSEntryTest extends EntryTest {
        public SFTPNSEntryTest() throws Exception {super(TYPE);}
    }
    public static class SFTPDirTest extends DirTest {
        public SFTPDirTest() throws Exception {super(TYPE);}

        @Test
        @Ignore
        public void test_list_and_getAttributes() throws Exception {
            if (m_file instanceof File) {
                List <URL> dirContent = m_subDir.list();
	            for (URL dirEntry: dirContent) {
                       String fileName = dirEntry.getPath();
                       String url = m_subDir.getURL().toString() + fileName;
                       File f = FileFactory.createFile(m_session, URLFactory.createURL(url));
                       long size = f.getSize();
                       long date = f.getMTime();
                       boolean readPermisssion = f.permissionsCheck("*", Permission.READ.getValue());
                       boolean writePermission = f.permissionsCheck("*", Permission.WRITE.getValue());
                       boolean executePermission = f.permissionsCheck("*", Permission.EXEC.getValue());

                       System.out.println(
                               fileName + " " +
                               (readPermisssion ? "r" : "-") + (writePermission ? "w" : "-") + (executePermission ? "x" : "-") + " " + 
                               (date != 0l ? new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(new Date(date)) : "?") + " " 
                               + (size != 0l ? "" + size + "B": "?")
                               );
                       f.close();
	            }
            } else {
                fail("Not an instance of class: File");
            }
            
        }

    }
    public static class SFTPDirectoryMakeTest extends MakeDirTest {
        public SFTPDirectoryMakeTest() throws Exception {super(TYPE);}
    }
    public static class SFTPFileReadTest extends ReadTest {
        public SFTPFileReadTest() throws Exception {super(TYPE);}
    }
    public static class SFTPFileWriteTest extends WriteTest {
        public SFTPFileWriteTest() throws Exception {super(TYPE);}
    }
    public static class SFTPDataMovementTest extends DataMovementTest {
        public SFTPDataMovementTest() throws Exception {super(TYPE, TYPE);}
    }
    public static class SFTPEmulatorDataMovementTest extends DataMovementTest {
        public SFTPEmulatorDataMovementTest() throws Exception {super(TYPE, "test");}
    }
    public static class SFTPDataCleanUp extends DataCleanUp {
        public SFTPDataCleanUp() throws Exception {super(TYPE);}
    }
}