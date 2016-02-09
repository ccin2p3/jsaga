package fr.in2p3.jsaga.adaptor.resource;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import java.util.Properties;

public interface NetworkResourceAdaptor extends ResourceAdaptor {
    /**
     * Obtains the list of network resources that are currently known to the resource manager.
     * @return a list of network resource identifications.
     */
    public String[] listNetworkResources() throws TimeoutException, NoSuccessException;

    /**
     * Obtains a network resource from a description
     * @param description
     * @return the native resource ID
     * @throws NotImplementedException
     * @throws NoSuccessException
     */
    public String acquireNetworkResource(Properties description);

    /**
     * Obtains the list of network templates that are currently known to the resource manager.
     * @return a list of network template identifications.
     */
    public String[] listNetworkTemplates() throws TimeoutException, NoSuccessException;
}
