package org.ogf.saga.resource.instance;

import org.ogf.saga.attributes.AsyncAttributes;
import org.ogf.saga.error.*;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.manager.ResourceManager;
import org.ogf.saga.resource.task.ResourceTask;
import org.ogf.saga.task.Async;

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
     * Attribute name: list of access URLs
     */
    public static final String ACCESS = "Access";

    /**
     * Attribute name: human readable description of the resource
     */
    public static final String RESOURCE_DESCRIPTION = "Description";

    /**
     * @return the resource identifier
     *
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public String getResourceId() throws NotImplementedException, NoSuccessException;

    /**
     * @return the resource type
     *
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public Type getType() throws NotImplementedException, NoSuccessException;

    /**
     * @return the resource manager
     *
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public ResourceManager getManager() throws NotImplementedException, NoSuccessException;

    /**
     * @return the list of access URLs
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
     */
    public String[] getAccess() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, TimeoutException,
            NoSuccessException;

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
     * @exception IncorrectStateException
     *      is thrown when the resource is in such a state that it cannot be released
     * @exception DoesNotExistException
     *      is thrown when the resource is not alive.
     */
    public void release() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, DoesNotExistException,
            TimeoutException, IncorrectStateException, NoSuccessException;
}
