package integration;

import org.apache.log4j.Logger;
import org.ogf.saga.resource.ResourceBaseTest;
import org.ogf.saga.resource.Type;


public class OpenstackResourceAdaptorTest extends ResourceBaseTest {

    private Logger m_logger = Logger.getLogger(this.getClass());

    public OpenstackResourceAdaptorTest() throws Exception {
        super("openstack");
    }

    @Override
    protected Object[] typeToBeTested() {
        return new Object[][] {
                {Type.COMPUTE},
                {Type.STORAGE}
        };
    }

}
