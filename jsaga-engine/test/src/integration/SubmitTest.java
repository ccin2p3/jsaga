package integration;

import org.ogf.saga.URL;
import org.ogf.saga.file.File;
import org.ogf.saga.file.FileFactory;

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
//        super.checkSubmit(new URL[]{new URL("gatekeeper://localhost:2119/C=FR/ST=France/L=Villeurbanne/O=CNRS/OU=IN2P3/CN=Sylvain%20Reynaud/E=sreynaud@in2p3.fr")});
//        super.checkSubmit(new URL[]{new URL("ssh://localhost:22")});
        super.checkSubmit(new URL[]{new URL("local:/")});
        URL expected = new URL("file://./jsaga-engine/config/var/"+this.getName()+".txt");
        File file = FileFactory.createFile(expected);
        assertTrue(file.isEntry());
        assertTrue(file.getSize() > 0);
    }

    public void test_sandbox() throws Exception {
        super.checkSubmit(new URL[]{new URL("local:/")});
        URL expected = new URL("file://./jsaga-engine/config/var/"+this.getName()+".txt");
        File file = FileFactory.createFile(expected);
        assertTrue(file.isEntry());
        assertTrue(file.getSize() > 0);
    }
}
