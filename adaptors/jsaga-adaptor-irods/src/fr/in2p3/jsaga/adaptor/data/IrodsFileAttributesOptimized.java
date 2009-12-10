package fr.in2p3.jsaga.adaptor.data;

import edu.sdsc.grid.io.MetaDataRecordList;
import edu.sdsc.grid.io.irods.IRODSMetaDataSet;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.DoesNotExistException;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   IrodsFileAttributesOptimized
 * Author: Pascal Calvat (pcalvat@cc.in2p3.fr)
 * Date:   13 may 2008
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class IrodsFileAttributesOptimized extends FileAttributes {

    public IrodsFileAttributesOptimized(MetaDataRecordList collection, MetaDataRecordList file) throws DoesNotExistException {
        // set name
		if (collection != null) {
			m_name = (String) collection.getValue(collection.getFieldIndex(IRODSMetaDataSet.DIRECTORY_NAME));

			String [] split = m_name.split("/");
			m_name =split[split.length-1];
		} else {
			m_name = (String) file.getValue(file.getFieldIndex(IRODSMetaDataSet.FILE_NAME));
		}

		if (m_name ==null || m_name.equals(".") || m_name.equals("..")) {
			throw new DoesNotExistException("Ignore this entry");
		}

        // set type
		if (collection != null) {
			m_type = FileAttributes.DIRECTORY_TYPE;
		} else  {
			m_type = FileAttributes.FILE_TYPE;
		}

        // set size
        if (file != null) {
            m_size = Long.parseLong((String)file.getValue(file.getFieldIndex(IRODSMetaDataSet.SIZE)));
        } else {
            m_size = 0;
        }

        // set permission
        m_permission = PermissionBytes.READ;
        /* This slow down execution
        m_permission = PermissionBytes.NONE;
        if (entry.canRead()) {
			m_permission = m_permission.or(PermissionBytes.READ);
        }
		if (entry.canWrite()) {
			m_permission = m_permission.or(PermissionBytes.WRITE);
		}
		*/

        // set last modified
		if (file != null) {
			try
			{
				String modificationDate = (String) file.getValue(file.getFieldIndex(IRODSMetaDataSet.MODIFICATION_DATE));
				modificationDate = modificationDate+"000";
				m_lastModified = Long.parseLong(modificationDate);
			} catch (Exception e){}
		}
	}
}