package fr.in2p3.jsaga.adaptor.openstack.data;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.types.ServiceType;
import org.openstack4j.model.common.Payload;
import org.openstack4j.model.common.Payloads;
import org.openstack4j.model.compute.ActionResponse;
import org.openstack4j.model.storage.object.SwiftObject;
import org.openstack4j.model.storage.object.options.ObjectListOptions;
import org.openstack4j.model.storage.object.options.ObjectPutOptions;
import org.openstack4j.openstack.OSFactory;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.write.DataWriterAdaptor;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterPutter;
import fr.in2p3.jsaga.adaptor.openstack.OpenstackAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.openstack.resource.OpenstackResourceAdaptor;
import fr.in2p3.jsaga.adaptor.openstack.util.SwiftURL;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;

public class OpenstackDataAdaptor extends OpenstackAdaptorAbstract implements DataReaderAdaptor, FileWriterPutter {

    protected Logger m_logger = Logger.getLogger(OpenstackDataAdaptor.class);
    private String m_container;
    
    @Override
    public String getType() {
        return "swift";
    }

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

    public void connect(String userInfo, String host, int port,
            String basePath, Map attributes) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            IncorrectURLException, BadParameterException, TimeoutException,
            NoSuccessException {
        super.connect(userInfo, host, port, 
                SwiftURL.getNovaPath(basePath), 
                attributes);
        // remove ".*/object-store/containers/"
        m_container = SwiftURL.getContainer(basePath);
        m_logger.debug("Connected to container " + m_container);
    }
    
    @Override
    public boolean exists(String absolutePath, String additionalArgs)
            throws PermissionDeniedException, TimeoutException,
            NoSuccessException {
        String containerPath = SwiftURL.getPath(absolutePath);
        m_logger.debug("exists " + containerPath);
        try {
            this.getSwiftObject(containerPath);
            return true;
        } catch (DoesNotExistException e) {
            return false;
        }
    }

    @Override
    public FileAttributes getAttributes(String absolutePath,
            String additionalArgs) throws PermissionDeniedException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        String containerPath = SwiftURL.getPath(absolutePath);
        return new SwiftObjectAttributes(this.getSwiftObject(containerPath));
    }

    @Override
    public FileAttributes[] listAttributes(String absolutePath,
            String additionalArgs) throws PermissionDeniedException,
            BadParameterException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        String objectPath = SwiftURL.getPath(absolutePath);
        List<SwiftObjectAttributes> attrs = new ArrayList<SwiftObjectAttributes>();
        ObjectListOptions options = ObjectListOptions.create()
                .path(objectPath);
        for (SwiftObject obj: m_os.objectStorage().objects().list(m_container, options)) {
            // do not add directory itself
            if (!obj.getName().equals(objectPath)) {
                attrs.add(new SwiftObjectAttributes(obj));
            }
        }
        FileAttributes[] attrArray = new FileAttributes[attrs.size()];
        return attrs.toArray(attrArray);
    }

    @Override
    public void makeDir(String parentAbsolutePath, String directoryName,
            String additionalArgs) throws PermissionDeniedException,
            BadParameterException, AlreadyExistsException, ParentDoesNotExist,
            TimeoutException, NoSuccessException {
        // openstack API createPath() needs "/" at the end
        String pseudoDirPath = SwiftURL.getPath(parentAbsolutePath) + directoryName + "/";
        m_logger.debug("mkdir: " + pseudoDirPath); 
        m_os.objectStorage().containers().createPath(m_container, pseudoDirPath);
    }

    @Override
    public void removeDir(String parentAbsolutePath, String directoryName,
            String additionalArgs) throws PermissionDeniedException,
            BadParameterException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        // pseudo dir has a "/" at the end
        this.removeSwiftObject(SwiftURL.getPath(parentAbsolutePath) + directoryName + "/");
    }

    @Override
    public void removeFile(String parentAbsolutePath, String fileName,
            String additionalArgs) throws PermissionDeniedException,
            BadParameterException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        this.removeSwiftObject(SwiftURL.getPath(parentAbsolutePath) + fileName);
    }

    private void removeSwiftObject(String objectPath) throws PermissionDeniedException,
            BadParameterException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        m_logger.debug("deleting object: " + objectPath); 
        ActionResponse ar = m_os.objectStorage().objects().delete(m_container, objectPath);
        if (!ar.isSuccess()) {
            if (ar.getCode() == 404) {
                throw new DoesNotExistException(objectPath);
            }
            throw new NoSuccessException(ar.getCode() + ":" + ar.getFault());
        }
    }


    @Override
    public void putFromStream(String absolutePath, boolean append,
            String additionalArgs, InputStream stream)
            throws PermissionDeniedException, BadParameterException,
            AlreadyExistsException, ParentDoesNotExist, TimeoutException,
            NoSuccessException {
        String objectPath = SwiftURL.getPath(absolutePath);
        // directory
        ObjectPutOptions options = ObjectPutOptions.create().path(SwiftURL.getDirectoryName(objectPath));
        // upload
        OSClient os = OSFactory.builder()
                .endpoint(m_os.getEndpoint())
                .token(m_token.getId())
                .tenantName(m_tenant)
                .authenticate();
        os.objectStorage().objects().put(m_container, 
                SwiftURL.getFileName(objectPath), 
                Payloads.create(stream), 
                options);
    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    ////////////
    // Private 
    ////////////
    private SwiftObject getSwiftObject(String containerPath) throws DoesNotExistException {
        SwiftObject so;
        if (!containerPath.endsWith("/")) {
            // catch a NumberFormatException in case of pseudo directory
            try {
                so = m_os.objectStorage().objects().get(m_container, containerPath);
                if (so != null) {
                    return so;
                } else {
                    throw new DoesNotExistException(containerPath);
                }
            } catch (NumberFormatException nfe) {
                // ignore
            }
        }
        // Otherwise list objects
        List<? extends SwiftObject> objs = m_os.objectStorage().objects().list(m_container);
        for (SwiftObject obj: m_os.objectStorage().objects().list(m_container)) {
            if (obj.getName().equals(containerPath)) {
                return obj;
            }
        }
        throw new DoesNotExistException(containerPath);
    }

}
