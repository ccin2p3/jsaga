package fr.in2p3.commons.filesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   LibraryLoader
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   16 mai 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class LibraryLoader {
    public static void load(String libName) {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            libName += "-win32";
        } else if (osName.startsWith("Linux")) {
            libName += "-linux";
        } else {
            throw new RuntimeException("Unsupported OS: "+osName);
        }
        try {
            System.loadLibrary(libName);
        } catch (UnsatisfiedLinkError e) {
            File lib = findLibraryForTest(libName);
            System.load(lib.toString());
        }
    }

    private static File findLibraryForTest(String libName) {
        String file = LibraryLoader.class.getCanonicalName().replaceAll("\\.", "/")+".class";
        URL url = LibraryLoader.class.getClassLoader().getResource(file);
        File dir = new File(url.getPath().replaceAll(file, ""));
        File projectDir = dir.getParentFile().getParentFile().getParentFile();
        File lib = new File(new File(new File(projectDir, libName), "target"), System.mapLibraryName(libName));
        if (lib.exists()) {
            return lib;
        } else {
            throw new RuntimeException(new FileNotFoundException(lib.getAbsolutePath()));
        }
    }
}
