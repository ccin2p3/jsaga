package fr.in2p3.jsaga.adaptor.resource.compute;

import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;

import java.util.Properties;

/**
 * This interface describes an adaptor responsible for dealing with compute resources.
 * You must use sub-interface {@link SecuredComputeResourceAdaptor} or {@link UnsecuredComputeResourceAdaptor}
 * in order to be able to implement compute resource acquirement.
 * 
 * @author schwarz
 *
 */
public interface ComputeResourceAdaptor extends ResourceAdaptor {
    /**
     * Obtains the list of compute resources that are currently known to the resource manager.
     * @return a list of compute resource identifications.
     * @throws TimeoutException in case of timeout
     * @throws NoSuccessException in case of error
     */
    public String[] listComputeResources() throws TimeoutException, NoSuccessException;

    /**
     * release a compute resource identified by ID after draining if requested
     * @param id a string that identifies the resource
     * @param drain a boolean that indicates if the resource must be drained before released
     * @throws DoesNotExistException if the resource does not exist
     * @throws NotImplementedException if the operation is not implemented
     * @throws NoSuccessException in case of error
     */
    public void release(String id, boolean drain) throws DoesNotExistException, NotImplementedException, NoSuccessException;

    /**
     * Obtains the list of compute templates that are currently known to the resource manager.
     * @return a list of compute template identifications.
     * @throws TimeoutException in case of timeout
     * @throws NoSuccessException in case of error
     */
    public String[] listComputeTemplates() throws TimeoutException, NoSuccessException;
}
