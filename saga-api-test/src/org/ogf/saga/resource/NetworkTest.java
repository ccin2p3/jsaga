package org.ogf.saga.resource;

import org.junit.Test;
import org.ogf.saga.resource.description.NetworkDescription;
import org.ogf.saga.resource.instance.Network;
import org.ogf.saga.resource.task.State;

public class NetworkTest extends ResourceBaseTest {

    public NetworkTest(String resourceprotocol) throws Exception {
        super(resourceprotocol);
    }
    protected Object[] typeToBeTested() {
        return new Object[][] {
                {Type.NETWORK}
        };
    }

    //////////////////
    // Network
    //////////////////
    @Test
    public void createAndDeleteNetwork() throws Exception {
        NetworkDescription nd = (NetworkDescription) ResourceFactory.createResourceDescription(Type.NETWORK);
        Network net = m_rm.acquireNetwork(nd);
        net.waitFor(120, State.ACTIVE);
        this.dumpResource(net);
        net.release();
    }
    

}
