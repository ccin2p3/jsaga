package fr.in2p3.commons.filesystem;

import java.io.File;
import java.io.FileNotFoundException;

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
    //private native int symlink(String filename, String linkname);
    private native void symlink(String filename, String linkname) throws FileSystemException ;
    //private native int chown(String filename, String user, String group);
    private native void chown(String filename, String user, String group) throws FileSystemException;
    //private native int getgrouplist(String user, String[] groups);
    private native String[] getgrouplist(String user) throws FileSystemException ;
    
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
    public void symlink(File file, String link) throws FileSystemException {
    	this.symlink(file.getAbsolutePath(),link);
    }

    public void chown(File file, String user) throws FileSystemException {
    	chown(file, user, null);
    }

    public void chgrp(File file, String group) throws FileSystemException {
    	chown(file, null, group);
    }

    public void chown(File file, String user, String group) throws FileSystemException {
    	this.chown(file.getAbsolutePath(), user!=null?user:"", group!=null?group:"");
    }
    
    public String[] getUserGroups(String user) throws FileSystemException {
		return this.getgrouplist(user);
    }
}
