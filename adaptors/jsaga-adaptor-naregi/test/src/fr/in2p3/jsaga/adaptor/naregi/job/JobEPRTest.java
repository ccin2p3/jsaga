package fr.in2p3.jsaga.adaptor.naregi.job;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobEPRTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobEPRTest extends TestCase {
    private static final String ID =
            "http://host119.ccvm.lan:8080/wsrf/services/BpelWFServiceContainer2/83e86d83-c319-47ca-9419-f40f3f81c6b9";
    private static final String EPR = "" +
            "<wsa:EndpointReference xmlns:wsa='http://schemas.xmlsoap.org/ws/2004/03/addressing'>\n" +
            "  <wsa:Address>http://host119.ccvm.lan:8080/wsrf/services/BpelWFServiceContainer2</wsa:Address>\n" +
            "  <wsa:ReferenceProperties xmlns:nrl-wsa='http://www.naregi.org/nrl/ws/addressing'>\n" +
            "    <nrl-wsa:resourceId>83e86d83-c319-47ca-9419-f40f3f81c6b9</nrl-wsa:resourceId>\n" +
            "  </wsa:ReferenceProperties>\n" +
            "</wsa:EndpointReference>";
    private static final String EPR2 = "" +
            "<ns1:EndpointReference xmlns:ns1=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\">\n" +
            " <ns1:Address xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xsi:type=\"xsd:anyURI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">http://host119.ccvm.lan:8080/wsrf/services/BpelWFServiceContainer2</ns1:Address>\n" +
            " <ns1:ReferenceProperties xsi:type=\"ns1:ReferencePropertiesType\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "  <nrl-wsa:resourceId xmlns:nrl-wsa=\"http://www.naregi.org/nrl/ws/addressing\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xsi:type=\"xsd:string\">83e86d83-c319-47ca-9419-f40f3f81c6b9</nrl-wsa:resourceId>\n" +
            " </ns1:ReferenceProperties>\n" +
            " <ns1:ReferenceParameters xsi:type=\"ns1:ReferenceParametersType\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>\n" +
            "</ns1:EndpointReference>";

    public void test_epr2id() throws Exception {
        JobEPR epr = new JobEPR(new ByteArrayInputStream(EPR.getBytes()));
        assertEquals(ID, epr.getJobId());
    }

    public void test_id2epr() throws Exception {
        JobEPR epr = new JobEPR(ID);
        assertEquals(EPR2, epr.getEPR());
    }
}
