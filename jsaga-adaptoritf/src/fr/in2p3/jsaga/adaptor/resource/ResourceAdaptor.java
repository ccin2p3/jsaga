package fr.in2p3.jsaga.adaptor.resource;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;

import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import java.util.Properties;

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
     * get the list of access points for the resource identified by ID
     * @param resourceId
     * @return
     * @throws NotImplementedException
     * @throws DoesNotExistException 
     */
    public String[] getAccess(String resourceId) throws NotImplementedException, DoesNotExistException;
    
    // TODO javadoc
    public ResourceStatus getResourceStatus(String resourceId) throws DoesNotExistException, NotImplementedException;

}
