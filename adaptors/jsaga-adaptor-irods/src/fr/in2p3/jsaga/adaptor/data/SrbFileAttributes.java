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

    public SrbFileAttributes(MetaDataRecordList collection, MetaDataRecordList file) throws DoesNotExistException {
        // set name
		if (collection != null) {
			m_name = (String) collection.getValue(collection.getFieldIndex(SRBMetaDataSet.DIRECTORY_NAME));
			
			String [] split = m_name.split("/");
			m_name =split[split.length-1];
			
		} else {
			m_name = (String) file.getValue(file.getFieldIndex(SRBMetaDataSet.FILE_NAME));
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