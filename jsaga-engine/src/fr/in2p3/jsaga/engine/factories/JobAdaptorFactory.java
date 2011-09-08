package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.engine.descriptors.AdaptorDescriptors;
import fr.in2p3.jsaga.engine.descriptors.JobAdaptorDescriptor;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobAdaptorFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobAdaptorFactory extends ServiceAdaptorFactory {
    private JobAdaptorDescriptor m_descriptor;

    public JobAdaptorFactory(AdaptorDescriptors descriptors) {
        m_descriptor = descriptors.getJobDesc();
    }

    public JobControlAdaptor getJobControlAdaptor(URL url, ContextImpl context) throws NotImplementedException, IncorrectURLException, NoSuccessException {
        if (url==null || url.getScheme()==null) {
            throw new IncorrectURLException("No protocol found in URL: "+url);
        }

        // create instance
        String scheme = context.getSchemeFromAlias(url.getScheme());
        Class clazz = m_descriptor.getClass(scheme);
        try {
            return (JobControlAdaptor) clazz.newInstance();
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
    }

    public Map getAttribute(URL url, ContextImpl context) throws NotImplementedException, NoSuccessException {
        String scheme = context.getSchemeFromAlias(url.getScheme());
        try {
            return getAttributes(url, context, m_descriptor.getDefaultsMap(scheme), ContextImpl.JOB_SERVICE_ATTRIBUTES);
        } catch (BadParameterException e) {
            throw new NoSuccessException(e);
        }
    }

    public void connect(URL url, JobControlAdaptor jobAdaptor, Map attributes, ContextImpl context) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, IncorrectURLException, BadParameterException, TimeoutException, NoSuccessException {
        // set security adaptor
        SecurityCredential credential = getCredential(url, context, jobAdaptor);

        // connect
        connect(jobAdaptor, credential, url, attributes);
    }

    public static void connect(JobControlAdaptor jobAdaptor, SecurityCredential credential, URL url, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, IncorrectURLException, BadParameterException, TimeoutException, NoSuccessException {
        jobAdaptor.setSecurityCredential(credential);
        jobAdaptor.connect(
                url.getUserInfo(),
                url.getHost(),
                url.getPort()>0 ? url.getPort() : jobAdaptor.getDefaultPort(),
                url.getPath(),
                attributes);
    }
    public static void disconnect(JobControlAdaptor jobAdaptor) throws NoSuccessException {
        jobAdaptor.disconnect();
    }
}
