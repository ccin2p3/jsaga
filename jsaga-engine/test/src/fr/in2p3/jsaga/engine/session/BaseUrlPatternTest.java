package fr.in2p3.jsaga.engine.session;

import org.junit.Assert;
import org.junit.Test;

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
public class BaseUrlPatternTest {
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

    @Test
    public void test_toString() {
        Assert.assertEquals("gridftp->gsiftp://cc*.*in2p3.fr/*/dteam", URL1.toString());
        Assert.assertEquals("gridftp->gsiftp://cc*.*in2p3.fr[:2811]/*/dteam", URL2.toString());
        Assert.assertEquals("gridftp->gsiftp://*:1234", URL3.toString());
        Assert.assertEquals("gridftp->gsiftp://cc*", URL4.toString());
        Assert.assertEquals("gridftp->gsiftp", URL5.toString());
    }

    @Test
    public void test_toRegExp() {
        System.out.println(URL1.toRegExp().toString());
        System.out.println(URL2.toRegExp().toString());
        System.out.println(URL3.toRegExp().toString());
        System.out.println(URL4.toRegExp().toString());
        System.out.println(URL5.toRegExp().toString());
    }

    @Test
    public void test_matches() {
        Assert.assertTrue(URL1.matches("gridftp://cclcgvmli07.in2p3.fr:2811/pnfs/dteam"));
        Assert.assertFalse(URL1.matches("gsiftp://cclcgvmli07.in2p3.fr:2811/pnfs/dteam"));
        Assert.assertFalse(URL1.matches("gridftp://licclcgvmli07.in2p3.fr/pnfs/dteam"));
        Assert.assertFalse(URL1.matches("gridftp://cclcgvmli07.in2p3.fr/dteam"));

        Assert.assertTrue(URL2.matches("gridftp://cclcgvmli07.in2p3.fr:2811/pnfs/dteam"));
        Assert.assertTrue(URL2.matches("gridftp://cclcgvmli07.in2p3.fr/pnfs/dteam"));
        Assert.assertFalse(URL2.matches("gridftp://cclcgvmli07.in2p3.fr:1111/pnfs/dteam"));

        Assert.assertTrue(URL3.matches("gridftp://cclcgvmli07.in2p3.fr:1234/pnfs/dteam"));
        Assert.assertFalse(URL3.matches("gridftp://cclcgvmli07.in2p3.fr/pnfs/dteam"));

        Assert.assertTrue(URL4.matches("gridftp://cclcgvmli07.in2p3.fr/pnfs/dteam"));
        Assert.assertFalse(URL4.matches("gridftp://lcgvmli07.in2p3.fr/pnfs/dteam"));

        Assert.assertTrue(URL5.matches("gridftp://cclcgvmli07.in2p3.fr/pnfs/dteam"));
        Assert.assertFalse(URL5.matches("ridftp://cclcgvmli07.in2p3.fr/pnfs/dteam"));
    }

    @Test
    public void test_conflictsWith() {
        Assert.assertTrue(URL1.conflictsWith(URL2));
        Assert.assertTrue(URL1.conflictsWith(URL3));
        Assert.assertFalse(URL2.conflictsWith(URL3));
    }

    @Test
    public void test_conflictsWith_more() {
        BaseUrlPattern url = new BaseUrlPattern(SCHEME, HOST, DOMAIN, new PortItem(), new DirItem(), new DirItem("dteam", "*", null));
        Assert.assertTrue(url.conflictsWith(
                new BaseUrlPattern(SCHEME, HOST, DOMAIN, new PortItem(), new DirItem(), new DirItem("myvo", null, "*"))));
        Assert.assertTrue(url.conflictsWith(
                new BaseUrlPattern(SCHEME, HOST, DOMAIN, new PortItem(), new DirItem(), new DirItem("eam", "*", null))));
        Assert.assertFalse(url.conflictsWith(
                new BaseUrlPattern(SCHEME, HOST, DOMAIN, new PortItem(), new DirItem(), new DirItem("tea", "*", null))));
    }
}
