package fr.in2p3.jsaga.adaptor.data;

import edu.sdsc.grid.io.MetaDataRecordList;
import edu.sdsc.grid.io.srb.SRBMetaDataSet;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.DoesNotExistException;

import java.text.SimpleDateFormat;
import java.util.Date;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   SrbFileAttributes
 * Author: Pascal Calvat (pcalvat@cc.in2p3.fr)
 * Date:   13 may 2008
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class SrbFileAttributes extends FileAttributes {
	private static final SimpleDateFormat dateStandard = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
	private static final String SLASH="/";

    public SrbFileAttributes(String basePath, MetaDataRecordList collection, MetaDataRecordList file, boolean findAttributes) throws DoesNotExistException {
        // set name
		if (collection != null) {
			if (findAttributes) {
				m_relativePath = (String) collection.getValue(collection.getFieldIndex(SRBMetaDataSet.DIRECTORY_NAME));
			}
			m_name = (String) collection.getValue(collection.getFieldIndex(SRBMetaDataSet.DIRECTORY_NAME));
			
			String [] split = m_name.split(SLASH);
			m_name =split[split.length-1];
		} else {
			m_name = (String) file.getValue(file.getFieldIndex(SRBMetaDataSet.FILE_NAME));
			if (findAttributes) {
				m_relativePath = file.getValue(file.getFieldIndex(SRBMetaDataSet.DIRECTORY_NAME))
				+SLASH+file.getValue(file.getFieldIndex(SRBMetaDataSet.FILE_NAME));
			}
		}
		
		// m_relativePath is mandatory in case of findAttributes
		if (findAttributes) {
			if (!m_relativePath.equals(basePath)) {
				m_relativePath = m_relativePath.substring(basePath.length()+1,m_relativePath.length());
			} else {
				m_relativePath = ".";
			}
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
            m_size = Long.parseLong((String)file.getValue(file.getFieldIndex(SRBMetaDataSet.SIZE)));
        } else {
            m_size = 0;
        }

        // set permission
		m_permission = PermissionBytes.READ;

        // set last modified
		if (file != null) {
			try
			{
				Date date = dateStandard.parse((String) file.getValue(file.getFieldIndex(SRBMetaDataSet.FILE_LAST_ACCESS_TIMESTAMP)));
				m_lastModified = date.getTime();
			} catch (Exception e){}
		}
	}
}