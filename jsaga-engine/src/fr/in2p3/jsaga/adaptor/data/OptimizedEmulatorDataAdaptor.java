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
    public String getType() {
        return "otest";
    }

    public void copy(String sourceAbsolutePath, String targetHost, int targetPort, String targetAbsolutePath, boolean overwrite, String additionalArgs) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        File file = m_server.getFile(sourceAbsolutePath);
        DataEmulatorConnection targetServer = new DataEmulatorConnection(this.getType(), targetHost, targetPort);

        // check if exists
        try {
            targetServer.getFile(targetAbsolutePath);
            if (!overwrite) {
                throw new AlreadyExistsException("File already exist");
            } else {
                targetServer.removeFile(targetAbsolutePath);
            }
        } catch(DoesNotExistException e) {
            // do nothing
        }

        // clone file
        File fileClone = (File) clone(file);
        fileClone.setName(targetServer.getEntryName(targetAbsolutePath));

        // add clone to target server
        DirectoryType parent;
        try {
            parent = targetServer.getParentDirectory(targetAbsolutePath);
        } catch (DoesNotExistException e) {
            throw new ParentDoesNotExist("Parent directory does not exist");
        }
        parent.addFile(fileClone);
        if(Base.DEBUG) targetServer.commit();
    }

    public void copyFrom(String sourceHost, int sourcePort, String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite, String additionalArgs) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        DataEmulatorConnection sourceServer = new DataEmulatorConnection(this.getType(), sourceHost, sourcePort);
        File file = sourceServer.getFile(sourceAbsolutePath);

        // check if exists
        try {
            m_server.getFile(targetAbsolutePath);
            if (!overwrite) {
                throw new AlreadyExistsException("File already exist");
            } else {
                m_server.removeFile(targetAbsolutePath);
            }
        } catch(DoesNotExistException e) {
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

    public void rename(String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException {
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
            throw new NoSuccessException("[ADAPTOR ERROR] Unexpected entry type: "+entry.getClass().getName());
        }
        if(Base.DEBUG) m_server.commit();
    }

    private static Serializable clone(Serializable bean) throws NoSuccessException {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Marshaller.marshal(bean, doc);
            return (Serializable) Unmarshaller.unmarshal(bean.getClass(), doc);
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
    }
}
