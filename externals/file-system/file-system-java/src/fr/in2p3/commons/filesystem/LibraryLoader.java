package fr.in2p3.commons.filesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        File lib;
        if ("jar".equals(url.getProtocol())) {
            String path = decode(url.getPath().replaceAll("file:", ""));
            File jar = new File(path.replaceAll("!/"+file, ""));
            if (! jar.exists()) {
                throw new RuntimeException(new FileNotFoundException(jar.getAbsolutePath()));
            }
            String parentDirName = jar.getParentFile().getName();
            if ("lib".equals(parentDirName) || "lib-adaptors".equals(parentDirName)) {
                // find library in directory lib/ (for runtime)
                String version = extractVersion(jar.getName());
                File dir = jar.getParentFile();
                lib = new File(dir, System.mapLibraryName(libName+"-"+version));
            } else {
                // find library in maven repository (for testing from maven)
                File projectDir = jar.getParentFile().getParentFile().getParentFile();
                String version = jar.getParentFile().getName();
                File dir = new File(new File(projectDir, libName), version);
                lib = new File(dir, System.mapLibraryName(libName+"-"+version));
            }
        } else {
            // find library in project target directory (for testing from IDE)
            String path = decode(url.getPath());
            File classesDir = new File(path.replaceAll(file, ""));
            if (! classesDir.exists()) {
                throw new RuntimeException(new FileNotFoundException(classesDir.getAbsolutePath()));
            }
            File projectDir = classesDir.getParentFile().getParentFile().getParentFile();
            File dir = new File(new File(projectDir, libName), "target");
            lib = new File(dir, System.mapLibraryName(libName));
        }
        if (! lib.exists()) {
            throw new RuntimeException(new FileNotFoundException(lib.getAbsolutePath()));
        }
        return lib;
    }

    private static String decode(String path) {
        return path.replaceAll("%20", " ");
    }
    private static String extractVersion(String jarName) {
        Pattern pattern = Pattern.compile(".*-([0-9\\.]+[0-9](?:-SNAPSHOT)?).*");
        Matcher matcher = pattern.matcher(jarName);
        if (matcher.matches() && matcher.groupCount()==1) {
            return matcher.group(1);
        } else {
            throw new RuntimeException("Failed to extract version from: "+jarName);
        }
    }
}
