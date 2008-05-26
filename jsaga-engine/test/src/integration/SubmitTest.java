package integration;

import org.ogf.saga.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SubmitTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SubmitTest extends AbstractSubmitTest {
    public SubmitTest() throws Exception {
        super();
    }

    public void test_singlejob() throws Exception {
        super.checkSubmit(new URL[]{new URL("test://emulator")});
    }

    public void test_multijobs() throws Exception {
        super.checkSubmit(new URL[]{new URL("test://emulator"), new URL("test://emulator"), new URL("test://emulator")});
    }

    public void test_staging() throws Exception {
        super.checkSubmit(new URL[]{new URL("local:/")});
    }

    public void test_sandbox() throws Exception {
        super.checkSubmit(new URL[]{new URL("local:/")});
    }
}
