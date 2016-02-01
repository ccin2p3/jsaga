package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.engine.descriptors.AdaptorDescriptors;
import fr.in2p3.jsaga.engine.descriptors.ResourceAdaptorDescriptor;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;

import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class ResourceAdaptorFactory extends ServiceAdaptorFactory {
    private ResourceAdaptorDescriptor m_descriptor;

    public ResourceAdaptorFactory(AdaptorDescriptors descriptors) {
        m_descriptor = descriptors.getResourceDesc();
    }

    public ResourceAdaptor getAdaptor(URL url, ContextImpl context) throws NotImplementedException, IncorrectURLException, NoSuccessException {
        if (url==null || url.getScheme()==null) {
            throw new IncorrectURLException("No protocol found in URL: "+url);
        }

        // create instance
        String scheme = context.getSchemeFromAlias(url.getScheme());
        Class clazz = m_descriptor.getClass(scheme);
        try {
            return (ResourceAdaptor) clazz.newInstance();
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
    }

    public Map getAttribute(URL url, ContextImpl context) throws NotImplementedException, NoSuccessException {
        String scheme = context.getSchemeFromAlias(url.getScheme());
        try {
            return getAttributes(url, context, m_descriptor.getDefaultsMap(scheme), ContextImpl.RESOURCE_SERVICE_ATTRIBUTES);
        } catch (BadParameterException e) {
            throw new NoSuccessException(e);
        }
    }

    public void connect(URL url, ResourceAdaptor adaptor, Map attributes, ContextImpl context) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, IncorrectURLException, BadParameterException, TimeoutException, NoSuccessException {
        // set security adaptor
        SecurityCredential credential = getCredential(url, context, adaptor);

        this.checkAttributesValidity(attributes, adaptor.getUsage());

        // connect
        connect(adaptor, credential, url, attributes);
    }

    public static void connect(ResourceAdaptor adaptor, SecurityCredential credential, URL url, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, IncorrectURLException, BadParameterException, TimeoutException, NoSuccessException {
        // check port
        if (url.getPort()<=0 && adaptor.getDefaultPort() == ClientAdaptor.NO_DEFAULT) {
            throw new BadParameterException("Missing PORT in URL:" + url.getString());
        }
        adaptor.setSecurityCredential(credential);
        adaptor.connect(
                url.getUserInfo(),
                url.getHost(),
                url.getPort()>0 ? url.getPort() : adaptor.getDefaultPort(),
                url.getPath(),
                attributes);
    }
    public static void disconnect(ResourceAdaptor adaptor) throws NoSuccessException {
        adaptor.disconnect();
    }
}
