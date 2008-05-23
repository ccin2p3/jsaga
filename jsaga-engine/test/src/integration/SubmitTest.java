package integration;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.jobcollection.*;
import junit.framework.TestCase;
import org.ogf.saga.URL;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.*;

import java.io.File;
import java.io.InputStream;

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

    public void test_fullprocess() throws Exception {
        super.checkSubmit(new URL[]{new URL("local:/")});
    }
}
