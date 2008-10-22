package fr.in2p3.jsaga;

import junit.framework.TestCase;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   uriTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   1 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class uriTest extends TestCase {
    public void test_basedirPath() throws Exception {
        assertEquals(
                "/path/to/",
                uri.basedirPath("http://host:1234/path/to/file.txt"));
        assertEquals(
                "/path/to/",
                uri.basedirPath("http://host:1234/path/to/directory/"));
        assertEquals(
                "/C:/Program Files/app/",
                uri.basedirPath("file:///C:/Program Files/app/file.dll"));
        assertEquals(
                "/",
                uri.basedirPath("gsiftp://host:2811/"));
        assertEquals(
                "/",
                uri.basedirPath("gsiftp://host:2811"));
    }

    public void test_filename() throws Exception {
        assertEquals(
                "file.txt",
                uri.filename("http://host:1234/path/to/file.txt"));
        assertEquals(
                "directory",
                uri.filename("http://host:1234/path/to/directory/"));
        assertEquals(
                "file.dll",
                uri.filename("file:///C:/Program Files/app/file.dll"));
        assertEquals(
                "",
                uri.filename("gsiftp://host:2811/"));
        assertEquals(
                "",
                uri.filename("gsiftp://host:2811"));
    }
}
