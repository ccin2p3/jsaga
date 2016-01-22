package fr.in2p3.jsaga.impl.resource.manager;

import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;
import org.ogf.saga.error.*;
import org.ogf.saga.job.JobService;
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
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

import java.util.List;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class ResourceManagerImpl extends AbstractAsyncResourceManagerImpl implements ResourceManager {
    /** constructor */
    public ResourceManagerImpl(Session session, URL rm, ResourceAdaptor adaptor) {
        super(session, rm, adaptor);
    }

    //----------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public List<String> listResources(Type type) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("listResources");
        if (timeout == WAIT_FOREVER) {
            return super.listResourcesSync(type);
        } else {
            try {
                return (List<String>) getResult(super.listResources(TaskMode.ASYNC, type), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> listTemplates(Type type) throws NotImplementedException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("listTemplates");
        if (timeout == WAIT_FOREVER) {
            return super.listTemplatesSync(type);
        } else {
            try {
                return (List<String>) getResult(super.listTemplates(TaskMode.ASYNC, type), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (AuthorizationFailedException e) {throw new NoSuccessException(e);}
            catch (AuthenticationFailedException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }
    public ResourceDescription getTemplate(String id) throws NotImplementedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("getTemplate");
        if (timeout == WAIT_FOREVER) {
            return super.getTemplateSync(id);
        } else {
            try {
                return (ResourceDescription) getResult(super.getTemplate(TaskMode.ASYNC, id), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (AuthenticationFailedException e) {throw new NoSuccessException(e);}
            catch (AuthorizationFailedException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    //----------------------------------------------------------------

    public Compute acquireCompute(ComputeDescription description) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("acquireCompute");
        if (timeout == WAIT_FOREVER) {
            return super.acquireComputeSync(description);
        } else {
            try {
                return (Compute) getResult(super.acquireCompute(TaskMode.ASYNC, description), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }
    public Compute acquireCompute(String id) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("acquireCompute");
        if (timeout == WAIT_FOREVER) {
            return super.acquireComputeSync(id);
        } else {
            try {
                return (Compute) getResult(super.acquireCompute(TaskMode.ASYNC, id), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }
    public void releaseCompute(String id) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("releaseCompute");
        if (timeout == WAIT_FOREVER) {
            super.releaseComputeSync(id);
        } else {
            try {
                getResult(super.releaseCompute(TaskMode.ASYNC, id), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }
    public void releaseCompute(String id, boolean drain) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("releaseCompute");
        if (timeout == WAIT_FOREVER) {
            super.releaseComputeSync(id, drain);
        } else {
            try {
                getResult(super.releaseCompute(TaskMode.ASYNC, id, drain), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    //----------------------------------------------------------------

    public Network acquireNetwork(NetworkDescription description) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("acquireNetwork");
        if (timeout == WAIT_FOREVER) {
            return super.acquireNetworkSync(description);
        } else {
            try {
                return (Network) getResult(super.acquireNetwork(TaskMode.ASYNC, description), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }
    public Network acquireNetwork(String id) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("acquireNetwork");
        if (timeout == WAIT_FOREVER) {
            return super.acquireNetworkSync(id);
        } else {
            try {
                return (Network) getResult(super.acquireNetwork(TaskMode.ASYNC, id), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }
    public void releaseNetwork(String id) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("releaseNetwork");
        if (timeout == WAIT_FOREVER) {
            super.releaseNetworkSync(id);
        } else {
            try {
                getResult(super.releaseNetwork(TaskMode.ASYNC, id), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    //----------------------------------------------------------------

    public Storage acquireStorage(StorageDescription description) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("acquireStorage");
        if (timeout == WAIT_FOREVER) {
            return super.acquireStorageSync(description);
        } else {
            try {
                return (Storage) getResult(super.acquireStorage(TaskMode.ASYNC, description), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }
    public Storage acquireStorage(String id) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("acquireStorage");
        if (timeout == WAIT_FOREVER) {
            return super.acquireStorageSync(id);
        } else {
            try {
                return (Storage) getResult(super.acquireStorage(TaskMode.ASYNC, id), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }
    public void releaseStorage(String id) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("releaseStorage");
        if (timeout == WAIT_FOREVER) {
            super.releaseStorageSync(id);
        } else {
            try {
                getResult(super.releaseStorage(TaskMode.ASYNC, id), timeout);
            }
            catch (IncorrectURLException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    ////////////////////////////////////////// private methods //////////////////////////////////////////

    private float getTimeout(String methodName) throws NoSuccessException {
        return getTimeout(JobService.class, methodName, m_url.getScheme());
    }
}
