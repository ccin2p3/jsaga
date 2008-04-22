package fr.in2p3.jsaga.adaptor.ssh.data;

import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.read.*;
import fr.in2p3.jsaga.adaptor.data.write.*;
import fr.in2p3.jsaga.adaptor.ssh.SSHAdaptorAbstract;

import org.ogf.saga.error.AlreadyExists;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Vector;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   SFTPDataAdaptor
 * Author: Nicolas DEMESY (nicolas.demesy@bt.com)
 * Date:   11 avril 2008
 * ***************************************************/

public class SFTPDataAdaptor extends SSHAdaptorAbstract implements
		FileReaderGetter, FileWriterPutter, DataRename {
	private ChannelSftp channelSftp;
	
	public String getType() {
		return "sftp";
	}

	public void connect(String userInfo, String host, int port,
			String basePath, Map attributes) throws NotImplemented,
			AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout,
			NoSuccess {
		super.connect(userInfo, host, port, basePath, attributes);
		
		// start sftp channel
		try {
			Channel channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp) channel;
		} catch (JSchException e) {
			throw new NoSuccess("Unable to open channel", e);
		}
	}

	public void disconnect() throws NoSuccess {
		channelSftp.exit();
		super.disconnect();
	}

	public void getToStream(String absolutePath, String additionalArgs,
			OutputStream stream) throws PermissionDenied, BadParameter,
			DoesNotExist, Timeout, NoSuccess {
		try {
			channelSftp.get(absolutePath, stream);
		} catch (SftpException e) {
			if (e.getid() == ChannelSftp.SSH_FX_NO_SUCH_FILE)
				throw new DoesNotExist(e);
			else if (e.getid() == ChannelSftp.SSH_FX_PERMISSION_DENIED)
				throw new PermissionDenied(e);
			else
				throw new NoSuccess(e);
		}
	}

	public long getSize(String absolutePath, String additionalArgs)
			throws PermissionDenied, BadParameter, DoesNotExist, Timeout,
			NoSuccess {
		try {
			Vector<LsEntry> vv = channelSftp.ls(absolutePath);
			if (vv != null) {
				for (int ii = 0; ii < vv.size(); ii++) {
					Object obj = vv.elementAt(ii);
					if (obj instanceof com.jcraft.jsch.ChannelSftp.LsEntry) {
						if (absolutePath
								.endsWith(((LsEntry) obj).getFilename())) {
							return ((LsEntry) obj).getAttrs().getSize();
						}
					}
				}
			}
			return -1;
		} catch (SftpException e) {
			if (e.getid() == ChannelSftp.SSH_FX_NO_SUCH_FILE)
				throw new DoesNotExist(e);
			if (e.getid() == ChannelSftp.SSH_FX_PERMISSION_DENIED)
				throw new PermissionDenied(e);

			// check source
			if (!isEntry(absolutePath, additionalArgs)) {
				throw new BadParameter("The entry is not a file.");
			}

			throw new NoSuccess(e);
		}
	}

	public boolean exists(String absolutePath, String additionalArgs)
			throws PermissionDenied, Timeout, NoSuccess {
		try {
			if (channelSftp.ls(absolutePath) != null)
				return true;
			else
				return false;
		} catch (SftpException e) {
			if (e.getid() == ChannelSftp.SSH_FX_NO_SUCH_FILE)
				return false;
			else
				throw new NoSuccess(e);
		}
	}

	public boolean isDirectory(String absolutePath, String additionalArgs)
			throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
		try {
			Vector<LsEntry> vv = channelSftp.ls(absolutePath);
			if (vv != null) {
				FileAttributes[] list = new SFTPFileAttributes[vv.size()];
				for (int ii = 0; ii < vv.size(); ii++) {
					Object obj = vv.elementAt(ii);
					if (obj instanceof com.jcraft.jsch.ChannelSftp.LsEntry) {
						if (((LsEntry) obj).getFilename().equals(".")) {
							if (((LsEntry) obj).getAttrs().isDir())
								return true;
							else
								return false;
						}
					}
				}
			}
			return false;
		} catch (SftpException e) {
			if (e.getid() == ChannelSftp.SSH_FX_NO_SUCH_FILE)
				throw new DoesNotExist(e);
			if (e.getid() == ChannelSftp.SSH_FX_PERMISSION_DENIED)
				throw new PermissionDenied(e);
			throw new NoSuccess(e);
		}

	}

	public boolean isEntry(String absolutePath, String additionalArgs)
			throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
		return !isDirectory(absolutePath, additionalArgs);
	}

	public FileAttributes[] listAttributes(String absolutePath,
			String additionalArgs) throws PermissionDenied, DoesNotExist,
			Timeout, NoSuccess {
		try {
			Vector<LsEntry> vv = channelSftp.ls(absolutePath);
			if (vv != null && vv.size() > 2) {
				// remove . and .. in the list
				FileAttributes[] list = new SFTPFileAttributes[vv.size() - 2];
				for (int ii = 2; ii < vv.size(); ii++) {
					Object obj = vv.elementAt(ii);
					if (obj instanceof LsEntry) {
						list[ii - 2] = new SFTPFileAttributes((LsEntry) obj);
					}
				}
				return list;
			} else {
				return new SFTPFileAttributes[0];
			}
		} catch (SftpException e) {
			if (e.getid() == ChannelSftp.SSH_FX_NO_SUCH_FILE)
				throw new DoesNotExist(e);
			if (e.getid() == ChannelSftp.SSH_FX_PERMISSION_DENIED)
				throw new PermissionDenied(e);
			throw new NoSuccess(e);
		}
	}

	public void putFromStream(String absolutePath, boolean append,
			String additionalArgs, InputStream stream) throws PermissionDenied,
			BadParameter, AlreadyExists, ParentDoesNotExist, Timeout, NoSuccess {
		try {
			if (append)
				channelSftp.put(stream, absolutePath, ChannelSftp.APPEND);
			else
				channelSftp.put(stream, absolutePath, ChannelSftp.OVERWRITE);
		} catch (SftpException e) {
			if (!exists(absolutePath, additionalArgs))
				throw new AlreadyExists(e);
			if (e.getid() == ChannelSftp.SSH_FX_PERMISSION_DENIED)
				throw new PermissionDenied(e);
			throw new NoSuccess(e);
		}
	}

	public void makeDir(String parentAbsolutePath, String directoryName,
			String additionalArgs) throws PermissionDenied, BadParameter,
			AlreadyExists, ParentDoesNotExist, Timeout, NoSuccess {
		try {
			channelSftp.mkdir(parentAbsolutePath + "/" + directoryName);
		} catch (SftpException e) {

			if (e.getid() == ChannelSftp.SSH_FX_PERMISSION_DENIED)
				throw new PermissionDenied(e);

			// check parent exist
			if (!exists(parentAbsolutePath, additionalArgs))
				throw new ParentDoesNotExist(e);

			// check directory already exist
			try {
				if (isDirectory(parentAbsolutePath + "/" + directoryName,
						additionalArgs))
					throw new AlreadyExists("The directory ["
							+ parentAbsolutePath + "/" + directoryName
							+ "] already exists.");
			} catch (DoesNotExist e1) {
				// means that the directory does not exist
			}

			try {
				if (!isDirectory(parentAbsolutePath, additionalArgs))
					throw new BadParameter(
							"The parent directory is not a directory.");
			} catch (DoesNotExist e1) {
				throw new BadParameter(
						"The parent directory is not a directory.", e1);
			}

			throw new NoSuccess(e);
		}
	}

	public void removeDir(String parentAbsolutePath, String directoryName,
			String additionalArgs) throws PermissionDenied, BadParameter,
			DoesNotExist, Timeout, NoSuccess {
		try {
			channelSftp.rmdir(parentAbsolutePath + "/" + directoryName);
		} catch (SftpException e) {

			// check directory
			if (e.getid() == ChannelSftp.SSH_FX_NO_SUCH_FILE)
				throw new DoesNotExist(e);
			if (e.getid() == ChannelSftp.SSH_FX_PERMISSION_DENIED)
				throw new PermissionDenied(e);

			// check directory
			if (!isDirectory(parentAbsolutePath + "/" + directoryName,
					additionalArgs)) {
				throw new BadParameter("The entry [" + parentAbsolutePath + "/"
						+ directoryName + "] is not a directory.");
			}

			throw new NoSuccess(e);
		}
	}

	public void removeFile(String parentAbsolutePath, String fileName,
			String additionalArgs) throws PermissionDenied, BadParameter,
			DoesNotExist, Timeout, NoSuccess {
		try {
			channelSftp.rm(parentAbsolutePath + "/" + fileName);
		} catch (SftpException e) {
			if (e.getid() == ChannelSftp.SSH_FX_NO_SUCH_FILE)
				throw new DoesNotExist(e);
			if (e.getid() == ChannelSftp.SSH_FX_PERMISSION_DENIED)
				throw new PermissionDenied(e);

			// check file
			if (isDirectory(parentAbsolutePath + "/" + fileName, additionalArgs)) {
				throw new BadParameter("The entry [" + parentAbsolutePath + "/"
						+ fileName + "] is a directory.");
			}

			throw new NoSuccess(e);
		}
	}

	public void rename(String sourceAbsolutePath, String targetAbsolutePath,
			boolean overwrite, String additionalArgs) throws PermissionDenied,
			BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
		if (overwrite)
			throw new NoSuccess("Overwrite not implemented");

		try {
			channelSftp.rename(sourceAbsolutePath, targetAbsolutePath);
		} catch (SftpException e) {
			if (e.getid() == ChannelSftp.SSH_FX_NO_SUCH_FILE)
				throw new DoesNotExist(e);
			if (e.getid() == ChannelSftp.SSH_FX_PERMISSION_DENIED)
				throw new PermissionDenied(e);
			throw new NoSuccess(e);
		}
	}
}
