package integration;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   RootIndexTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   31 mars 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class RootIndexTest extends TestCase {
    public void test_index() throws Exception {
        final String pkgName = "integration";

        // list class files
        URL resource = RootIndexTest.class.getClassLoader().getResource(pkgName);
        if(resource==null) throw new Exception("Package not found: "+pkgName);
        File directory = new File(resource.getFile());
        String[] classFiles = directory.list(new FilenameFilter(){
            public boolean accept(File dir, String name) {
                return !name.contains("$") && name.endsWith(".class");
            }
        });

        // filter test suites
        for (int i=0; i<classFiles.length; i++) {
            String className = pkgName+'.'+classFiles[i].substring(0,classFiles[i].length()-6);
            Class clazz = Class.forName(className);
            if (extendsClass(clazz, TestSuite.class)) {
                System.out.println(clazz.getName());
            }
        }
    }

    private static boolean extendsClass(Class clazz, Class extended) {
        Class extd = clazz.getSuperclass();
        if (extd != null) {
            if (extd.equals(extended)) {
                return true;
            } else {
                return extendsClass(extd, extended);
            }
        } else {
            return false;
        }
    }
}
