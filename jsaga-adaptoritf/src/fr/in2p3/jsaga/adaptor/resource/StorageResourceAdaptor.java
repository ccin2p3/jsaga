package fr.in2p3.jsaga.adaptor.resource;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import java.util.Properties;

public interface StorageResourceAdaptor extends ResourceAdaptor {
    /**
     * Obtains the list of storage resources that are currently known to the resource manager.
     * @return a list of storage resource identifications.
     */
    public String[] listStorageResources() throws TimeoutException, NoSuccessException;

    /**
     * Obtains a storage resource from a description
     * @param description
     * @return the native resource ID
     * @throws NotImplementedException
     * @throws NoSuccessException
     */
    public String acquireStorageResource(Properties description);
    
    /**
     * Obtains the list of storage templates that are currently known to the resource manager.
     * @return a list of storage template identifications.
     */
    public String[] listStorageTemplates() throws TimeoutException, NoSuccessException;
}
