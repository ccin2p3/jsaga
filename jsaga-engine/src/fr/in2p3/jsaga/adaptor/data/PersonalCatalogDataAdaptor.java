package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.impl.DataCatalogHandler;
import fr.in2p3.jsaga.adaptor.data.link.LinkAdaptor;
import fr.in2p3.jsaga.adaptor.data.link.NotLink;
import fr.in2p3.jsaga.adaptor.data.read.*;
import fr.in2p3.jsaga.adaptor.data.write.DirectoryWriter;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.adaptor.schema.data.catalog.*;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   PersonalCatalogDataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   19 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class PersonalCatalogDataAdaptor implements LogicalReader, LogicalWriter, DirectoryReader, DirectoryWriter, LinkAdaptor {
    private DataCatalogHandler m_catalog;

    public String[] getSupportedContextTypes() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        // do nothing
    }

    public String getScheme() {
        return "catalog";
    }

    public String[] getSchemeAliases() {
        return null;
    }

    public void connect(String userInfo, String host, int port) throws AuthenticationFailed, AuthorizationFailed, Timeout, NoSuccess {
        m_catalog = DataCatalogHandler.getInstance();
        m_catalog.commit();
    }

    public void disconnect() throws NoSuccess {
        m_catalog.commit();
    }

    public boolean exists(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        try {
            m_catalog.getEntry(absolutePath);
            return true;
        } catch (IncorrectState e) {
            return false;
        }
    }

    public boolean isDirectory(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        EntryType entry = m_catalog.getEntry(absolutePath);
        return entry instanceof DirectoryType;
    }

    public boolean isEntry(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        EntryType entry = m_catalog.getEntry(absolutePath);
        return entry instanceof FileType;
    }

    public void addLocation(String logicalEntry, URI replicaEntry) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        // get or create logical entry
        File file;
        try {
            file = m_catalog.getFile(logicalEntry);
        } catch(IncorrectState e) {
            try {
                file = m_catalog.addFile(logicalEntry);
            } catch (DoesNotExist e2) {
                throw new IncorrectState(e2);
            }
        }
        // add replica location
        if (arrayContains(file.getReplica(), replicaEntry.toString())) {
            throw new AlreadyExists("Replica location already registered: "+replicaEntry.toString());
        } else {
            file.addReplica(replicaEntry.toString());
        }
        m_catalog.commit();
    }

    public void removeLocation(String logicalEntry, URI replicaEntry) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        // get logical entry
        File file = m_catalog.getFile(logicalEntry);
        // remove replica location
        if (! file.removeReplica(replicaEntry.toString())) {
            throw new DoesNotExist("Replica location no registered: "+replicaEntry.toString());
        }
        m_catalog.commit();
    }

    public void removeFile(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        m_catalog.removeFile(absolutePath);
        m_catalog.commit();
    }

    public String[] listLocations(String logicalEntry) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        File file = m_catalog.getFile(logicalEntry);
        return file.getReplica();
    }

    public String[] list(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        EntryType[] list = m_catalog.listEntries(absolutePath);
        String[] ret = new String[list.length];
        for (int i=0; i<list.length; i++) {
            ret[i] = list[i].getName();
        }
        return ret;
    }

    public FileAttributes[] listAttributes(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        EntryType[] list = m_catalog.listEntries(absolutePath);
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
                    ret[i].size = file.getReplicaCount();
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
            parent = m_catalog.getDirectory(parentAbsolutePath);
        } catch (IncorrectState e) {
            throw new DoesNotExist("Parent directory does not exist: "+parentAbsolutePath);
        }
        try {
            m_catalog.getEntry(parent, directoryName);
            throw new AlreadyExists("Entry already exists: "+directoryName);
        } catch(IncorrectState e) {
            m_catalog.addDirectory(parent, directoryName);
        }
        m_catalog.commit();
    }

    public void removeDir(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        m_catalog.removeDirectory(absolutePath);
        m_catalog.commit();
    }

    public boolean isLink(String absolutePath) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        EntryType entry = m_catalog.getEntry(absolutePath);
        return entry instanceof FileType && entry.getLink()!=null;
    }

    public String readLink(String absolutePath) throws NotLink, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        EntryType entry = m_catalog.getEntry(absolutePath);
        if (entry instanceof FileType && entry.getLink()!=null) {
            return entry.getLink();
        } else {
            throw new NotLink(absolutePath);
        }
    }

    public void link(String sourceAbsolutePath, String linkAbsolutePath, boolean overwrite) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        File link;
        try {
            link = m_catalog.getFile(linkAbsolutePath);
            if (! overwrite) {
                throw new AlreadyExists("Link already exists: "+linkAbsolutePath);
            }
        } catch(IncorrectState e) {
            try {
                link = m_catalog.addFile(linkAbsolutePath);
            } catch (DoesNotExist e2) {
                throw new IncorrectState("Parent directory does not exist for link: "+linkAbsolutePath, e2);
            }
        }
        link.setLink(sourceAbsolutePath);
    }

    private boolean arrayContains(String[] array, String value) {
        for (int i=0; array!=null && i<array.length; i++) {
            if (array[i].equals(value)) {
                return true;
            }
        }
        return false;
    }
}
