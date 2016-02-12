package fr.in2p3.jsaga.adaptor.resource.compute;

import java.util.Properties;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import fr.in2p3.jsaga.adaptor.resource.SecuredResource;

public interface SecuredComputeResourceAdaptor extends ComputeResourceAdaptor {

    public SecuredResource acquireComputeResource(Properties description) throws NotImplementedException, NoSuccessException;

}
