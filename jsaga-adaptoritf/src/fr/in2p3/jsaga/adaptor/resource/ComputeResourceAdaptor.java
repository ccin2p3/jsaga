package fr.in2p3.jsaga.adaptor.resource;

import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import java.util.Properties;

public interface ComputeResourceAdaptor extends ResourceAdaptor {
    /**
     * Obtains the list of compute resources that are currently known to the resource manager.
     * @return a list of compute resource identifications.
     */
    public String[] listComputeResources() throws TimeoutException, NoSuccessException;

    public String acquireComputeResource(Properties description) throws NotImplementedException, NoSuccessException;
    
    public void release(String id, boolean drain) throws DoesNotExistException, NotImplementedException, NoSuccessException;

    /**
     * Obtains the list of compute templates that are currently known to the resource manager.
     * @return a list of compute template identifications.
     */
    public String[] listComputeTemplates() throws TimeoutException, NoSuccessException;
}
