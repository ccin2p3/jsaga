package fr.in2p3.jsaga.adaptor.resource;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

import java.util.Properties;

public interface ResourceAdaptor extends ClientAdaptor {
    /**
     * Obtains the list of resources that are currently known to the resource manager.
     * @return a list of resource identifications.
     */
    public String[] listResources() throws TimeoutException, NoSuccessException;

    /**
     * Obtains the list of templates that are currently known to the resource manager.
     * @return a list of template identifications.
     */
    public String[] listTemplates() throws TimeoutException, NoSuccessException;

    /**
     * Obtains the specified template
     * @return the template description
     */
    public Properties getTemplate(String id) throws TimeoutException, NoSuccessException;

    public void reconfigure(Properties description);

    public void acquire(Properties description);
    public void release(boolean drain);

    public String[] getAccess();
}
