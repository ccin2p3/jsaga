package fr.in2p3.jsaga.adaptor.lfc;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.maatg.glite.dm.ns.CNSConnection;
import fr.maatg.glite.dm.ns.CNSFile;

/**
 * @author Jerome Revillard
 */
public class NSFileAttributes extends FileAttributes {
	private static Logger s_logger = Logger.getLogger(NSFileAttributes.class);
	
    private CNSFile m_file;
    private String m_owner;
    private String m_group;
    private CNSConnection connection;

    public NSFileAttributes(CNSFile file, CNSConnection connection) {
        m_file = file;
        m_owner = null;
        m_group = null;
        this.connection = connection;
    }
    
    public CNSFile getLFCFile(){
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
		if ((m_file.getFileMode() & CNSConnection.S_IRUSR) != 0)
			permissionBytesList.add(PermissionBytes.READ);
		if ((m_file.getFileMode() & CNSConnection.S_IWUSR) != 0)
			permissionBytesList.add(PermissionBytes.WRITE);
		if ((m_file.getFileMode() & CNSConnection.S_IXUSR) != 0)
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
		if ((m_file.getFileMode() & CNSConnection.S_IRGRP) != 0)
			permissionBytesList.add(PermissionBytes.READ);
		if ((m_file.getFileMode() & CNSConnection.S_IWGRP) != 0)
			permissionBytesList.add(PermissionBytes.WRITE);
		if ((m_file.getFileMode() & CNSConnection.S_IXGRP) != 0)
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
		if ((m_file.getFileMode() & CNSConnection.S_IROTH) != 0)
			permissionBytesList.add(PermissionBytes.READ);
		if ((m_file.getFileMode() & CNSConnection.S_IWOTH) != 0)
			permissionBytesList.add(PermissionBytes.WRITE);
		if ((m_file.getFileMode() & CNSConnection.S_IXOTH) != 0)
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

    public synchronized String getOwner() {
        if (m_owner == null) {
            try {
                m_owner = m_file.owner(connection).getName();
            } catch (Exception e) {
                s_logger.error("Unable to get the owner of "+getName()+":"+e.getMessage());
                return ID_UNKNOWN;
            }
        }
        return m_owner;
    }

    public synchronized String getGroup() {
        if (m_group == null) {
            try {
                m_group = m_file.group(connection).getName();
            } catch (Exception e) {
                s_logger.error("Unable to get the group of "+getName()+":"+e.getMessage());
                return ID_UNKNOWN;
            }
        }
        return m_group;
    }

    public long getLastModified() {
        return m_file.lastModifiedTime();
    }
}
