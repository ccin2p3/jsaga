package integration;

import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.AbstractTest_JUNIT4;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.job.run.MinimalTest;
import org.ogf.saga.job.run.OptionalTest;
import org.ogf.saga.job.run.RequiredTest;
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
@RunWith(Suite.class)
@SuiteClasses({
    BesTestSuite.BesJobRunRequiredTest.class,
    BesTestSuite.BesJobRunOptionalTest.class
})
public class BesTestSuite {

    public static class BesConnectionTest extends AbstractTest_JUNIT4 {
        public BesConnectionTest() throws Exception {super();}
   
        @Test
        public void test_AREX_Nordugrid() throws Exception {
            System.setProperty("javax.net.ssl.keyStore", "/home/schwarz/.jsaga/keystoreGrid2.jks");
            System.setProperty("javax.net.ssl.trustStore", "/home/schwarz/.jsaga/truststore.jks");
            this.test_BES("bes://interop.grid.niif.hu:2010/arex-x509");
        }
        
        @Test
        public void test_GenesisII_Futuregrid_India() throws Exception {
            System.setProperty("javax.net.ssl.keyStore", "/home/schwarz/.jsaga/keystoreGrid2.jks");
            System.setProperty("javax.net.ssl.trustStore", "/home/schwarz/.jsaga/truststore.jks");
            this.test_BES("bes://i134r.idp.iu.futuregrid.org:18443/axis/services/GeniiBESPortType?genii-container-id=ECBCAEC8-5FFF-11E0-B887-28C73890A7D4");
        }
        
        @Test
        public void test_GenesisII_Futuregrid_Camillus() throws Exception {
            System.setProperty("javax.net.ssl.keyStore", "/home/schwarz/.jsaga/keystoreGrid2.jks");
            System.setProperty("javax.net.ssl.trustStore", "/home/schwarz/.jsaga/truststore.jks");
            this.test_BES("bes://camillus.cs.virginia.edu:18445/axis/services/GeniiBESPortType?genii-container-id=D384C93A-86A5-EC0F-CCF3-3040B22C8582");
        }
        
        @Test
        public void test_GenesisII_Futuregrid_Alamo() throws Exception {
            // resource 40D784B8-9611-D69E-929E-9EE716D5783E is unknown
            System.setProperty("javax.net.ssl.keyStore", "/home/schwarz/.jsaga/keystoreGrid2.jks");
            System.setProperty("javax.net.ssl.trustStore", "/home/schwarz/.jsaga/truststore.jks");
            this.test_BES("bes://129.114.32.10:18443/axis/services/GeniiBESPortType?genii-container-id=8B3AC57-D09F-63F1-6EE6-2664E1EF9699");
        }

//        public void test_GenesisII_Futuregrid_Sierra() throws Exception {
//            // resource 9BC75AB7-41C5-DF7F-905D-90058E35D2FB is unknown
//            this.test_BES("bes://s79r.idp.sdsc.futuregrid.org:18443/axis/services/GeniiBESPortType?genii-container-id=D3C0D562-DB2A-7650-1799-63AB627860A9");
//        }
        @Test
        public void test_GenesisII_Futuregrid_XCGServer1() throws Exception {
            System.setProperty("javax.net.ssl.keyStore", "/home/schwarz/.jsaga/keystoreGrid2.jks");
            System.setProperty("javax.net.ssl.trustStore", "/home/schwarz/.jsaga/truststore.jks");
            this.test_BES("bes://xcg-server1.uvacse.virginia.edu:20443/axis/services/GeniiBESPortType?genii-container-id=93B641B7-9422-EA4C-A90B-CA6A9D98E344");
        }
        
        

        @Test
        public void test_Unicore6_local() throws Exception {
            System.setProperty("javax.net.ssl.keyStore", "/home/schwarz/.jsaga/contexts/unicore6/demouser.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "the!user");
            System.setProperty("javax.net.ssl.trustStore", "/home/schwarz/.jsaga/truststore.jks");
            this.test_BES("bes://localhost6:8080/DEMO-SITE/services/BESFactory?res=default_bes_factory");
        }
        
//        public void test_Unicore6_Futuregrid_India() throws Exception {
//            // PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
//            this.test_BES("bes://i134r.idp.iu.futuregrid.org:8080/DEMO-SITE/services/BESFactory?res=default_bes_factory");
//        }
        
// This endpoint does not answer anymore
//        public void test_Unicore6_Futuregrid_Sierra() throws Exception {
//            //PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
//            this.test_BES("bes://s79r.idp.sdsc.futuregrid.org:8080/DEMO-SITE/services/BESFactory?res=default_bes_factory");
//        }
        
        // This endpoint does not answer anymore
//        public void test_Cream_Infn() throws Exception {
//            // PKIX path validation failed: java.security.cert.CertPathValidatorException: timestamp check failed
//            this.test_BES("bes://omii002.cnaf.infn.it:8443/ce-cream/services/CreamBes");
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
    public static class BesJobRunMinimalTest extends MinimalTest {
        public BesJobRunMinimalTest() throws Exception {super("bes");}
    }
    
    // test cases
    public static class BesJobRunRequiredTest extends RequiredTest {
        public BesJobRunRequiredTest() throws Exception {super("bes");}
        @Override @Test @Ignore("Not supported")
        public void test_run_error() {}
        @Override @Test @Ignore("Not supported")
        public void test_cancel_running() {}
    }
    
    // test cases
    public static class BesJobRunOptionalTest extends OptionalTest {
        public BesJobRunOptionalTest() throws Exception {super("bes");}
        @Override @Test @Ignore("Not supported")
        public void test_resume_done() {}
        @Override @Test @Ignore("Not supported")
        public void test_resume_running() {}
        @Override @Test @Ignore("Not supported")
        public void test_suspend_done() {}
        @Override @Test @Ignore("Not supported")
        public void test_suspend_running() {}
    }
    
}