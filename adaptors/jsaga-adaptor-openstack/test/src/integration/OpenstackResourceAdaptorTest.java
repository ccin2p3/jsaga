package integration;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.resource.ResourceBaseTest;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.instance.Network;
import org.ogf.saga.resource.instance.Storage;


public class OpenstackResourceAdaptorTest extends ResourceBaseTest {

    private Logger m_logger = Logger.getLogger(this.getClass());

    public OpenstackResourceAdaptorTest() throws Exception {
        super("openstack");
    }

}
