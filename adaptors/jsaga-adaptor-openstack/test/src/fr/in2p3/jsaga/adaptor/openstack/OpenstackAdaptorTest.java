package fr.in2p3.jsaga.adaptor.openstack;

import java.util.Map;

import org.junit.Test;
import org.ogf.saga.JSAGABaseTest;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.openstack.OpenstackAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.openstack.security.OpenstackSecurityCredential;

public class OpenstackAdaptorTest extends JSAGABaseTest {

    public OpenstackAdaptorTest() throws Exception {
        super();
    }

    @Test
    public void connect() throws NotImplementedException, AuthenticationFailedException, 
                AuthorizationFailedException, IncorrectURLException, BadParameterException, 
                TimeoutException, NoSuccessException {
        OpenstackAdaptorAbstract adaptor = new OpenstackAdaptorAbstract() {

            @Override
            public Usage getUsage() {
                return null;
            }

            @Override
            public Default[] getDefaults(Map attributes) throws IncorrectStateException {
                return null;
            }
        };
        adaptor.setSecurityCredential(new OpenstackSecurityCredential("schwarz", "6505e3d8-b40a-4305-aecd-d25c614d1e72", "ccin2p3"));
        adaptor.connect(null, "cckeystone.in2p3.fr", 5000, null, null);
    }
}
