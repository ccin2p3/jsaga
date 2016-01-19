package org.ogf.saga.resource.instance;

import org.ogf.saga.attributes.AsyncAttributes;
import org.ogf.saga.error.*;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.manager.ResourceManager;
import org.ogf.saga.resource.task.ResourceTask;
import org.ogf.saga.task.Async;

import java.util.List;

public interface Resource<R,RD> extends ResourceTask, Async, AsyncAttributes<R> {

    // Required attributes:

    /**
     * Attribute name: the resource type
     */
    public static final String RESOURCE_TYPE = "Type";

    /**
     * Attribute name: SAGA representation of the resource identifier
     */
    public static final String RESOURCE_ID = "ResourceID";

    /**
     * Attribute name: URL representation of the resource manager
     */
    public static final String MANAGER_ID = "ManagerID";

    /**
     * Attribute name: human readable description of the resource
     */
    public static final String RESOURCE_DESCRIPTION = "Description";

    /**
     * @return the resource type
     */
    public Type getType();

    /**
     * @return the resource manager
     */
    public ResourceManager getManager();

    /**
     * @return the list of access URLs
     */
    public List<String> getAccess();

    /**
     * Retrieves the resource description that was used to create this resource instance.
     *
     * @return
     *      the resource description
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception DoesNotExistException
     *      is thrown in cases where the resource description is not available,
     *      for instance when the resource was not created through SAGA and the
     *      resource was obtained using the ResourceManager.acquire() call.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public RD getDescription() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, DoesNotExistException,
            TimeoutException, NoSuccessException;

    /**
     * Reconfigure
     *
     * @param description
     *      the new resource description
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception BadParameterException
     *      is thrown when the resource description contains invalid values.
     */
    public void reconfigure(RD description) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            TimeoutException, NoSuccessException;

    /**
     * Release resource
     *
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception AuthorizationFailedException
     *      is thrown when none of the available contexts of the
     *      used session could be used for successful authorization.
     *      This error indicates that the resource could not be accessed
     *      at all, and not that an operation was not available due to
     *      restricted permissions.
     * @exception AuthenticationFailedException
     *      is thrown when operation failed because none of the available
     *      session contexts could successfully be used for authentication.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception BadParameterException
     *      is thrown when the resource manager cannot parse the resource id.
     * @exception DoesNotExistException
     *      is thrown when the resource manager can handle the resource id,
     *      but the referenced resource is not alive.
     */
    public void release() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, DoesNotExistException,
            TimeoutException, NoSuccessException;
}
