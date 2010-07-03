package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.impl.DataCatalogHandler;
import fr.in2p3.jsaga.adaptor.data.link.LinkAdaptor;
import fr.in2p3.jsaga.adaptor.data.link.NotLink;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReaderMetaData;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriterMetaData;
import fr.in2p3.jsaga.adaptor.schema.data.catalog.*;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;

import java.util.*;

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
public class PersonalCatalogDataAdaptor implements LogicalReaderMetaData, LogicalWriterMetaData, LinkAdaptor {
    private DataCatalogHandler m_catalog;

    public String getType() {
        return "catalog";
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

    public BaseURL getBaseURL() throws IncorrectURLException {
        return null;
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailedException, AuthorizationFailedException, IncorrectURLException, TimeoutException, NoSuccessException {
        if (host != null) {
            throw new IncorrectURLException("Protocol '"+this.getType()+"' does not support hostname in URL: "+host);
        }
        m_catalog = DataCatalogHandler.getInstance();
        if(Base.DEBUG) m_catalog.commit();
    }

    public void disconnect() throws NoSuccessException {
        m_catalog.commit();
    }

    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
            m_catalog.getEntry(absolutePath);
            return true;
        } catch (DoesNotExistException e) {
            return false;
        }
    }

    public void create(String logicalEntry, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        try {
            m_catalog.getFile(logicalEntry);
            throw new AlreadyExistsException("File already exists: "+logicalEntry);
        } catch (DoesNotExistException e) {
            try {
                m_catalog.addFile(logicalEntry);
            } catch (DoesNotExistException e2) {
                throw new ParentDoesNotExist(e2);
            }
            if(Base.DEBUG) m_catalog.commit();
        }
    }

    public void addLocation(String logicalEntry, URL replicaEntry, String additionalArgs) throws PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        // get or create logical entry
        File file;
        try {
            file = m_catalog.getFile(logicalEntry);
        } catch(DoesNotExistException e) {
            throw new IncorrectStateException(e);
        }
        // add replica location (if it does not already exist)
        if (! arrayContains(file.getReplica(), replicaEntry.toString())) {
            file.addReplica(replicaEntry.toString());
        }
        if(Base.DEBUG) m_catalog.commit();
    }

    public void removeLocation(String logicalEntry, URL replicaEntry, String additionalArgs) throws PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        // get logical entry
        File file = m_catalog.getFile(logicalEntry);
        // remove replica location
        if (! file.removeReplica(replicaEntry.toString())) {
            throw new DoesNotExistException("Replica location not registered");
        }
        if(Base.DEBUG) m_catalog.commit();
    }

    public String[] listLocations(String logicalEntry, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        File file = m_catalog.getFile(logicalEntry);
        return file.getReplica();
    }

    public void setMetaData(String logicalEntry, String name, String[] values, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        EntryType entry;
        try {
            entry = m_catalog.getEntry(logicalEntry);
        } catch (DoesNotExistException e) {
            throw new NoSuccessException(e);
        }
        Metadata md = findMetadata(entry, name);
        if (md != null) {
            md.setName(name);
            md.setValue(values);
        } else {
            md = new Metadata();
            md.setName(name);
            md.setValue(values);
            entry.addMetadata(md);
        }
    }

    public void removeMetaData(String logicalEntry, String name, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException, DoesNotExistException {
        EntryType entry;
        try {
            entry = m_catalog.getEntry(logicalEntry);
        } catch (DoesNotExistException e) {
            throw new NoSuccessException(e);
        }
        Metadata md = findMetadata(entry, name);
        if (md != null) {
            entry.removeMetadata(md);
        } else {
            throw new DoesNotExistException("Meta-data '"+name+"' does not exist for entry: "+logicalEntry);
        }
    }

    public Map listMetaData(String logicalEntry, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        EntryType entry;
        try {
            entry = m_catalog.getEntry(logicalEntry);
        } catch (DoesNotExistException e) {
            throw new NoSuccessException(e);
        }
        Map map = new HashMap();
        for (int i=0; i<entry.getMetadataCount(); i++) {
            Metadata md = entry.getMetadata(i);
            map.put(md.getName(), md.getValue());
        }
        return map;
    }

    public FileAttributes[] findAttributes(String logicalDir, Map keyValuePatterns, boolean recursive, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        List found = new ArrayList();
        DirectoryType dir = m_catalog.getDirectory(logicalDir);
        findAttributesRecursive(dir, keyValuePatterns, found, null, recursive);
        return (FileAttributes[]) found.toArray(new FileAttributes[found.size()]);
    }
    private void findAttributesRecursive(DirectoryType dir, Map keyValuePatterns, List found, String relativePath, boolean recursive) {
        for (int i=0; i<dir.getDirectoryCount(); i++) {
            Directory childDir = dir.getDirectory(i);
            String childRelativePath = (relativePath!=null ? relativePath+"/"+childDir.getName() : childDir.getName());
            if (matchesAllPatterns(childDir, keyValuePatterns)) {
                found.add(new CatalogFileAttributes(childDir, childRelativePath));
            }
            if (recursive) {
                findAttributesRecursive(childDir, keyValuePatterns, found, childRelativePath, recursive);
            }
        }
        for (int i=0; i<dir.getFileCount(); i++) {
            File childFile = dir.getFile(i);
            String childRelativePath = (relativePath!=null ? relativePath+"/"+childFile.getName() : childFile.getName());
            if (matchesAllPatterns(childFile, keyValuePatterns)) {
                found.add(new CatalogFileAttributes(childFile, childRelativePath));
            }
        }
    }
    private boolean matchesAllPatterns(EntryType entry, Map keyValuePatterns) {
        boolean matchAllPatterns = (keyValuePatterns.size()>0);
        for (Iterator it=keyValuePatterns.entrySet().iterator(); matchAllPatterns && it.hasNext(); ) {
            Map.Entry e = (Map.Entry) it.next();
            String key = (String) e.getKey();
            String value = (String) e.getValue();
            Metadata md = findMetadata(entry, key);
            matchAllPatterns &= (md!=null && (value==null || toSet(md.getValue()).contains(value)));
        }
        return matchAllPatterns;
    }
    private Set toSet(String[] values) {
        Set<String> set = new HashSet<String>();
        for (int i=0; values!=null && i<values.length; i++) {
            set.add(values[i]);
        }
        return set;
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        EntryType[] list = m_catalog.listEntries(absolutePath);
        FileAttributes[] ret = new FileAttributes[list.length];
        for (int i=0; i<list.length; i++) {
            ret[i] = new CatalogFileAttributes(list[i]);
        }
        return ret;
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        EntryType entry = m_catalog.getEntry(absolutePath);
        return new CatalogFileAttributes(entry);
    }

    public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        DirectoryType parent;
        try {
            parent = m_catalog.getDirectory(parentAbsolutePath);
        } catch (DoesNotExistException e) {
            throw new ParentDoesNotExist("Parent directory does not exist");
        }
        try {
            m_catalog.getEntry(parent, directoryName);
            throw new AlreadyExistsException("Entry already exists");
        } catch(DoesNotExistException e) {
            m_catalog.addDirectory(parent, directoryName);
        }
        if(Base.DEBUG) m_catalog.commit();
    }

    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        DirectoryType parent = m_catalog.getDirectory(parentAbsolutePath);
        m_catalog.removeDirectory(parent, directoryName);
        if(Base.DEBUG) m_catalog.commit();
    }

    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        DirectoryType parent = m_catalog.getDirectory(parentAbsolutePath);
        m_catalog.removeFile(parent, fileName);
        if(Base.DEBUG) m_catalog.commit();
    }

    public boolean isLink(String absolutePath) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        EntryType entry = m_catalog.getEntry(absolutePath);
        return entry instanceof FileType && entry.getLink()!=null;
    }

    public String readLink(String absolutePath) throws NotLink, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        EntryType entry = m_catalog.getEntry(absolutePath);
        if (entry instanceof FileType && entry.getLink()!=null) {
            return entry.getLink();
        } else {
            throw new NotLink(absolutePath);
        }
    }

    public void link(String sourceAbsolutePath, String linkAbsolutePath, boolean overwrite) throws PermissionDeniedException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException {
        File link;
        try {
            link = m_catalog.getFile(linkAbsolutePath);
            if (! overwrite) {
                throw new AlreadyExistsException("Link already exists");
            }
        } catch(DoesNotExistException e) {
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

    private Metadata findMetadata(EntryType entry, String name) {
        for (int i=0; i<entry.getMetadataCount(); i++) {
            Metadata md = entry.getMetadata(i);
            if (md.getName().equals(name)) {
                return md;
            }
        }
        return null;
    }
}
