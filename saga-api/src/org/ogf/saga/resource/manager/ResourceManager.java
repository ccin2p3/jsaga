package org.ogf.saga.resource.manager;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.description.NetworkDescription;
import org.ogf.saga.resource.description.ResourceDescription;
import org.ogf.saga.resource.description.StorageDescription;
import org.ogf.saga.resource.instance.Compute;
import org.ogf.saga.resource.instance.Network;
import org.ogf.saga.resource.instance.Resource;
import org.ogf.saga.resource.instance.Storage;
import org.ogf.saga.task.Async;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.util.List;

/**
 * The resource manager can translate resource requests into stateful resource handles.
 * It also manages the persistence of those resource handles, and of resource pools.
 */
public interface ResourceManager extends SagaObject, Async {
    /**
     * Obtains the list of pilot/vm/ar (etc.) instances that are currently
     * known to the resource manager.
     *
     * @param type
     *      the resource type (default = any)
     * @return
     *      a list of resource identifications.
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
    public List<String> listResources(Type type) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, TimeoutException,
            NoSuccessException;

    /**
     * Return resource handle for some known resource.
     * See drmaav2::machine_info? Add GLUE inspection as read-only attributes?
     * link to SD or ISN?
     *
     * @param id
     *      the resource identifier (can also be an old fashioned JobService URL)
     * @return
     *      the resource handle
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
    public Resource getResource(String id) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * List available templates.
     *
     * @param type
     *          the resource type (default = any)
     * @return the list of templates
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public List<String> listTemplates(Type type) throws NotImplementedException,
            TimeoutException, NoSuccessException;

    /**
     * Human readable description of template
     *
     * @param id
     *          the template identifier
     * @return the template description
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     * @exception BadParameterException
     *      is thrown when the resource manager cannot parse the template id.
     * @exception DoesNotExistException
     *      is thrown when the resource manager can handle the template id,
     *      but the referenced template is not found.
     */
    public ResourceDescription getTemplate(String id) throws NotImplementedException,
            BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException;

    //----------------------------------------------------------------

    /**
     * Acquire compute resource matching from requirements
     *
     * @param description
     *      the resource description
     * @return the resource handle
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
    public Compute acquireCompute(ComputeDescription description) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            TimeoutException, NoSuccessException;

    /**
     * Close compute resource, even if it is not drained.
     *
     * @param id
     *      the resource identifier
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
    public void releaseCompute(String id) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    /**
     * Close compute resource
     *
     * @param id
     *      the resource identifier
     * @param drain
     *      true if the resource must be drained, else false
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
    public void releaseCompute(String id, boolean drain) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    //----------------------------------------------------------------

    /**
     * Acquire network resource matching from requirements
     *
     * @param description
     *      the resource description
     * @return the resource handle
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
    public Network acquireNetwork(NetworkDescription description) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            TimeoutException, NoSuccessException;

    /**
     * Close network resource
     *
     * @param id
     *      the resource identifier
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
    public void releaseNetwork(String id) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    //----------------------------------------------------------------

    /**
     * Acquire storage resource matching from requirements
     *
     * @param description
     *      the resource description
     * @return the resource handle
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
    public Storage acquireStorage(StorageDescription description) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            TimeoutException, NoSuccessException;

    /**
     * Close storage resource
     *
     * @param id
     *      the resource identifier
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
    public void releaseStorage(String id) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException;

    //----------------------------------------------------------------

    /**
     * Creates a task that obtains the list of pilot/vm/ar (etc.) instances
     * that are currently known to the resource manager.
     *
     * @param mode
     *      the task mode.
     * @param type
     *      the resource type (default = any)
     * @return the task.
     * @exception NotImplementedException
     *      is thrown when the task version of this method is not implemented.
     */
    public Task<ResourceManager, List<String>> listResources(TaskMode mode, Type type)
            throws NotImplementedException;

    /**
     * Creates a task that returns resource handle for some known compute resource.
     *
     * @param mode
     *      the task mode.
     * @param id
     *      the resource identifier (can also be an old fashioned JobService URL)
     * @return the task.
     * @exception NotImplementedException
     *      is thrown when the task version of this method is not implemented.
     */
    public Task<ResourceManager, Resource> getResource(TaskMode mode, String id)
            throws NotImplementedException;

    /**
     * Creates a task that obtains the list of available templates.
     *
     * @param mode
     *      the task mode.
     * @param type
     *      the resource type (default = any)
     * @return the task.
     * @exception NotImplementedException
     *      is thrown when the task version of this method is not implemented.
     */
    public Task<ResourceManager, List<String>> listTemplates(TaskMode mode, Type type)
            throws NotImplementedException;

    /**
     * Creates a task that returns human readable description of template.
     *
     * @param mode
     *      the task mode.
     * @param id
     *      the template identifier
     * @return the task.
     * @exception NotImplementedException
     *      is thrown when the task version of this method is not implemented.
     */
    public Task<ResourceManager, ResourceDescription> getTemplate(TaskMode mode, String id)
            throws NotImplementedException;

    //----------------------------------------------------------------

    /**
     * Creates a task that acquires compute resource matching from requirements.
     *
     * @param mode
     *      the task mode.
     * @param description
     *      the resource description.
     * @return the task.
     * @exception NotImplementedException
     *      is thrown when the task version of this method is not implemented.
     */
    public Task<ResourceManager, Compute> acquireCompute(TaskMode mode, ComputeDescription description)
            throws NotImplementedException;

    /**
     * Creates a task that closes compute resource, even if it is not drained.
     *
     * @param mode
     *      the task mode.
     * @param id
     *      the resource identifier.
     * @return the task.
     * @exception NotImplementedException
     *      is thrown when the task version of this method is not implemented.
     */
    public Task<ResourceManager, Void> releaseCompute(TaskMode mode, String id)
            throws NotImplementedException;

    /**
     * Creates a task that closes compute resource.
     *
     * @param mode
     *      the task mode.
     * @param id
     *      the resource identifier.
     * @param drain
     *      true if the resource must be drained, else false
     * @return the task.
     * @exception NotImplementedException
     *      is thrown when the task version of this method is not implemented.
     */
    public Task<ResourceManager, Void> releaseCompute(TaskMode mode, String id, boolean drain)
            throws NotImplementedException;

    //----------------------------------------------------------------

    /**
     * Creates a task that acquires network resource matching from requirements.
     *
     * @param mode
     *      the task mode.
     * @param description
     *      the resource description
     * @return the task.
     * @exception NotImplementedException
     */
    public Task<ResourceManager, Network> acquireNetwork(TaskMode mode, NetworkDescription description)
            throws NotImplementedException;

    /**
     * Creates a task that closes network resource.
     *
     * @param mode
     *      the task mode.
     * @param id
     *      the resource identifier
     * @return the task.
     * @exception NotImplementedException
     */
    public Task<ResourceManager, Void> releaseNetwork(TaskMode mode, String id)
            throws NotImplementedException;

    //----------------------------------------------------------------

    /**
     * Creates a task that acquires storage resource matching from requirements.
     *
     * @param mode
     *      the task mode.
     * @param description
     *      the resource description
     * @return the task.
     * @exception NotImplementedException
     */
    public Task<ResourceManager, Storage> acquireStorage(TaskMode mode, StorageDescription description)
            throws NotImplementedException;

    /**
     * Creates a task that closes storage resource.
     *
     * @param mode
     *      the task mode.
     * @param id
     *      the resource identifier
     * @return the task.
     * @exception NotImplementedException
     */
    public Task<ResourceManager, Void> releaseStorage(TaskMode mode, String id)
            throws NotImplementedException;
}
