package integration;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformerFactory;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformerTest;
import junit.framework.TestCase;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobWrapperTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobWrapperTest extends TestCase {
    private String m_workDir;
    protected void setUp() {
        m_workDir = System.getProperty("user.dir");
        System.setProperty("user.dir", Base.JSAGA_HOME.getAbsolutePath());
    }
    protected void tearDown() {
        System.setProperty("user.dir", m_workDir);
    }

    public void test_wrapper_1_generate_Application() throws Exception {
        Source stylesheet = new StreamSource(XSLTransformerFactory.class.getClassLoader().getResourceAsStream("xsl/execution/wrapper_1-generate.xsl"));
        Source xml = new StreamSource(XSLTransformerTest.class.getClassLoader().getResourceAsStream("wrapper/test_Application.xml"));
        Result result = new StreamResult(System.out);
        TransformerFactory.newInstance().newTransformer(stylesheet).transform(xml, result);
    }

    public void test_wrapper_1_generate_DataStaging() throws Exception {
        Source stylesheet = new StreamSource(XSLTransformerFactory.class.getClassLoader().getResourceAsStream("xsl/execution/wrapper_1-generate.xsl"));
        Source xml = new StreamSource(XSLTransformerTest.class.getClassLoader().getResourceAsStream("wrapper/test_DataStaging.xml"));
        Result result = new StreamResult(System.out);
        TransformerFactory.newInstance().newTransformer(stylesheet).transform(xml, result);
    }

    public void test_wrapper_1_generate_URI() throws Exception {
        Source stylesheet = new StreamSource(XSLTransformerFactory.class.getClassLoader().getResourceAsStream("xsl/execution/wrapper_1-generate.xsl"));
        Source xml = new StreamSource(XSLTransformerTest.class.getClassLoader().getResourceAsStream("wrapper/test_URI.xml"));
        Result result = new StreamResult(System.out);
        TransformerFactory.newInstance().newTransformer(stylesheet).transform(xml, result);
    }
}
