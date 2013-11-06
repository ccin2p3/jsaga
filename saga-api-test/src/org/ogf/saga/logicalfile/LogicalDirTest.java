package org.ogf.saga.logicalfile;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSDirectory;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.namespace.base.DirBaseTest;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LogicalDirTest
* Author: lionel.schwarz@in2p3.fr
* Date:   5 NOV 2013
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class LogicalDirTest extends DirBaseTest {
    protected LogicalDirTest(String protocol) throws Exception {
        super(protocol);
    }

    @Test
    public void test_open() throws Exception {
        NSDirectory dir = m_subDir.openDir(URLFactory.createURL(".."), Flags.NONE.getValue());
        Assert.assertTrue(
                dir instanceof LogicalDirectory);
        Assert.assertEquals(
                DEFAULT_DIRNAME,
                dir.getName().getPath()+"/");
        NSEntry entry = m_subDir.open(URLFactory.createURL(DEFAULT_FILENAME), Flags.NONE.getValue());
        Assert.assertTrue(
                entry instanceof LogicalFile);
        Assert.assertEquals(
                DEFAULT_FILENAME,
                entry.getName().getPath());
    }

    @Override
    @Test
    public void test_find() throws Exception {
        if (m_subDir instanceof LogicalDirectory) {
            List<URL> list = ((LogicalDirectory) m_subDir).find(DEFAULT_FILEPATTERN, null, Flags.NONE.getValue());
            Assert.assertEquals(
                    1,
                    list.size());
            Assert.assertEquals(
                    DEFAULT_FILENAME,
                    list.get(0).toString());
        } else {
        	Assert.fail("Not an instance of class: LogicalDirectory");
        }
    }

    @Test
    public void test_find_norecurse() throws Exception {
        if (m_dir instanceof LogicalDirectory) {
            List<URL> list = ((LogicalDirectory) m_dir).find(DEFAULT_FILEPATTERN, null, Flags.NONE.getValue());
            Assert.assertEquals(
                    0,
                    list.size());
        } else {
        	Assert.fail("Not an instance of class: LogicalDirectory");
        }
    }

    @Override
    @Test
    public void test_find_recurse() throws Exception {
        if (m_dir instanceof LogicalDirectory) {
            List<URL> list = ((LogicalDirectory) m_dir).find(DEFAULT_FILEPATTERN, null, Flags.RECURSIVE.getValue());
            Assert.assertEquals(
                    1,
                    list.size());
            Assert.assertEquals(
                    DEFAULT_SUBDIRNAME +DEFAULT_FILENAME,
                    list.get(0).toString());
        } else {
        	Assert.fail("Not an instance of class: LogicalDirectory");
        }
    }

    @Test
    public void test_isFile() throws Exception {
        if (m_subDir instanceof LogicalDirectory) {
        	Assert.assertFalse(((LogicalDirectory) m_subDir).isFile(URLFactory.createURL("..")));
        	Assert.assertTrue(((LogicalDirectory) m_subDir).isFile(URLFactory.createURL(DEFAULT_FILENAME)));
        } else {
        	Assert.fail("Not an instance of class: LogicalDirectory");
        }
    }
    
}
