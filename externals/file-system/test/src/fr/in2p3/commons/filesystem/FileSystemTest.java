package fr.in2p3.commons.filesystem;

import junit.framework.TestCase;

import java.io.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   FileSystemTest
 * Author:
 * Date:
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class FileSystemTest extends TestCase {
    private File m_file;

    public void setUp() throws IOException {
        m_file = File.createTempFile("test", ".txt");
    }
    public void tearDown() throws IOException {
        if (! m_file.delete()) {
            throw new IOException("Failed to delete file: "+m_file);
        }
    }

    public void test_stat() throws FileNotFoundException {
        FileStat stat = new FileSystem().stat(m_file);
        System.out.println(stat);
    }
}
