package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.data.impl.DataEmulatorConnection;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.schema.data.emulator.*;
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
* File:   OptimizedEmulatorDataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   22 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class OptimizedEmulatorDataAdaptor extends EmulatorDataAdaptor implements DataCopy, DataRename {
    public String getScheme() {
        return "otest";
    }

    public String[] getSchemeAliases() {
        return new String[]{"oemulated"};
    }

    public void copy(String sourceAbsolutePath, String targetHost, int targetPort, String targetAbsolutePath, boolean overwrite) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        File file = m_server.getFile(sourceAbsolutePath);
        DataEmulatorConnection targetServer = new DataEmulatorConnection(this.getScheme(), targetHost, targetPort);

        // check if exists
        try {
            targetServer.getFile(targetAbsolutePath);
            if (!overwrite) {
                throw new AlreadyExists("File already exist");
            } else {
                targetServer.removeFile(targetAbsolutePath);
            }
        } catch(DoesNotExist e) {
            // do nothing
        }

        // clone file
        File fileClone = (File) clone(file);
        fileClone.setName(targetServer.getEntryName(targetAbsolutePath));

        // add clone to target server
        DirectoryType parent = targetServer.getParentDirectory(targetAbsolutePath);
        parent.addFile(fileClone);
        if(Base.DEBUG) targetServer.commit();
    }

    public void copyFrom(String sourceHost, int sourcePort, String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        DataEmulatorConnection sourceServer = new DataEmulatorConnection(this.getScheme(), sourceHost, sourcePort);
        File file = sourceServer.getFile(sourceAbsolutePath);

        // check if exists
        try {
            m_server.getFile(targetAbsolutePath);
            if (!overwrite) {
                throw new AlreadyExists("File already exist");
            } else {
                m_server.removeFile(targetAbsolutePath);
            }
        } catch(DoesNotExist e) {
            // do nothing
        }

        // clone file
        File fileClone = (File) clone(file);
        fileClone.setName(m_server.getEntryName(targetAbsolutePath));

        // add clone to target server
        DirectoryType parent = m_server.getParentDirectory(targetAbsolutePath);
        parent.addFile(fileClone);
        if(Base.DEBUG) m_server.commit();
    }

    public void rename(String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite) throws PermissionDenied, BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        // get parents and entry names
        DirectoryType sourceParent = m_server.getParentDirectory(sourceAbsolutePath);
        DirectoryType targetParent = m_server.getParentDirectory(targetAbsolutePath);
        String sourceEntryName = m_server.getEntryName(sourceAbsolutePath);
        String targetEntryName = m_server.getEntryName(targetAbsolutePath);

        // get entry
        EntryType entry = m_server.getEntry(sourceParent, sourceEntryName);

        // rename entry
        entry.setName(targetEntryName);

        // move entry
        if (entry instanceof Directory) {
            Directory directory = (Directory) entry;
            targetParent.addDirectory(directory);
            sourceParent.removeDirectory(directory);
        } else if (entry instanceof FileType) {
            File file = (File) entry;
            targetParent.addFile(file);
            sourceParent.removeFile(file);
        } else {
            throw new NoSuccess("[ADAPTOR ERROR] Unexpected entry type: "+entry.getClass().getName());
        }
        if(Base.DEBUG) m_server.commit();
    }

    private static Serializable clone(Serializable bean) throws NoSuccess {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Marshaller.marshal(bean, doc);
            return (Serializable) Unmarshaller.unmarshal(bean.getClass(), doc);
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }
}
