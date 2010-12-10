package fr.in2p3.jsaga.adaptor.lfc;

import java.io.IOException;
import java.net.URI;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.ietf.jgss.GSSCredential;

import fr.in2p3.jsaga.adaptor.lfc.LfcConnection.AccessType;
import fr.in2p3.jsaga.adaptor.lfc.LfcConnection.LFCFile;
import fr.in2p3.jsaga.adaptor.lfc.LfcConnection.LFCReplica;
import fr.in2p3.jsaga.adaptor.lfc.LfcConnection.LfcError;
import fr.in2p3.jsaga.adaptor.lfc.LfcConnection.ReceiveException;

/**
 * Provides a High-Level view of an LFC server.
 * 
 * @author Max Berger
 * @author Jerome Revillard
 */
public class LfcConnector {
	private final String vo;
	private final String server;
	private final int port;
	private final GSSCredential gssCredential;
//	private final LfcConnection lfcConnection;

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
	 */
	private LfcConnector(String host, int port, String vo, GSSCredential gssCredential) throws IOException, ReceiveException {
		this.server = host;
		this.port = port;
		this.vo = vo;
		this.gssCredential = gssCredential;
//		lfcConnection = new LfcConnection(host, port, gssCredential);
//		startSession();
	}
	
	public static LfcConnector getInstance(String host, int port, String vo, GSSCredential gssCredential) throws IllegalArgumentException, IOException, ReceiveException{
		if(host == null || "".equals(host)){
			throw new IllegalArgumentException("The LFC host must be set.");
		}
		if(port <= 0){
			throw new IllegalArgumentException("The LFC port must be greater than 0.");
		}
		return new LfcConnector(host, port, vo, gssCredential);
	}
	
//	public void startSession() throws IOException, ReceiveException, TimeoutException{
//		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
//		try{
//			lfcConnection.startSession();
//		} catch (IOException e) {
//			throw e;
//		} catch (ReceiveException e) {
//			e.setExecutedCmd("startSession()");
//			throw e;
//		}
//	}
//	
//	public void closeSession() throws IOException, ReceiveException, TimeoutException{
//		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
//		try{
//			lfcConnection.closeSession();
//		} catch (IOException e) {
//			throw e;
//		} catch (ReceiveException e) {
//			e.setExecutedCmd("closeSession()");
//			throw e;
//		}
//	}

	public void close(LfcConnection lfcConnection){
		lfcConnection.close();
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
	 * @throws TimeoutException 
	 */
	public Collection<LFCReplica> listReplicas(String path, String guid) throws IOException, ReceiveException, TimeoutException {
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		final Collection<LFCReplica> retVal;
		try {
			retVal = lfcConnection.listReplica(path, guid);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("listReplicas("+path+","+guid+")");
			throw e;
		} finally {
			close(lfcConnection);
		}
		return retVal;
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
	 * @throws TimeoutException 
	 */
	public LFCFile stat(String path, boolean followSymbolicLink, boolean getGUID) throws IOException, ReceiveException, TimeoutException {
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		final LFCFile file;
		try {
			if(getGUID == true && followSymbolicLink == false){
				throw new InvalidParameterException("LFC stat: Cannot have both getGUID=true and followSymbolicLink=false");
			}
			if(!getGUID){
				if (followSymbolicLink) {
					file = lfcConnection.stat(path);
				} else {
					file = lfcConnection.lstat(path);
				}
			}else{
				file = lfcConnection.statg(path, null);
			}
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("stat("+path+") - followSymbolicLink="+followSymbolicLink);
			throw e;
		} finally {
			close(lfcConnection);
		}
		return file;
	}
	
	/**
	 * Get the GUID of an PFN path
	 * @param sfn	The PFN for which the GUID will be returned
	 * @return The GUID of the PFN
	 * @throws IOException
	 * @throws ReceiveException
	 * @throws TimeoutException
	 */
	public LFCFile getGuidFromPfn(String sfn) throws IOException, ReceiveException, TimeoutException {
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		final LFCFile file;
		try {
		    file = lfcConnection.statr(sfn);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("getGuidFromSfn("+sfn+")");
			throw e;
		} finally {
			close(lfcConnection);
		}
		return file;
	}
	
	/**
	 * Resolve one level of symbolic link path
	 * @param link The symbolic link path
	 * @return  The path to which point the symbolic link
	 * @throws IOException
	 * @throws ReceiveException
	 * @throws TimeoutException
	 */
	public String readlink(String link) throws IOException, ReceiveException, TimeoutException {
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		final String path;
		try {
			path = lfcConnection.readlink(link);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("readlink("+link+")");
			throw e;
		} finally {
			close(lfcConnection);
		}
		return path;
	}

	/**
	 * Create a symbolic link
	 * @param path		The original file/directory
	 * @param target	The path of the link
	 * @throws IOException
	 * @throws ReceiveException
	 * @throws TimeoutException
	 */
	public void symbLink(String path, String target, boolean overwrite) throws IOException, ReceiveException, TimeoutException {
		//FIXME: DO A TRANSACTION....
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		try {
			lfcConnection.link(path, target);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			if(LfcError.FILE_EXISTS.equals(e.getLFCError())){
				//The file already exists...remove if 'overwrite'==true;
				if(overwrite == true){
					this.unlink(path);
					this.symbLink(path, target, false);
				}else{
					e.setExecutedCmd("symbLink("+path+","+target+")");
					throw e;
				}
			}else{
				e.setExecutedCmd("symbLink("+path+","+target+")");
				throw e;
			}
		} finally {
			close(lfcConnection);
		}
	}

	/**
	 * Test if the file or the directory can be read.
	 * 
	 * @param path
	 * @return <code>true</code> if it can be read
	 * @throws IOException
	 * @throws ReceiveException 
	 * @throws TimeoutException 
	 */
	public boolean canRead(String path) throws IOException, ReceiveException, TimeoutException {
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		try {
			lfcConnection.access(path, AccessType.READ_OK);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			if (LfcError.PERMISSION_DENIED.equals(e.getLFCError())) {
				return false;
			}else{
				e.setExecutedCmd("canRead("+path+")");
				throw e;
			}
		} finally {
			close(lfcConnection);
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
	 * @throws TimeoutException 
	 */
	public boolean canWrite(String path) throws IOException, ReceiveException, TimeoutException {
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		try {
			lfcConnection.access(path, AccessType.WRITE_OK);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			if (LfcError.PERMISSION_DENIED.equals(e.getLFCError())) {
				return false;
			}else{
				e.setExecutedCmd("canWrite("+path+")");
				throw e;
			}
		} finally {
			close(lfcConnection);
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
	 * @throws TimeoutException 
	 */
	public boolean exist(String path) throws IOException, ReceiveException, TimeoutException {
		try {
			this.stat(path,false,false);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
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
	 * @throws ReceiveException 
	 * @throws ReceiveException 
	 * @throws TimeoutException 
	 */
	public Collection<String> getGrpByGids(int[] gids) throws IOException, ReceiveException, TimeoutException {
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		try {
			return lfcConnection.getGrpByGids(gids);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("getGrpByGids("+Arrays.toString(gids)+")");
			throw e;
		} finally {
			close(lfcConnection);
		}
	}

	public String getUsrByUid(int uid) throws IOException, ReceiveException, TimeoutException {
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		try {
			return lfcConnection.getUsrByUid(uid);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("getUsrByUid("+uid+")");
			throw e;
		} finally {
			close(lfcConnection);
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
	 * @throws TimeoutException 
	 */
	public Collection<LFCFile> list(String path, boolean followSymbolicLink) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= LfcConnection.MAX_RETRY_IF_TIMEOUT; z++) {
			LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
			try{
				try {
					final long fileID = lfcConnection.opendir(path, null);
					Collection<LFCFile> files = lfcConnection.readdir(fileID);
					lfcConnection.closedir();
					return files;
				} finally {
					close(lfcConnection);
				}
			} catch (IOException e) {
				throw e;
			} catch (ReceiveException e) {
				e.setExecutedCmd("list("+path+") - followSymbolicLink="+followSymbolicLink);
				throw e;
			}catch (TimeoutException e) {
				if(z == LfcConnection.MAX_RETRY_IF_TIMEOUT ){
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
	 * @throws TimeoutException 
	 */
	public void mkdir(String path) throws IOException, ReceiveException, TimeoutException {
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		try {
			lfcConnection.mkdir(path, UUID.randomUUID().toString());
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("mkdir("+path+")");
			throw e;
		} finally {
			close(lfcConnection);
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
	 * @throws TimeoutException 
	 */
	public boolean deleteDir(String path) throws IOException, ReceiveException, TimeoutException {
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		try {
			lfcConnection.rmdir(path);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("deleteFile("+path+")");
			throw e;
		} finally {
			close(lfcConnection);
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
	 * @throws TimeoutException 
	 */
	public void unlink(String path) throws IOException, ReceiveException, TimeoutException {
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		try{
			try {
				lfcConnection.unlink(path);
			} finally {
				close(lfcConnection);
			}
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
	 * @throws TimeoutException 
	 */
	public void deleteReplica(String guid, String sfn) throws IOException, ReceiveException, TimeoutException {
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		try {
			lfcConnection.delReplica(guid, sfn);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("deleteReplica("+guid+","+sfn+")");
			throw e;
		} finally {
			close(lfcConnection);
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
	 * @throws TimeoutException 
	 */
	public boolean deleteGuid(String guid, boolean force) throws IOException, ReceiveException, TimeoutException {
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		try {
			return lfcConnection.delFiles(new String[] { guid }, force);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("deleteGuid("+guid+","+force+")");
			throw e;
		} finally {
			close(lfcConnection);
		}
	}

	/**
	 * Change access mode of a LFC directory/file. Symbolic link are not
	 * supported yet
	 * 
	 * @param path
	 *            File path
	 * @param mode
	 *            Absolute LFC permissions (see {@link LfcConnection} for values...)
	 * @throws IOException
	 * @throws ReceiveException 
	 * @throws TimeoutException 
	 */
	public void chmod(String path, int mode) throws IOException, ReceiveException, TimeoutException {
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		try {
			lfcConnection.chmod(path, mode);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("chmod("+path+","+mode+")");
			throw e;
		} finally {
			close(lfcConnection);
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
	 * @throws TimeoutException 
	 */
	public void chown(String path, boolean recursive, boolean followSymbolicLinks, String usrName, String grpName) throws IOException, ReceiveException, TimeoutException {
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		int new_uid = -1;
		int new_gid = -1;
		try{
			if (usrName != null) {
				if (usrName.equals("root")) {
					new_uid = 0;
				} else {
					try {
						new_uid = lfcConnection.getUsrByName(usrName);
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
						new_gid = lfcConnection.getGrpByName(grpName);
					} catch (IOException e) {
						throw new IOException("Unable to find the gid of " + grpName + " in the LFC");
					}
				}
			}
		}finally{
			close(lfcConnection);
		}
		chown(path, recursive, followSymbolicLinks, new_uid, new_gid);
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
	 * @throws TimeoutException 
	 */
	public void chown(String path, boolean recursive, boolean followSymbolicLinks, int new_uid, int new_gid) throws IOException, ReceiveException, TimeoutException {
		if (recursive == true) {
			// TODO ....
			throw new UnsupportedOperationException("Recurcive chown is not implemented...");
		}
		if (new_uid < 0 && new_gid < 0) {
			throw new IllegalArgumentException("You must specify at least a new owner or a new group");
		}
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		try {
			if (followSymbolicLinks) {
				lfcConnection.chown(path, new_uid, new_gid);
			} else {
				lfcConnection.lchown(path, new_uid, new_gid);
			}
		} catch (IOException e) {
				throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("chown("+path+") - uid="+new_uid+", gid="+new_gid+", followSymbolicLinks="+followSymbolicLinks);
			throw e;
		} finally {
			close(lfcConnection);
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
	 * @throws TimeoutException 
	 */
	public void rename(String oldPath, String newPath) throws IOException, ReceiveException, TimeoutException {
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		try {
			lfcConnection.rename(oldPath, newPath);
		} catch (IOException e) {
				throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("rename("+oldPath+", "+newPath+")");
			throw e;
		} finally {
			close(lfcConnection);
		}
	}

	/**
	 * Create a new File
	 * 
	 * @param fileSize
	 *            the size of the file. If <0 then 0 will be set
	 * @return a GUID to the new file
	 * @throws IOException
	 * @throws ReceiveException 
	 * @throws TimeoutException 
	 */
	public String create(long fileSize) throws IOException, ReceiveException, TimeoutException {
		String guid = UUID.randomUUID().toString();
		String parent = "/grid/" + vo + "/generated/" + dateAsPath();
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		try {
			lfcConnection.mkdir(parent, UUID.randomUUID().toString());
		} catch (ReceiveException e) {
			if(!LfcError.FILE_EXISTS.equals(e.getLFCError())){
				e.setExecutedCmd("create(/grid/" + vo + "/generated/" + dateAsPath()+")");
				throw e;
			}
		} finally {
			close(lfcConnection);
		}
		String path = parent + "/file-" + guid;
		return create("lfn:///" + path, guid, fileSize);
	}

	/**
	 * Create a new File at a specific location
	 * 
	 * @param location
	 *            The lfn of the file to create (lfn:///grid/vo/...).
	 * @param guid
	 *            The guid of the file
	 * @param fileSize
	 *            The size of the file.If <0 then 0 will be set
	 * @return a GUID to the new file
	 * @throws IOException
	 * @throws ReceiveException 
	 * @throws TimeoutException 
	 */
	public String create(String location, String guid, long fileSize) throws IOException, ReceiveException, TimeoutException {
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		try{
			try {
				lfcConnection.creat(location, guid);
				if (fileSize > 0L) {
					lfcConnection.setfsize(location, fileSize);
				}
			} finally {
				close(lfcConnection);
			}
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("create("+location+", "+guid+")");
			throw e;
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
	 */
	public void addReplica(String guid, URI target) throws IOException, ReceiveException, TimeoutException {
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		try {
			lfcConnection.addReplica(guid, target);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("create("+guid+", "+target+")");
			throw e;
		} finally {
			close(lfcConnection);
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
	 * @throws TimeoutException
	 */
	public void setFileSize(String location, long fileSize) throws IOException, ReceiveException, TimeoutException {
		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		try {
			lfcConnection.setfsize(location, fileSize);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			e.setExecutedCmd("setFileSize("+location+", "+fileSize+")");
			throw e;
		} finally {
			close(lfcConnection);
		}
	}
}
