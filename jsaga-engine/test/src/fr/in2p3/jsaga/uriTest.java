package fr.in2p3.jsaga;

import org.junit.Assert;
import org.junit.Test;


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
public class uriTest {
    @Test
    public void test_basedirPath() throws Exception {
        Assert.assertEquals(
                "/path/to/",
                uri.basedirPath("http://host:1234/path/to/file.txt"));
        Assert.assertEquals(
                "/path/to/",
                uri.basedirPath("http://host:1234/path/to/directory/"));
        Assert.assertEquals(
                "/C:/Program Files/app/",
                uri.basedirPath("file:///C:/Program Files/app/file.dll"));
        Assert.assertEquals(
                "/",
                uri.basedirPath("gsiftp://host:2811/"));
        Assert.assertEquals(
                "/",
                uri.basedirPath("gsiftp://host:2811"));
    }

    @Test
    public void test_filename() throws Exception {
        Assert.assertEquals(
                "file.txt",
                uri.filename("http://host:1234/path/to/file.txt"));
        Assert.assertEquals(
                "directory",
                uri.filename("http://host:1234/path/to/directory/"));
        Assert.assertEquals(
                "file.dll",
                uri.filename("file:///C:/Program Files/app/file.dll"));
        Assert.assertEquals(
                "",
                uri.filename("gsiftp://host:2811/"));
        Assert.assertEquals(
                "",
                uri.filename("gsiftp://host:2811"));
    }
}
