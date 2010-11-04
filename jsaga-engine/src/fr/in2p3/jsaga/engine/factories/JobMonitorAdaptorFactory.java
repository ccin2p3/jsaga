package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobMonitorAdaptorFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobMonitorAdaptorFactory extends ServiceAdaptorFactory {
    public JobMonitorAdaptor getJobMonitorAdaptor(JobControlAdaptor controlAdaptor) {
        return controlAdaptor.getDefaultJobMonitor();
    }

    public void connect(URL monitorURL, JobMonitorAdaptor monitorAdaptor, Map monitorAttributes, ContextImpl context) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        // check URL
        if (monitorURL==null || monitorURL.getScheme()==null) {
            throw new IncorrectURLException("Invalid entry name: "+monitorURL);
        }

        // set security adaptor
        SecurityCredential credential = getCredential(monitorURL, context, monitorAdaptor);

        // connect
        connect(monitorAdaptor, credential, monitorURL, monitorAttributes);
    }

    public static void connect(JobMonitorAdaptor monitorAdaptor, SecurityCredential credential, URL url, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, IncorrectURLException, BadParameterException, TimeoutException, NoSuccessException {
        monitorAdaptor.setSecurityCredential(credential);
        monitorAdaptor.connect(
                url.getUserInfo(),
                url.getHost(),
                url.getPort()>0 ? url.getPort() : monitorAdaptor.getDefaultPort(),
                url.getPath(),
                attributes);
    }
    public static void disconnect(JobMonitorAdaptor monitorAdaptor) throws NoSuccessException {
        monitorAdaptor.disconnect();
    }
}
