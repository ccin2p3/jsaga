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
    
    public void release(String resourceId) throws DoesNotExistException, NotImplementedException;

    public String[] getAccess(String resourceId);
}
