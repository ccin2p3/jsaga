package fr.in2p3.jsaga.adaptor.openstack.resource;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.description.ResourceDescription;
import org.ogf.saga.resource.instance.Resource;
import org.openstack4j.api.Builders;
import org.openstack4j.api.types.ServiceType;
import org.openstack4j.model.ModelEntity;
import org.openstack4j.model.common.Link;
import org.openstack4j.model.compute.Address;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Image;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.compute.builder.ServerCreateBuilder;
import org.openstack4j.model.identity.Access.Service;
import org.openstack4j.model.identity.Endpoint;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.openstack.OpenstackAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.resource.ComputeResourceAdaptor;
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
        implements ComputeResourceAdaptor {

    @Override
    public Usage getUsage() {
        return null;
    }

    @Override
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        // TODO: default MEM, NBCPU...
        return null;
    }

    //////////////////////
    // Servers (Resources)
    //////////////////////
    @Override
    public Properties getDescription(String resourceId) throws DoesNotExistException, NotImplementedException {
        Properties p = new Properties();
        ServiceType serviceType;
        // What kind of resource is this?
        try {
            serviceType = this.typeFromServiceURL(resourceId);
        } catch (MalformedURLException e) {
            throw new DoesNotExistException(e);
        }
        if (serviceType.equals(ServiceType.COMPUTE)) {
            p.setProperty(Resource.RESOURCE_TYPE, Type.COMPUTE.name());
            if (resourceId.contains("/servers/")) {
                String serverId = resourceId.replaceAll(".*/servers/", "");
                Server server = m_os.compute().servers().get(serverId);
                if (server == null) {
                    throw new DoesNotExistException("This template does not exist");
                }
                // concat addresses
                String addresses = "";
                for (List<? extends Address> addrs: server.getAddresses().getAddresses().values()) {
                    for (Address addr: addrs) {
                        addresses = addresses + addr.getAddr() + ",";
                    }
                }
                addresses = addresses.replaceAll(",$", "");
                p.setProperty(ComputeDescription.HOST_NAMES, addresses);
                // get Flavor
                Flavor flavor = server.getFlavor();
                if (flavor != null) {
                    p.setProperty(ComputeDescription.MEMORY, Integer.toString(flavor.getRam()));
                    p.setProperty(ComputeDescription.SIZE, Integer.toString(flavor.getVcpus()));
                }
                Image image = server.getImage();
                if (image != null) {
                    p.setProperty(ComputeDescription.MACHINE_OS, image.getName());
                }
            } else {
                throw new NotImplementedException();
            }
        } else {
            throw new NotImplementedException();
        }
        return p;
    }

    @Override
    public String[] getAccess(String resourceId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] listComputeResources() throws TimeoutException,
            NoSuccessException {
        List<? extends Server> listOfVM = m_os.compute().servers().list();
        String[] listOfResources = new String[listOfVM.size()];
        int count=0;
        for (Server s: listOfVM) {
            listOfResources[count] = this.getHRef(s.getLinks());
            count++;
        }
        return listOfResources;
    }

    @Override
    public String acquireComputeResource(Properties description) throws NotImplementedException, NoSuccessException {
        // Some attributes are not supported
        if (description.containsKey(ComputeDescription.HOST_NAMES) ||
                description.containsKey(ComputeDescription.MACHINE_ARCH) ||
                description.containsKey(ComputeDescription.MACHINE_OS)) {
            throw new NotImplementedException();
        }
        // Some attributes are mandatory
        if (!description.containsKey(ComputeDescription.TEMPLATE)) {
            throw new NoSuccessException("Mandatory: " + ComputeDescription.TEMPLATE);
        }
        // Build server create
        ServerCreateBuilder scb = Builders.server();
        scb.name("jsaga-" + m_credential.getUserID() + "-" + UUID.randomUUID());
        // TODO discover flavor
        scb.flavor("2");
        scb.image(description.getProperty(ComputeDescription.TEMPLATE));
        ServerCreate sc = scb.build();
        Server vm = m_os.compute().servers().boot(sc);
        return this.getHRef(vm.getLinks());
    }

    @Override
    public void release(String resourceId, boolean drain) throws DoesNotExistException, NotImplementedException {
        ServiceType serviceType;
        // What kind of resource is this?
        try {
            serviceType = this.typeFromServiceURL(resourceId);
        } catch (MalformedURLException e) {
            throw new DoesNotExistException(e);
        }
        if (serviceType.equals(ServiceType.COMPUTE)) {
            if (resourceId.contains("/servers/")) {
                String serverId = resourceId.replaceAll(".*/servers/", "");
                m_os.compute().servers().delete(serverId);
            } else {
                throw new NotImplementedException();
            }
        } else {
            throw new NotImplementedException();
        }
    }

    @Override
    public void release(String resourceId) throws DoesNotExistException, NotImplementedException {
        this.release(resourceId, false);
    }

    /////////////////////
    // Images (Templates)
    /////////////////////
    @Override
    public Properties getTemplate(String id) throws TimeoutException, NoSuccessException, 
                    DoesNotExistException, NotImplementedException {
        Properties p = new Properties();
        ServiceType serviceType;
        // What kind of template is this?
        try {
            serviceType = this.typeFromServiceURL(id);
        } catch (MalformedURLException e) {
            throw new DoesNotExistException(e);
        }
        if (serviceType.equals(ServiceType.COMPUTE)) {
            p.setProperty(Resource.RESOURCE_TYPE, Type.COMPUTE.name());
            if (id.contains("/images/")) {
                String imageId = id.replaceAll(".*/images/", "");
                Image image = m_os.compute().images().get(imageId);
                if (image == null) {
                    throw new DoesNotExistException("This template does not exist");
                }
                p.setProperty(ComputeDescription.MACHINE_OS, image.getName());
                // TODO: add other attributes
            } else {
                throw new NotImplementedException();
            }
        } else {
            throw new NotImplementedException();
        }
        return p;
    }

    @Override
    public String[] listComputeTemplates() throws TimeoutException,
            NoSuccessException {
        List<? extends Image> listOfImages = m_os.compute().images().list();
        String[] listOfTemplates = new String[listOfImages.size()];
        int count=0;
        for (Image i: listOfImages) {
            listOfTemplates[count] = this.getHRef(i.getLinks());
            count++;
        }
        // TODO: add flavors?
        return listOfTemplates;
    }

    private String getHRef(List<? extends Link> links) throws NoSuccessException {
        for (Link link: links) {
            if ("self".equals(link.getRel())) {
                return link.getHref();
            }
        }
        throw new NoSuccessException("Cound not find HRef");
    }
}
