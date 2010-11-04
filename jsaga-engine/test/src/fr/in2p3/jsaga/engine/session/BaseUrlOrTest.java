package fr.in2p3.jsaga.engine.session;

import fr.in2p3.jsaga.impl.context.ContextImpl;
import junit.framework.TestCase;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   BaseUrlOrTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class BaseUrlOrTest extends TestCase {
    public void test_or_items() throws Exception {
        String url = "{gsiftp,gridftp->gsiftp}://{cc,*host*}*.{in2p3.fr,*de}/*/{dteam,lhc*}";
        String[] expected = new String[]{
                "gsiftp://cc*.in2p3.fr/*/dteam",
                "gsiftp://cc*.in2p3.fr/*/lhc*",
                "gsiftp://cc*.*de/*/dteam",
                "gsiftp://cc*.*de/*/lhc*",
                "gsiftp://*host*.in2p3.fr/*/dteam",
                "gsiftp://*host*.in2p3.fr/*/lhc*",
                "gsiftp://*host*.*de/*/dteam",
                "gsiftp://*host*.*de/*/lhc*",
                "gridftp->gsiftp://cc*.in2p3.fr/*/dteam",
                "gridftp->gsiftp://cc*.in2p3.fr/*/lhc*",
                "gridftp->gsiftp://cc*.*de/*/dteam",
                "gridftp->gsiftp://cc*.*de/*/lhc*",
                "gridftp->gsiftp://*host*.in2p3.fr/*/dteam",
                "gridftp->gsiftp://*host*.in2p3.fr/*/lhc*",
                "gridftp->gsiftp://*host*.*de/*/dteam",
                "gridftp->gsiftp://*host*.*de/*/lhc*"};
        assertEquals(expected, split(url));
    }

    public void test_or_groups() throws Exception {
        String url = "gsiftp://{cc*.in2p3.fr,*host*.*de}/{*/dteam,pnfs/lhc*}";
        String[] expected = new String[]{
                "gsiftp://cc*.in2p3.fr/*/dteam",
                "gsiftp://cc*.in2p3.fr/pnfs/lhc*",
                "gsiftp://*host*.*de/*/dteam",
                "gsiftp://*host*.*de/pnfs/lhc*"};
        assertEquals(expected, split(url));
    }

    private static String[] split(String url) throws Exception {
        Context context = ContextFactory.createContext("VOMS");
        context.setVectorAttribute(ContextImpl.BASE_URL_INCLUDES, new String[]{url});
        return context.getVectorAttribute(ContextImpl.BASE_URL_INCLUDES);
    }

    private static void dump(String[] array) {
        for (String s : array) {
            System.out.println("                \""+s+"\",");
        }
    }

    private static void assertEquals(String[] expected, String[] array) {
        assertEquals("Unexpected array length", expected.length, array.length);
        for (int i=0; i<array.length; i++) {
            assertEquals(expected[i], array[i]);
        }
    }
}
