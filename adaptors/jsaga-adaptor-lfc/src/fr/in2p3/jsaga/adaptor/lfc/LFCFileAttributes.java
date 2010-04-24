package fr.in2p3.jsaga.adaptor.lfc;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;

/**
 * @author Jerome Revillard
 */
public class LFCFileAttributes extends FileAttributes {
	private static Logger s_logger = Logger.getLogger(LFCFileAttributes.class);
	
    private LfcConnection.LFCFile m_file;

    public LFCFileAttributes(LfcConnection.LFCFile file) {
        m_file = file;
    }
    
    public LfcConnection.LFCFile getLFCFile(){
    	return m_file;
    }

    public String getName() {
        return m_file.getFileName();
    }

    public int getType() {
        if (m_file.isDirectory()) {
            return TYPE_DIRECTORY;
        } else if (m_file.isRegularFile()){
                return TYPE_FILE;    
        }else if (m_file.isSymbolicLink()){
                return TYPE_LINK;
        }else{
        	return TYPE_UNKNOWN;
        }
    }

    public long getSize() {
        return m_file.getFileSize();
    }

    public PermissionBytes getUserPermission() {
    	List<PermissionBytes> permissionBytesList = new ArrayList<PermissionBytes>();
		if ((m_file.getFileMode() & LfcConnection.S_IRUSR) != 0)
			permissionBytesList.add(PermissionBytes.READ);
		if ((m_file.getFileMode() & LfcConnection.S_IWUSR) != 0)
			permissionBytesList.add(PermissionBytes.WRITE);
		if ((m_file.getFileMode() & LfcConnection.S_IXUSR) != 0)
			permissionBytesList.add(PermissionBytes.EXEC);
    	
		if(permissionBytesList.isEmpty()){
			return PermissionBytes.NONE;
		}else{
			PermissionBytes permissionBytes = permissionBytesList.get(0);
			for (int i = 1; i < permissionBytesList.size(); i++) {
				permissionBytes = permissionBytes.or(permissionBytesList.get(i));
			}
			return permissionBytes;
		}
    }

    public PermissionBytes getGroupPermission() {
    	List<PermissionBytes> permissionBytesList = new ArrayList<PermissionBytes>();
		if ((m_file.getFileMode() & LfcConnection.S_IRGRP) != 0)
			permissionBytesList.add(PermissionBytes.READ);
		if ((m_file.getFileMode() & LfcConnection.S_IWGRP) != 0)
			permissionBytesList.add(PermissionBytes.WRITE);
		if ((m_file.getFileMode() & LfcConnection.S_IXGRP) != 0)
			permissionBytesList.add(PermissionBytes.EXEC);
    	
		if(permissionBytesList.isEmpty()){
			return PermissionBytes.NONE;
		}else{
			PermissionBytes permissionBytes = permissionBytesList.get(0);
			for (int i = 1; i < permissionBytesList.size(); i++) {
				permissionBytes = permissionBytes.or(permissionBytesList.get(i));
			}
			return permissionBytes;
		}
    }

    public PermissionBytes getAnyPermission() {
    	List<PermissionBytes> permissionBytesList = new ArrayList<PermissionBytes>();
		if ((m_file.getFileMode() & LfcConnection.S_IROTH) != 0)
			permissionBytesList.add(PermissionBytes.READ);
		if ((m_file.getFileMode() & LfcConnection.S_IWOTH) != 0)
			permissionBytesList.add(PermissionBytes.WRITE);
		if ((m_file.getFileMode() & LfcConnection.S_IXOTH) != 0)
			permissionBytesList.add(PermissionBytes.EXEC);
    	
		if(permissionBytesList.isEmpty()){
			return PermissionBytes.NONE;
		}else{
			PermissionBytes permissionBytes = permissionBytesList.get(0);
			for (int i = 1; i < permissionBytesList.size(); i++) {
				permissionBytes = permissionBytes.or(permissionBytesList.get(i));
			}
			return permissionBytes;
		}
    }

    public String getOwner() {
        try {
			return m_file.owner().getName();
		} catch (Exception e) {
			s_logger.error("Unable to get the owner of "+getName()+":"+e.getMessage());
			return ID_UNKNOWN;
		}
    }

    public String getGroup() {
        try {
			return m_file.group().getName();
		} catch (Exception e) {
			s_logger.error("Unable to get the group of "+getName()+":"+e.getMessage());
			return ID_UNKNOWN;
		}
    }

    public long getLastModified() {
        return m_file.lastModifiedTime();
    }
}
