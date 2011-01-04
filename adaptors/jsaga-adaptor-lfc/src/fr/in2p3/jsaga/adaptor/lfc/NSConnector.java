package fr.in2p3.jsaga.adaptor.lfc;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.ietf.jgss.GSSCredential;

import fr.in2p3.jsaga.adaptor.lfc.NSConnection.AccessType;
import fr.in2p3.jsaga.adaptor.lfc.NSConnection.LFCBrokenPipeException;
import fr.in2p3.jsaga.adaptor.lfc.NSConnection.NSError;
import fr.in2p3.jsaga.adaptor.lfc.NSConnection.ReceiveException;

/**
 * Provides a High-Level view of an LFC server.
 * 
 * @author Max Berger
 * @author Jerome Revillard
 */
public class NSConnector {
	private final String vo;
	private final String server;
	private final int port;
	private final GSSCredential gssCredential;
	
	/**
	 * Create a new LfcConector
	 * 
	 * @param server
	 *            Server to connect to
	 * @param port
	 *            Port to connect to
	 * @param vo
	 *            VO of this server
	 * @param gssCredential
	 * 			  The credential with which the LFC server will be accessed.
	 * @throws ReceiveException 
	 * @throws IOException 
	 * @throws TimeoutException 
	 */
	private NSConnector(String host, int port, String vo, GSSCredential gssCredential) throws IOException, ReceiveException, LFCBrokenPipeException {
		this.server = host;
		this.port = port;
		this.vo = vo;
		this.gssCredential = gssCredential;
	}
	
	public static NSConnector getInstance(String host, int port, String vo, GSSCredential gssCredential) throws IllegalArgumentException, IOException, ReceiveException, LFCBrokenPipeException{
		if(host == null || "".equals(host)){
			throw new IllegalArgumentException("The NS host must be set.");
		}
		if(port <= 0){
			throw new IllegalArgumentException("The NS port must be greater than 0.");
		}
		return new NSConnector(host, port, vo, gssCredential);
	}
	
	public NSConnection getNewConnection() throws IOException, ReceiveException, LFCBrokenPipeException{
		return new NSConnection(server, port, gssCredential);
	}
	
	public void startSession(NSConnection connection) throws IOException, ReceiveException, LFCBrokenPipeException {
		try{
			connection.startSession();
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("startSession()");
			throw e;
		}
	}
	
	public void closeSession(NSConnection connection) throws IOException, ReceiveException, LFCBrokenPipeException {
		try{
			connection.endSession();
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("closeSession()");
			throw e;
		}
	}
	
	public void startTransaction(NSConnection connection, String comment) throws IOException, ReceiveException, LFCBrokenPipeException {
		try{
			connection.startTransaction(comment);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("startTransaction("+comment != null ? "'"+comment+"'" : "''"+")");
			throw e;
		}
	}
	
	public void endTransaction(NSConnection connection) throws IOException, ReceiveException, LFCBrokenPipeException {
		try{
			connection.endTransaction();
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("endTransaction()");
			throw e;
		}
	}
	
	public void abordTransaction(NSConnection connection) throws IOException, ReceiveException, LFCBrokenPipeException {
		try{
			connection.abordTransaction();
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("abordTransaction()");
			throw e;
		}
	}

	/**
	 * Close the connection
	 * @throws IOException If a problem occurs
	 */
	private void close(NSConnection connection) throws IOException{
		connection.close();
	}	
	
	/**
	 * Get the list of file replicas according to the file path or to the file
	 * guid
	 * 
	 * @param path
	 *            Path of the file
	 * @param guid
	 *            GUID of the file
	 * @return A list of SRM URIs
	 * @throws IOException
	 *             if anything goes wrong
	 * @throws ReceiveException 
	 * @throws LFCBrokenPipeException 
	 */
	// /!\ SESSIONS NOT SUPPORTED /!\
	public Collection<NSReplica> listReplicas(String path, String guid) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			final Collection<NSReplica> replicas = new ArrayList<NSReplica>();
			short flag = NSConnection.CNS_LIST_BEGIN;
			final short[] eol = new short[]{0};
			while (eol[0] != 1) {
				replicas.addAll(connection.listReplica(path, guid, flag, eol));
				flag = NSConnection.CNS_LIST_CONTINUE;
			}
			// Removed to speed up the process as the connection will be closed.
			//connection.listReplica(path, guid, NSConnection.CNS_LIST_END, eol);
			return replicas;
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("listReplicas("+path+","+guid+")");
			throw e;
		} finally {
			close(connection);
		}
	}

	/**
	 * Get the different file/directory/symbolic link attributes. Do not retrieve the file GUID
	 * 
	 * @param path
	 *            The file path
	 * @param followSymbolicLink
	 *            If <code>true</code> and If the path represent a symbolic
	 *            link, return the pointed file/directory attributes, otherwise,
	 *            return the symbolic link attributes
	 * @return
	 * @throws IOException
	 * @throws ReceiveException 
	 * @throws LFCBrokenPipeException 
	 */
	public NSFile stat(String path, boolean followSymbolicLink, boolean getGUID) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			return stat(connection, path, followSymbolicLink, getGUID);
		} finally {
			close(connection);
		}
	}
	
	public NSFile stat(NSConnection connection, String path, boolean followSymbolicLink, boolean getGUID) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSFile file;
		try {
			if(getGUID == true && followSymbolicLink == false){
				throw new IllegalArgumentException("LFC stat: Cannot have both getGUID=true and followSymbolicLink=false");
			}
			if(!getGUID){
				if (followSymbolicLink) {
					file = connection.stat(path);
				} else {
					file = connection.lstat(path);
				}
			}else{
				file = connection.statg(path, null);
			}
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("stat("+path+") - followSymbolicLink="+followSymbolicLink);
			throw e;
		}
		return file;
	}
	
	/**
	 * Get the GUID of an PFN path
	 * @param sfn	The PFN for which the GUID will be returned
	 * @return The GUID of the PFN
	 * @throws IOException
	 * @throws ReceiveException
	 * @throws LFCBrokenPipeException
	 */
	public NSFile getGuidFromPfn(String sfn) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			return getGuidFromPfn(connection, sfn);
		} finally {
			close(connection);
		}
	}
	
	public NSFile getGuidFromPfn(NSConnection connection, String sfn) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSFile file;
		try {
		    file = connection.statr(sfn);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("getGuidFromSfn("+sfn+")");
			throw e;
		}
		return file;
	}
	
	/**
	 * Resolve one level of symbolic link path
	 * @param link The symbolic link path
	 * @return  The path to which point the symbolic link
	 * @throws IOException
	 * @throws ReceiveException
	 * @throws LFCBrokenPipeException
	 */
	public String readlink(String link) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			return readlink(connection, link);
		} finally {
			close(connection);
		}
	}
	
	public String readlink(NSConnection connection, String link) throws IOException, ReceiveException, LFCBrokenPipeException {
		final String path;
		try {
			path = connection.readlink(link);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("readlink("+link+")");
			throw e;
		}
		return path;
	}

	/**
	 * Create a symbolic link
	 * @param path		The original file/directory
	 * @param target	The path of the link
	 * @throws IOException
	 * @throws ReceiveException
	 * @throws LFCBrokenPipeException
	 */
	public void symbLink(String path, String target, boolean overwrite) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			symbLink(connection, false, path, target, overwrite);
		} finally {
			close(connection);
		}
	}
	
	public void symbLink(NSConnection connection, String path, String target, boolean overwrite) throws IOException, ReceiveException, LFCBrokenPipeException {
		symbLink(connection, true, path, target, overwrite);
	}
	
	private void symbLink(NSConnection connection, boolean createNewConnection, String path, String target, boolean overwrite) throws IOException, ReceiveException, LFCBrokenPipeException {
		try {
			connection.link(path, target);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			if(NSError.FILE_EXISTS.equals(e.getLFCError())){
				//The file already exists...remove if 'overwrite'==true;
				if(overwrite == true){
					NSConnection newConnection = connection;
					if(createNewConnection){
						newConnection = new NSConnection(server, port, gssCredential);
					}
					newConnection.startTransaction(null);
					try{
						this.unlink(newConnection, path);
						this.symbLink(newConnection, path, target, false);
						newConnection.endTransaction();
					}finally{
						if(createNewConnection){
							newConnection.close();
						}
					}
				}else{
					e.setExecutedCmd("symbLink("+path+","+target+")");
					throw e;
				}
			}else{
				e.setExecutedCmd("symbLink("+path+","+target+")");
				throw e;
			}
		}
	}

	/**
	 * Test if the file or the directory can be read.
	 * 
	 * @param path
	 * @return <code>true</code> if it can be read
	 * @throws IOException
	 * @throws ReceiveException 
	 * @throws LFCBrokenPipeException 
	 */
	public boolean canRead(String path) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			return canRead(connection, path);
		} finally {
			close(connection);
		}
	}
	
	public boolean canRead(NSConnection connection, String path) throws IOException, ReceiveException, LFCBrokenPipeException {
		try {
			connection.access(path, AccessType.READ_OK);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			if (NSError.PERMISSION_DENIED.equals(e.getLFCError())) {
				return false;
			}else{
				e.setExecutedCmd("canRead("+path+")");
				throw e;
			}
		}
		return true;
	}

	/**
	 * Test if the file or the directory can be written.
	 * 
	 * @param path
	 * @return <code>true</code> if it can be written
	 * @throws IOException
	 * @throws ReceiveException
	 * @throws LFCBrokenPipeException 
	 */
	public boolean canWrite(String path) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			return canWrite(connection, path);
		} finally {
			close(connection);
		}
	}
	
	public boolean canWrite(NSConnection connection, String path) throws IOException, ReceiveException, LFCBrokenPipeException {
		try {
			connection.access(path, AccessType.WRITE_OK);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			if (NSError.PERMISSION_DENIED.equals(e.getLFCError())) {
				return false;
			}else{
				e.setExecutedCmd("canWrite("+path+")");
				throw e;
			}
		}
		return true;
	}

	/**
	 * Test the existence of a path
	 * 
	 * @param path
	 *            path to test
	 * @return <code>true</code> if the path exists
	 * @throws IOException
	 * @throws ReceiveException 
	 * @throws LFCBrokenPipeException 
	 */
	public boolean exist(String path) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			return exist(connection, path);
		} finally {
			close(connection);
		}
	}
	
	public boolean exist(NSConnection connection, String path) throws IOException, ReceiveException, LFCBrokenPipeException {
		try {
			this.stat(connection, path,false,false);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			if (NSError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
				return false;
			}else{
				e.setExecutedCmd("exist("+path+")");
				throw e;
			}
		}
		return true;
	}

	/**
	 * Get the group names which correspond to specific gids.
	 * 
	 * @return A collection of groups names
	 * @throws ReceiveException 
	 * @throws ReceiveException 
	 * @throws LFCBrokenPipeException 
	 */
	public Collection<String> getGrpByGids(int[] gids) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			return getGrpByGids(connection, gids);
		} finally {
			close(connection);
		}
	}
	
	public Collection<String> getGrpByGids(NSConnection connection, int[] gids) throws IOException, ReceiveException, LFCBrokenPipeException {
		try {
			return connection.getGrpByGids(gids);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("getGrpByGids("+Arrays.toString(gids)+")");
			throw e;
		}
	}

	/**
	 * Get the user name which correspond to specific uid.
	 * @return The corresponding user name
	 * @throws ReceiveException 
	 * @throws ReceiveException 
	 * @throws LFCBrokenPipeException 
	 */
	public String getUsrByUid(int uid) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			return getUsrByUid(connection, uid);
		} finally {
			close(connection);
		}
	}
	
	public String getUsrByUid(NSConnection connection, int uid) throws IOException, ReceiveException, LFCBrokenPipeException {
		try {
			return connection.getUsrByUid(uid);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("getUsrByUid("+uid+")");
			throw e;
		}
	}

	/**
	 * Get the content of a directory.
	 * 
	 * @param path
	 *            path of the directory
	 * @return A collection of files or directories inside the given path
	 * @throws IOException
	 * @throws ReceiveException 
	 * @throws LFCBrokenPipeException 
	 */
	// /!\ SESSIONS NOT SUPPORTED /!\
	public Collection<NSFile> list(String path, boolean followSymbolicLink) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= NSConnection.MAX_RETRY_IF_BROKEN_PIPE; z++) {
			NSConnection connection = new NSConnection(server, port, gssCredential);
			try{
				try {
					final long fileID = connection.opendir(path, null);
					Collection<NSFile> files = new ArrayList<NSFile>();
					short bod = 1;
					final short[] eod = new short[]{0};
					while (eod[0] != 1) {
						files.addAll(connection.readdir(fileID, bod, eod));
						bod = 0;
					}
					//FIXME: it raises an error;
//					connection.closedir();
					return files;
				} finally {
					close(connection);
				}
			} catch (IOException e) {
				throw e;
			} catch (ReceiveException e) {
				e.setExecutedCmd("list("+path+") - followSymbolicLink="+followSymbolicLink);
				throw e;
			}catch (LFCBrokenPipeException e) {
				if(z == NSConnection.MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}

	/**
	 * Create a directory in the LFC
	 * 
	 * @param path
	 *            path of the directory
	 * @throws IOException
	 * @throws ReceiveException 
	 * @throws LFCBrokenPipeException 
	 */
	public void mkdir(String path) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			mkdir(connection, path);
		} finally {
			close(connection);
		}
	}

	public void mkdir(NSConnection connection, String path) throws IOException, ReceiveException, LFCBrokenPipeException {
		try {
			connection.mkdir(path, UUID.randomUUID().toString());
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("mkdir("+path+")");
			throw e;
		}
	}
	/**
	 * Delete a directory from the LFC
	 * 
	 * @param path
	 *            path of the directory
	 * @return <code>true</code> is everything was ok.
	 * @throws IOException
	 * @throws ReceiveException 
	 * @throws LFCBrokenPipeException 
	 */
	public boolean deleteDir(String path) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			return deleteDir(connection, path);
		} finally {
			close(connection);
		}
	}
	
	public boolean deleteDir(NSConnection connection, String path) throws IOException, ReceiveException, LFCBrokenPipeException {
		try {
			connection.rmdir(path);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("deleteFile("+path+")");
			throw e;
		}
		return true;
	}
	
	/**
	 * Delete a file from the LFC
	 * 
	 * @param path
	 *            path of the file
	 * @throws IOException
	 * @throws ReceiveException 
	 * @throws LFCBrokenPipeException 
	 */
	public void unlink(String path) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try{
			unlink(connection, path);
		} finally {
			close(connection);
		}	
	}
	
	public void unlink(NSConnection connection, String path) throws IOException, ReceiveException, LFCBrokenPipeException {
		try{
		    connection.unlink(path);	
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("unlink("+path+")");
			throw e;
		}
	}
	
	/**
	 * @param guid
	 * @param sfn
	 * @throws IOException
	 * @throws ReceiveException
	 * @throws LFCBrokenPipeException 
	 */
	public void deleteReplica(String guid, String sfn) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			deleteReplica(connection, guid, sfn);
		} finally {
			close(connection);
		}
	}

	public void deleteReplica(NSConnection connection, String guid, String sfn) throws IOException, ReceiveException, LFCBrokenPipeException {
		try {
			connection.delReplica(guid, sfn);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("deleteReplica("+guid+","+sfn+")");
			throw e;
		}
	}

	/**
	 * Delete a file and all the replicas if <code>force==true</code>.
	 * 
	 * @param guid
	 *            GUID of the file.
	 * @param force
	 * 			  Force replicas deletion too.
	 * @return true if the file was deleted.
	 * @throws IOException
	 * @throws ReceiveException 
	 * @throws LFCBrokenPipeException 
	 */
	public boolean deleteGuid(String guid, boolean force) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			return deleteGuid(connection, guid, force);
		} finally {
			close(connection);
		}
	}
	
	public boolean deleteGuid(NSConnection connection, String guid, boolean force) throws IOException, ReceiveException, LFCBrokenPipeException {
		try {
			return connection.delFiles(new String[] { guid }, force);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("deleteGuid("+guid+","+force+")");
			throw e;
		}
	}

	/**
	 * Change access mode of a LFC directory/file. Symbolic link are not
	 * supported yet
	 * 
	 * @param path
	 *            File path
	 * @param mode
	 *            Absolute LFC permissions (see {@link NSConnection} for values...)
	 * @throws IOException
	 * @throws ReceiveException 
	 * @throws LFCBrokenPipeException 
	 */
	public void chmod(String path, int mode) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			chmod(connection, path, mode);
		} finally {
			close(connection);
		}
	}
	
	public void chmod(NSConnection connection, String path, int mode) throws IOException, ReceiveException, LFCBrokenPipeException {
		try {
			connection.chmod(path, mode);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("chmod("+path+","+mode+")");
			throw e;
		}
	}

	/**
	 * Change owner and group of a file or a directory. At least user name or
	 * group name need to be specified (both can be specified) If a name is
	 * <code>null</code> then the name is ignored.
	 * 
	 * @param path
	 *            Current file name
	 * @param recursive
	 *            Recursive mode
	 * @param followSymbolicLinks
	 *            If the path is a symbolic link, changes the owner‐ship of the
	 *            linked file or directory instead of the symbolic link itself
	 * @param usrName
	 *            The new owner of the file
	 * @param grpName
	 *            The new group of the file
	 * @throws IOException
	 * @throws ReceiveException 
	 * @throws LFCBrokenPipeException 
	 */
	public void chown(String path, boolean recursive, boolean followSymbolicLinks, String usrName, String grpName) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try{
			chown(connection, path, recursive, followSymbolicLinks, usrName, grpName);
		}finally{
			close(connection);
		}
	}
	
	public void chown(NSConnection connection, String path, boolean recursive, boolean followSymbolicLinks, String usrName, String grpName) throws IOException, ReceiveException, LFCBrokenPipeException {
		int new_uid = -1;
		int new_gid = -1;
		if (usrName != null) {
			if (usrName.equals("root")) {
				new_uid = 0;
			} else {
				try {
					new_uid = connection.getUsrByName(usrName);
				} catch (IOException e) {
					//FIXME: check of the correct error would be better 
					throw new IOException("Unable to find the uid of " + usrName + " in the LFC");
				}
			}
		}
		if (grpName != null) {
			if (grpName.equals("root")) {
				new_gid = 0;
			} else {
				try {
					new_gid = connection.getGrpByName(grpName);
				} catch (IOException e) {
					throw new IOException("Unable to find the gid of " + grpName + " in the LFC");
				}
			}
		}
		chown(connection, path, recursive, followSymbolicLinks, new_uid, new_gid);
	}

	/**
	 * Change owner and group of a file or a directory. At least user ID or
	 * group ID need to be specified (both can be specified) If an ID value is
	 * inferior to 0 then the ID is ignored.
	 * 
	 * @param path
	 *            Current file name
	 * @param recursive
	 *            Recursive mode
	 * @param followSymbolicLinks
	 *            If the path is a symbolic link, changes the owner‐ship of the
	 *            linked file or directory instead of the symbolic link itself
	 * @param new_uid
	 *            New owner ID
	 * @param new gid New group ID
	 * @throws IOException
	 *             If a problem occurs
	 * @throws ReceiveException 
	 * @throws LFCBrokenPipeException 
	 */
	public void chown(String path, boolean recursive, boolean followSymbolicLinks, int new_uid, int new_gid) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			chown(connection, path, recursive, followSymbolicLinks, new_uid, new_gid);
		}finally{
			close(connection);
		}
	}
	
	public void chown(NSConnection connection, String path, boolean recursive, boolean followSymbolicLinks, int new_uid, int new_gid) throws IOException, ReceiveException, LFCBrokenPipeException {
		if (recursive == true) {
			// TODO ....
			throw new UnsupportedOperationException("Recurcive chown is not implemented...");
		}
		if (new_uid < 0 && new_gid < 0) {
			throw new IllegalArgumentException("You must specify at least a new owner or a new group");
		}
		try {
			if (followSymbolicLinks) {
				connection.chown(path, new_uid, new_gid);
			} else {
				connection.lchown(path, new_uid, new_gid);
			}
		} catch (IOException e) {
				throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("chown("+path+") - uid="+new_uid+", gid="+new_gid+", followSymbolicLinks="+followSymbolicLinks);
			throw e;
		}
	}

	/**
	 * Rename a file
	 * 
	 * @param oldPath
	 *            current file name
	 * @param newPath
	 *            new file name
	 * @throws IOException
	 * @throws ReceiveException 
	 * @throws LFCBrokenPipeException 
	 */
	public void rename(String oldPath, String newPath) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			rename(connection, oldPath, newPath);
		} finally {
			close(connection);
		}
	}
	
	public void rename(NSConnection connection, String oldPath, String newPath) throws IOException, ReceiveException, LFCBrokenPipeException {
		try {
			connection.rename(oldPath, newPath);
		} catch (IOException e) {
				throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("rename("+oldPath+", "+newPath+")");
			throw e;
		}
	}

	/**
	 * Create a new File
	 * 
	 * @param fileSize
	 *            the size of the file
	 * @return a GUID to the new file
	 * @throws IOException
	 * @throws ReceiveException 
	 * @throws LFCBrokenPipeException 
	 */
	public String create(long fileSize) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			return create(connection, false, fileSize);
		} finally {
			close(connection);
		}
	}
	
	/**
	 * See {@link NSConnector#create(long)}
	 * WARN: THe connection that you will use here must not 
	 * have an opened session or an opened transaction if the
	 * fileSize parameter is > 0 because a transaction will be 
	 * opened.
	 * 
	 * @param connection
	 * @param fileSize
	 * 				the size of the file
	 * @return a GUID to the new file
	 * @throws IOException
	 * @throws ReceiveException
	 * @throws LFCBrokenPipeException
	 */
	public String create(NSConnection connection, long fileSize) throws IOException, ReceiveException, LFCBrokenPipeException {
		return create(connection, true, fileSize);
	}
	
	
	public String create(NSConnection connection, boolean createNewConnection, long fileSize) throws IOException, ReceiveException, LFCBrokenPipeException {
		String guid = UUID.randomUUID().toString();
		String parent = "/grid/" + vo + "/generated/" + dateAsPath();
		try {
			connection.mkdir(parent, UUID.randomUUID().toString());
		} catch (ReceiveException e) {
			if(!NSError.FILE_EXISTS.equals(e.getLFCError())){
				e.setExecutedCmd("create(/grid/" + vo + "/generated/" + dateAsPath()+")");
				throw e;
			}
		}
		String path = parent + "/file-" + guid;
		return create(connection, createNewConnection, "lfn:///" + path, guid, fileSize);
	}

	/**
	 * Create a new File at a specific location.
	 * 
	 * @param location
	 *            The lfn of the file to create (lfn:///grid/vo/...).
	 * @param guid
	 *            The guid of the file
	 * @param fileSize
	 *            The size of the file
	 * @return a GUID to the new file
	 * @throws IOException
	 * @throws ReceiveException 
	 * @throws LFCBrokenPipeException 
	 */
	public String create(String location, String guid, long fileSize) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try{
			return create(connection, false, location, guid, fileSize);
		} finally {
			close(connection);
		}
	}
	

	public String create(NSConnection connection, boolean createNewConnection, String location, String guid, long fileSize) throws IOException, ReceiveException, LFCBrokenPipeException {
		NSConnection m_connection = connection;
		boolean newConnectionCreated = false;
		try{
			if (fileSize > 0L) {
				if(createNewConnection){
					m_connection = new NSConnection(server, port, gssCredential);
					newConnectionCreated = true;
				}
				m_connection.startTransaction(null);
			}
			boolean done = false;
			try{
				m_connection.creat(location, guid);
				if (fileSize > 0L) {
					m_connection.setfsize(location, fileSize);
					m_connection.endTransaction();
				}
			}finally{
				if( fileSize > 0L && !done){
					m_connection.abordTransaction();
				}
			}
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("create("+location+", "+guid+")");
			throw e;
		}finally{
			if(newConnectionCreated){
				m_connection.close();
			}
		}
		return guid;
	}

	private String dateAsPath() {
		Calendar c = GregorianCalendar.getInstance();
		StringBuilder b = new StringBuilder();
		b.append(c.get(Calendar.YEAR));
		b.append('-');
		// Java counts month starting with 0 !
		int month = c.get(Calendar.MONTH) + 1;
		if (month < 10)
			b.append('0');
		b.append(month);
		b.append('-');
		int day = c.get(Calendar.DAY_OF_MONTH);
		if (day < 10)
			b.append('0');
		b.append(day);
		return b.toString();
	}

	/**
	 * Add a Replica entry for the given file
	 * 
	 * @param guid
	 *            GUID of the file (without decoration)
	 * @param target
	 *            The physical location of the file.
	 * @throws IOException
	 * @throws ReceiveException 
	 * @throws LFCBrokenPipeException
	 */
	public void addReplica(String guid, URI target) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			addReplica(connection, guid, target);
		} finally {
			close(connection);
		}
	}
	
	public void addReplica(NSConnection connection, String guid, URI target) throws IOException, ReceiveException, LFCBrokenPipeException {
		try {
			connection.addReplica(guid, target);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("create("+guid+", "+target+")");
			throw e;
		}
	}
	
	
	/**
	 * Set the correct file size to a specific location
	 * @param location 
	 * 				The lfn of the file to create (lfn:///grid/vo/...).
	 * @param fileSize
	 * 				The size of the file.If <0 then 0 will be set
	 * @throws IOException
	 * @throws ReceiveException
	 * @throws LFCBrokenPipeException
	 */
	public void setFileSize(String location, long fileSize) throws IOException, ReceiveException, LFCBrokenPipeException {
		final NSConnection connection = new NSConnection(server, port, gssCredential);
		try {
			setFileSize(connection, location, fileSize);
		} finally {
			close(connection);
		}
	}
	
	public void setFileSize(NSConnection connection, String location, long fileSize) throws IOException, ReceiveException, LFCBrokenPipeException {
		try {
			connection.setfsize(location, fileSize);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("setFileSize("+location+", "+fileSize+")");
			throw e;
		}
	}
}