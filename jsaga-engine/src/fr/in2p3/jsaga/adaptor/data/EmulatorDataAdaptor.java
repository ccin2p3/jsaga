package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.impl.DataEmulatorConnection;
import fr.in2p3.jsaga.adaptor.data.impl.DataEmulatorConnectionAbstract;
import fr.in2p3.jsaga.adaptor.data.link.LinkAdaptor;
import fr.in2p3.jsaga.adaptor.data.link.NotLink;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStreamFactory;
import fr.in2p3.jsaga.adaptor.schema.data.emulator.*;
import fr.in2p3.jsaga.adaptor.schema.data.emulator.File;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import org.ogf.saga.error.*;

import java.io.*;
import java.util.Map;

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
public class EmulatorDataAdaptor implements FileReaderStreamFactory, FileWriterStreamFactory, LinkAdaptor
//        , DataFilteredList
{
    protected DataEmulatorConnectionAbstract m_server;

    public String getType() {
        return "test";
    }

    public Usage getUsage() {
        return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;
    }

    public Class[] getSupportedSecurityCredentialClasses() {
        return null;    // no context class
    }

    public void setSecurityCredential(SecurityCredential credential) {
        // do nothing
    }

    public int getDefaultPort() {
        return 1234;
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailedException, AuthorizationFailedException, TimeoutException, NoSuccessException {
        m_server = new DataEmulatorConnection(this.getType(), host, port);
        if(Base.DEBUG) m_server.commit();
    }

    public void disconnect() throws NoSuccessException {
        m_server.commit();
    }

    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
            m_server.getEntry(absolutePath);
            return true;
        } catch (DoesNotExistException e) {
            return false;
        }
    }

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        File file = m_server.getFile(absolutePath);
        String content = file.getContent()!=null ? file.getContent() : "";
        return new ByteArrayInputStream(content.getBytes());
    }

    public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        DirectoryType parent;
        try {
            parent = m_server.getDirectory(parentAbsolutePath);
        } catch (DoesNotExistException e) {
            throw new ParentDoesNotExist("Parent directory does not exist");
        }
        File file;
        try {
            file = m_server.getFile(parent, fileName);
            if (exclusive) {
                throw new AlreadyExistsException("File already exists");
            } else if (append) {
                // do nothing
            } else {
                file.setContent(null);  //overwrite
            }
        } catch(DoesNotExistException e) {
            file = m_server.addFile(parent, fileName);
        }
        return new EmulatorOutputStream(m_server, file);
    }

    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        DirectoryType parent = m_server.getDirectory(parentAbsolutePath);
        m_server.removeFile(parent, fileName);
        if(Base.DEBUG) m_server.commit();
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        EntryType entry = m_server.getEntry(absolutePath);
        return new EmulatorFileAttributes(entry);
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        EntryType[] list = m_server.listEntries(absolutePath);
        FileAttributes[] ret = new FileAttributes[list.length];
        for (int i=0; i<list.length; i++) {
            ret[i] = new EmulatorFileAttributes(list[i]);
        }
        return ret;
    }

    public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        DirectoryType parent;
        try {
            parent = m_server.getDirectory(parentAbsolutePath);
        } catch (DoesNotExistException e) {
            throw new ParentDoesNotExist("Parent directory does not exist");
        }
        try {
            m_server.getEntry(parent, directoryName);
            throw new AlreadyExistsException("Entry already exists");
        } catch(DoesNotExistException e) {
            m_server.addDirectory(parent, directoryName);
        }
        if(Base.DEBUG) m_server.commit();
    }

    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        DirectoryType parent = m_server.getDirectory(parentAbsolutePath);
        m_server.removeDirectory(parent, directoryName);
        if(Base.DEBUG) m_server.commit();
    }

    public boolean isLink(String absolutePath) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        EntryType entry = m_server.getEntry(absolutePath);
        return entry instanceof FileType && ((FileType)entry).getLink()!=null;
    }

    public String readLink(String absolutePath) throws NotLink, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        EntryType entry = m_server.getEntry(absolutePath);
        if (entry instanceof FileType && ((FileType)entry).getLink()!=null) {
            return ((FileType)entry).getLink();
        } else {
            throw new NotLink(absolutePath);
        }
    }

    public void link(String sourceAbsolutePath, String linkAbsolutePath, boolean overwrite) throws PermissionDeniedException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException {
        File link;
        try {
            link = m_server.getFile(linkAbsolutePath);
            if (! overwrite) {
                throw new AlreadyExistsException("Link already exists");
            }
        } catch(DoesNotExistException e) {
            link = m_server.addFile(linkAbsolutePath);
        }
        link.setLink(sourceAbsolutePath);
    }

    ///////////////////////////////////////// optional interfaces /////////////////////////////////////////

    public FileAttributes[] listDirectories(String absolutePath) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        EntryType[] list = m_server.listDirectories(absolutePath);
        FileAttributes[] ret = new FileAttributes[list.length];
        for (int i=0; i<list.length; i++) {
            ret[i] = new EmulatorFileAttributes(list[i]);
        }
        return ret;
    }
}
