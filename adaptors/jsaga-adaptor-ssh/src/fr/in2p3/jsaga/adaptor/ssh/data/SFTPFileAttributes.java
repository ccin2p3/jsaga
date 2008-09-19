package fr.in2p3.jsaga.adaptor.ssh.data;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SFTPFileAttributes
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   11 avril 2008
* ***************************************************/

public class SFTPFileAttributes extends FileAttributes {
   
	private int S_IRUSR = 00400; // read by owner
	private int S_IWUSR = 00200; // write by owner
	private int S_IXUSR = 00100; // execute/search by owner

	private int S_IRGRP = 00040; // read by group
	private int S_IWGRP = 00020; // write by group
	private int S_IXGRP = 00010; // execute/search by group

	private int S_IROTH = 00004; // read by others
	private int S_IWOTH = 00002; // write by others
	private int S_IXOTH = 00001; // execute/search by others


	public SFTPFileAttributes(LsEntry entry) {
		m_name = entry.getFilename();
		m_size = entry.getAttrs().getSize();
		if(entry.getAttrs().isDir())
			m_type = FileAttributes.DIRECTORY_TYPE;
		else if(entry.getAttrs().isLink())
			m_type = FileAttributes.LINK_TYPE;
		else 
			m_type = FileAttributes.FILE_TYPE;
		
		m_group =  String.valueOf(entry.getAttrs().getGId());
		m_owner =  String.valueOf(entry.getAttrs().getUId());

        m_lastModified = ((long) entry.getAttrs().getMTime()) * 1000;

        int permissions = entry.getAttrs().getPermissions();
		if((permissions & S_IRUSR)!=0) 
			m_permission = m_permission.or(PermissionBytes.READ);

	    if((permissions & S_IWUSR)!=0)
	    	m_permission = m_permission.or(PermissionBytes.WRITE);

	    if ((permissions & S_IXUSR)!=0)  
	    	m_permission = m_permission.or(PermissionBytes.EXEC);
	    
	    if((permissions & S_IRGRP)!=0)
	    	m_permission = m_permission.or(PermissionBytes.READ);
	    
	    if((permissions & S_IWGRP)!=0)
	    	m_permission = m_permission.or(PermissionBytes.WRITE);
	    
	    if((permissions & S_IXGRP)!=0) 
	    	m_permission = m_permission.or(PermissionBytes.EXEC);
	   
	    if((permissions & S_IROTH) != 0)
	    	m_permission = m_permission.or(PermissionBytes.READ);
	    
	    if((permissions & S_IWOTH) != 0)
	    	m_permission = m_permission.or(PermissionBytes.WRITE);
	    	
	    if((permissions & S_IXOTH) != 0)
	    	m_permission = m_permission.or(PermissionBytes.EXEC);
	   
	}

}
