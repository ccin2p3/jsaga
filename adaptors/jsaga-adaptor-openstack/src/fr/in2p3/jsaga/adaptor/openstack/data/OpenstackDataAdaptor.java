package fr.in2p3.jsaga.adaptor.openstack.data;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
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
import org.openstack4j.api.types.ServiceType;
import org.openstack4j.model.storage.object.SwiftObject;
import org.openstack4j.model.storage.object.options.ObjectListOptions;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.openstack.OpenstackAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.openstack.resource.OpenstackResourceAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;

public class OpenstackDataAdaptor extends OpenstackAdaptorAbstract implements DataReaderAdaptor {

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
        super.connect(userInfo, host, port, basePath, attributes);
        // remove ".*/object-store/containers/"
        m_container = basePath.replaceAll("^.*" + ServiceType.OBJECT_STORAGE.getServiceName() + "/containers/", "");
        m_logger.debug("Connected to container " + m_container);
    }
    
    @Override
    public boolean exists(String absolutePath, String additionalArgs)
            throws PermissionDeniedException, TimeoutException,
            NoSuccessException {
        ObjectListOptions options = ObjectListOptions.create()
            .path(absolutePath);
        List<? extends SwiftObject> objs = m_os.objectStorage().objects().list(m_container, options);
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public FileAttributes getAttributes(String absolutePath,
            String additionalArgs) throws PermissionDeniedException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileAttributes[] listAttributes(String absolutePath,
            String additionalArgs) throws PermissionDeniedException,
            BadParameterException, DoesNotExistException, TimeoutException,
            NoSuccessException {
        // TODO Auto-generated method stub
        return null;
    }

}
