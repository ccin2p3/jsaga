package fr.in2p3.jsaga.adaptor.openstack.resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.description.StorageDescription;
import org.ogf.saga.resource.instance.Resource;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.types.ServiceType;
import org.openstack4j.model.ModelEntity;
import org.openstack4j.model.common.Link;
import org.openstack4j.model.compute.ActionResponse;
import org.openstack4j.model.compute.Address;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Image;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.compute.builder.ServerCreateBuilder;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.storage.object.SwiftContainer;
import org.openstack4j.model.storage.object.SwiftObject;
import org.openstack4j.model.storage.object.options.ContainerListOptions;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.openstack.networking.domain.NeutronNetwork.NetworkConcreteBuilder;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UFile;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.openstack.OpenstackAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.resource.ResourceStatus;
import fr.in2p3.jsaga.adaptor.resource.SecuredResource;
import fr.in2p3.jsaga.adaptor.resource.compute.SecuredComputeResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.network.UnsecuredNetworkResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.storage.UnsecuredStorageResourceAdaptor;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   OpenstackResourceAdaptor
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   26 JAN 2016
 * ***************************************************/

public class OpenstackResourceAdaptor extends OpenstackAdaptorAbstract
        implements SecuredComputeResourceAdaptor, UnsecuredStorageResourceAdaptor, UnsecuredNetworkResourceAdaptor {

    protected Logger m_logger = Logger.getLogger(OpenstackResourceAdaptor.class);

    /**
     * The keypair name already installed on openstack and used to connect to VM
     */
    public static String PARAM_KEYPAIRNAME = "KeypairName";
    /**
     * The local private key corresponding to the keypair installed on openstack
     */
    public static String PARAM_PRIVATEKEY = "PrivateKey";
    
    private String m_keypairName = null;
    private String m_privateKey = null;
    
    @Override
    public void connect(String userInfo, String host, int port,
            String basePath, Map attributes) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            IncorrectURLException, BadParameterException, TimeoutException,
            NoSuccessException {
        super.connect(userInfo, host, port, basePath, attributes);
        if (attributes.containsKey(PARAM_KEYPAIRNAME)) {
            m_keypairName = (String) attributes.get(PARAM_KEYPAIRNAME);
        }
        if (attributes.containsKey(PARAM_PRIVATEKEY)) {
            m_privateKey = (String) attributes.get(PARAM_PRIVATEKEY);
        }
    }
    
    @Override
    public Usage getUsage() {
        return new UAnd.Builder()
        .and(new UFile(PARAM_PRIVATEKEY))
        .and(new U(PARAM_KEYPAIRNAME))
        .build();
    }

    @Override
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;
    }

    //////////////////////
    // Servers (Resources)
    //////////////////////
    @Override
    public Properties getDescription(String resourceId) 
            throws DoesNotExistException, NotImplementedException, BadParameterException {
        m_logger.debug("Getting description of " + resourceId);
        ModelEntity entity = this.getEntityByName(resourceId);
        Properties p = new Properties();
        if (entity instanceof Server) {
            // NOVA server
            p.setProperty(Resource.RESOURCE_TYPE, Type.COMPUTE.name());
            // search by name
            Server server = (Server)entity;
            if (server.getLaunchedAt() != null) {
                p.setProperty(ComputeDescription.START, Long.toString(server.getLaunchedAt().getTime()/1000));
            }
            // get Flavor
            List<String> templates = new ArrayList<String>();
            Flavor flavor = server.getFlavor();
            if (flavor != null) {
                p.setProperty(ComputeDescription.MEMORY, Integer.toString(flavor.getRam()));
                p.setProperty(ComputeDescription.SIZE, Integer.toString(flavor.getVcpus()));
                templates.add(urlOfInternalId(internalIdOf(flavor)));
            }
            // get image
            Image image = server.getImage();
            if (image != null) {
                templates.add(urlOfInternalId(internalIdOf(image)));
            }
            if (templates.size()>0) {
                p.put(ComputeDescription.TEMPLATE, templates.toArray(new String[templates.size()]));
            }
        } else if (entity instanceof SwiftContainer){
            // OBJECT_STORAGE swift container
            p.setProperty(Resource.RESOURCE_TYPE, Type.STORAGE.name());
            SwiftContainer sc = (SwiftContainer)entity;
            p.setProperty(StorageDescription.SIZE, Long.toString(sc.getTotalSize()));
        } else if (entity instanceof Network) {
            // NEURONE network 
            p.setProperty(Resource.RESOURCE_TYPE, Type.NETWORK.name());
        } else {
            throw new NotImplementedException();
        }
        return p;
    }

    @Override
    public String[] getAccess(String resourceId) throws NotImplementedException, DoesNotExistException {
        ModelEntity entity = this.getEntityByName(resourceId);
        List<String> accesses = new ArrayList<String>();
        if (entity instanceof Server) {
            // NOVA server
            // access = ssh://172.0.24.44
            Server server = (Server)entity;
            for (List<? extends Address> addrs: server.getAddresses().getAddresses().values()) {
                for (Address addr: addrs) {
                    m_logger.debug(addr.getAddr());
                    // TODO: param accessProtocol?
                    accesses.add("ssh://" + addr.getAddr());
                }
            }
        } else if (entity instanceof SwiftContainer){
            // OBJECT_STORAGE swift container
            // access = swift://keystone:5000/v2/object-store/containers/NAME
            accesses.add(m_os.getEndpoint().replaceAll("^.*://", "swift://") + resourceId);
        } else if (entity instanceof Network) {
            // TODO network access
        } else {
            throw new NotImplementedException();
        }
        return accesses.toArray(new String[accesses.size()]);
    }

    @Override
    public ResourceStatus getResourceStatus(String resourceId) throws DoesNotExistException, NotImplementedException {
        ModelEntity entity = this.getEntityByName(resourceId);
        if (entity instanceof Server) {
            return new OpenstackServerStatus((Server)entity);
        } else if (entity instanceof SwiftContainer){
            return new OpenstackSwiftContainerStatus((SwiftContainer)entity);
        }
        throw new NotImplementedException();
    }

    @Override
    public String[] listComputeResources() throws TimeoutException,
            NoSuccessException {
        List<? extends Server> listOfVM = m_os.compute().servers().list();
        String[] listOfResources = new String[listOfVM.size()];
        int count=0;
        for (Server s: listOfVM) {
            listOfResources[count] = this.internalIdOf(s);
            count++;
        }
        return listOfResources;
    }

    @Override
    public SecuredResource acquireComputeResource(Properties description) throws NotImplementedException, NoSuccessException {
        if (!m_os.getSupportedServices().contains(ServiceType.COMPUTE)) {
            throw new NoSuccessException("COMPUTE service is not supported on this server");
        }
        // hostnames is not supported
        if (description.containsKey(ComputeDescription.HOST_NAMES)) {
            throw new NotImplementedException();
        }
        // ARCH other then 'ANY' is not supported
        if (description.containsKey(ComputeDescription.MACHINE_ARCH) &&
                !description.getProperty(ComputeDescription.MACHINE_ARCH).equalsIgnoreCase("any")) {
            throw new NotImplementedException();
        }
        // OS other then 'ANY' is not supported
        if (description.containsKey(ComputeDescription.MACHINE_OS) &&
                !description.getProperty(ComputeDescription.MACHINE_OS).equalsIgnoreCase("any")) {
            throw new NotImplementedException();
        }
        // Some attributes are mandatory
        if (!description.containsKey(ComputeDescription.TEMPLATE)) {
            throw new NoSuccessException("Mandatory: " + ComputeDescription.TEMPLATE);
        }
        // Build server create
        ServerCreateBuilder scb = Builders.server();
        
        // TEMPLATE can be an IMAGE, FLAVOR
        Boolean hasImage = false;
        Boolean hasFlavor = false;
        Pattern p = Pattern.compile("(\\[.*]-\\[)(.+)(])");
        for (String template: (String[])description.get(ComputeDescription.TEMPLATE)) {
            // Check that template name is in the JSAGA form
            // [URL]-[nova/images/.*]
            Matcher m = p.matcher(template);
            if (!m.find()) {
                throw new NoSuccessException("Malformed templateId: " + template);
            }
            // check image
            try {
                scb.image(this.getImageByName(m.group(2)));
                hasImage = true;
            } catch (DoesNotExistException e) {
                try {
                    scb.flavor(this.getFlavorByName(m.group(2)));
                    hasFlavor = true;
                } catch (DoesNotExistException e1) {
                    throw new NoSuccessException(e1);
                }
            }
        }
        
        // If no image, exception
        if (!hasImage) {
            throw new NoSuccessException("Image is mandatory");
        }
        
        // if no flavor, get one with requirements MEMORY and SIZE
        if (!hasFlavor) {
            try {
                scb.flavor(this.getMostAppropriateFlavorInList(m_os.compute().flavors().list(), description));
            } catch (DoesNotExistException e) {
                throw new NoSuccessException("No flavor matching requirements");
            }
        }
        
        // How we will connect to server
        Boolean connectWithKey = (m_keypairName != null && m_privateKey != null);
        if (connectWithKey) {
            scb.keypairName(m_keypairName);
        }
        
        // Give it a name
        String serverName = "jsaga-server-" + m_credential.getUserID() + "-" + UUID.randomUUID();
        scb.name(serverName);
        
        // Boot
        ServerCreate sc = scb.build();
        Server vm = m_os.compute().servers().boot(sc);
//        Server vm = m_os.compute().servers().bootAndWaitActive(sc, 60000);
        
        // create the SecuredResource for a security context to be attached to the session
        SecuredResource sr;
        // getAdminPass is never null... even if keypair was provided
        if (connectWithKey) {
            m_logger.debug("Building a SSH context...");
            sr = new SecuredResource(internalIdOfServerName(serverName), "SSH");
            sr.setProperty(Context.USERID, description.getProperty("AdminUser"));
            // SSH property
            sr.setProperty("UserPrivateKey", m_privateKey);
            sr.put("JobServiceAttributes", new String[]{"ssh.KnownHosts="});
            
        } else {
            m_logger.debug("Building a UserPass context...");
            sr = new SecuredResource(internalIdOfServerName(serverName), "UserPass");
            sr.setProperty(Context.USERID, description.getProperty("AdminUser"));
            sr.setProperty(Context.USERPASS, vm.getAdminPass());
        }
        return sr;
    }

    @Override
    public void release(String resourceId, boolean drain) 
            throws DoesNotExistException, NotImplementedException, NoSuccessException {
        m_logger.debug("Releasing " + resourceId);
        if (drain) {
            // TODO drain
            throw new NotImplementedException();
        }
        ModelEntity entity = this.getEntityByName(resourceId);
        if (entity instanceof Server) {
            Server server = (Server)entity;
            ActionResponse ar = m_os.compute().servers().delete(server.getId());
            if (!ar.isSuccess()) {
                throw new NoSuccessException(ar.getFault());
            }
            return;
        } else if (entity instanceof SwiftContainer) {
            SwiftContainer sc = (SwiftContainer)entity;
            for (SwiftObject so: m_os.objectStorage().objects().list(sc.getName())) {
                ActionResponse ar = m_os.objectStorage().objects().delete(sc.getName(), so.getName());
                if (!ar.isSuccess()) {
                    m_logger.warn("Could not delete object:" + ar.getCode() + ":" + ar.toString());
                } else {
                    m_logger.debug("deleted object " + so.getName());
                }
            }
            ActionResponse ar = m_os.objectStorage().containers().delete(sc.getName());
            if (!ar.isSuccess()) {
                throw new NoSuccessException(ar.getCode() + ":" + ar.getFault());
            }
            return;
        }
        throw new NotImplementedException();
    }

    @Override
    public void release(String resourceId) throws DoesNotExistException, NotImplementedException, NoSuccessException {
        this.release(resourceId, false);
    }

    /////////////////////
    // Images (Templates)
    /////////////////////
    @Override
    public Properties getTemplate(String id) throws TimeoutException, NoSuccessException, 
                    DoesNotExistException, NotImplementedException {
        ModelEntity entity = this.getEntityByName(id);
        Properties p = new Properties();
        if (entity instanceof Image) {
            p.setProperty(Resource.RESOURCE_TYPE, Type.COMPUTE.name());
            Image i = (Image)entity;
            p.setProperty(ComputeDescription.MACHINE_OS, i.getName());
            // TODO: add other attributes
        } else if (entity instanceof Flavor) {
            p.setProperty(Resource.RESOURCE_TYPE, Type.COMPUTE.name());
            Flavor f = (Flavor)entity;
            p.setProperty(ComputeDescription.MEMORY, Integer.toString(f.getRam()));
            p.setProperty(ComputeDescription.SIZE, Integer.toString(f.getVcpus()));
        } else {
            throw new NotImplementedException();
        }
        return p;
    }

    @Override
    public String[] listComputeTemplates() throws TimeoutException,
            NoSuccessException {
        List<? extends Image> listOfImages = m_os.compute().images().list();
        List<? extends Flavor> listOfFlavors = m_os.compute().flavors().list();
        List<String> listOfTemplates = new ArrayList<String>();
        // list of ACTIVE images
        for (Image i: listOfImages) {
            if ("ACTIVE".equals(i.getStatus())) {
                listOfTemplates.add(this.internalIdOf(i));
            }
        }
        // UNION with list of not disabled flavors
        for (Flavor f: listOfFlavors) {
            if (!f.isDisabled()) {
                listOfTemplates.add(this.internalIdOf(f));
            }
        }
        return listOfTemplates.toArray(new String[listOfTemplates.size()]);
    }

    //////////////////
    // Storage
    //////////////////
    @Override
    public String[] listStorageResources() throws TimeoutException,
            NoSuccessException {
        List<? extends SwiftContainer> listOfContainers = m_os.objectStorage().containers().list();
        String[] listOfResources = new String[listOfContainers.size()];
        int count=0;
        for (SwiftContainer s: listOfContainers) {
            listOfResources[count] = this.internalIdOf(s);
            count++;
        }
        return listOfResources;
    }

    @Override
    public String[] listStorageTemplates() throws TimeoutException,
            NoSuccessException {
        return new String[]{};
    }

    @Override
    public String acquireStorageResource(Properties description)
            throws NotImplementedException, NoSuccessException {
        if (!m_os.getSupportedServices().contains(ServiceType.OBJECT_STORAGE)) {
            throw new NoSuccessException("STORAGE service is not supported on this server");
        }
        String containerName;
        containerName = description.getProperty(StorageDescription.ACCESS,
                "jsaga-container-" + m_credential.getUserID() + "-" + UUID.randomUUID());
        m_os.objectStorage().containers().create(containerName);
        return internalIdOfContainerName(containerName);
    }

    /////////////
    // Network
    ////////////
    @Override
    public String[] listNetworkResources() throws TimeoutException,
            NoSuccessException {
        List<? extends Network> listOfNetwork = m_os.networking().network().list();
        String[] listOfResources = new String[listOfNetwork.size()];
        int count=0;
        for (Network s: listOfNetwork) {
            listOfResources[count] = this.internalIdOf(s);
            count++;
        }
        return listOfResources;
    }

    @Override
    public String[] listNetworkTemplates() throws TimeoutException,
            NoSuccessException {
        return new String[]{};
    }

    @Override
    public String acquireNetworkResource(Properties description)
            throws NotImplementedException, NoSuccessException {
        if (!m_os.getSupportedServices().contains(ServiceType.NETWORK)) {
            throw new NoSuccessException("NETWORK service is not supported on this server");
        }
        NetworkConcreteBuilder ndb = new NetworkConcreteBuilder();
        ndb.name("jsaga-net-" + m_credential.getUserID() + "-" + UUID.randomUUID());
        Network net = m_os.networking().network().create(ndb.build());
        return this.internalIdOf(net);
    }


    ///////////////
    // Private 
    ///////////////
    /*
     * nova/images/image_name => [URL]-[nova/images/image_name]
     * 
     */
    private String urlOfInternalId(String id) {
        return "[" + m_os.getEndpoint() + "]-[" + id + "]";
    }
    private String internalIdOf(Image i) {
        return ServiceType.COMPUTE.getServiceName() + "/images/" + i.getName();
    }
    private String internalIdOf(Flavor f) {
        return ServiceType.COMPUTE.getServiceName() + "/flavors/" + f.getName();
    }
    private String internalIdOf(Server s) {
        return this.internalIdOfServerName(s.getName());
    }
    private String internalIdOf(SwiftContainer sc) {
        return this.internalIdOfContainerName(sc.getName());
    }
    private String internalIdOf(Network net) {
        return this.internalIdOfNetworkName(net.getName());
    }
    private String internalIdOfServerName(String name) {
        return ServiceType.COMPUTE.getServiceName() + "/servers/" + name;
    }
    private String internalIdOfContainerName(String name) {
        return ServiceType.OBJECT_STORAGE.getServiceName() + "/containers/" + name;
    }
    private String internalIdOfNetworkName(String name) {
        return ServiceType.NETWORK.getServiceName() + "/networks/" + name;
    }
    
    private ModelEntity getEntityByName(String resourceId) throws DoesNotExistException {
        if (resourceId.startsWith(ServiceType.COMPUTE.getServiceName())) {
            if (resourceId.contains("/servers/")) {
                return this.getServerByName(resourceId);
            } else if (resourceId.contains("/images/")) {
                return this.getImageByName(resourceId);
            } else if (resourceId.contains("/flavors")) {
                return this.getFlavorByName(resourceId);
            } else {
                throw new DoesNotExistException(resourceId); 
            }
        } else if (resourceId.startsWith(ServiceType.OBJECT_STORAGE.getServiceName())) {
            return this.getContainerByName(resourceId);
        } else if (resourceId.startsWith(ServiceType.NETWORK.getServiceName())) {
            
        }
        throw new DoesNotExistException(resourceId); 
        
    }
        
    private Server getServerByName(String internalId) throws DoesNotExistException {
        String serverId = internalId.replaceAll(".*/servers/", "");
        Map<String,String> param = new HashMap<String,String>();
        param.put("name", serverId);
        // this must be able to run outside the main thread: need a OSClient
        OSClient os = OSFactory.builder()
                .endpoint(m_os.getEndpoint())
                .token(m_token.getId())
                .tenantName(m_tenant)
                .authenticate();
        for (Server s: os.compute().servers().list(param)) {
            if (s.getName().equals(serverId)) {
                return s;
            }
        }
        throw new DoesNotExistException("This resource does not exist: " + serverId);
    }

    private Image getImageByName(String internalId) throws DoesNotExistException {
        String imageId = internalId.replaceAll(".*/images/", "");
        for (Image i: m_os.compute().images().list()) {
            if (i.getName().equals(imageId)) {
                return i;
            }
        }
        throw new DoesNotExistException("This template does not exist: " + imageId);
    }

    private Flavor getFlavorByName(String internalId) throws DoesNotExistException {
        String flavorId = internalId.replaceAll(".*/flavors/", "");
        for (Flavor f: m_os.compute().flavors().list()) {
            if (f.getName().equals(flavorId)) {
                return f;
            }
        }
        throw new DoesNotExistException("This template does not exist: " + flavorId);
    }

    private SwiftContainer getContainerByName(String internalId) throws DoesNotExistException {
        String containerId = internalId.replaceAll(".*/containers/", "");
        // this must be able to run outside the main thread: need a OSClient
        OSClient os = OSFactory.builder()
                .endpoint(m_os.getEndpoint())
                .token(m_token.getId())
                .tenantName(m_tenant)
                .authenticate();
        ContainerListOptions clo = ContainerListOptions.create();
        for (SwiftContainer sc: os.objectStorage().containers().list(clo)) {
            if (sc.getName().equals(containerId)) {
                return sc;
            }
        }
        throw new DoesNotExistException("This resource does not exist: " + containerId);
    }

    /*
     * unit testable
     * get the "smallest" enabled and public flavor that matches both requested memory and cpus
     */
    Flavor getMostAppropriateFlavorInList(List<? extends Flavor> list, Properties constraints) throws DoesNotExistException {
        int requestedRam = Integer.parseInt(constraints.getProperty(ComputeDescription.MEMORY, "0"));
        int requestedCpu = Integer.parseInt(constraints.getProperty(ComputeDescription.SIZE, "1"));
        Flavor mostAppropriateFlavor = null;
        for (Flavor f: list) {
            if (!f.isDisabled() &&
                    f.isPublic() &&
                    f.getRam() >= requestedRam &&
                    f.getVcpus() >= requestedCpu) {
                // check if this flavor is most appropriate than previous
                if (mostAppropriateFlavor == null) {
                    mostAppropriateFlavor = f;
                } else {
                    if (f.getRam() < mostAppropriateFlavor.getRam() && f.getVcpus() < mostAppropriateFlavor.getVcpus()) {
                        mostAppropriateFlavor = f;
                    }
                }
            }
        }
        if (mostAppropriateFlavor == null) {
            throw new DoesNotExistException();
        }
        return mostAppropriateFlavor;
    }
    
    @Deprecated
    private String getHRef(List<? extends Link> links) throws NoSuccessException {
        for (Link link: links) {
            if ("self".equals(link.getRel())) {
                return link.getHref();
            }
        }
        throw new NoSuccessException("Cound not find HRef");
    }

}
