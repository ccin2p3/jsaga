package fr.in2p3.jsaga.adaptor.resource.storage;

import java.util.Properties;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import fr.in2p3.jsaga.adaptor.resource.SecuredResource;

/**
 * You must use this interface if your adaptor provides compute resources that need additional security context
 * (eg. a VM that needs a SSH context). Otherwise, use the simpler interface {@link UnsecuredStorageResourceAdaptor}.
 * 
 * @author schwarz
 *
 */
public interface SecuredStorageResourceAdaptor extends StorageResourceAdaptor {

    /**
     * Obtains a storage resource from a description
     * 
     * @param description a list of Properties amongst SAGA @{link ComputeDescription}
     * @return a {@link SecuredResource} that contains the ID of the resource along with the security context
     * necessary to access the resource.
     * @throws NotImplementedException if the operation is not implemented
     * @throws NoSuccessException in case of error
     */
    public SecuredResource acquireStorageResource(Properties description) throws NotImplementedException, NoSuccessException;

}
