package org.ogf.saga.resource.instance;

import org.ogf.saga.error.*;
import org.ogf.saga.resource.description.ComputeDescription;

public interface Compute extends Resource<Compute, ComputeDescription> {
    /**
     * Release resource
     *
     * @param drain
     *      if true, then the resource is drained before it is released (default = false)
     */
    public void release(boolean drain) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, DoesNotExistException,
            TimeoutException, NoSuccessException;
}
