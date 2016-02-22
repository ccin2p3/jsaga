package fr.in2p3.jsaga.adaptor.resource.network;

import java.util.Properties;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

/**
 * You can implement this interface if your adaptor does provide network resources that are available without
 * any additional security contexts. In other case, please consider using {@link SecuredNetworkResourceAdaptor} instead.
 * 
 * @author schwarz
 *
 */
public interface UnsecuredNetworkResourceAdaptor extends NetworkResourceAdaptor {

    /**
     * Obtains a network resource from a description
     * @param description
     * @return the native resource ID
     * @throws NotImplementedException if the operation is not implemented
     * @throws NoSuccessException in case of error
     */
    public String acquireNetworkResource(Properties description) throws NotImplementedException, NoSuccessException;
    

}
