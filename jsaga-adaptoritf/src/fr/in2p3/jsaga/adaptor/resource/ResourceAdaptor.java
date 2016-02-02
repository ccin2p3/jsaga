package fr.in2p3.jsaga.adaptor.resource;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

import java.util.Properties;

public interface ResourceAdaptor extends ClientAdaptor {

    /**
     * Obtains the list of templates that are currently known to the resource manager.
     * @return a list of template identifications.
     */
    public String[] listTemplates() throws TimeoutException, NoSuccessException;

    /**
     * Obtains the specified template
     * @return the template description
     */
    public Properties getTemplate(String templateId) throws TimeoutException, NoSuccessException;

    public void check(String resourceId);
    
    public void release(String resourceId);

    public String[] getAccess(String resourceId);
}
