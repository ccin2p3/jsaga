package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.commons.filesystem.FileStat;
import fr.in2p3.commons.filesystem.FileSystem;
import fr.in2p3.commons.filesystem.GroupNotFoundException;
import fr.in2p3.commons.filesystem.UserNotFoundException;
import fr.in2p3.jsaga.adaptor.data.file.FileDataAdaptor;
import fr.in2p3.jsaga.adaptor.data.link.LinkAdaptor;
import fr.in2p3.jsaga.adaptor.data.link.NotLink;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionAdaptorBasic;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.*;
import org.ogf.saga.permissions.Permission;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   LinuxDataAdaptor
 * Author:
 * Date:
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class LinuxDataAdaptor extends FileDataAdaptor implements LinkAdaptor, PermissionAdaptorBasic{
	private FileSystem _linuxFs;
	
    public String getType() {
        return "linux";
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailedException, AuthorizationFailedException, TimeoutException, NoSuccessException {
        super.connect(userInfo, host, port, basePath, attributes);
        _linuxFs = new FileSystem();
    }

    public void disconnect() throws NoSuccessException {
    	_linuxFs = null;
    	super.disconnect();
    }
    
    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        File entry = super.newEntry(absolutePath);
        FileStat stat;
        try {
            stat = _linuxFs.stat(entry);
        } catch (FileNotFoundException e) {
            throw new DoesNotExistException("Entry does not exist: "+entry, e);
        }
        return new LinuxFileAttributes(stat);
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        File[] list = super.listFiles(absolutePath);
        FileAttributes[] ret = new FileAttributes[list.length];
        for (int i=0; i<list.length; i++) {
            FileStat stat;
            try {
                stat = _linuxFs.stat(list[i]);
            } catch (FileNotFoundException e) {
                throw new DoesNotExistException("Entry does not exist: "+list[i], e);
            }
            ret[i] = new LinuxFileAttributes(stat);
        }
        return ret;
    }

    /********************************************/
    /* LinkAdaptor */
    /********************************************/
	public boolean isLink(String absolutePath)
			throws PermissionDeniedException, DoesNotExistException,
			TimeoutException, NoSuccessException {
        return ((LinuxFileAttributes)getAttributes(absolutePath, null)).isLink();
	}

	public String readLink(String absolutePath) throws NotLink,
			PermissionDeniedException, DoesNotExistException, TimeoutException,
			NoSuccessException {
		return ((LinuxFileAttributes)getAttributes(absolutePath,null)).readLink();
	}

	public void link(String sourceAbsolutePath, String linkAbsolutePath,
			boolean overwrite) throws PermissionDeniedException,
			DoesNotExistException, AlreadyExistsException, TimeoutException,
			NoSuccessException {
        File sourceEntry = super.newEntry(sourceAbsolutePath);
		if (!sourceEntry.exists()) { throw new DoesNotExistException("File does not exist: " + sourceAbsolutePath);}
		try {
	        File linkEntry = super.newEntry(linkAbsolutePath);
			if (!overwrite) { throw new AlreadyExistsException("File already exists: " + linkAbsolutePath);}
			linkEntry.delete();
		} catch (DoesNotExistException e) {
			// Nothing to do
		}
		try {
			_linuxFs.symlink(sourceEntry, linkAbsolutePath);
		} catch (FileNotFoundException fnfe) {
			throw new DoesNotExistException(fnfe);
		} catch (GeneralSecurityException e) {
			throw new PermissionDeniedException(e);
		} catch (IOException ioe) {
			throw new NoSuccessException(ioe);
		}
	}
	
    /********************************************/
    /* PermissionAdaptorBasic */
    /********************************************/
	
	public int[] getSupportedScopes() {
		return new int[] {SCOPE_USER, SCOPE_GROUP, SCOPE_ANY};
	}

	public void setOwner(String id) throws PermissionDeniedException,
			TimeoutException, BadParameterException, NoSuccessException {
		/*
		 	_linuxFs.chown(file, id);
		*/
		// TODO setOwner
		throw new BadParameterException("To implement");
	}

	public void setGroup(String id) throws PermissionDeniedException,
			TimeoutException, BadParameterException, NoSuccessException {
		/*
			_linuxFs.chgrp(file, id);
		*/
		// TODO setGroup
		throw new BadParameterException("To implement");
	}

	public void setGroup(String absolutePath, String id) throws PermissionDeniedException,
		TimeoutException, BadParameterException, NoSuccessException {
		try {
			_linuxFs.chgrp(super.newEntry(absolutePath), id);
		} catch (FileNotFoundException e) {
			throw new BadParameterException(e);
		} catch (IllegalArgumentException e) {
			throw new BadParameterException(e);
		} catch (DoesNotExistException e) {
			throw new BadParameterException(e);
		} catch (GeneralSecurityException e) {
			throw new PermissionDeniedException(e);
		} catch (IllegalAccessException e) {
			throw new BadParameterException("Not implemented");
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}
	
	public String[] getGroupsOf(String id) throws BadParameterException, NoSuccessException {
		try {
    		return _linuxFs.getUserGroups(id);
		} catch (IllegalArgumentException e) { // User not found
			throw new BadParameterException(e);
		} catch (IllegalAccessException e) { // Not implemented
			throw new BadParameterException(e);
		} catch (Exception e) { // Internal error in JNI
			throw new NoSuccessException(e);
		}    		
	}

	public void permissionsAllow(String absolutePath, int scope,
			PermissionBytes permissions) throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
		permissionsSet(absolutePath, scope, permissions, true);
	}

	public void permissionsDeny(String absolutePath, int scope,
			PermissionBytes permissions) throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
		permissionsSet(absolutePath, scope, permissions, false);
	}
	
	/**
	 * Set a particular permission on a file
	 * @param absolutePath the path of the file
	 * @param scope the scope USER, GROUP or ANY
	 * @param permissions the permission to set
	 * @param allow indicates in permissions are allowed or denied
	 * @throws PermissionDeniedException
	 * @throws TimeoutException
	 * @throws NoSuccessException
	 */
	private void permissionsSet(String absolutePath, int scope,	PermissionBytes permissions, boolean allow) 
			throws PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
        	// Get current permissions
    		LinuxFileAttributes current_perms = (LinuxFileAttributes) getAttributes(absolutePath,null);
    		PermissionBytes user_perms = current_perms.getUserPermission();
    		PermissionBytes group_perms = current_perms.getGroupPermission();
    		PermissionBytes other_perms = current_perms.getAnyPermission();
    		File entry;
			entry = super.newEntry(absolutePath);
			// Add or remove permissions
			if (scope == SCOPE_ANY) {
				other_perms = setPermission(other_perms, permissions, allow);
			} else if (scope == SCOPE_GROUP) {
				group_perms = setPermission(group_perms, permissions, allow);
			} else {
				user_perms = setPermission(user_perms, permissions, allow);
			}
			// call Linux routines
			_linuxFs.chmod(entry, toUnixPermissions(user_perms), toUnixPermissions(group_perms), toUnixPermissions(other_perms));
		} catch (DoesNotExistException e) {
			throw new NoSuccessException("File not found: " + absolutePath);
		} catch (FileNotFoundException e) {
			throw new NoSuccessException("File not found: " + absolutePath);
		}
	}
	
	/**
	 * add/remove permissions on a given permission
	 * @param oldPerm the current permissions
	 * @param newPerm the new permissions to add/remove
	 * @param allow indicates if new permissions are added (allowed) or removed (denied)
	 * @return
	 */
	private PermissionBytes setPermission(PermissionBytes oldPerm, PermissionBytes newPerm, boolean allow) {
		for (Permission p : new Permission[] {Permission.READ, Permission.WRITE, Permission.EXEC}) {
			if (newPerm.contains(p)) {
				if (allow) { oldPerm = oldPerm.or(newPerm); }
				else       { oldPerm = oldPerm.xor(newPerm); }
			}
		}
		return oldPerm;
	}
	
	private int toUnixPermissions (PermissionBytes pb) {
		int perms = 0;
		perms += pb.contains(Permission.READ) ? FileStat.READ:0;
		perms += pb.contains(Permission.WRITE) ? FileStat.WRITE:0;
		perms += pb.contains(Permission.EXEC) ? FileStat.EXEC:0;
		return perms;		
	}
}
