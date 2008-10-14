package fr.in2p3.jsaga.helpers;

import junit.framework.TestCase;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   EntryPathTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class EntryPathTest extends TestCase {
    private static final String DIR_PATH = "path/to//directory///";
    private static final String FILE_PATH = "path/to//file.txt";
    private static final String DIR = "directory///";
    private static final String FILE = "file.txt";

    private static final String EXPECTED_BASE_DIR = "path/to//";
    private static final String EXPECTED_DIR_NAME = "directory";
    private static final String EXPECTED_FILE_NAME = "file.txt";

    public void test_absolute_dir_path() {
        EntryPath path = new EntryPath("/"+DIR_PATH);
        assertEquals("/"+EXPECTED_BASE_DIR, path.getBaseDir());
        assertEquals(EXPECTED_DIR_NAME, path.getEntryName());
    }

    public void test_absolute_file_path() {
        EntryPath path = new EntryPath("/"+FILE_PATH);
        assertEquals("/"+EXPECTED_BASE_DIR, path.getBaseDir());
        assertEquals(EXPECTED_FILE_NAME, path.getEntryName());
    }

    public void test_absolute_dir() {
        EntryPath path = new EntryPath("/"+DIR);
        assertEquals("/", path.getBaseDir());
        assertEquals(EXPECTED_DIR_NAME, path.getEntryName());
    }

    public void test_absolute_file() {
        EntryPath path = new EntryPath("/"+FILE);
        assertEquals("/", path.getBaseDir());
        assertEquals(EXPECTED_FILE_NAME, path.getEntryName());
    }

    public void test_relative_dir_path() {
        EntryPath path = new EntryPath(DIR_PATH);
        assertEquals(EXPECTED_BASE_DIR, path.getBaseDir());
        assertEquals(EXPECTED_DIR_NAME, path.getEntryName());
    }

    public void test_relative_file_path() {
        EntryPath path = new EntryPath(FILE_PATH);
        assertEquals(EXPECTED_BASE_DIR, path.getBaseDir());
        assertEquals(EXPECTED_FILE_NAME, path.getEntryName());
    }

    public void test_relative_dir() {
        EntryPath path = new EntryPath(DIR);
        assertEquals("", path.getBaseDir());
        assertEquals(EXPECTED_DIR_NAME, path.getEntryName());
    }

    public void test_relative_file() {
        EntryPath path = new EntryPath(FILE);
        assertEquals("", path.getBaseDir());
        assertEquals(EXPECTED_FILE_NAME, path.getEntryName());
    }
}
