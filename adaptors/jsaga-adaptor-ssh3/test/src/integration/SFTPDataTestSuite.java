package integration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.ogf.saga.BaseTest;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.file.DirTest;
import org.ogf.saga.file.Directory;
import org.ogf.saga.file.File;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.file.MakeDirTest;
import org.ogf.saga.file.ReadTest;
import org.ogf.saga.file.WriteTest;
import org.ogf.saga.monitoring.Callback;
import org.ogf.saga.monitoring.Metric;
import org.ogf.saga.monitoring.Monitorable;
import org.ogf.saga.namespace.DataMovementTest;
import org.ogf.saga.namespace.EntryTest;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.permissions.Permission;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import fr.in2p3.jsaga.impl.file.copy.AbstractCopyTask;

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
        
        @Before @Override
        public void setUp() throws Exception {
            super.setUp();
            String bufferString = "01234567";
            for (int i=0; i<17; i++) {
                bufferString += bufferString;
            }
            Buffer buffer = BufferFactory.createBuffer(bufferString.getBytes());

            String File1MBName = "file1-1MB.txt";
            NSEntry m_file_1MB = m_dir.open(createURL(m_subDirUrl, File1MBName), FLAGS_FILE);
            ((File)m_file_1MB).write(buffer);
            m_file_1MB.close();

            File1MBName = "file2-1MB.txt";
            m_file_1MB = m_dir.open(createURL(m_subDirUrl, File1MBName), FLAGS_FILE);
            ((File)m_file_1MB).write(buffer);
            m_file_1MB.close();
        }
        
        @Test @Override
        public void test_copy_recurse() throws Exception {
            super.test_copy_recurse();
            URL newSubDir = createURL(m_dirUrl2, DEFAULT_DIRNAME + DEFAULT_SUBDIRNAME);
            assertEquals(1024*1024*2+DEFAULT_CONTENT.length(),
                    ((Directory)m_dir2).getSize(newSubDir));
        }


    }
    public static class SFTPEmulatorDataMovementTest extends DataMovementTest {
        public SFTPEmulatorDataMovementTest() throws Exception {super(TYPE, "test");}
    }
    
    public static class AkosCopy extends BaseTest {

        public AkosCopy() throws Exception {
            super();
            // TODO Auto-generated constructor stub
        }
        
        @Test
        public void copy() throws Exception {
            URL fromUrl = URLFactory.createURL("sftp://localhost/home/schwarz/.jsaga/var/tmp/ssh/SRC/AKOSTST/");
            URL toUrl = URLFactory.createURL("sftp://localhost/home/schwarz/.jsaga/var/tmp/ssh/DST/");
            boolean isFromDir = true;
            boolean isMove = false;
            
            int flags = Flags.RECURSIVE.getValue(); boolean overwrite = false;
            Session session = SessionFactory.createSession(true);
            
            NSEntry fromEntry;
//            try {
                fromEntry = isFromDir ?
                        NSFactory.createNSDirectory(session, fromUrl, Flags.NONE.getValue()) : // if it is a dir use create NS dir
                        NSFactory.createNSEntry(session, fromUrl, Flags.NONE.getValue()); // else entry
//            }
//            catch (AlreadyExistsException e) { monitor.failed(e.getMessage()); throw new OperationException("Directory already exists!", e); }
//            catch (IncorrectURLException e) {  monitor.failed(e.getMessage()); throw new OperationException(e); } // should not happen
//            catch (NotImplementedException e) { monitor.failed(e.getMessage()); throw new OperationException("Operation not supported!", e); }
//            catch (AuthenticationFailedException e) { monitor.failed(e.getMessage()); throw new OperationException("Authentication failed!", e); }
//            catch (AuthorizationFailedException e) { monitor.failed(e.getMessage()); throw new OperationException("Authorization failed!", e); }
//            catch (PermissionDeniedException e) { monitor.failed(e.getMessage()); throw new OperationException("Permission denied!", e); }
//            catch (BadParameterException e) { monitor.failed(e.getMessage()); throw new URIException("Malformed URI: " + fromUri, e); } // getFullPath truncates subdir name
//            catch (DoesNotExistException e) { monitor.failed(e.getMessage()); throw new OperationException(e); }  // should not happen
//            catch (TimeoutException e) { monitor.failed(e.getMessage()); throw new OperationException("Connection timeout!", e); }
//            catch (NoSuccessException e) { monitor.failed(e.getMessage()); throw new OperationException(e); } // ?
    
            Task<NSEntry, Void> task = null;
//            try {
                task =
                    isMove ?
                            fromEntry.move(TaskMode.TASK, toUrl, flags):  // overwrite OVERWRITE causes exception (fixed by Lionel)
                            fromEntry.copy(TaskMode.TASK, toUrl, flags);  // TaskMode.ASYNC: calls Task.run() implicitely
//            } catch (NotImplementedException e) {
//                monitor.failed("Copy or move operation is not implemented by the adaptor");
//                try { fromEntry.close(); } catch (Exception x) {}
//                throw new OperationException("Copy or move operation is not implemented by the adaptor", e);   
//            }
               
            String taskId = task.getId(); // get local id
            logger.info("New copy/move task with adaptor-managed id: " + taskId);
        
            // try to add task monitoring callback
            try {
                Metric metric = task.getMetric(AbstractCopyTask.FILE_COPY_PROGRESS); // "file.copy.progress"
                metric.addCallback(new Callback(){
                    public boolean cb(Monitorable mt, Metric metric, Context ctx) throws NotImplementedException, AuthorizationFailedException {
                        try {
                            String value = metric.getAttribute(Metric.VALUE);
                            String unit = metric.getAttribute(Metric.UNIT);
//                            System.out.println("Progress: "+value+" "+unit);
                        }
                        catch (NotImplementedException e) {throw e;}
                        catch (AuthorizationFailedException e) {throw e;}
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        // callback must stay registered
                        return true;
                    }
                });
                logger.trace("FILE_COPY_PROGRESS callback registered");
            }
            catch (Exception e) { logger.warn("Cannot register progress monitor for copy/move task! ("  + e.getMessage() + ")");    }
           
            // try to add task state monitoring callback
//            try {
//                Metric metric = task.getMetric(Task.TASK_STATE); // "task.state"
//                metric.addCallback(new CopyStateMonitor(task, taskId, taskRegistry, taskResourceRegistry, monitor));
//                log.trace("TASK_STATE callback registered");
//            }
//            catch (NotImplementedException e) { log.warn("Cannot register state monitor for copy/move task! ("  + e.getMessage() + ")");    }
//            catch (AuthenticationFailedException e) { log.warn("Cannot register state monitor for copy/move task! ("  + e.getMessage() + ")");    }
//            catch (AuthorizationFailedException e) { log.warn("Cannot register state monitor for copy/move task! ("  + e.getMessage() + ")");    }
//            catch (PermissionDeniedException e) { log.warn("Cannot register state monitor for copy/move task! ("  + e.getMessage() + ")");    }
//            catch (DoesNotExistException e) { log.warn("Cannot register state monitor for copy/move task! ("  + e.getMessage() + ")");    }
//            catch (TimeoutException e) { log.warn("Cannot register state monitor for copy/move task! ("  + e.getMessage() + ")");    }
//            catch (NoSuccessException e) { log.warn("Cannot register state monitor for copy/move task! ("  + e.getMessage() + ")");    }
//            catch (IncorrectStateException e) {    log.warn("Cannot register state monitor for copy/move task! ("  + e.getMessage() + ")"); } // task.run()
            // try to get source file size
//            try {
//                if (!isFromDir)
//                    monitor.setTotalDataSize(getFileSize(fromUrl, session));
//            } catch (Exception e) {
//                log.debug("Cannot get file size of source file to be copied...");
//            } // silently ignore if source file size cannot be retrieved
    
//            taskRegistry.put(taskId, task); // register task for later query (getStatus)
//            taskResourceRegistry.put(taskId, fromEntry); // register resources to be released when task completed
//            try {
                task.run();
                logger.info("JSAGA task started (run)...");
                task.waitFor();
                switch(task.getState()) {
                case DONE:
                    System.out.println("File successfully copied !");
                    break;
                default:
                    task.rethrow();
                    break;
            }
//            } // start task
//            catch (NoSuccessException e) {  try { fromEntry.close(); } catch (Exception x) {} monitor.failed("Cannot run task!"); throw new OperationException("Cannot run task!", e); } // task.run()
//            catch (TimeoutException e) {  try { fromEntry.close(); } catch (Exception x) {}    monitor.failed("Cannot run task!"); throw new OperationException("Cannot run task!", e); } // task.run()
//            catch (IncorrectStateException e) {     try { fromEntry.close(); } catch (Exception x) {}    monitor.failed("Cannot run task!"); throw new OperationException("Cannot run task!", e);    } // task.run()
//            catch (NotImplementedException e) {  try { fromEntry.close(); } catch (Exception x) {}    monitor.failed("Cannot run task!"); throw new OperationException("Cannot run task!", e); }
        }
        
    }
}