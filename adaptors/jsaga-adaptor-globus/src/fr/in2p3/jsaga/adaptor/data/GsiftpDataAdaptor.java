package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.optimise.*;
import fr.in2p3.jsaga.adaptor.data.read.*;
import fr.in2p3.jsaga.adaptor.data.read.FileReader;
import fr.in2p3.jsaga.adaptor.data.write.DirectoryWriter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
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
public class GsiftpDataAdaptor implements FileReader, FileWriter, DirectoryReader, DirectoryWriter, DataCopy, DataRename, DataGet, DataPut {
    private GsiftpDataAdaptorAbstract m_adaptor;

    public GsiftpDataAdaptor() {
        m_adaptor = new GsiftpDefaultDataAdaptor();
    }

    public String[] getSchemeAliases() {
        return new String[]{"gsiftp", "gridftp"};
    }

    public Usage getUsage() {
        return m_adaptor.getUsage();
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return m_adaptor.getDefaults(attributes);
    }

    public Class[] getSupportedSecurityAdaptorClasses() {
        return m_adaptor.getSupportedSecurityAdaptorClasses();
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        m_adaptor.setSecurityAdaptor(securityAdaptor);
    }

    public int getDefaultPort() {
        return m_adaptor.getDefaultPort();
    }

    public void connect(String userInfo, String host, int port, Map attributes) throws AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
        // connect
        m_adaptor.connect(userInfo, host, port, attributes);

        // check version
        boolean old;
        try {
            m_adaptor.m_client.quote("HELP MLSD");
            old = false;
        } catch (ServerException e) {
            if (e.getMessage().indexOf("Unknown command MLSD") > -1) {
                old = true;
            } else {
                // unknown
                throw new NoSuccess(e);
            }
        } catch (IOException e) {
            throw new NoSuccess(e);
        }

        // may replace implementation
        GsiftpDataAdaptorAbstract sav = m_adaptor;
        if (old) {
            m_adaptor = new Gsiftp1DataAdaptor();
        } else {
            m_adaptor = new Gsiftp2DataAdaptor();
        }
        m_adaptor.m_client = sav.m_client;
        m_adaptor.m_credential = sav.m_credential;
    }

    public void disconnect() throws NoSuccess {
        m_adaptor.disconnect();
    }

    public boolean exists(String absolutePath) throws PermissionDenied, Timeout, NoSuccess {
        return m_adaptor.exists(absolutePath);
    }

    public boolean isDirectory(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        return m_adaptor.isDirectory(absolutePath);
    }

    public boolean isEntry(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        return m_adaptor.isEntry(absolutePath);
    }

    public long getSize(String absolutePath) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        return m_adaptor.getSize(absolutePath);
    }

    public InputStream getInputStream(String absolutePath) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        return m_adaptor.getInputStream(absolutePath);
    }

    public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append) throws PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return m_adaptor.getOutputStream(parentAbsolutePath, fileName, exclusive, append);
    }

    /** does not work */
    public void getToStream(String absolutePath, OutputStream stream) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        m_adaptor.getToStream(absolutePath, stream);
    }

    /** does not work */
    public void putFromStream(String absolutePath, InputStream stream, boolean append) throws PermissionDenied, BadParameter, AlreadyExists, Timeout, NoSuccess {
        m_adaptor.putFromStream(absolutePath, stream, append);
    }

    public void copy(String sourceAbsolutePath, String targetHost, int targetPort, String targetAbsolutePath, boolean overwrite) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        m_adaptor.copy(sourceAbsolutePath, targetHost, targetPort, targetAbsolutePath, overwrite);
    }

    public void copyFrom(String sourceHost, int sourcePort, String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        m_adaptor.copyFrom(sourceHost, sourcePort, sourceAbsolutePath, targetAbsolutePath, overwrite);
    }

    public void rename(String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite) throws PermissionDenied, BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        m_adaptor.rename(sourceAbsolutePath, targetAbsolutePath, overwrite);
    }

    public void removeFile(String parentAbsolutePath, String fileName) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        m_adaptor.removeFile(parentAbsolutePath, fileName);
    }

    public FileAttributes[] listAttributes(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        return m_adaptor.listAttributes(absolutePath);
    }

    public void makeDir(String parentAbsolutePath, String directoryName) throws PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        m_adaptor.makeDir(parentAbsolutePath, directoryName);
    }

    public void removeDir(String parentAbsolutePath, String directoryName) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        m_adaptor.removeDir(parentAbsolutePath, directoryName);
    }
}
