package fr.in2p3.jsaga.impl.url;

import junit.framework.TestCase;

import java.io.IOException;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   UniversalFileTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class UniversalFileTest extends TestCase {
    private UniversalFile m_winAbsolute;
    private UniversalFile m_absolute;
    private UniversalFile m_relative;
    private UniversalFile m_directory;

    public void setUp() {
        m_winAbsolute = new UniversalFile("C:/path/to/file");
        m_absolute = new UniversalFile("/path/to/file");
        m_relative = new UniversalFile("path/to/file");
        m_directory = new UniversalFile("/dir/");
    }

    public void test_getPath() {
        assertEquals("C:/path/to/file", m_winAbsolute.getPath());
        assertEquals("/path/to/file", m_absolute.getPath());
        assertEquals("path/to/file", m_relative.getPath());
        assertEquals("/dir/", m_directory.getPath());
        assertEquals("/path/to/file", new UniversalFile("//path/to/file").getPath());
        assertEquals("/path/to/file", new UniversalFile("///////path/to/file").getPath());
    }

    public void test_getParent() {
        assertEquals("C:/path/to/", m_winAbsolute.getParent());
        assertEquals("/path/to/", m_absolute.getParent());
        assertEquals("path/to/", m_relative.getParent());
        assertEquals("/", m_directory.getParent());
        assertEquals("/", new UniversalFile("/file").getParent());
        assertEquals("./", new UniversalFile("file").getParent());
    }

    public void test_getCanonicalPath() throws IOException {
        assertEquals("C:/path/to/file", m_winAbsolute.getCanonicalPath());
        assertEquals("/path/to/file", m_absolute.getCanonicalPath());
        assertEquals("path/to/file", m_relative.getCanonicalPath());
        assertEquals("/dir/", m_directory.getCanonicalPath());
    }

    public void test_isAbsolute() {
        assertEquals(System.getProperty("os.name").startsWith("Windows"), m_winAbsolute.isAbsolute());
        assertTrue(m_absolute.isAbsolute());
        assertFalse(m_relative.isAbsolute());
        assertTrue(m_directory.isAbsolute());
    }

    public void test_isDirectory() {
        assertFalse(m_winAbsolute.isDirectory());
        assertFalse(m_absolute.isDirectory());
        assertFalse(m_relative.isDirectory());
        assertTrue(m_directory.isDirectory());
    }
}
