package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
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

import java.util.Map;

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

    public Usage getUsage() {
        return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return null;
    }

    public Class[] getSupportedSecurityAdaptorClasses() {
        return null;    // no context class
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        // do nothing
    }

    public String[] getSchemeAliases() {
        return new String[]{"catalog"};
    }

    public int getDefaultPort() {
        return 0;
    }

    public void connect(String userInfo, String host, int port, Map attributes) throws AuthenticationFailed, AuthorizationFailed, Timeout, NoSuccess {
        m_catalog = DataCatalogHandler.getInstance();
        if(Base.DEBUG) m_catalog.commit();
    }

    public void disconnect() throws NoSuccess {
        m_catalog.commit();
    }

    public boolean exists(String absolutePath) throws PermissionDenied, Timeout, NoSuccess {
        try {
            m_catalog.getEntry(absolutePath);
            return true;
        } catch (DoesNotExist e) {
            return false;
        }
    }

    public boolean isDirectory(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        EntryType entry = m_catalog.getEntry(absolutePath);
        return entry instanceof DirectoryType;
    }

    public boolean isEntry(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        EntryType entry = m_catalog.getEntry(absolutePath);
        return entry instanceof FileType;
    }

    public void addLocation(String logicalEntry, URI replicaEntry) throws PermissionDenied, IncorrectState, Timeout, NoSuccess {
        // get or create logical entry
        File file;
        try {
            file = m_catalog.getFile(logicalEntry);
        } catch(DoesNotExist e) {
            try {
                file = m_catalog.addFile(logicalEntry);
            } catch (DoesNotExist e2) {
                throw new IncorrectState(e2);
            }
        }
        // add replica location (if it does not already exist)
        if (! arrayContains(file.getReplica(), replicaEntry.toString())) {
            file.addReplica(replicaEntry.toString());
        }
        if(Base.DEBUG) m_catalog.commit();
    }

    public void removeLocation(String logicalEntry, URI replicaEntry) throws PermissionDenied, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        // get logical entry
        File file = m_catalog.getFile(logicalEntry);
        // remove replica location
        if (! file.removeReplica(replicaEntry.toString())) {
            throw new DoesNotExist("Replica location no registered");
        }
        if(Base.DEBUG) m_catalog.commit();
    }

    public void removeFile(String parentAbsolutePath, String fileName) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        DirectoryType parent = m_catalog.getDirectory(parentAbsolutePath);
        m_catalog.removeFile(parent, fileName);
        if(Base.DEBUG) m_catalog.commit();
    }

    public String[] listLocations(String logicalEntry) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        File file = m_catalog.getFile(logicalEntry);
        return file.getReplica();
    }

    public FileAttributes[] listAttributes(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        EntryType[] list = m_catalog.listEntries(absolutePath);
        FileAttributes[] ret = new FileAttributes[list.length];
        for (int i=0; i<list.length; i++) {
            ret[i] = new CatalogFileAttributes(list[i]);
        }
        return ret;
    }

    public void makeDir(String parentAbsolutePath, String directoryName) throws PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        DirectoryType parent = m_catalog.getDirectory(parentAbsolutePath);
        try {
            m_catalog.getEntry(parent, directoryName);
            throw new AlreadyExists("Entry already exists");
        } catch(DoesNotExist e) {
            m_catalog.addDirectory(parent, directoryName);
        }
        if(Base.DEBUG) m_catalog.commit();
    }

    public void removeDir(String parentAbsolutePath, String directoryName) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        DirectoryType parent = m_catalog.getDirectory(parentAbsolutePath);
        m_catalog.removeDirectory(parent, directoryName);
        if(Base.DEBUG) m_catalog.commit();
    }

    public boolean isLink(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        EntryType entry = m_catalog.getEntry(absolutePath);
        return entry instanceof FileType && entry.getLink()!=null;
    }

    public String readLink(String absolutePath) throws NotLink, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        EntryType entry = m_catalog.getEntry(absolutePath);
        if (entry instanceof FileType && entry.getLink()!=null) {
            return entry.getLink();
        } else {
            throw new NotLink(absolutePath);
        }
    }

    public void link(String sourceAbsolutePath, String linkAbsolutePath, boolean overwrite) throws PermissionDenied, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        File link;
        try {
            link = m_catalog.getFile(linkAbsolutePath);
            if (! overwrite) {
                throw new AlreadyExists("Link already exists");
            }
        } catch(DoesNotExist e) {
            link = m_catalog.addFile(linkAbsolutePath);
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
