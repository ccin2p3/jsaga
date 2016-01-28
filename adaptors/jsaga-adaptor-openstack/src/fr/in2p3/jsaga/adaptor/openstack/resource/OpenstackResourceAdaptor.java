package fr.in2p3.jsaga.adaptor.openstack.resource;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.description.ResourceDescription;
import org.openstack4j.api.Builders;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Image;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.compute.builder.ServerCreateBuilder;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.openstack.OpenstackAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   OpenstackResourceAdaptor
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   26 JAN 2016
 * ***************************************************/

public class OpenstackResourceAdaptor extends OpenstackAdaptorAbstract
        implements ResourceAdaptor {

    public static final String DESC_NAME = "Name";
    public static final String DESC_FLAVOR = "Flavor";
    public static final String DESC_IMAGE = "Image";
    
    @Override
    public Usage getUsage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] listResources() throws TimeoutException, NoSuccessException {
        List<? extends Server> listOfVM = m_os.compute().servers().list();
        String[] listOfResources = new String[listOfVM.size()];
        int count=0;
        for (Server i: listOfVM) {
            listOfResources[count] = i.getId();
            count++;
        }
        return listOfResources;
    }

    @Override
    public String[] listTemplates() throws TimeoutException, NoSuccessException {
        return listTemplates(null);
    }

    // TODO: define in interface?
    public String[] listTemplates(Type type) throws TimeoutException, NoSuccessException {
        if (Type.COMPUTE.equals(type) || type == null) {
            List<? extends Flavor> listOfFlavors = m_os.compute().flavors().list();
            String[] listOfTemplates = new String[listOfFlavors.size()];
            int count=0;
            for (Flavor i: listOfFlavors) {
                listOfTemplates[count] = i.getId();
                count++;
            }
            return listOfTemplates;
        }
        throw new NoSuccessException("type not supported: " + type.name());
    }
    
    @Override
    public Properties getTemplate(String id) throws TimeoutException,
            NoSuccessException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void reconfigure(Properties description) {
        // TODO Auto-generated method stub
        
    }

    // TODO: return Resource?
    // TODO throw Exception?
    @Override
    public void acquire(Properties description) {
        if (description.containsKey(ResourceDescription.TYPE)
                && description.containsKey(DESC_NAME)
                && description.containsKey(DESC_FLAVOR)
                && description.containsKey(DESC_IMAGE)
        ) {
            ServerCreate sc = this.prepareServerCreate(description);
            Server vm = m_os.compute().servers().boot(sc);
            return;
        };
//            throw new NoSuccessException("Invalid desc");
        System.out.println("Invalid description");
        return;
    }

    // TODO: need resourceID?
    @Override
    public void release(boolean drain) {
        // TODO Auto-generated method stub
        
    }

    // TODO: need resourceID?
    // TODO: why not acquire.getDescription.getAttribute(Access) ?
    @Override
    public String[] getAccess() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public Properties translateDescription(ResourceDescription rd) {
        Properties prop = new Properties();
        return prop;
    }
    
    private ServerCreate prepareServerCreate(Properties desc) {
        ServerCreateBuilder scb = Builders.server();

        scb.name(desc.getProperty(DESC_NAME));
        scb.flavor(desc.getProperty(DESC_FLAVOR));
        scb.image(desc.getProperty(DESC_IMAGE));
        
        return scb.build();
    }
}
