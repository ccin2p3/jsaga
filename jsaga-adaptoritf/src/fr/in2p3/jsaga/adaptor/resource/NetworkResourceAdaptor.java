package fr.in2p3.jsaga.adaptor.resource;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

import java.util.Properties;

public interface NetworkResourceAdaptor extends ResourceAdaptor {
    /**
     * Obtains the list of resources that are currently known to the resource manager.
     * @return a list of resource identifications.
     */
    public String[] listNetworkResources() throws TimeoutException, NoSuccessException;

    public void acquireNetworkResource(Properties description);

    // TODO templates
}
