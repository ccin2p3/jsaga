package fr.in2p3.jsaga.adaptor.wms.job;

import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   StagingJDLTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 avr. 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class StagingJDLTest {
    private static String EXPECTED_INPUT[] = {
        "C:/DOCUME~1/SYLVAI~1/LOCALS~1/Temp/local-165e18fe-0884-4e34-b09c-eee48517e7b8.sh -> gsiftp://lapp-wms02.in2p3.fr:2811/tmp/1272544850703/worker-165e18fe-0884-4e34-b09c-eee48517e7b8.sh (false)",
        "C:/DOCUME~1/SYLVAI~1/LOCALS~1/Temp/local-165e18fe-0884-4e34-b09c-eee48517e7b8.input -> gsiftp://lapp-wms02.in2p3.fr:2811/tmp/1272544850703/worker-165e18fe-0884-4e34-b09c-eee48517e7b8.input (false)"
    };

    @Test
    public void test_parse() throws Exception {
        // parse
        InputStream stream = StagingJDLTest.class.getClassLoader().getResourceAsStream("jdl-test.txt");
        StagingJDL jdl = new StagingJDL(stream);

        // extract info
        StagingTransfer[] input = jdl.getInputStagingTransfer("gsiftp://lapp-wms02.in2p3.fr:2811/tmp/1272544850703");
        for (int i=0; i<input.length; i++) {
            Assert.assertEquals(EXPECTED_INPUT[i], toString(input[i]));
        }
    }

    private static String toString(StagingTransfer transfer) {
        return transfer.getFrom()+" -> "+transfer.getTo()+" ("+transfer.isAppend()+")";
    }
}
