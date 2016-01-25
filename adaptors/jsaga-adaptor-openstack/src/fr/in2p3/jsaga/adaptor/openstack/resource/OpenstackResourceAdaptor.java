package fr.in2p3.jsaga.adaptor.openstack.resource;
import java.util.Map;
import java.util.Properties;

import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.openstack.OpenstackAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;


public class OpenstackResourceAdaptor extends OpenstackAdaptorAbstract
        implements ResourceAdaptor {

    public Usage getUsage() {
        // TODO Auto-generated method stub
        return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        // TODO Auto-generated method stub
        return null;
    }

    public String[] listResources() throws TimeoutException, NoSuccessException {
        // TODO Auto-generated method stub

        return null;
    }

    public String[] listTemplates() throws TimeoutException, NoSuccessException {
        // TODO Auto-generated method stub
        return null;
    }

    public Properties getTemplate(String id) throws TimeoutException,
            NoSuccessException {
        // TODO Auto-generated method stub
        return null;
    }

    public void reconfigure(Properties description) {
        // TODO Auto-generated method stub
        
    }

    public void acquire(Properties description) {
        // TODO Auto-generated method stub
        
    }

    public void release(boolean drain) {
        // TODO Auto-generated method stub
        
    }

    public String[] getAccess() {
        // TODO Auto-generated method stub
        return null;
    }

}
