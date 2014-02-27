package fr.in2p3.jsaga.adaptor.wms.job;


import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DefaultJDLTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   12 oct. 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class DefaultJDLTest {
    @Test
    public void test_parse() throws Exception {
        InputStream jdl = DefaultJDLTest.class.getClassLoader().getResourceAsStream("etc/glite_wms.conf");
        Properties prop = new Properties();
        new DefaultJDL(jdl).fill(prop);
        prop.store(System.out, "JDL");
    }
}
