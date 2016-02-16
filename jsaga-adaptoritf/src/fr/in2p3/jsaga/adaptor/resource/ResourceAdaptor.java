package fr.in2p3.jsaga.adaptor.resource;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;

import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import java.util.Properties;

/**
 * An interface for describing an adaptor able to create/release/list resources. Please consider using sub-interfaces
 * {@link ComputeResourceAdaptor} for compute resources, {@link StorageResourceAdaptor} for storage resources and/or
 * {@link NetworkResourceAdaptor} for network resources.
 * 
 * @author schwarz
 *
 */
public interface ResourceAdaptor extends ClientAdaptor {

    /**
     * Obtains the specified template
     * @return the template description
     * @throws DoesNotExistException 
     * @throws NotImplementedException 
     */
    public Properties getTemplate(String templateId) throws TimeoutException, NoSuccessException, 
        DoesNotExistException, NotImplementedException;

    /**
     * Get the description of a resource
     * @param resourceId
     * @return a list of Properties that describe the resource
     * @throws DoesNotExistException if the resource is not available anymore
     * @throws NotImplementedException 
     * @throws BadParameterException 
     */
    public Properties getDescription(String resourceId) throws TimeoutException, NoSuccessException, DoesNotExistException, NotImplementedException, BadParameterException;
    
    /**
     * release a resource identified by ID
     * @param resourceId
     * @throws DoesNotExistException
     * @throws NotImplementedException
     * @throws NoSuccessException
     */
    public void release(String resourceId) throws DoesNotExistException, NotImplementedException, NoSuccessException;

    /**
     * get the list of access points for the resource identified by ID. If the resource was acquired along with security
     * properties see {@link SecuredComputeResourceAdaptor} in the same session, a context is built with these
     * properties and added to the current session, so that the user can immediately connect to the resource.
     * 
     * @param resourceId
     * @return the list of access points for the resource
     * @throws DoesNotExistException if the resource does not exist
     * @throws NotImplementedException if the operation is not implemented
     */
    public String[] getAccess(String resourceId) throws NotImplementedException, DoesNotExistException;
    
    /**
     * get the status of the resource
     * @param resourceId the identifier of the resource
     * @return the status of the resource
     * @throws DoesNotExistException if the resource does not exist
     * @throws NotImplementedException if the operation is not implemented
     */
    public ResourceStatus getResourceStatus(String resourceId) throws DoesNotExistException, NotImplementedException;

}
