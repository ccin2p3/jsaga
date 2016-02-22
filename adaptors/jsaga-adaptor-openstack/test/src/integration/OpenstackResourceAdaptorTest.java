package integration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ogf.saga.resource.ComputeTest;
import org.ogf.saga.resource.NetworkTest;
import org.ogf.saga.resource.StorageTest;

@RunWith(Suite.class)
@SuiteClasses({
    OpenstackResourceAdaptorTest.OpenstackServerTest.class
    ,OpenstackResourceAdaptorTest.OpenstackSwiftContainerTest.class
    ,OpenstackResourceAdaptorTest.OpenstackNeuroneTest.class
})

public class OpenstackResourceAdaptorTest {
    public static class OpenstackServerTest extends ComputeTest {
        public OpenstackServerTest() throws Exception { super("openstack");}
    }
    
    public static class OpenstackSwiftContainerTest extends StorageTest {
        public OpenstackSwiftContainerTest() throws Exception { super("openstack");}
    }
    
    public static class OpenstackNeuroneTest extends NetworkTest {
        public OpenstackNeuroneTest() throws Exception { super("openstack");}
    }
}
