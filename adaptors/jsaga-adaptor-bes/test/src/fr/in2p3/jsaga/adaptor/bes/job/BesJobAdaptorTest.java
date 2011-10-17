package fr.in2p3.jsaga.adaptor.bes.job;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.ogf.saga.AbstractTest;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;
import org.xml.sax.SAXException;

import fr.in2p3.jsaga.adaptor.bes.BesUtils;
import fr.in2p3.jsaga.engine.descriptors.AdaptorDescriptors;
import fr.in2p3.jsaga.engine.factories.JobAdaptorFactory;
import fr.in2p3.jsaga.generated.org.w3.x2005.x08.addressing.EndpointReferenceType;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import fr.in2p3.jsaga.impl.session.SessionImpl;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   BatchSSHJobAdaptorTest
 * Author:
 * Date:
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class BesJobAdaptorTest extends AbstractTest {
	
	public BesJobAdaptorTest() throws Exception {
		super();
	}

	private final String ACTIVITY_IDENTIFIER = 
		"<ns2:EndpointReferenceType xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ns2:EndpointReferenceType\" xmlns:ns2=\"http://www.w3.org/2005/08/addressing\">" +
		"<ns2:Address xsi:type=\"ns2:AttributedURIType\">https://somehost:8443/BESservice</ns2:Address>" +
		"<ns2:ReferenceParameters xsi:type=\"ns2:ReferenceParametersType\">" +
		"<ns7:resource-key xmlns:ns7=\"http://edu.virginia.vcgr.genii/ref-params\">71E2C50E-0CF4-79FC-2A5F-6578990CD3C0</ns7:resource-key>" +
		"</ns2:ReferenceParameters>" +
		"<ns2:Metadata xsi:type=\"ns2:MetadataType\">" +
		"<ns9:WSResourceInterfaces xmlns:ns9=\"http://schemas.ggf.org/ogsa/2006/05/wsrf-bp\">3000200a120420f</ns9:WSResourceInterfaces>" +
		"<ns10:EndpointIdentifier xmlns:ns10=\"http://schemas.ggf.org/naming/2006/03/naming\">urn:ws-naming:epi:228854DE-EEDF-8B01-9B8A-F30E98C03C6B</ns10:EndpointIdentifier>" +
		"<ns11:SecurityTokenReference xmlns:ns11=\"http://docs.oasis-open.org/wss/2005/xx/oasis-2005xx-wss-wssecurity-secext-1.1.xsd\">\n" +
		"   <ns11:Embedded>\n" +
		"    <ns12:BinarySecurityToken ValueType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509PKIPathv1\" ns13:Id=\"RecipientMessageIdentity\" xmlns:ns12=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:ns13=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">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</ns12:BinarySecurityToken>\n" +
		"   </ns11:Embedded>\n" +
		"  </ns11:SecurityTokenReference>" +
		"<ns14:PolicyAttachment xmlns:ns14=\"http://www.w3.org/ns/ws-policy\">\n" +
		"   <ns14:AppliesTo>\n" +
		"    <ns14:URI>urn:wsaaction:*</ns14:URI>\n" +
		"   </ns14:AppliesTo>\n" +
		"   <ns14:Policy>\n" +
		"    <ns14:PolicyReference URI=\"http://www.ogf.org/ogsa/2007/05/secure-communication#ServerTLS\"/>\n" +
		"    <ns14:PolicyReference ns14:Optional=\"true\" URI=\"http://www.ogf.org/ogsa/2007/05/secure-communication#UsernameToken\"/>\n" +
		"   </ns14:Policy>\n" +
		"  </ns14:PolicyAttachment>" +
		"<ns15:container-id xmlns:ns15=\"http://vcgr.cs.virginia.edu/Genesis-II\">ECBCAEC8-5FFF-11E0-B887-28C73890A7D4</ns15:container-id>" +
		"</ns2:Metadata>" +
		"</ns2:EndpointReferenceType>";

    public void test_getScheme() {
        assertEquals(
                "bes",
                new BesJobControlAdaptor().getType());
    }

    public void test_AREX() throws NoSuccessException {
    	this.test_BES("bes://interop.grid.niif.hu:2010/arex-x509");
    }
    public void test_GenesisII() throws NoSuccessException {
    	this.test_BES("bes://i134r.idp.iu.futuregrid.org:18443/axis/services/GeniiBESPortType?genii-container-id=ECBCAEC8-5FFF-11E0-B887-28C73890A7D4");
    }
    public void test_Unicore6() throws NoSuccessException {
    	this.test_BES("bes://localhost6:8080/DEMO-SITE/services/BESFactory?res=default_bes_factory");
    }
    
    /**
     * This test needs 3 system properties:
     * -Djavax.net.ssl.keyStorePassword=
	 * -Djavax.net.ssl.keyStore=
	 * -Djavax.net.ssl.trustStore=
     * @throws NoSuccessException
     */
    private void test_BES(String bes_url) throws NoSuccessException {
        AdaptorDescriptors descriptors = AdaptorDescriptors.getInstance();
        JobAdaptorFactory m_jobAdaptorFactory = new JobAdaptorFactory(descriptors);
    	Session session = SessionFactory.createSession();
    	BesJobControlAdaptor adaptor;

        URL url;
        ContextImpl context;
        Map attributes;
        
        // connect to control services
    	try {
			url = URLFactory.createURL(bes_url);
			
			context = ((SessionImpl)session).getBestMatchingContext(url);

	        attributes = m_jobAdaptorFactory.getAttribute(url, context);
	        adaptor = new BesJobControlAdaptor();
	        m_jobAdaptorFactory.connect(url, adaptor, attributes, context);
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
		assertTrue(adaptor.getBESAttributes().isIsAcceptingNewActivities());
		adaptor.disconnect();
    }
    
    public void test_jobSerialize() throws NoSuccessException, NoSuchAlgorithmException, SAXException {
    	EndpointReferenceType epr = (EndpointReferenceType) BesUtils.deserialize(ACTIVITY_IDENTIFIER, EndpointReferenceType.class);

    	// serialization of the reference job identity
    	BesJob job = new BesJob();
    	job.setActivityId(epr, true);
		File xmlJob = job.getXmlJob();
		assertTrue(xmlJob.exists());
		
		// deserialization of the result
		BesJob job_deserialized = new BesJob();
		job_deserialized.setNativeId(job.getNativeId());
		EndpointReferenceType epr_deserialized = job_deserialized.getActivityIdentifier();
		assertEquals(epr, epr_deserialized);

		// clean and check that file is removed
		xmlJob.delete();
		assertFalse(xmlJob.exists());
    }
}
