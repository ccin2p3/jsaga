package fr.in2p3.jsaga.adaptor.resource.compute;

import java.util.Properties;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

public interface UnsecuredComputeResourceAdaptor extends ComputeResourceAdaptor {

    /**
     * Obtains a compute resource from a description
     * @param description
     * @return the native resource ID
     * @throws NotImplementedException
     * @throws NoSuccessException
     */
    public String acquireComputeResource(Properties description) throws NotImplementedException, NoSuccessException;
    

}
