package fr.in2p3.commons.filesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   FileSystem
 * Author:
 * Date:
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class FileSystem {
    static {
        LibraryLoader.load("file-system");
    }

    private native boolean stat(String path, FileStat stat);
    private native boolean chmod(String path, int user_perms, int group_perms, int other_perms);
    private native int symlink(String filename, String linkname);
    private native int chown(String filename, String user, String group);
    private static final int PERMISSIONDENIED = -1;
    private static final int FILEDOESNOTEXIST = -2;
    private static final int FILEALREADYEXISTS = -3;
    private static final int INTERNALERROR = -4;
    private static final int USERDOESNOTEXIST = -5;
    private static final int GROUPDOESNOTEXIST = -6;
    
    public native void intArray(int arr[]);
    public native void stringArray(String arr[]);

    public FileStat stat(File file) throws FileNotFoundException {
        FileStat stat = new FileStat(file.getName());
        if (! this.stat(file.getAbsolutePath(), stat)) {
            throw new FileNotFoundException("File not found: "+file);
        }
        return stat;
    }
    
    public void chmod(File file, int user_perms, int group_perms, int other_perms) throws FileNotFoundException {
        if (! this.chmod(file.getAbsolutePath(), user_perms, group_perms, other_perms)) {
            throw new FileNotFoundException("File not found: "+file);
        }
    }
    public void symlink(File file, String link) throws PermissionDeniedException, FileNotFoundException, FileAlreadyExistsException, IOException {
    	int ret = this.symlink(file.getAbsolutePath(),link);
    	if (ret == PERMISSIONDENIED) {
    		throw new PermissionDeniedException();
    	}
    	if (ret == FILEDOESNOTEXIST) {
    		throw new FileNotFoundException();
    	}
    	if (ret == FILEALREADYEXISTS) {
    		throw new FileAlreadyExistsException();
    	}
    	if (ret == INTERNALERROR) {
    		throw new IOException();
    	}
    }

    public void chown(File file, String user) throws PermissionDeniedException, FileNotFoundException, UserNotFoundException, IOException {
    	chown(file, user, null);
    }

    public void chgrp(File file, String group) throws PermissionDeniedException, FileNotFoundException, GroupNotFoundException, IOException {
    	chown(file, null, group);
    }

    public void chown(File file, String user, String group) throws PermissionDeniedException, FileNotFoundException, UserNotFoundException, GroupNotFoundException, IOException {
    	int ret = this.chown(file.getAbsolutePath(), user!=null?user:"", group!=null?group:"");
    	if (ret == PERMISSIONDENIED) {
    		throw new PermissionDeniedException();
    	}
    	if (ret == FILEDOESNOTEXIST) {
    		throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
    	}
    	if (ret == USERDOESNOTEXIST) {
    		throw new UserNotFoundException("User not found: " + user);
    	}
    	if (ret == GROUPDOESNOTEXIST) {
    		throw new GroupNotFoundException("Group not found: " + group);
    	}
    	if (ret == INTERNALERROR) {
    		throw new IOException();
    	}
    }
}
