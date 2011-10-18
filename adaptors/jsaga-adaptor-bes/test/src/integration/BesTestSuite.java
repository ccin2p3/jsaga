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