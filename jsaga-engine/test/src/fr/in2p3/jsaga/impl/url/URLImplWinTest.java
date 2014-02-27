package fr.in2p3.jsaga.impl.url;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   URLImplTest
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   1 fev 2011
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class URLImplWinTest extends Assert {
    
    protected static String _abs_path;
    protected static String _file;
    protected static String _rel_path;
    protected static String _file_encoded;
    
    public URLImplWinTest() throws Exception {
        super();
    }

    @BeforeClass
    public static void setUp() throws Exception {
        _abs_path = "C:/path/";
        _rel_path = "relpath/";
        _file = "file with# and{}and%and[]and?end";
        _file_encoded = "file%20with%23%20and%7B%7Dand%25and%5B%5Dand%3Fend";
    }

    @Test
    public void test_antislash()  throws Exception {
        URL url = URLFactory.createURL("c:\\path");
        // backslash should be changed in slash
        assertEquals("c:/path", url.getString());
    }
    
    @Test
    public void test_replace() throws Exception {
        URL url;
        String newWinFile = "e:/data/file.txt";
        
        url = URLFactory.createURL(_abs_path+_file);
        url.setPath(newWinFile);
        assertEquals(newWinFile, url.getString());
        
        url = URLFactory.createURL("file:/"+_abs_path+_file);
        url.setPath(newWinFile);
        assertEquals("file:/" + newWinFile, url.getString());
        assertEquals("/"+newWinFile, url.getPath());

        url = URLFactory.createURL("file:///"+_abs_path+_file);
        url.setPath(newWinFile);
        assertEquals("file:/" + newWinFile, url.getString());
        assertEquals("/"+newWinFile, url.getPath());

    
        url = URLFactory.createURL("file:/"+_abs_path+_file);
        url.setPath("////" + newWinFile);
        assertEquals("file:/" + newWinFile, url.getString());
        assertEquals("/"+newWinFile, url.getPath());
    }
    
    @Test(expected=BadParameterException.class)
    public void test_relative() throws Exception {
        URL url = URLFactory.createURL("uri://host/path");
        url.setString(_abs_path+_file);
    }
    
    @Test
    public void test_specialChars() throws Exception {
        URL url = URLFactory.createURL(_file);
        // # and other chars should not be considered as special characters
        assertEquals(_file, url.getPath());
        
    }

    @Test
    public void test_getEscaped() throws Exception {
        URL url = URLFactory.createURL("file:/"+_abs_path+_file);
        assertEquals("file:/"+_abs_path+_file_encoded, url.getEscaped());
    }
    
    @Test
    public void test_isabsolute() throws Exception {
        URL url = URLFactory.createURL(_abs_path+_file);
        assertFalse(url.isAbsolute());
    }

    @Test
    public void test_normalize() throws Exception {
        URL url, normalized;
        
        url = URLFactory.createURL(_abs_path + "./dummy/../" + _file);
        normalized = url.normalize();
        assertEquals(_abs_path+_file, normalized.getString());
        
        url = URLFactory.createURL(_rel_path + "./dummy/../" + _file);
        normalized = url.normalize();
        assertEquals(_rel_path+_file, normalized.getString());
    }
    
    @Test
    public void test_resolve() throws Exception {
        URL url;
        URL resolved;
        
        url = URLFactory.createURL(_abs_path);
        resolved = url.resolve(URLFactory.createURL(_file));
        assertEquals(_abs_path+_file, resolved.getString());
        
        url = URLFactory.createURL(_rel_path);
        resolved = url.resolve(URLFactory.createURL(_file));
        assertEquals(_rel_path+_file,resolved.getString());
        
        url = URLFactory.createURL(_rel_path+"file.txt");
        resolved = url.resolve(URLFactory.createURL(_file));
        assertEquals(_rel_path+_file,resolved.getString());
        
    }
    
    @Test
    public void test_query_fragment() throws Exception {
        URL url;
        url = URLFactory.createURL(_abs_path+_file);
        url.setQuery("query=value");
        assertEquals("query=value", url.getQuery());
        url = URLFactory.createURL(_abs_path+_file);
        url.setFragment("fragment");
        assertEquals("fragment", url.getFragment());
    }
    
}
