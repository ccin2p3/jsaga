package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.engine.descriptors.AdaptorDescriptors;
import fr.in2p3.jsaga.engine.descriptors.DataAdaptorDescriptor;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import fr.in2p3.jsaga.impl.session.SessionImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataAdaptorFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   15 juin 2007
* ***************************************************
* Description:                                      */
/**
 * Create and manage data adaptors
 */
public class DataAdaptorFactory extends ServiceAdaptorFactory {
    public static final boolean PHYSICAL = false;
    public static final boolean LOGICAL = true;
    
    private DataAdaptorDescriptor m_descriptor;

    public DataAdaptorFactory(AdaptorDescriptors descriptors) {
        m_descriptor = descriptors.getDataDesc();
    }

    /**
     * Create a new instance of data adaptor for URL <code>url</code>.
     * @param url the URL of the service
     * @param session the security session
     * @return the data adaptor instance
     */
    public DataAdaptor getDataAdaptor(URL url, Session session) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (url==null || url.getScheme()==null) {
            throw new IncorrectURLException("No protocol found in URL: "+url);
        }

        // get context (security + config)
        ContextImpl context = ((SessionImpl)session).getBestMatchingContext(url);

        // create adaptor instance
        String scheme = context.getSchemeFromAlias(url.getScheme());
        Class clazz = m_descriptor.getClass(scheme);
        try {
            return (DataAdaptor) clazz.newInstance();
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
    }

    /**
     * Create a new instance of data adaptor for URL <code>url</code> and connect to service.
     * @param url the URL of the service
     * @param session the security session
     * @param expectsLogical the expected type of adaptor
     * @return the data adaptor instance
     */
    public DataAdaptor getDataAdaptorAndConnect(URL url, Session session, boolean expectsLogical) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (url==null || url.getScheme()==null) {
            throw new IncorrectURLException("No protocol found in URL: "+url);
        }

        // get context (security + config)
        ContextImpl context = ((SessionImpl)session).getBestMatchingContext(url);

        // create adaptor instance
        String scheme = context.getSchemeFromAlias(url.getScheme());
        Class clazz = m_descriptor.getClass(scheme);
        DataAdaptor dataAdaptor;
        try {
            dataAdaptor = (DataAdaptor) clazz.newInstance();
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }

        // check if adaptor is of expected type
        if (expectsLogical && !(dataAdaptor instanceof LogicalReader || dataAdaptor instanceof LogicalWriter)) {
            throw new BadParameterException("Protocol '"+scheme+"' is not a logical file protocol");
        }

        // get service attributes
        Map attributes = getAttributes(url, context, m_descriptor.getDefaultsMap(scheme));

        // set credential
        SecurityCredential credential = getCredential(url, context, dataAdaptor);
        dataAdaptor.setSecurityCredential(credential);

        // connect
        dataAdaptor.connect(
                url.getUserInfo(),
                url.getHost(),
                url.getPort()>0 ? url.getPort() : dataAdaptor.getDefaultPort(),
                url.getPath(),
                attributes);
        return dataAdaptor;
    }
}
