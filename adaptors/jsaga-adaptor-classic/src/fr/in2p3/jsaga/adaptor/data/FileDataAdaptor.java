package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.read.*;
import fr.in2p3.jsaga.adaptor.data.read.FileReader;
import fr.in2p3.jsaga.adaptor.data.write.DirectoryWriter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import org.ogf.saga.error.*;

import java.io.*;
import java.util.Map;

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
public class FileDataAdaptor implements FileReader, FileWriter, DirectoryReader, DirectoryWriter, DataRename {
    private static final boolean s_isWindows = System.getProperty("os.name").startsWith("Windows");
    private String m_drive;

    public Usage getUsage() {
        return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return null;
    }

    public Class[] getSupportedSecurityAdaptorClasses() {
        return null;    // no context class
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        // do nothing
    }

    public String[] getSchemeAliases() {
        return new String[]{"file", "local"};
    }

    public int getDefaultPort() {
        return 0;
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailed, AuthorizationFailed, Timeout, NoSuccess {
        if (s_isWindows) {
            m_drive = host;
        }
    }

    public void disconnect() throws NoSuccess {
        // do nothing
    }

    public boolean exists(String absolutePath) throws PermissionDenied, Timeout, NoSuccess {
        File entry = newPath(absolutePath);
        return entry==null || entry.exists();
    }

    public boolean isDirectory(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        return newEntry(absolutePath).isDirectory();
    }

    public boolean isEntry(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        return newEntry(absolutePath).isFile();
    }

    public long getSize(String absolutePath) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        return newFile(absolutePath).length();
    }

    public InputStream getInputStream(String absolutePath) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        try {
            File file = newFile(absolutePath);
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new DoesNotExist("File does not exist");
        }
    }

    public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append) throws PermissionDenied, BadParameter, AlreadyExists, ParentDoesNotExist, Timeout, NoSuccess {
        File parentDirectory;
        try {
            parentDirectory = newDirectory(parentAbsolutePath);
        } catch (DoesNotExist e) {
            throw new ParentDoesNotExist("Parent directory does not exist");
        }
        File file = new File(parentDirectory, fileName);
        try {
            if (!file.createNewFile()) {
                if (exclusive) {
                    throw new AlreadyExists("File already exist");
                } else if (append) {
                    return new FileOutputStream(file, true);
                } else {
                    return new FileOutputStream(file, false);   //overwrite
                }
            } else {
                return new FileOutputStream(file, false);
            }
        } catch (IOException e) {
            throw new NoSuccess("Failed to create file: "+fileName, e);
        }
    }

    public void removeFile(String parentAbsolutePath, String fileName) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        File parentDirectory = newDirectory(parentAbsolutePath);
        File file = new File(parentDirectory, fileName);
        if (!file.exists()) {
            throw new DoesNotExist("File does not exist");
        } else if (!file.delete()) {
            throw new NoSuccess("Failed to remove file: "+fileName);
        }
    }

    public FileAttributes[] listAttributes(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        File[] list;
        File entry = this.newPath(absolutePath);
        if (entry == null) {
            list = File.listRoots();
        } else if (entry.exists()) {
            list = newEntry(absolutePath).listFiles();
        } else {
            throw new DoesNotExist("Entry does not exist");
        }
        if (list == null) {
            throw new PermissionDenied("Permission denied");
        }
        FileAttributes[] ret = new FileAttributes[list.length];
        for (int i=0; i<list.length; i++) {
            ret[i] = new LocalFileAttributes(list[i]);
        }
        return ret;
    }

    public void makeDir(String parentAbsolutePath, String directoryName) throws PermissionDenied, BadParameter, AlreadyExists, ParentDoesNotExist, Timeout, NoSuccess {
        File parentDirectory;
        try {
            parentDirectory = newDirectory(parentAbsolutePath);
        } catch (DoesNotExist e) {
            throw new ParentDoesNotExist("Parent directory does not exist");
        }
        File directory = new File(parentDirectory, directoryName);
        if (directory.exists()) {
            throw new AlreadyExists("Directory already exist");
        } else if (!directory.mkdir()) {
            throw new NoSuccess("Failed to create directory: "+directoryName);
        }
    }

    public void removeDir(String parentAbsolutePath, String directoryName) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        File parentDirectory = newDirectory(parentAbsolutePath);
        File directory = new File(parentDirectory, directoryName);
        if (!directory.exists()) {
            throw new DoesNotExist("Directory does not exist");
        } else if (!directory.delete()) {
            throw new NoSuccess("Failed to remove directory: "+directoryName);
        }
    }

    public void rename(String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite) throws PermissionDenied, BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        File source = newEntry(sourceAbsolutePath);
        File target = newPath(targetAbsolutePath);
        if (!overwrite && target!=null && target.exists()) {
            throw new AlreadyExists("Target entry already exists");
        }
        source.renameTo(target);
    }

    ////////////////////////////////////// private methods //////////////////////////////////////

    private File newPath(String absolutePath) throws NoSuccess {
        if (s_isWindows) {
            if (m_drive != null) {
                return new File(m_drive+":"+absolutePath.replaceAll("%20", " "));
            } else if (absolutePath.matches("/+([A-Za-z]:).*") || absolutePath.startsWith("./")) {
                return new File(absolutePath.replaceAll("%20", " "));
            } else if (absolutePath.matches("/+")) {
                return null;
            } else {
                throw new NoSuccess("Absolute path in Windows must start with drive letter");
            }
        } else {
            return new File(absolutePath.replaceAll("%20", " "));
        }
    }

    private File newEntry(String absolutePath) throws DoesNotExist, NoSuccess {
        File entry = this.newPath(absolutePath);
        if (entry!=null && entry.exists()) {
            return entry;
        } else {
            throw new DoesNotExist("Entry does not exist");
        }
    }

    private File newFile(String absolutePath) throws BadParameter, DoesNotExist, NoSuccess {
        File entry = this.newEntry(absolutePath);
        if (entry.isFile()) {
            return entry;
        } else {
            throw new BadParameter("Entry is a directory: "+absolutePath);
        }
    }

    private File newDirectory(String absolutePath) throws BadParameter, DoesNotExist, NoSuccess {
        File entry = this.newEntry(absolutePath);
        if (entry.isDirectory()) {
            return entry;
        } else {
            throw new BadParameter("Entry is a file: "+absolutePath);
        }
    }
}
