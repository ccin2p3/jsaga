package fr.in2p3.jsaga.engine.session;

import fr.in2p3.jsaga.engine.session.item.*;
import junit.framework.TestCase;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   BaseUrlPatternTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class BaseUrlPatternTest extends TestCase {
    private static final BaseUrlItem SCHEME = new SchemeItem("gsiftp", "gridftp");
    private static final BaseUrlItem HOST = new HostItem("cc", null, "*");
    private static final BaseUrlItem DOMAIN = new DomainItem("in2p3.fr", "*");
    private static final BaseUrlItem PORT_DEFAULT = new PortItem("2811", PortItem.OPTIONAL);
    private static final BaseUrlItem PORT = new PortItem("1234", PortItem.REQUIRED);
    private static final BaseUrlItem DIR = new DirItem("dteam", null, null);

    private static final BaseUrlPattern URL1 = new BaseUrlPattern(SCHEME, HOST, DOMAIN, new PortItem(), new DirItem(), DIR);
    private static final BaseUrlPattern URL2 = new BaseUrlPattern(SCHEME, HOST, DOMAIN, PORT_DEFAULT, new DirItem(), DIR);
    private static final BaseUrlPattern URL3 = new BaseUrlPattern(SCHEME, new HostItem(), new DomainItem(), PORT);
    private static final BaseUrlPattern URL4 = new BaseUrlPattern(SCHEME, HOST);
    private static final BaseUrlPattern URL5 = new BaseUrlPattern(SCHEME);

    public void test_toString() {
        assertEquals("gridftp->gsiftp://cc*.*in2p3.fr/*/dteam", URL1.toString());
        assertEquals("gridftp->gsiftp://cc*.*in2p3.fr[:2811]/*/dteam", URL2.toString());
        assertEquals("gridftp->gsiftp://*:1234", URL3.toString());
        assertEquals("gridftp->gsiftp://cc*", URL4.toString());
        assertEquals("gridftp->gsiftp", URL5.toString());
    }

    public void test_toRegExp() {
        System.out.println(URL1.toRegExp().toString());
        System.out.println(URL2.toRegExp().toString());
        System.out.println(URL3.toRegExp().toString());
        System.out.println(URL4.toRegExp().toString());
        System.out.println(URL5.toRegExp().toString());
    }

    public void test_matches() {
        assertTrue(URL1.matches("gridftp://cclcgvmli07.in2p3.fr:2811/pnfs/dteam"));
        assertFalse(URL1.matches("gsiftp://cclcgvmli07.in2p3.fr:2811/pnfs/dteam"));
        assertFalse(URL1.matches("gridftp://licclcgvmli07.in2p3.fr/pnfs/dteam"));
        assertFalse(URL1.matches("gridftp://cclcgvmli07.in2p3.fr/dteam"));

        assertTrue(URL2.matches("gridftp://cclcgvmli07.in2p3.fr:2811/pnfs/dteam"));
        assertTrue(URL2.matches("gridftp://cclcgvmli07.in2p3.fr/pnfs/dteam"));
        assertFalse(URL2.matches("gridftp://cclcgvmli07.in2p3.fr:1111/pnfs/dteam"));

        assertTrue(URL3.matches("gridftp://cclcgvmli07.in2p3.fr:1234/pnfs/dteam"));
        assertFalse(URL3.matches("gridftp://cclcgvmli07.in2p3.fr/pnfs/dteam"));

        assertTrue(URL4.matches("gridftp://cclcgvmli07.in2p3.fr/pnfs/dteam"));
        assertFalse(URL4.matches("gridftp://lcgvmli07.in2p3.fr/pnfs/dteam"));

        assertTrue(URL5.matches("gridftp://cclcgvmli07.in2p3.fr/pnfs/dteam"));
        assertFalse(URL5.matches("ridftp://cclcgvmli07.in2p3.fr/pnfs/dteam"));
    }

    public void test_conflictsWith() {
        assertTrue(URL1.conflictsWith(URL2));
        assertTrue(URL1.conflictsWith(URL3));
        assertFalse(URL2.conflictsWith(URL3));
    }

    public void test_conflictsWith_more() {
        BaseUrlPattern url = new BaseUrlPattern(SCHEME, HOST, DOMAIN, new PortItem(), new DirItem(), new DirItem("dteam", "*", null));
        assertTrue(url.conflictsWith(
                new BaseUrlPattern(SCHEME, HOST, DOMAIN, new PortItem(), new DirItem(), new DirItem("myvo", null, "*"))));
        assertTrue(url.conflictsWith(
                new BaseUrlPattern(SCHEME, HOST, DOMAIN, new PortItem(), new DirItem(), new DirItem("eam", "*", null))));
        assertFalse(url.conflictsWith(
                new BaseUrlPattern(SCHEME, HOST, DOMAIN, new PortItem(), new DirItem(), new DirItem("tea", "*", null))));
    }
}
