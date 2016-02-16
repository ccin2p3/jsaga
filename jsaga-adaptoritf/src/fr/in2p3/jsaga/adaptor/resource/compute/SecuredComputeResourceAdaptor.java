package fr.in2p3.jsaga.adaptor.resource.compute;

import java.util.Properties;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import fr.in2p3.jsaga.adaptor.resource.SecuredResource;

/**
 * You must use this interface if your adaptor provides compute resources that need additional security context
 * (eg. a VM that needs a SSH context). Otherwise, use the simpler interface {@link UnsecuredComputeResourceAdaptor}.
 * 
 * @author schwarz
 *
 */
public interface SecuredComputeResourceAdaptor extends ComputeResourceAdaptor {

    /**
     * Obtains a compute resource from a description
     * @param description
     * @return a {@link SecuredResource} that contains the ID of the resource along with the security context
     * necessary to access the resource.
     * @throws NotImplementedException if the operation is not implemented
     * @throws NoSuccessException in case of error
     */
    public SecuredResource acquireComputeResource(Properties description) throws NotImplementedException, NoSuccessException;

}
