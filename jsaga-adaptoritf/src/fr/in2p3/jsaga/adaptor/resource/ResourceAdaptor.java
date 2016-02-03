package fr.in2p3.jsaga.adaptor.resource;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;

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
     * Check that a resource is still available
     * @param resourceId
     * @throws DoesNotExistException if the resource is not available anymore
     */
    public void check(String resourceId) throws TimeoutException, NoSuccessException, DoesNotExistException;
    
    public void release(String resourceId);

    public String[] getAccess(String resourceId);
}
