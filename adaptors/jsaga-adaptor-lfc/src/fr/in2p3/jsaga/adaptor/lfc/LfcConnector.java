package fr.in2p3.jsaga.adaptor.lfc;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.apache.log4j.Logger;
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
	private static Logger s_logger = Logger.getLogger(LfcConnector.class);

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
	 */
	private LfcConnector(String host, int port, String vo, GSSCredential gssCredential) {
		this.server = host;
		this.port = port;
		this.vo = vo;
		this.gssCredential = gssCredential;
	}
	
	public static LfcConnector getInstance(String host, int port, String vo, GSSCredential gssCredential) throws IllegalArgumentException{
		if(host == null || "".equals(host)){
			throw new IllegalArgumentException("The LFC host must be set.");
		}
		if(port <= 0){
			throw new IllegalArgumentException("The LFC port must be greater than 0.");
		}
		return new LfcConnector(host, port, vo, gssCredential);
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
	 */
	public Collection<LFCReplica> listReplicas(String path, String guid) throws IOException, ReceiveException {
		final LfcConnection connection = new LfcConnection(server, port, gssCredential);
		final Collection<LFCReplica> retVal;
		try {
			retVal = connection.listReplica(path, guid);
		} finally {
			connection.close();
		}
		return retVal;
	}

	/**
	 * Get the different file/directory/symbolic link attributes
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
	 */
	public LFCFile stat(String path, boolean followSymbolicLink) throws IOException, ReceiveException {
		final LfcConnection connection = new LfcConnection(server, port, gssCredential);
		final LFCFile file;
		try {
			if (followSymbolicLink) {
				file = connection.stat(path);
			} else {
				file = connection.lstat(path);
			}
		} finally {
			connection.close();
		}
		return file;
	}

	/**
	 * Test if the file or the directory can be read.
	 * 
	 * @param path
	 * @return <code>true</code> if it can be read
	 * @throws IOException
	 * @throws ReceiveException 
	 */
	public boolean canRead(String path) throws IOException, ReceiveException {
		final LfcConnection connection = new LfcConnection(server, port, gssCredential);
		try {
			connection.access(path, AccessType.READ_OK);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			if (LfcError.PERMISSION_DENIED.equals(e.getLFCError())) {
				return false;
			}else{
				throw e;
			}
		} finally {
			connection.close();
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
	 */
	public boolean canWrite(String path) throws IOException, ReceiveException {
		final LfcConnection connection = new LfcConnection(server, port, gssCredential);
		try {
			connection.access(path, AccessType.WRITE_OK);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			if (LfcError.PERMISSION_DENIED.equals(e.getLFCError())) {
				return false;
			}else{
				throw e;
			}
		} finally {
			connection.close();
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
	 */
	public boolean exist(String path) throws IOException, ReceiveException {
		final LfcConnection connection = new LfcConnection(server, port, gssCredential);
		try {
			connection.access(path, AccessType.EXIST_OK);
		} catch (IOException e) {
			throw e;
		} catch (ReceiveException e) {
			if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
				return false;
			}else{
				throw e;
			}
		} finally {
			connection.close();
		}
		return true;
	}

	/**
	 * Get the group names which correspond to specific gids.
	 * @throws ReceiveException 
	 */
	public Collection<String> getGrpByGids(int[] gids) throws IOException, ReceiveException {
		final LfcConnection connection = new LfcConnection(server, port, gssCredential);
		try {
			return connection.getGrpByGids(gids);
		} finally {
			connection.close();
		}
	}

	public String getUsrByUid(int uid) throws IOException, ReceiveException {
		final LfcConnection connection = new LfcConnection(server, port, gssCredential);
		try {
			return connection.getUsrByUid(uid);
		} finally {
			connection.close();
		}
	}

	/**
	 * Get the content of a directory. If the given path is not a directory it
	 * will return <code>null</code>
	 * 
	 * @param path
	 *            path of the directory
	 * @return A collection of files or directories inside the given path or
	 *         null if the given path is not a directory
	 * @throws IOException
	 * @throws ReceiveException 
	 */
	public Collection<LFCFile> list(String path, boolean followSymbolicLink) throws IOException, ReceiveException {
		final LFCFile file = this.stat(path, followSymbolicLink);
		if (!file.isDirectory()) {
			return null;
		}

		final LfcConnection lfcConnection = new LfcConnection(server, port, gssCredential);
		final Collection<LFCFile> files;
		try {
			final long fileID = lfcConnection.opendir(path, null);
			files = lfcConnection.readdir(fileID);
			lfcConnection.closedir();
		} finally {
			lfcConnection.close();
		}
		return files;
	}

	/**
	 * Create a directory in the LFC
	 * 
	 * @param path
	 *            path of the directory
	 * @throws IOException
	 * @throws ReceiveException 
	 */
	public void mkdir(String path) throws IOException, ReceiveException {
		final LfcConnection connection = new LfcConnection(server, port, gssCredential);
		try {
			connection.mkdir(path, UUID.randomUUID().toString());
		} finally {
			connection.close();
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
	 */
	public boolean deleteDir(String path) throws IOException, ReceiveException {
		final LfcConnection connection = new LfcConnection(server, port, gssCredential);
		try {
			connection.rmdir(path);
		} finally {
			connection.close();
		}
		return true;
	}
	
	/**
	 * Delete a file from the LFC
	 * 
	 * @param path
	 *            path of the file
	 * @return <code>true</code> is everything was ok.
	 * @throws IOException
	 * @throws ReceiveException 
	 */
	public boolean deleteFile(String path) throws IOException, ReceiveException {
		final LfcConnection connection = new LfcConnection(server, port, gssCredential);
		final Collection<LFCReplica> replicas;
		try {
			replicas = connection.listReplica(path, null);
		} finally {
			connection.close();
		}
		for (LFCReplica replica : replicas) {
			s_logger.info("Deleting Replica: " + replica.getSfn());
			final LfcConnection connection2 = new LfcConnection(server, port, gssCredential);
			try {
				connection2.delReplica(replica.getGuid(), replica.getSfn());
			} finally {
				connection2.close();
			}
			//FIXME: DPM????
		}
		s_logger.info("Deleting path: " + path);
		final LfcConnection connection3 = new LfcConnection(server, port, gssCredential);
		try {
			connection3.unlink(path);
		} finally {
			connection3.close();
		}
		return true;
	}

	/**
	 * Delete a file and all the replicas.
	 * 
	 * @param guid
	 *            GUID of the file.
	 * @return true if the file was deleted.
	 * @throws IOException
	 * @throws ReceiveException 
	 */
	public boolean deleteGuid(String guid) throws IOException, ReceiveException {
		final LfcConnection lfc_connection = new LfcConnection(server, port, gssCredential);
		final Collection<LFCReplica> replicas;
		try {
			replicas = lfc_connection.listReplica(null, guid);

			for (LFCReplica replica : replicas) {
				s_logger.info("Deleting Replica: " + replica.getSfn());
				lfc_connection.delReplica(guid, replica.getSfn());
				// FIXME: Delete from SRM here????
			}
			s_logger.info("Deleting GUID: " + guid);
			return lfc_connection.delFiles(new String[] { guid }, false);
		} finally {
			lfc_connection.close();
		}
	}

	/**
	 * Change access mode of a LFC directory/file. Symbolic link are not
	 * supported yet
	 * 
	 * @param path
	 *            File path
	 * @param mode
	 *            Absolute UNIX like mode (octal value)
	 * @throws IOException
	 * @throws ReceiveException 
	 */
	public void chmod(String path, int mode) throws IOException, ReceiveException {
		LfcConnection lfc_connection = new LfcConnection(server, port, gssCredential);
		try {
			lfc_connection.chmod(path, mode);
		} finally {
			lfc_connection.close();
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
	 */
	public void chown(String path, boolean recursive, boolean followSymbolicLinks, String usrName, String grpName) throws IOException, ReceiveException {
		LfcConnection connection;
		int new_uid = -1;
		int new_gid = -1;
		if (usrName != null) {
			if (usrName.equals("root")) {
				new_uid = 0;
			} else {
				connection = new LfcConnection(server, port, gssCredential);
				try {
					new_uid = connection.getUsrByName(usrName);
				} catch (Exception e) {
					//FIXME: check of the correct error would be better 
					throw new IOException("Unable to find the uid of " + usrName + " in the LFC");
				} finally {
					connection.close();
				}
			}
		}
		if (grpName != null) {
			if (grpName.equals("root")) {
				new_gid = 0;
			} else {
				connection = new LfcConnection(server, port, gssCredential);
				try {
					new_gid = connection.getGrpByName(grpName);
				} catch (IOException e) {
					throw new IOException("Unable to find the gid of " + grpName + " in the LFC");
				} finally {
					connection.close();
				}
			}
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
	 */
	public void chown(String path, boolean recursive, boolean followSymbolicLinks, int new_uid, int new_gid) throws IOException, ReceiveException {
		if (recursive == true) {
			// TODO ....
			throw new UnsupportedOperationException("Recurcive chown is not implemented...");
		}
		if (new_uid < 0 && new_gid < 0) {
			throw new IllegalArgumentException("You must specify at least a new owner or a new group");
		}
		LfcConnection connection = new LfcConnection(server, port, gssCredential);
		try {
			if (followSymbolicLinks) {
				connection.chown(path, new_uid, new_gid);
			} else {
				connection.lchown(path, new_uid, new_gid);
			}
		} finally {
			connection.close();
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
	 */
	public void rename(String oldPath, String newPath) throws IOException, ReceiveException {
		LfcConnection connection = new LfcConnection(server, port, gssCredential);
		try {
			connection.rename(oldPath, newPath);
		} finally {
			connection.close();
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
	 */
	public String create(long fileSize) throws IOException, ReceiveException {
		String guid = UUID.randomUUID().toString();
		String parent = "/grid/" + vo + "/generated/" + dateAsPath();
//		LfcConnection connection = new LfcConnection(server, port, gssCredential);
//		try {
//			connection.mkdir(parent, UUID.randomUUID().toString());
//		} catch (IOException e) {
//			//TODO: Check what's happen if the directory already exists of 
//			s_logger.debug("Creating parent", e);
//		} finally {
//			connection.close();
//		}
		String path = parent + "/file-" + guid;
		URI uri = null;
		try {
			uri = new URI("lfn:///" + path);
		} catch (URISyntaxException e) {
		}
		return create(uri, guid, fileSize);
	}

	/**
	 * Create a new File at a specific location
	 * 
	 * @param location
	 *            The lfn of the file to create (lfn:////grid/vo/...). Please
	 *            note the 4 (!) slashes. These are required to specify an empty
	 *            hostname and an absolute directory.
	 * @param guid
	 *            The guid of the file
	 * @param fileSize
	 *            The size of the file.If <0 then 0 will be set
	 * @return a GUID to the new file
	 * @throws IOException
	 * @throws ReceiveException 
	 */
	public String create(URI location, String guid, long fileSize) throws IOException, ReceiveException {
		String path = location.getPath();
		s_logger.info("Creating " + guid + " with path " + path);
		LfcConnection connection = new LfcConnection(server, port, gssCredential);
		try {
			connection.creat(path, guid);
		} finally {
			connection.close();
		}
		if (fileSize > 0L) {
			connection = new LfcConnection(server, port, gssCredential);
			try {
				connection.setfsize(location.getPath(), fileSize);
			} catch (IOException e) {
				s_logger.debug("Creating parent", e);
			} finally {
				connection.close();
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
	 *            an SRM url.
	 * @throws IOException
	 * @throws ReceiveException 
	 */
	public void addReplica(String guid, URI target) throws IOException, ReceiveException {
		final LfcConnection connection = new LfcConnection(server, port, gssCredential);
		try {
			connection.addReplica(guid, target);
		} finally {
			connection.close();
		}
	}

	/**
	 * @return the server used by this connector.
	 */
	public String getServer() {
		return server;
	}

	/**
	 * @return the port used by this connector.
	 */
	public int getPort() {
		return port;
	}

}
