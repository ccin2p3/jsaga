package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.impl.DataEmulatorConnection;
import fr.in2p3.jsaga.adaptor.data.impl.DataEmulatorConnectionAbstract;
import fr.in2p3.jsaga.adaptor.data.link.LinkAdaptor;
import fr.in2p3.jsaga.adaptor.data.link.NotLink;
import fr.in2p3.jsaga.adaptor.data.read.*;
import fr.in2p3.jsaga.adaptor.data.readwrite.DataCopy;
import fr.in2p3.jsaga.adaptor.data.write.*;
import fr.in2p3.jsaga.adaptor.schema.data.emulator.*;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.ogf.saga.error.*;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.Serializable;
import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   EmulatorDataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class EmulatorDataAdaptor implements FileReader, FileWriter, DirectoryReader, DirectoryWriter, LinkAdaptor, DataCopy {
    protected DataEmulatorConnectionAbstract m_server;

    public String[] getSupportedContextTypes() {
        return null;
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        // do nothing
    }

    public String getScheme() {
        return "test";
    }

    public String[] getSchemeAliases() {
        return new String[]{"emulated"};
    }

    public void connect(String userInfo, String host, int port) throws AuthenticationFailed, AuthorizationFailed, Timeout, NoSuccess {
        m_server = new DataEmulatorConnection("test", host, port);
        m_server.commit();
    }

    public void disconnect() throws NoSuccess {
        m_server.commit();
    }

    public boolean exists(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        try {
            m_server.getEntry(absolutePath);
            return true;
        } catch (IncorrectState e) {
            return false;
        }
    }

    public boolean isDirectory(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        EntryType entry = m_server.getEntry(absolutePath);
        return entry instanceof DirectoryType;
    }

    public boolean isEntry(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        EntryType entry = m_server.getEntry(absolutePath);
        return entry instanceof FileType;
    }

    public int getSize(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        File file = m_server.getFile(absolutePath);
        return file.getContent().length();
    }

    public FileReaderStream openFileReaderStream(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        File file = m_server.getFile(absolutePath);
        return new EmulatorFileReaderStream(file.getContent());
    }

    public FileWriterStream openFileWriterStream(String parentAbsolutePath, String fileName, boolean overwrite, boolean append) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        DirectoryType parent;
        try {
            parent = m_server.getDirectory(parentAbsolutePath);
        } catch (IncorrectState e) {
            throw new DoesNotExist("Parent directory does not exist: "+parentAbsolutePath);
        }
        File file;
        try {
            file = m_server.getFile(parent, fileName);
            if (append) {
                // do nothing
            } else if (overwrite) {
                file.setContent(null);
            } else {
                throw new AlreadyExists("File already exists: "+fileName);
            }
        } catch(IncorrectState e) {
            file = m_server.addFile(parent, fileName);
        }
        return new EmulatorFileWriterStream(m_server, file);
    }

    public void removeFile(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        m_server.removeFile(absolutePath);
        m_server.commit();
    }

    public String[] list(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        EntryType[] list = m_server.listEntries(absolutePath);
        String[] ret = new String[list.length];
        for (int i=0; i<list.length; i++) {
            ret[i] = list[i].getName();
        }
        return ret;
    }

    public FileAttributes[] listAttributes(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        EntryType[] list = m_server.listEntries(absolutePath);
        FileAttributes[] ret = new FileAttributes[list.length];
        for (int i=0; i<list.length; i++) {
            ret[i] = new FileAttributes();
            ret[i].name = list[i].getName();
            if (list[i] instanceof DirectoryType) {
                ret[i].type = FileAttributes.DIRECTORY_TYPE;
                ret[i].size = 0;
            } else if (list[i] instanceof FileType) {
                FileType file = (FileType) list[i];
                if (file.getLink() != null) {
                    ret[i].type = FileAttributes.LINK_TYPE;
                    ret[i].size = 0;
                } else {
                    ret[i].type = FileAttributes.FILE_TYPE;
                    ret[i].size = (file.getContent()!=null ? file.getContent().length() : 0);
                }
            } else {
                ret[i].type = FileAttributes.UNKNOWN_TYPE;
                ret[i].size = -1;
            }
        }
        return ret;
    }

    public void makeDir(String parentAbsolutePath, String directoryName) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        DirectoryType parent;
        try {
            parent = m_server.getDirectory(parentAbsolutePath);
        } catch (IncorrectState e) {
            throw new DoesNotExist("Parent directory does not exist: "+parentAbsolutePath);
        }
        try {
            m_server.getEntry(parent, directoryName);
            throw new AlreadyExists("Entry already exists: "+directoryName);
        } catch(IncorrectState e) {
            m_server.addDirectory(parent, directoryName);
        }
        m_server.commit();
    }

    public void removeDir(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        m_server.removeDirectory(absolutePath);
        m_server.commit();
    }

    public boolean isLink(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        EntryType entry = m_server.getEntry(absolutePath);
        return entry instanceof FileType && ((FileType)entry).getLink()!=null;
    }

    public String readLink(String absolutePath) throws NotLink, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        EntryType entry = m_server.getEntry(absolutePath);
        if (entry instanceof FileType && ((FileType)entry).getLink()!=null) {
            return ((FileType)entry).getLink();
        } else {
            throw new NotLink(absolutePath);
        }
    }

    public void link(String sourceAbsolutePath, String linkAbsolutePath, boolean overwrite) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        File link;
        try {
            link = m_server.getFile(linkAbsolutePath);
            if (! overwrite) {
                throw new AlreadyExists("Link already exists: "+linkAbsolutePath);
            }
        } catch(IncorrectState e) {
            try {
                link = m_server.addFile(linkAbsolutePath);
            } catch (DoesNotExist e2) {
                throw new IncorrectState("Parent directory does not exist for link: "+linkAbsolutePath, e2);
            }
        }
        link.setLink(sourceAbsolutePath);
    }

    public void copy(String sourceAbsolutePath, String targetHost, int targetPort, String targetAbsolutePath, boolean overwrite) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        File file = m_server.getFile(sourceAbsolutePath);
        DataEmulatorConnection targetServer = new DataEmulatorConnection("test", targetHost, targetPort);

        // check if exists
        try {
            targetServer.getFile(targetAbsolutePath);
            if (!overwrite) {
                throw new AlreadyExists("File already exist: "+targetAbsolutePath);
            } else {
                targetServer.removeFile(targetAbsolutePath);
            }
        } catch(IncorrectState e) {
            // do nothing
        }

        // clone file
        File fileClone = (File) clone(file);

        // get parent directory
        DirectoryType parent;
        try {
            parent = targetServer.getDirectory(targetAbsolutePath);
        } catch(IncorrectState e) {
            parent = targetServer.getParentDirectory(targetAbsolutePath);
            fileClone.setName(targetServer.getEntryName(targetAbsolutePath));
        }

        // add clone to target server
        parent.addFile(fileClone);
        targetServer.commit();
    }

    public static Serializable clone(Serializable bean) throws NoSuccess {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Marshaller.marshal(bean, doc);
            return (Serializable) Unmarshaller.unmarshal(bean.getClass(), doc);
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }
}
