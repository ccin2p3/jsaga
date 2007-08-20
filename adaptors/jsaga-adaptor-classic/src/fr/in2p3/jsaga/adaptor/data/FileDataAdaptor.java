package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.read.*;
import fr.in2p3.jsaga.adaptor.data.read.FileReader;
import fr.in2p3.jsaga.adaptor.data.write.*;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import org.ogf.saga.error.*;
import org.ogf.saga.permissions.Permission;

import java.io.*;

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
public class FileDataAdaptor implements FileReader, FileWriter, DirectoryReader, DirectoryWriter {
    public String[] getSupportedContextTypes() {
        return null;    // no context type
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        // do nothing
    }

    public String getScheme() {
        return "file";
    }

    public String[] getSchemeAliases() {
        return new String[]{"local"};
    }

    public void connect(String userInfo, String host, int port) throws AuthenticationFailed, AuthorizationFailed, Timeout, NoSuccess {
        // do nothing
    }

    public void disconnect() throws NoSuccess {
        // do nothing
    }

    public boolean exists(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        return new File(absolutePath).exists();
    }

    public boolean isDirectory(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        return newEntry(absolutePath).isDirectory();
    }

    public boolean isEntry(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        return newEntry(absolutePath).isFile();
    }

    public int getSize(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        return (int) newFile(absolutePath).length();
    }

    public FileReaderStream openFileReaderStream(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        try {
            return new LocalFileReaderStream(newFile(absolutePath));
        } catch (FileNotFoundException e) {
            throw new IncorrectState("File does not exist: "+absolutePath);
        }
    }

    public FileWriterStream openFileWriterStream(String parentAbsolutePath, String fileName, boolean overwrite, boolean append) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        File parentDirectory;
        try {
            parentDirectory = newDirectory(parentAbsolutePath);
        } catch (IncorrectState e) {
            throw new DoesNotExist("Parent directory does not exist: "+parentAbsolutePath);
        }
        File file = new File(parentDirectory, fileName);
        try {
            if (!file.createNewFile()) {
                if (append) {
                    return new LocalFileWriterStream(file, true);
                } else if (overwrite) {
                    return new LocalFileWriterStream(file, false);
                } else {
                    throw new AlreadyExists("File already exist: "+fileName);
                }
            } else {
                return new LocalFileWriterStream(file, false);
            }
        } catch (IOException e) {
            throw new NoSuccess("Failed to create file: "+fileName, e);
        }
    }

    public void removeFile(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        File file = newFile(absolutePath);
        if (!file.delete()) {
            throw new NoSuccess("Failed to remove file: "+absolutePath);
        }
    }

    public String[] list(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        File entry = newEntry(absolutePath);
        if (entry.isDirectory()) {
            return entry.list();
        } else {
            return new String[]{entry.getName()};
        }
    }

    public FileAttributes[] listAttributes(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        File entry = newEntry(absolutePath);
        File[] fileArray = entry.listFiles();
        FileAttributes[] attributeArray = new FileAttributes[fileArray.length];
        for (int i=0; i<fileArray.length; i++) {
            File file = fileArray[i];
            FileAttributes attr = new FileAttributes();
            // set attributes
            attr.name = file.getName();
            attr.size = (int) (file.isFile() ? file.length() : 0);
            attr.type = file.isDirectory()
                    ? FileAttributes.DIRECTORY_TYPE
                    : file.isFile()
                        ? FileAttributes.FILE_TYPE
                        : FileAttributes.UNKNOWN_TYPE;
            attr.permission = Permission.NONE;
            if(file.canRead()) attr.permission.or(Permission.READ);
            if(file.canWrite()) attr.permission.or(Permission.WRITE);
            // add to list
            attributeArray[i] = attr;
        }
        return attributeArray;
    }

    public void makeDir(String parentAbsolutePath, String directoryName) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        File parentDirectory;
        try {
            parentDirectory = newDirectory(parentAbsolutePath);
        } catch (IncorrectState e) {
            throw new DoesNotExist("Parent directory does not exist: "+parentAbsolutePath);
        }
        File directory = new File(parentDirectory, directoryName);
        if (directory.exists()) {
            throw new AlreadyExists("Directory already exist: "+directoryName);
        } else if (!directory.mkdir()) {
            throw new NoSuccess("Failed to create directory: "+directoryName);
        }
    }

    public void removeDir(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        File directory = newDirectory(absolutePath);
        if (!directory.delete()) {
            throw new NoSuccess("Failed to remove directory: "+absolutePath);
        }
    }

    private static File newEntry(String absolutePath) throws IncorrectState {
        File entry = new File(absolutePath);
        if (entry.exists()) {
            return entry;
        } else {
            throw new IncorrectState("Entry does not exist: "+absolutePath);
        }
    }

    private static File newFile(String absolutePath) throws BadParameter, IncorrectState {
        File entry = newEntry(absolutePath);
        if (entry.isFile()) {
            return entry;
        } else {
            throw new BadParameter("Entry is a directory: "+absolutePath);
        }
    }

    private static File newDirectory(String absolutePath) throws BadParameter, IncorrectState {
        File entry = newEntry(absolutePath);
        if (entry.isDirectory()) {
            return entry;
        } else {
            throw new BadParameter("Entry is a file: "+absolutePath);
        }
    }
}
