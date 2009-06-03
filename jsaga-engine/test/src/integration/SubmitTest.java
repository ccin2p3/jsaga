package integration;

import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;
import org.ogf.saga.file.File;
import org.ogf.saga.file.FileFactory;
import integration.abstracts.AbstractSubmitTest;

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
 * depends on module jsaga-adaptor-classic
 */
public class SubmitTest extends AbstractSubmitTest {
    public SubmitTest() throws Exception {
        super();
    }

    public void test_singlejob() throws Exception {
        super.checkSubmit(new URL[]{URLFactory.createURL("test://emulator")});
    }

    public void test_multijobs() throws Exception {
        super.checkSubmit(new URL[]{
                URLFactory.createURL("test://emulator"),
                URLFactory.createURL("test://emulator"),
                URLFactory.createURL("test://emulator")});
    }

    /**
     * fixme: requires plugin to support protocol "file"
     * fr.in2p3.jsaga.adaptor.job.local.LocalJobControlAdaptor#getSupportedSandboxProtocols() {return new String[]{"file"};}
     */
    public void test_staging() throws Exception {
//        super.checkSubmit(new URL[]{URLFactory.createURL("gatekeeper://localhost:2119/CN=Sylvain Reynaud/E=sreynaud@in2p3.fr")});
//        super.checkSubmit(new URL[]{URLFactory.createURL("ssh://localhost:22")});
        super.checkSubmit(new URL[]{URLFactory.createURL("local:/")});
        URL expected = URLFactory.createURL("file://./jsaga-engine/config/var/"+this.getName()+".txt");
        File file = FileFactory.createFile(expected);
        assertTrue(file.isEntry());
        assertTrue(file.getSize() > 0);
    }

    /** fixme: does not work [IncorrectState: Can not listen to job in 'New' state] */
    public void test_sandbox() throws Exception {
        super.checkSubmit(new URL[]{URLFactory.createURL("local:/")});
        URL expected = URLFactory.createURL("file://./jsaga-engine/config/var/"+this.getName()+".txt");
        File file = FileFactory.createFile(expected);
        assertTrue(file.isEntry());
        assertTrue(file.getSize() > 0);
    }
}
