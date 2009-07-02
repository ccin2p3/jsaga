package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderGetter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterPutter;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import org.globus.ftp.*;
import org.ogf.saga.error.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   GsiftpDPMDataAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   2 juil. 2009
 * ***************************************************
 * Description:                                      */
/**
 * workaround for DPM
 */
public class GsiftpDPMDataAdaptor implements DataCopy, DataRename, FileReaderGetter, FileWriterPutter {
    private Gsiftp2DataAdaptor m_adaptor;

    public GsiftpDPMDataAdaptor() {
        m_adaptor = new Gsiftp2DataAdaptor();
    }

    public String getType() {
        return "gsiftp-dpm";
    }

    public void getToStream(String absolutePath, String additionalArgs, OutputStream stream) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        final boolean autoFlush = false;
        final boolean ignoreOffset = true;
        try {
            m_adaptor.m_client.setType(GridFTPSession.TYPE_IMAGE);
            m_adaptor.m_client.setMode(GridFTPSession.MODE_STREAM); //MODE_EBLOCK induce error: "451 refusing to store with active mode"
            m_adaptor.m_client.setPassive();
            m_adaptor.m_client.setLocalActive();
            m_adaptor.m_client.get(
                    absolutePath,
                    new DataSinkStream(stream, autoFlush, ignoreOffset),
                    null);
        } catch (Exception e) {
            throw m_adaptor.rethrowException(e);
        }
    }

    public void putFromStream(String absolutePath, boolean append, String additionalArgs, InputStream stream) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        final int DEFAULT_BUFFER_SIZE = 16384;
        try {
            m_adaptor.m_client.setType(GridFTPSession.TYPE_IMAGE);
            m_adaptor.m_client.setMode(GridFTPSession.MODE_EBLOCK);
            m_adaptor.m_client.setPassive();
            m_adaptor.m_client.setLocalActive();
            m_adaptor.m_client.put(
                absolutePath,
                new DataSourceStream(stream, DEFAULT_BUFFER_SIZE),
                    null,
                    append);
        } catch (Exception e) {
            try {
                throw m_adaptor.rethrowExceptionFull(e);
            } catch (DoesNotExistException e2) {
                throw new ParentDoesNotExist(e);
            }
        }
    }

    public Usage getUsage() {
        return m_adaptor.getUsage();
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return m_adaptor.getDefaults(attributes);
    }

    public Class[] getSupportedSecurityAdaptorClasses() {
        return m_adaptor.getSupportedSecurityAdaptorClasses();
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        m_adaptor.setSecurityAdaptor(securityAdaptor);
    }

    public BaseURL getBaseURL() throws IncorrectURLException {
        return m_adaptor.getBaseURL();
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        m_adaptor.connect(userInfo, host, port, null, attributes);
    }

    public void disconnect() throws NoSuccessException {
        m_adaptor.disconnect();
    }

    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        return m_adaptor.exists(absolutePath, additionalArgs);
    }

    public void copy(String sourceAbsolutePath, String targetHost, int targetPort, String targetAbsolutePath, boolean overwrite, String additionalArgs) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        m_adaptor.copy(sourceAbsolutePath, targetHost, targetPort, targetAbsolutePath, overwrite, additionalArgs);
    }

    public void copyFrom(String sourceHost, int sourcePort, String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite, String additionalArgs) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        m_adaptor.copyFrom(sourceHost, sourcePort, sourceAbsolutePath, targetAbsolutePath, overwrite, additionalArgs);
    }

    public void rename(String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException {
        m_adaptor.rename(sourceAbsolutePath, targetAbsolutePath, overwrite, additionalArgs);
    }

    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        m_adaptor.removeFile(parentAbsolutePath, fileName, additionalArgs);
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        return m_adaptor.getAttributes(absolutePath, additionalArgs);
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        return m_adaptor.listAttributes(absolutePath, additionalArgs);
    }

    public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        m_adaptor.makeDir(parentAbsolutePath, directoryName, additionalArgs);
    }

    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        m_adaptor.removeDir(parentAbsolutePath, directoryName, additionalArgs);
    }
}
