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
import org.openstack4j.core.transport.HttpResponse;
import org.openstack4j.model.common.DLPayload;
import org.openstack4j.model.common.Payloads;
import org.openstack4j.model.compute.ActionResponse;
import org.openstack4j.model.storage.block.options.DownloadOptions;
import org.openstack4j.model.storage.object.SwiftObject;
import org.openstack4j.model.storage.object.options.ObjectListOptions;
import org.openstack4j.model.storage.object.options.ObjectLocation;
import org.openstack4j.model.storage.object.options.ObjectPutOptions;
import org.openstack4j.openstack.OSFactory;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterPutter;
import fr.in2p3.jsaga.adaptor.openstack.OpenstackAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.openstack.util.SwiftURL;

public class OpenstackDataAdaptor extends OpenstackAdaptorAbstract 
        implements FileReaderStreamFactory, FileWriterPutter {

    protected Logger m_logger = Logger.getLogger(OpenstackDataAdaptor.class);
    
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
    }
    
    @Override
    public boolean exists(String absolutePath, String additionalArgs)
            throws PermissionDeniedException, TimeoutException,
            NoSuccessException {
        ObjectLocation objectLocation = SwiftURL.getObjectLocation(absolutePath);
        m_logger.debug("exists " + objectLocation.getURI());
        try {
            this.getSwiftObject(objectLocation);
            return true;
        } catch (DoesNotExistException e) {
            return false;
        }
    }

    @Override
    public FileAttributes getAttributes(String absolutePath,
            String additionalArgs) throws PermissionDeniedException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        ObjectLocation objectLocation = SwiftURL.getObjectLocation(absolutePath);
        return new SwiftObjectAttributes(this.getSwiftObject(objectLocation));
    }

    @Override
    public FileAttributes[] listAttributes(String absolutePath,
            String additionalArgs) throws PermissionDeniedException,
            BadParameterException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        ObjectLocation objectLocation = SwiftURL.getObjectLocation(absolutePath);
        List<SwiftObjectAttributes> attrs = new ArrayList<SwiftObjectAttributes>();
        ObjectListOptions options = ObjectListOptions.create()
                .path(objectLocation.getObjectName());
        for (SwiftObject obj: m_os.objectStorage().objects().list(objectLocation.getContainerName(), options)) {
            // do not add directory itself
            if (!obj.getName().equals(objectLocation.getObjectName())) {
                attrs.add(new SwiftObjectAttributes(obj));
            }
        }
        FileAttributes[] attrArray = new FileAttributes[attrs.size()];
        return attrs.toArray(attrArray);
    }

    @Override
    public InputStream getInputStream(String absolutePath, String additionalArgs)
            throws PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        ObjectLocation objectLocation = SwiftURL.getObjectLocation(absolutePath);
        DownloadOptions options = DownloadOptions.create();
        DLPayload file = m_os.objectStorage().objects().download(
                objectLocation.getContainerName(), 
                objectLocation.getObjectName(), 
                options);
        file.getInputStream();
        HttpResponse response = file.getHttpResponse();
        if (response.getStatus() == 404) {
            throw new DoesNotExistException(objectLocation.getURI());
        }
        return file.getInputStream();
    }


    @Override
    public void makeDir(String parentAbsolutePath, String directoryName,
            String additionalArgs) throws PermissionDeniedException,
            BadParameterException, AlreadyExistsException, ParentDoesNotExist,
            TimeoutException, NoSuccessException {
        // createPath() needs "/" at the end
//        String pseudoDirPath = SwiftURL.getPath(parentAbsolutePath) + directoryName + "/";
        ObjectLocation objectLocation = SwiftURL.getObjectLocation(parentAbsolutePath + directoryName + "/");
        try {
            this.getSwiftObject(objectLocation);
            throw new AlreadyExistsException(objectLocation.getURI());
        } catch (DoesNotExistException e) {
            // ignore
        }
        // createPath() does not return error if alreadyexistsn must test before
        m_logger.debug("mkdir: " + objectLocation.getURI()); 
        m_os.objectStorage().containers().createPath(objectLocation.getContainerName(), objectLocation.getObjectName());
    }

    @Override
    public void removeDir(String parentAbsolutePath, String directoryName,
            String additionalArgs) throws PermissionDeniedException,
            BadParameterException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        // pseudo dir has a "/" at the end
        this.removeSwiftObject(SwiftURL.getObjectLocation(parentAbsolutePath + directoryName + "/"));
    }

    @Override
    public void removeFile(String parentAbsolutePath, String fileName,
            String additionalArgs) throws PermissionDeniedException,
            BadParameterException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        this.removeSwiftObject(SwiftURL.getObjectLocation(parentAbsolutePath + fileName));
    }

    private void removeSwiftObject(ObjectLocation objectLocation) throws PermissionDeniedException,
            BadParameterException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        m_logger.debug("deleting object: " + objectLocation.getURI()); 
        ActionResponse ar = m_os.objectStorage().objects().delete(objectLocation.getContainerName(), objectLocation.getObjectName());
        if (!ar.isSuccess()) {
            if (ar.getCode() == 404) {
                throw new DoesNotExistException(objectLocation.getURI());
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
        if (append) {
            throw new NoSuccessException("Append mode is not supported");
        }
        ObjectLocation objectLocation = SwiftURL.getObjectLocation(absolutePath);
        // directory
        ObjectPutOptions options = ObjectPutOptions.create().path(SwiftURL.getDirectoryName(objectLocation.getObjectName()));
        // upload
        // needs a client because put is done on a separate thread
        OSClient os = OSFactory.builder()
                .endpoint(m_os.getEndpoint())
                .token(m_token.getId())
                .tenantName(m_tenant)
                .authenticate();
        os.objectStorage().objects().put(
                objectLocation.getContainerName(), 
                SwiftURL.getFileName(objectLocation.getObjectName()), 
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
    private SwiftObject getSwiftObject(ObjectLocation objLocation) throws DoesNotExistException {
        SwiftObject so;
//        if (!containerPath.endsWith("/")) {
            // catch a NumberFormatException in case of pseudo directory
            try {
                so = m_os.objectStorage().objects().get(objLocation);
                if (so != null) {
                    return so;
                } else {
                    throw new DoesNotExistException(objLocation.getURI());
                }
            } catch (NumberFormatException nfe) {
                // ignore
            }
//        }
        // Otherwise list objects
        for (SwiftObject obj: m_os.objectStorage().objects().list(objLocation.getContainerName())) {
            if (obj.getName().equals(objLocation.getObjectName())) {
                return obj;
            }
        }
        throw new DoesNotExistException(objLocation.getURI());
    }
}
