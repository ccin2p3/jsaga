package fr.in2p3.jsaga.adaptor.data;

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
public class IrodsFileAttributesOptimized /*extends FileAttributes*/ {
//    private String m_name;
//    private MetaDataRecordList m_collection;
//    private MetaDataRecordList m_file;
//
//    public IrodsFileAttributesOptimized(MetaDataRecordList collection, MetaDataRecordList file) throws DoesNotExistException {
//        // check if entry must be ignored
//        String name;
//		if (collection != null) {
//			name = (String) collection.getValue(collection.getFieldIndex(IRODSMetaDataSet.DIRECTORY_NAME));
//
//			String [] split = name.split("/");
//			name =split[split.length-1];
//		} else {
//			name = (String) file.getValue(file.getFieldIndex(IRODSMetaDataSet.FILE_NAME));
//		}
//		if (name ==null || name.equals(".") || name.equals("..")) {
//			throw new DoesNotExistException("Ignore this entry");
//		}
//
//        // set attributes
//        m_name = name;
//        m_collection = collection;
//        m_file = file;
//	}
//
//    public String getName() {
//        return m_name;
//    }
//
//    public int getType() {
//        if (m_collection != null) {
//            return TYPE_DIRECTORY;
//        } else if (m_file != null) {
//            return TYPE_FILE;
//        } else {
//            return TYPE_UNKNOWN;
//        }
//    }
//
//    public long getSize() {
//        if (m_file != null) {
//            return Long.parseLong((String)m_file.getValue(m_file.getFieldIndex(IRODSMetaDataSet.SIZE)));
//        } else {
//            return SIZE_UNKNOWN;
//        }
//    }
//
//    public PermissionBytes getUserPermission() {
//        return PERMISSION_UNKNOWN;
//    }
//
//    public PermissionBytes getGroupPermission() {
//        return PERMISSION_UNKNOWN;
//    }
//
//    public PermissionBytes getAnyPermission() {
//        return PERMISSION_UNKNOWN;
//    }
//
//    public String getOwner() {
//        return ID_UNKNOWN;
//    }
//
//    public String getGroup() {
//        return ID_UNKNOWN;
//    }
//
//    public long getLastModified() {
//        if (m_file != null) {
//            try {
//                String modificationDate = (String) m_file.getValue(m_file.getFieldIndex(IRODSMetaDataSet.MODIFICATION_DATE));
//                modificationDate = modificationDate+"000";
//                return Long.parseLong(modificationDate);
//            } catch (Exception e){
//                return DATE_UNKNOWN;
//            }
//        } else {
//            return DATE_UNKNOWN;
//        }
//    }
}
