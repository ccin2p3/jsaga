package jar.data;

import fr.in2p3.jsaga.adaptor.ourgrid.data.OurGridDataAdaptor;
import fr.in2p3.jsaga.adaptor.ourgrid.job.OurGridJobControlAdaptor;
import fr.in2p3.jsaga.adaptor.ourgrid.security.OurGridSecurityAdaptor;
import junit.framework.TestCase;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   MyProtocolDataAdaptorTest
 * Author:
 * Date:
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class OurGridDataAdaptorTest extends TestCase {
    public void test_getScheme() {
        assertEquals("ourgrid",new OurGridJobControlAdaptor().getType());
        assertEquals("ourgrid",new OurGridDataAdaptor().getType());
        assertEquals("ourgrid",new OurGridSecurityAdaptor().getType());
    }
}
