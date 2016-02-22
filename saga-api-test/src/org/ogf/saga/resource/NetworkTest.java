package org.ogf.saga.resource;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.resource.description.NetworkDescription;
import org.ogf.saga.resource.description.ResourceDescription;
import org.ogf.saga.resource.instance.Resource;

public abstract class NetworkTest extends ResourceBaseTest {

    public NetworkTest(String resourceprotocol) throws Exception {
        super(resourceprotocol, Type.NETWORK);
    }
    @Override
    protected Resource acquire(ResourceDescription rd) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        return m_rm.acquireNetwork((NetworkDescription) rd);
    }

}
