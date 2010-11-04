package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStreamFactory;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import org.globus.ftp.FeatureList;
import org.globus.ftp.exception.ServerException;
import org.ogf.saga.error.*;

import java.io.*;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GsiftpDataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GsiftpDataAdaptor implements DataCopy, DataRename, FileReaderStreamFactory, FileWriterStreamFactory
{
    private GsiftpDataAdaptorAbstract m_adaptor;

    public GsiftpDataAdaptor() {
        m_adaptor = new GsiftpDefaultDataAdaptor();
    }

    public String getType() {
        return "gsiftp";
    }

    public Usage getUsage() {
        return m_adaptor.getUsage();
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return m_adaptor.getDefaults(attributes);
    }

    public Class[] getSupportedSecurityCredentialClasses() {
        return m_adaptor.getSupportedSecurityCredentialClasses();
    }

    public void setSecurityCredential(SecurityCredential credential) {
        m_adaptor.setSecurityCredential(credential);
    }

    public int getDefaultPort() {
        return m_adaptor.getDefaultPort();
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        // connect
        m_adaptor.connect(userInfo, host, port, null, attributes);

        // check version
        FeatureList fl;
        try {
            fl = m_adaptor.m_client.getFeatureList();
        } catch (IOException e) {
            throw new NoSuccessException(e);
        } catch (ServerException e) {
            throw new NoSuccessException(e);
        }

        // replace implementation
        GsiftpDataAdaptorAbstract sav = m_adaptor;
        if (fl.contains("PARALLEL") && fl.contains("SIZE") && fl.contains("ERET") && fl.contains("ESTO")) {
            // <*>              = PARALLEL, SIZE, ERET, ESTO
            if (fl.contains("DCAU") && fl.contains("MDTM") && fl.contains("REST STREAM")) {
                // <globus>     = <*> + DCAU, MDTM, "REST STREAM"
                if (fl.contains("SPAS") && fl.contains("SPOR")) {
                    // <new>    = <globus> + MLST..., SPAS, SPOR, UTF8, "LANG EN"
                    m_adaptor = new Gsiftp2DataAdaptor();
                } else {
                    // <old>    = <globus>
                    m_adaptor = new Gsiftp1DataAdaptor();
                }
            } else if (fl.contains("SBUF") && fl.contains("EOF")) {
                // <oldDCache>  = <*> + SBUF + EOF
                // <newDCache>  = <*> + SBUF + EOF + GETPUT, CKSM, SCKS, MODEX
                m_adaptor = new GsiftpDCacheDataAdaptor();
            }
        } else {
            throw new NotImplementedException("Unsupported server implementation");
        }
        m_adaptor.m_client = sav.m_client;
        m_adaptor.m_credential = sav.m_credential;
    }

    public void disconnect() throws NoSuccessException {
        m_adaptor.disconnect();
    }

    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        return m_adaptor.exists(absolutePath, additionalArgs);
    }

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        return m_adaptor.getInputStream(absolutePath, additionalArgs);
    }

    public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        return m_adaptor.getOutputStream(parentAbsolutePath, fileName, exclusive, append, additionalArgs);
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
