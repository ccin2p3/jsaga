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
import org.ogf.saga.resource.instance.Resource;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.types.ServiceType;
import org.openstack4j.model.common.Link;
import org.openstack4j.model.compute.ActionResponse;
import org.openstack4j.model.compute.Address;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Image;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.Server.Status;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.compute.builder.ServerCreateBuilder;
import org.openstack4j.openstack.OSFactory;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UFile;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.openstack.OpenstackAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.resource.ResourceStatus;
import fr.in2p3.jsaga.adaptor.resource.SecuredResource;
import fr.in2p3.jsaga.adaptor.resource.compute.SecuredComputeResourceAdaptor;
import fr.in2p3.jsaga.impl.context.ContextImpl;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   OpenstackResourceAdaptor
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   26 JAN 2016
 * ***************************************************/

public class OpenstackResourceAdaptor extends OpenstackAdaptorAbstract
        implements SecuredComputeResourceAdaptor {

    protected Logger m_logger = Logger.getLogger(OpenstackResourceAdaptor.class);

    public static String PARAM_KEYPAIRNAME = "KeypairName";
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
        Properties p = new Properties();
        if (resourceId.startsWith(ServiceType.COMPUTE.getServiceName())) {
            p = this.getComputeDescription(resourceId);
        } else {
            throw new NotImplementedException();
        }
        return p;
    }

    private Properties getComputeDescription(String resourceId) 
            throws DoesNotExistException, NotImplementedException, BadParameterException {
        Properties p = new Properties();
        p.setProperty(Resource.RESOURCE_TYPE, Type.COMPUTE.name());
        if (resourceId.contains("/servers/")) {
            // search by name
            Server server = this.getServerByName(resourceId);
            // get Flavor
            Flavor flavor = server.getFlavor();
            if (flavor != null) {
                p.setProperty(ComputeDescription.MEMORY, Integer.toString(flavor.getRam()));
                p.setProperty(ComputeDescription.SIZE, Integer.toString(flavor.getVcpus()));
            }
            // FIXME: this is not OS!
            Image image = server.getImage();
            if (image != null) {
                p.setProperty(ComputeDescription.MACHINE_OS, image.getName());
            }
        } else {
            throw new NotImplementedException();
        }
        return p;
    }
    
    @Override
    public String[] getAccess(String resourceId) throws NotImplementedException, DoesNotExistException {
        m_logger.info("getAccess");
        List<String> accesses = new ArrayList<String>();
        if (resourceId.contains("/servers/")) {
            // search by name
            Server server = this.getServerByName(resourceId);
            m_logger.debug("vmstate:" + server.getVmState());
            for (List<? extends Address> addrs: server.getAddresses().getAddresses().values()) {
                for (Address addr: addrs) {
                    m_logger.debug(addr.getAddr());
                    accesses.add("ssh://" + addr.getAddr());
                }
            }
            return accesses.toArray(new String[accesses.size()]);
        } else {
            throw new NotImplementedException();
        }
    }

    @Override
    public ResourceStatus getResourceStatus(String resourceId) throws DoesNotExistException, NotImplementedException {
        if (resourceId.contains("/servers/")) {
            Server server = this.getServerByName(resourceId);
            return new OpenstackResourceStatus(server.getStatus(), server.getVmState());
        } else {
            throw new NotImplementedException();
        }
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
            } catch (DoesNotExistException e) {
                try {
                    scb.flavor(this.getFlavorByName(m.group(2)));
                } catch (DoesNotExistException e1) {
                    throw new NoSuccessException(e1);
                }
            }
        }
        Boolean connectWithKey = (m_keypairName != null && m_privateKey != null);
        // TODO discover flavor if MEMORY, SIZE...
        if (connectWithKey) {
            scb.keypairName(m_keypairName);
        }
        String serverName = "jsaga-" + m_credential.getUserID() + "-" + UUID.randomUUID();
        scb.name(serverName);
        ServerCreate sc = scb.build();
        Server vm = m_os.compute().servers().boot(sc);
//        Server vm = m_os.compute().servers().bootAndWaitActive(sc, 60000);
        // Cannot use vm.getName() because it is empty
        SecuredResource sr;
        // getAdminPass is never null... even if keypair was provided
        if (connectWithKey) {
            m_logger.debug("Building a SSH context...");
            sr = new SecuredResource(internalIdOfServerName(serverName), "SSH");
            sr.setProperty(Context.USERID, "root");
            // SSH property
            sr.setProperty("UserPrivateKey", m_privateKey);
            sr.put(ContextImpl.JOB_SERVICE_ATTRIBUTES, new String[]{"ssh.KnownHosts="});
            sr.put(ContextImpl.BASE_URL_INCLUDES, new String[]{"ssh://"});
        } else {
            m_logger.debug("Building a UserPass context...");
            sr = new SecuredResource(internalIdOfServerName(serverName), "UserPass");
            // TODO make root user customizable
            sr.setProperty(Context.USERID, "root");
            sr.setProperty(Context.USERPASS, vm.getAdminPass());
            sr.put(ContextImpl.BASE_URL_INCLUDES, new String[]{"ssh://"});
        }
        return sr;
    }

    @Override
    public void release(String resourceId, boolean drain) 
            throws DoesNotExistException, NotImplementedException, NoSuccessException {
        if (drain) {
            // TODO drain
            throw new NotImplementedException();
        }
        if (resourceId.startsWith(ServiceType.COMPUTE.getServiceName())) {
            if (resourceId.contains("/servers/")) {
                Server server = this.getServerByName(resourceId);
                ActionResponse ar = m_os.compute().servers().delete(server.getId());
                if (!ar.isSuccess()) {
                    throw new NoSuccessException(ar.getFault());
                }
            } else {
                throw new NotImplementedException();
            }
        } else {
            throw new NoSuccessException();
        }
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
        Properties p = new Properties();
        if (id.startsWith(ServiceType.COMPUTE.getServiceName())) {
            p.setProperty(Resource.RESOURCE_TYPE, Type.COMPUTE.name());
            if (id.contains("/images/")) {
                Image i = this.getImageByName(id);
                p.setProperty(ComputeDescription.MACHINE_OS, i.getName());
                // TODO: add other attributes
                return p;
            } else if (id.contains("/flavors")) {
                Flavor f = this.getFlavorByName(id);
                p.setProperty(ComputeDescription.MEMORY, Integer.toString(f.getRam()));
                p.setProperty(ComputeDescription.SIZE, Integer.toString(f.getVcpus()));
                return p;
            } else {
                throw new NotImplementedException();
            }
        } else {
            throw new NotImplementedException();
        }
    }

    @Override
    public String[] listComputeTemplates() throws TimeoutException,
            NoSuccessException {
        List<? extends Image> listOfImages = m_os.compute().images().list();
        List<? extends Flavor> listOfFlavors = m_os.compute().flavors().list();
        String[] listOfTemplates = new String[listOfImages.size()+listOfFlavors.size()];
        int count=0;
        for (Image i: listOfImages) {
            listOfTemplates[count] = this.internalIdOf(i);
            count++;
        }
        for (Flavor f: listOfFlavors) {
            listOfTemplates[count] = this.internalIdOf(f);
            count++;
        }
        return listOfTemplates;
    }

    //////////////
    // Security context to access new VMs
    //////////////
    @Deprecated
    public Properties getSecurityProperties(String resourceId) throws NotImplementedException, DoesNotExistException {
        if (resourceId.contains("/servers/")) {
            Server server = this.getServerByName(resourceId);
            if (server.getKeyName() != null) {
                return null;
            }
            Properties p = new Properties();
            p.setProperty(Context.TYPE, "UserPass");
            p.setProperty(Context.USERID, "root");
            p.setProperty(Context.USERPASS, server.getAdminPass());
            return p;
        } else {
            throw new NotImplementedException();
        }
    }

    ///////////////
    // Private 
    ///////////////
    private String internalIdOf(Image i) {
        return ServiceType.COMPUTE.getServiceName() + "/images/" + i.getName();
    }
    private String internalIdOf(Flavor f) {
        return ServiceType.COMPUTE.getServiceName() + "/flavors/" + f.getName();
    }
    private String internalIdOf(Server s) {
        return this.internalIdOfServerName(s.getName());
    }
    private String internalIdOfServerName(String name) {
        return ServiceType.COMPUTE.getServiceName() + "/servers/" + name;
    }
    
    private Server getServerByName(String internalId) throws DoesNotExistException {
        String serverId = internalId.replaceAll(".*/servers/", "");
        Map<String,String> param = new HashMap<String,String>();
        param.put("name", serverId);
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
