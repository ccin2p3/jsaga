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

    public native void intArray(int arr[]);
    public native void stringArray(String arr[]);

    public FileStat stat(File file) throws FileNotFoundException {
        FileStat stat = new FileStat(file.getName());
        if (! this.stat(file.getAbsolutePath(), stat)) {
            throw new FileNotFoundException("File not found: "+file);
        }
        return stat;
    }
}
