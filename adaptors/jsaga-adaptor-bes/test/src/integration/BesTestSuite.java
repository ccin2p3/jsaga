package integration;

import java.util.Map;

import junit.framework.Test;

import org.ogf.saga.AbstractTest;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.job.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import fr.in2p3.jsaga.adaptor.bes.job.BesJobControlAdaptor;
import fr.in2p3.jsaga.engine.descriptors.AdaptorDescriptors;
import fr.in2p3.jsaga.engine.factories.JobAdaptorFactory;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import fr.in2p3.jsaga.impl.session.SessionImpl;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesTestSuite
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   23 Nov. 2010
***************************************************
* Description:                                      */
/**
 *
 */
public class BesTestSuite extends JSAGATestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new BesTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(BesTestSuite.class);}}

    public static class BesConnectionTest extends AbstractTest {
    	public BesConnectionTest() throws Exception {super();}
   
    	public void test_AREX_Nordugrid() throws Exception {
        	this.test_BES("bes://interop.grid.niif.hu:2010/arex-x509");
        }
        
    	public void test_GenesisII_Futuregrid_India() throws Exception {
        	this.test_BES("bes://i134r.idp.iu.futuregrid.org:18443/axis/services/GeniiBESPortType?genii-container-id=ECBCAEC8-5FFF-11E0-B887-28C73890A7D4");
        }
        
    	public void test_GenesisII_Futuregrid_Alamo() throws Exception {
        	// resource 40D784B8-9611-D69E-929E-9EE716D5783E is unknown
        	this.test_BES("bes://129.114.32.10:18443/axis/services/GeniiBESPortType?genii-container-id=98B3AC57-D09F-63F1-6EE6-2664E1EF9699");
        }

//        public void test_GenesisII_Futuregrid_Sierra() throws Exception {
//        	// resource 9BC75AB7-41C5-DF7F-905D-90058E35D2FB is unknown
//        	this.test_BES("bes://s79r.idp.sdsc.futuregrid.org:18443/axis/services/GeniiBESPortType?genii-container-id=D3C0D562-DB2A-7650-1799-63AB627860A9");
//        }

    	public void test_Unicore6_local() throws Exception {
        	this.test_BES("bes://localhost6:8080/DEMO-SITE/services/BESFactory?res=default_bes_factory");
        }
    	
//        public void test_Unicore6_Futuregrid_India() throws Exception {
//        	// PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
//        	this.test_BES("bes://i134r.idp.iu.futuregrid.org:8080/DEMO-SITE/services/BESFactory?res=default_bes_factory");
//        }
    	
// This endpoint does not answer anymore
//        public void test_Unicore6_Futuregrid_Sierra() throws Exception {
//        	//PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
//        	this.test_BES("bes://s79r.idp.sdsc.futuregrid.org:8080/DEMO-SITE/services/BESFactory?res=default_bes_factory");
//        }
    	
    	// This endpoint does not answer anymore
//        public void test_Cream_Infn() throws Exception {
//        	// PKIX path validation failed: java.security.cert.CertPathValidatorException: timestamp check failed
//        	this.test_BES("bes://omii002.cnaf.infn.it:8443/ce-cream/services/CreamBes");
//        }
        
        	
        /**
         * This test needs 3 system properties:
         * -Djavax.net.ssl.keyStorePassword=
    	 * -Djavax.net.ssl.keyStore=
    	 * -Djavax.net.ssl.trustStore=
         * @throws NoSuccessException
         */
        private void test_BES(String bes_url) throws Exception {
            AdaptorDescriptors descriptors = AdaptorDescriptors.getInstance();
            JobAdaptorFactory m_jobAdaptorFactory = new JobAdaptorFactory(descriptors);
        	Session session = SessionFactory.createSession();
        	BesJobControlAdaptor adaptor;

            // connect to control services
			URL url = URLFactory.createURL(bes_url);
			ContextImpl context = ((SessionImpl)session).getBestMatchingContext(url);
			Map attributes = m_jobAdaptorFactory.getAttribute(url, context);
	        adaptor = new BesJobControlAdaptor();
	        m_jobAdaptorFactory.connect(url, adaptor, attributes, context);

    		assertTrue(adaptor.getBESAttributes().isIsAcceptingNewActivities());
    		adaptor.disconnect();
        }
        
    }
    
    // test cases
    public static class BesJobRunMinimalTest extends JobRunMinimalTest {
        public BesJobRunMinimalTest() throws Exception {super("bes");}
    }
    
    // test cases
    public static class BesJobRunRequiredTest extends JobRunRequiredTest {
        public BesJobRunRequiredTest() throws Exception {super("bes");}
        public void test_run_error() { super.ignore("return code not supported"); }
        public void test_cancel_running() { super.ignore("not supported"); }
    }
    
    // test cases
    public static class BesJobRunOptionalTest extends JobRunOptionalTest {
        public BesJobRunOptionalTest() throws Exception {super("bes");}
        public void test_resume_done() { super.ignore("not supported"); }
        public void test_resume_running() { super.ignore("not supported"); }
        public void test_suspend_done() { super.ignore("not supported"); }
        public void test_suspend_running() { super.ignore("not supported"); }
    }
    
}