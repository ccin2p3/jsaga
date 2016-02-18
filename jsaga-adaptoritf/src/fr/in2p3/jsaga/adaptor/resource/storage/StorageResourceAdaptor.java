package fr.in2p3.jsaga.adaptor.resource.storage;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.compute.SecuredComputeResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.compute.UnsecuredComputeResourceAdaptor;

import java.util.Properties;

/**
 * This interface describes an adaptor responsible for dealing with storage resources.
 * You must use sub-interface {@link SecuredStorageResourceAdaptor} or {@link UnsecuredStorageResourceAdaptor}
 * in order to be able to implement storage resource acquirement.
 * 
 * @author schwarz
 *
 */
public interface StorageResourceAdaptor extends ResourceAdaptor {
    /**
     * Obtains the list of storage resources that are currently known to the resource manager.
     * @return a list of storage resource identifications.
     */
    public String[] listStorageResources() throws TimeoutException, NoSuccessException;

    /**
     * Obtains the list of storage templates that are currently known to the resource manager.
     * @return a list of storage template identifications.
     */
    public String[] listStorageTemplates() throws TimeoutException, NoSuccessException;
}
