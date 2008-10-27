package fr.in2p3.jsaga.adaptor.data.file;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.BaseURL;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.write.DataWriterTimes;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStreamFactory;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import org.ogf.saga.error.*;

import java.io.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   FileDataAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   16 août 2007
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class FileDataAdaptor implements FileReaderStreamFactory, FileWriterStreamFactory, DataRename, DataWriterTimes {
    private static final boolean s_isWindows = System.getProperty("os.name").startsWith("Windows");
    private String m_drive;

    public String getType() {
        return "file";
    }

    public Usage getUsage() {
        return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;
    }

    public Class[] getSupportedSecurityAdaptorClasses() {
        return null;    // no context class
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        // do nothing
    }

    public BaseURL getBaseURL() throws IncorrectURLException {
        return null;
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailedException, AuthorizationFailedException, TimeoutException, NoSuccessException {
        if (s_isWindows) {
            m_drive = host;
        }
    }

    public void disconnect() throws NoSuccessException {
        // do nothing
    }

    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        File entry = newPath(absolutePath);
        return entry==null || entry.exists();
    }

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        try {
            File file = newFile(absolutePath);
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new DoesNotExistException("File does not exist");
        }
    }

    public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        File parentDirectory;
        try {
            parentDirectory = newDirectory(parentAbsolutePath);
        } catch (DoesNotExistException e) {
            throw new ParentDoesNotExist("Parent directory does not exist");
        }
        File file = new File(parentDirectory, fileName);
        try {
            if (!file.createNewFile()) {
                if (exclusive) {
                    throw new AlreadyExistsException("File already exist");
                } else if (append) {
                    return new FileOutputStream(file, true);
                } else {
                    return new FileOutputStream(file, false);   //overwrite
                }
            } else {
                return new FileOutputStream(file, false);
            }
        } catch (IOException e) {
            throw new NoSuccessException("Failed to create file: "+fileName, e);
        }
    }

    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        File parentDirectory = newDirectory(parentAbsolutePath);
        File file = new File(parentDirectory, fileName);
        if (!file.exists()) {
            throw new DoesNotExistException("File does not exist");
        } else if (!file.delete()) {
            throw new NoSuccessException("Failed to remove file: "+fileName);
        }
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        File entry = this.newEntry(absolutePath);
        return new LocalFileAttributes(entry);
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        File[] list;
        File entry = this.newPath(absolutePath);
        if (entry == null) {
            list = File.listRoots();
        } else if (entry.isDirectory()) {
            list = newEntry(absolutePath).listFiles();
        } else if (entry.exists()) {
            throw new BadParameterException("Entry is not a directory");
        } else {
            throw new DoesNotExistException("Entry does not exist");
        }
        if (list == null) {
            throw new PermissionDeniedException("Permission denied");
        }
        FileAttributes[] ret = new FileAttributes[list.length];
        for (int i=0; i<list.length; i++) {
            ret[i] = new LocalFileAttributes(list[i]);
        }
        return ret;
    }

    public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        File parentDirectory;
        try {
            parentDirectory = newDirectory(parentAbsolutePath);
        } catch (DoesNotExistException e) {
            throw new ParentDoesNotExist("Parent directory does not exist");
        }
        File directory = new File(parentDirectory, directoryName);
        if (directory.exists()) {
            throw new AlreadyExistsException("Directory already exist");
        } else if (!directory.mkdir()) {
            throw new NoSuccessException("Failed to create directory: "+directoryName);
        }
    }

    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        File parentDirectory = newDirectory(parentAbsolutePath);
        File directory = new File(parentDirectory, directoryName);
        if (!directory.exists()) {
            throw new DoesNotExistException("Directory does not exist");
        } else if (!directory.delete()) {
            throw new NoSuccessException("Failed to remove directory: "+directoryName);
        }
    }

    public void rename(String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException {
        File source = newEntry(sourceAbsolutePath);
        File target = newPath(targetAbsolutePath);
        if (!overwrite && target!=null && target.exists()) {
            throw new AlreadyExistsException("Target entry already exists");
        }
        source.renameTo(target);
    }

    public void setLastModified(String absolutePath, String additionalArgs, long lastModified) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        File entry = newEntry(absolutePath);
        entry.setLastModified(lastModified);
    }

    ////////////////////////////////////// private methods //////////////////////////////////////

    private File newPath(String absolutePath) throws NoSuccessException {
        if (s_isWindows) {
            Matcher m;
            if (m_drive != null) {
                return new File(m_drive+":"+absolutePath);
            } else if ( (m=Pattern.compile("/+(\\./+)+([A-Za-z]:.*)").matcher(absolutePath)).matches() ) {
                return new File(m.group(m.groupCount()));
            } else if (absolutePath.matches("/+[A-Za-z]:.*") || absolutePath.startsWith("./")) {
                return new File(absolutePath);
            } else if (absolutePath.matches("/+")) {
                return null;
            } else {
                throw new NoSuccessException("Absolute path in Windows must start with drive letter: "+absolutePath);
            }
        } else {
            return new File(absolutePath);
        }
    }

    private File newEntry(String absolutePath) throws DoesNotExistException, NoSuccessException {
        File entry = this.newPath(absolutePath);
        if (entry!=null && entry.exists()) {
            return entry;
        } else {
            throw new DoesNotExistException("Entry does not exist");
        }
    }

    private File newFile(String absolutePath) throws BadParameterException, DoesNotExistException, NoSuccessException {
        File entry = this.newEntry(absolutePath);
        if (entry.isFile()) {
            return entry;
        } else {
            throw new BadParameterException("Entry is a directory: "+absolutePath);
        }
    }

    private File newDirectory(String absolutePath) throws BadParameterException, DoesNotExistException, NoSuccessException {
        File entry = this.newEntry(absolutePath);
        if (entry.isDirectory()) {
            return entry;
        } else {
            throw new BadParameterException("Entry is a file: "+absolutePath);
        }
    }
}
