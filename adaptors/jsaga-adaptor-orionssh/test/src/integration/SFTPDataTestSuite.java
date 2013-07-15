package integration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import junit.framework.Test;

import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.permissions.Permission;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SFTPDataTestSuite
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   15 avril 2008
****************************************************/

public class SFTPDataTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new SFTPDataTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(SFTPDataTestSuite.class);}}

    /** test cases */
    public static class SFTPNSEntryTest extends NSEntryTest {
        public SFTPNSEntryTest() throws Exception {super("sftp");}
    }
    public static class SFTPDirectoryListTest extends DirectoryListTest {
        public SFTPDirectoryListTest() throws Exception {super("sftp");}
        public void test_list_and_getAttributes() throws Exception {
        	String new_content = "new_content";
            if (m_file instanceof File) {
            	// Write another file
                URL file2Url = createURL(m_subDirUrl, "File2.txt");
                NSEntry file = m_subDir.open(file2Url, FLAGS_FILE);
                Buffer buffer = BufferFactory.createBuffer(new_content.getBytes());
                ((File)file).write(buffer);
                file.close();
                checkWrited(file2Url, new_content);
                // delete created file
                List <URL> dirContent = m_subDir.list();
//              m_dir.close();

	              for (URL dirEntry: dirContent) {
//	                   if (m_subDir.isEntry(dirEntry)) {
	                       String fileName = dirEntry.getPath();
	                       File f = FileFactory.createFile(m_session, URLFactory.createURL(m_subDir.getURL().toString() + fileName));
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
//	                   }
	              }
                file.remove();
            } else {
                fail("Not an instance of class: File");
            }
            
        }

    }
    public static class SFTPDirectoryMakeTest extends DirectoryMakeTest {
        public SFTPDirectoryMakeTest() throws Exception {super("sftp");}
    }
    public static class SFTPDirectoryTest extends DirectoryTest {
        public SFTPDirectoryTest() throws Exception {super("sftp");}
    }
    public static class SFTPFileReadTest extends FileReadTest {
        public SFTPFileReadTest() throws Exception {super("sftp");}
    }
    public static class SFTPFileWriteTest extends FileWriteTest {
        public SFTPFileWriteTest() throws Exception {super("sftp");}
    }
    public static class SFTPNSCopyTest extends NSCopyTest {
        public SFTPNSCopyTest() throws Exception {super("sftp", "sftp");}
    }
    public static class SFTPNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public SFTPNSCopyRecursiveTest() throws Exception {super("sftp", "sftp");}
    }
    public static class SFTPNSMoveTest extends NSMoveTest {
        public SFTPNSMoveTest() throws Exception {super("sftp", "sftp");}
    }
    public static class SFTP_to_EmulatorNSCopyTest extends NSCopyTest {
        public SFTP_to_EmulatorNSCopyTest() throws Exception {super("sftp", "test");}
    }
    public static class SFTP_to_EmulatorNSCopyRecursiveTest extends NSCopyRecursiveTest {
        public SFTP_to_EmulatorNSCopyRecursiveTest() throws Exception {super("sftp", "test");}
    }
    public static class SFTP_to_EmulatorNSMoveTest extends NSMoveTest {
        public SFTP_to_EmulatorNSMoveTest() throws Exception {super("sftp", "test");}
    }
}