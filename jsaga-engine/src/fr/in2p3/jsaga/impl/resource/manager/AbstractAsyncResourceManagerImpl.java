package fr.in2p3.jsaga.impl.resource.manager;

import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;
import fr.in2p3.jsaga.impl.task.AbstractThreadedTask;
import org.ogf.saga.error.*;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.description.NetworkDescription;
import org.ogf.saga.resource.description.ResourceDescription;
import org.ogf.saga.resource.description.StorageDescription;
import org.ogf.saga.resource.instance.Compute;
import org.ogf.saga.resource.instance.Network;
import org.ogf.saga.resource.instance.Storage;
import org.ogf.saga.resource.manager.ResourceManager;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

import java.util.List;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public abstract class AbstractAsyncResourceManagerImpl extends AbstractSyncResourceManagerImpl implements ResourceManager {
    /** constructor */
    public AbstractAsyncResourceManagerImpl(Session session, URL rm, ResourceAdaptor adaptor) {
        super(session, rm, adaptor);
    }

    public Task<ResourceManager, List<String>> listResources(TaskMode mode, final Type type) throws NotImplementedException {
        return new AbstractThreadedTask<ResourceManager,List<String>>(mode) {
            public List<String> invoke() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, TimeoutException, NoSuccessException {
                return AbstractAsyncResourceManagerImpl.super.listResourcesSync(type);
            }
        };
    }

    public Task<ResourceManager, List<String>> listTemplates(TaskMode mode, final Type type) throws NotImplementedException {
        return new AbstractThreadedTask<ResourceManager, List<String>>(mode) {
            public List<String> invoke() throws NotImplementedException, TimeoutException, NoSuccessException {
                return AbstractAsyncResourceManagerImpl.super.listTemplatesSync(type);
            }
        };
    }
    public Task<ResourceManager, ResourceDescription> getTemplate(TaskMode mode, final String id) throws NotImplementedException {
        return new AbstractThreadedTask<ResourceManager, ResourceDescription>(mode) {
            public ResourceDescription invoke() throws NotImplementedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncResourceManagerImpl.super.getTemplateSync(id);
            }
        };
    }

    //----------------------------------------------------------------

    public Task<ResourceManager, Compute> acquireCompute(TaskMode mode, final ComputeDescription description) throws NotImplementedException {
        return new AbstractThreadedTask<ResourceManager,Compute>(mode) {
            public Compute invoke() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
                return AbstractAsyncResourceManagerImpl.super.acquireComputeSync(description);
            }
        };
    }
    public Task<ResourceManager, Compute> acquireCompute(TaskMode mode, final String id) throws NotImplementedException {
        return new AbstractThreadedTask<ResourceManager,Compute>(mode) {
            public Compute invoke() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncResourceManagerImpl.super.acquireComputeSync(id);
            }
        };
    }
    public Task<ResourceManager, Void> releaseCompute(TaskMode mode, final String id) throws NotImplementedException {
        return new AbstractThreadedTask<ResourceManager, Void>(mode) {
            public Void invoke() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncResourceManagerImpl.super.releaseComputeSync(id);
                return null;
            }
        };
    }
    public Task<ResourceManager, Void> releaseCompute(TaskMode mode, final String id, final boolean drain) throws NotImplementedException {
        return new AbstractThreadedTask<ResourceManager, Void>(mode) {
            public Void invoke() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncResourceManagerImpl.super.releaseComputeSync(id, drain);
                return null;
            }
        };
    }

    //----------------------------------------------------------------

    public Task<ResourceManager, Network> acquireNetwork(TaskMode mode, final NetworkDescription description) throws NotImplementedException {
        return new AbstractThreadedTask<ResourceManager, Network>(mode) {
            public Network invoke() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
                return AbstractAsyncResourceManagerImpl.super.acquireNetworkSync(description);
            }
        };
    }
    public Task<ResourceManager, Network> acquireNetwork(TaskMode mode, final String id) throws NotImplementedException {
        return new AbstractThreadedTask<ResourceManager,Network>(mode) {
            public Network invoke() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncResourceManagerImpl.super.acquireNetworkSync(id);
            }
        };
    }
    public Task<ResourceManager, Void> releaseNetwork(TaskMode mode, final String id) throws NotImplementedException {
        return new AbstractThreadedTask<ResourceManager, Void>(mode) {
            public Void invoke() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncResourceManagerImpl.super.releaseNetworkSync(id);
                return null;
            }
        };
    }

    //----------------------------------------------------------------

    public Task<ResourceManager, Storage> acquireStorage(TaskMode mode, final StorageDescription description) throws NotImplementedException {
        return new AbstractThreadedTask<ResourceManager, Storage>(mode) {
            public Storage invoke() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
                return AbstractAsyncResourceManagerImpl.super.acquireStorageSync(description);
            }
        };
    }
    public Task<ResourceManager, Storage> acquireStorage(TaskMode mode, final String id) throws NotImplementedException {
        return new AbstractThreadedTask<ResourceManager,Storage>(mode) {
            public Storage invoke() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncResourceManagerImpl.super.acquireStorageSync(id);
            }
        };
    }
    public Task<ResourceManager, Void> releaseStorage(TaskMode mode, final String id) throws NotImplementedException {
        return new AbstractThreadedTask<ResourceManager, Void>(mode) {
            public Void invoke() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncResourceManagerImpl.super.releaseStorageSync(id);
                return null;
            }
        };
    }
}
