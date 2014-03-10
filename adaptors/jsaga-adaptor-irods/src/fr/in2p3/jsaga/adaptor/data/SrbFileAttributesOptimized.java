package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.DoesNotExistException;

import java.text.SimpleDateFormat;
import java.util.Date;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   SrbFileAttributesOptimized
 * Author: Pascal Calvat (pcalvat@cc.in2p3.fr)
 * Date:   13 may 2008
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class SrbFileAttributesOptimized /*extends FileAttributes*/ {
//	private static final SimpleDateFormat dateStandard = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
//	private static final String SLASH="/";
//
//    private String m_name;
//    private MetaDataRecordList m_collection;
//    private MetaDataRecordList m_file;
//
//    public SrbFileAttributesOptimized(String basePath, MetaDataRecordList collection, MetaDataRecordList file, boolean findAttributes) throws DoesNotExistException {
//        // check if entry must be ignored
//        String name;
//		if (collection != null) {
//			if (findAttributes) {
//				m_relativePath = (String) collection.getValue(collection.getFieldIndex(SRBMetaDataSet.DIRECTORY_NAME));
//			}
//			name = (String) collection.getValue(collection.getFieldIndex(SRBMetaDataSet.DIRECTORY_NAME));
//
//			String [] split = name.split(SLASH);
//			name =split[split.length-1];
//		} else {
//			name = (String) file.getValue(file.getFieldIndex(SRBMetaDataSet.FILE_NAME));
//			if (findAttributes) {
//				m_relativePath = file.getValue(file.getFieldIndex(SRBMetaDataSet.DIRECTORY_NAME))
//				+SLASH+file.getValue(file.getFieldIndex(SRBMetaDataSet.FILE_NAME));
//			}
//		}
//
//		// m_relativePath is mandatory in case of findAttributes
//		if (findAttributes) {
//			if (!m_relativePath.equals(basePath)) {
//				m_relativePath = m_relativePath.substring(basePath.length()+1,m_relativePath.length());
//			} else {
//				m_relativePath = ".";
//			}
//		}
//
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
//            return Long.parseLong((String)m_file.getValue(m_file.getFieldIndex(SRBMetaDataSet.SIZE)));
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
//                Date date = dateStandard.parse((String) m_file.getValue(m_file.getFieldIndex(SRBMetaDataSet.FILE_LAST_ACCESS_TIMESTAMP)));
//                return date.getTime();
//            } catch (Exception e){
//                return DATE_UNKNOWN;
//            }
//        } else {
//            return DATE_UNKNOWN;
//        }
//    }
}