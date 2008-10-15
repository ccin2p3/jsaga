package fr.in2p3.jsaga.adaptor.ssh.data;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import fr.in2p3.jsaga.adaptor.data.BaseURL;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderGetter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterPutter;
import fr.in2p3.jsaga.adaptor.ssh.SSHAdaptorAbstract;
import fr.in2p3.jsaga.helpers.EntryPath;
import org.ogf.saga.error.*;

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

    public BaseURL getBaseURL() throws IncorrectURL {
        return new BaseURL(22);
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

	public boolean exists(String absolutePath, String additionalArgs)
			throws PermissionDenied, Timeout, NoSuccess {
		try {
            return (channelSftp.lstat(absolutePath) != null);
        } catch (SftpException e) {
			if (e.getid() == ChannelSftp.SSH_FX_NO_SUCH_FILE)
				return false;
			else
				throw new NoSuccess(e);
		}
	}

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        try {
            String filename = new EntryPath(absolutePath).getEntryName();
            SftpATTRS attrs = channelSftp.lstat(absolutePath);
            return new SFTPFileAttributes(filename, attrs);
        } catch (SftpException e) {
            if (e.getid() == ChannelSftp.SSH_FX_NO_SUCH_FILE)
                throw new DoesNotExist(e);
            if (e.getid() == ChannelSftp.SSH_FX_PERMISSION_DENIED)
                throw new PermissionDenied(e);
            throw new NoSuccess(e);
        }
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
                        LsEntry entry = (LsEntry) obj;
                        list[ii - 2] = new SFTPFileAttributes(entry.getFilename(), entry.getAttrs());
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
        String absolutePath = parentAbsolutePath+"/"+directoryName;
		try {
			channelSftp.mkdir(absolutePath);
		} catch (SftpException e) {
            switch(e.getid()) {
                case ChannelSftp.SSH_FX_PERMISSION_DENIED:
                    throw new PermissionDenied(e);
                case ChannelSftp.SSH_FX_NO_SUCH_FILE:
                    // check parent entry
                    while(parentAbsolutePath.endsWith("/")) {
                        parentAbsolutePath = parentAbsolutePath.substring(0, parentAbsolutePath.length()-1);
                    }
                    try {
                        switch(this.getAttributes(parentAbsolutePath, additionalArgs).getType()) {
                            case FileAttributes.FILE_TYPE:
                                throw new BadParameter("Parent entry is a file: "+parentAbsolutePath);
                            case FileAttributes.LINK_TYPE:
                                throw new BadParameter("Parent entry is a link: "+parentAbsolutePath);
                            case FileAttributes.UNKNOWN_TYPE:
                                throw new NoSuccess("Parent entry type is unknown: "+parentAbsolutePath, e);
                            default:
                                throw new NoSuccess("Unexpected error");
                        }
                    } catch (DoesNotExist e2) {
                        throw new ParentDoesNotExist("Parent entry does not exist: "+parentAbsolutePath, e);
                    }
                case ChannelSftp.SSH_FX_FAILURE:
                    // check entry
                    if (exists(absolutePath, additionalArgs)) {
                        throw new AlreadyExists("Entry already exists: "+absolutePath);
                    }
                    throw new NoSuccess(e);
                default:
                    throw new NoSuccess(e);
            }
		}
	}

	public void removeDir(String parentAbsolutePath, String directoryName,
			String additionalArgs) throws PermissionDenied, BadParameter,
			DoesNotExist, Timeout, NoSuccess {
        String absolutePath = parentAbsolutePath+"/"+directoryName;
		try {
			channelSftp.rmdir(absolutePath);
		} catch (SftpException e) {
            switch(e.getid()) {
                case ChannelSftp.SSH_FX_PERMISSION_DENIED:
                    throw new PermissionDenied(e);
                case ChannelSftp.SSH_FX_NO_SUCH_FILE:
                    try {
                        switch(this.getAttributes(absolutePath, additionalArgs).getType()) {
                            case FileAttributes.FILE_TYPE:
                                throw new BadParameter("Entry is a file: "+absolutePath);
                            case FileAttributes.LINK_TYPE:
                                throw new BadParameter("Entry is a link: "+absolutePath);
                            case FileAttributes.UNKNOWN_TYPE:
                                throw new NoSuccess("Entry type is unknown: "+absolutePath, e);
                            default:
                                throw new NoSuccess("Unexpected error");
                        }
                    } catch(DoesNotExist e2) {
                        throw new DoesNotExist("Entry does not exist: "+absolutePath, e);
                    }
                default:
                    throw new NoSuccess(e);
            }
		}
	}

	public void removeFile(String parentAbsolutePath, String fileName,
			String additionalArgs) throws PermissionDenied, BadParameter,
			DoesNotExist, Timeout, NoSuccess {
        String absolutePath = parentAbsolutePath+"/"+fileName;
        try {
			channelSftp.rm(absolutePath);
		} catch (SftpException e) {
            switch(e.getid()) {
                case ChannelSftp.SSH_FX_PERMISSION_DENIED:
                    try {
                        switch(this.getAttributes(absolutePath, additionalArgs).getType()) {
                            case FileAttributes.DIRECTORY_TYPE:
                                throw new BadParameter("Entry is a directory: "+absolutePath);
                            default:
                                throw new PermissionDenied(e);
                        }
                    } catch(DoesNotExist e2) {
                        throw new DoesNotExist("Entry does not exist: "+absolutePath, e);
                    }
                case ChannelSftp.SSH_FX_NO_SUCH_FILE:
                    throw new DoesNotExist("Entry does not exist: "+absolutePath, e);
                default:
                    throw new NoSuccess(e);
            }
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
