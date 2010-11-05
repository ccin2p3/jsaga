package fr.in2p3.jsaga.impl.url;

import junit.framework.TestCase;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   URLImplTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class URLImplTest extends TestCase {
    private static final String URL_SIMPLE = "uri://userinfo@host:1234/path?query=value#fragment";
    private static final String URL_SPACES = "uri://host:1234/path with spaces";

    public void test_remove() throws Exception {
        URL url;

        url = URLFactory.createURL(URL_SIMPLE);
        url.setString();
        assertEquals("", url.getString());

        url = URLFactory.createURL(URL_SIMPLE);
        url.setScheme();
        assertEquals("userinfo@host:1234/path?query=value#fragment", url.getString());

        url = URLFactory.createURL(URL_SIMPLE);
        url.setUserInfo();
        assertEquals("uri://host:1234/path?query=value#fragment", url.getString());

        url = URLFactory.createURL(URL_SIMPLE);
        url.setUserInfo();
        url.setPort();
        url.setHost();
        assertEquals("uri:///path?query=value#fragment", url.getString());

        url = URLFactory.createURL(URL_SIMPLE);
        url.setPort();
        assertEquals("uri://userinfo@host/path?query=value#fragment", url.getString());

        url = URLFactory.createURL(URL_SIMPLE);
        url.setPath();
        assertEquals("uri://userinfo@host:1234?query=value#fragment", url.getString());

        url = URLFactory.createURL(URL_SIMPLE);
        url.setQuery();
        assertEquals("uri://userinfo@host:1234/path#fragment", url.getString());

        url = URLFactory.createURL(URL_SIMPLE);
        url.setFragment();
        assertEquals("uri://userinfo@host:1234/path?query=value", url.getString());
    }

    public void test_replace() throws Exception {
        URL url;

        url = URLFactory.createURL(URL_SIMPLE);
        url.setString("NEW:///");
        assertEquals("NEW:///", url.getString());

        url = URLFactory.createURL(URL_SIMPLE);
        url.setScheme("NEW");
        assertEquals("NEW://userinfo@host:1234/path?query=value#fragment", url.getString());

        url = URLFactory.createURL(URL_SIMPLE);
        url.setUserInfo("NEW");
        assertEquals("uri://NEW@host:1234/path?query=value#fragment", url.getString());

        url = URLFactory.createURL(URL_SIMPLE);
        url.setHost("NEW");
        assertEquals("uri://userinfo@NEW:1234/path?query=value#fragment", url.getString());

        url = URLFactory.createURL(URL_SIMPLE);
        url.setPort(5678);
        assertEquals("uri://userinfo@host:5678/path?query=value#fragment", url.getString());

        url = URLFactory.createURL(URL_SIMPLE);
        url.setPath("/NEW");
        assertEquals("uri://userinfo@host:1234/NEW?query=value#fragment", url.getString());

        url = URLFactory.createURL(URL_SIMPLE);
        url.setQuery("NEW=new");
        assertEquals("uri://userinfo@host:1234/path?NEW=new#fragment", url.getString());

        url = URLFactory.createURL(URL_SIMPLE);
        url.setFragment("NEW");
        assertEquals("uri://userinfo@host:1234/path?query=value#NEW", url.getString());
    }

    public void test_getString() throws Exception {
        URL url = URLFactory.createURL(URL_SPACES);
        assertEquals("uri://host:1234/path with spaces", url.getString());
    }

    public void test_getEscaped() throws Exception {
        URL url = URLFactory.createURL(URL_SPACES);
        assertEquals("uri://host:1234/path%20with%20spaces", url.getEscaped());
    }
}
