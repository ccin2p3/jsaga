package fr.in2p3.jsaga.adaptor.ssh.data;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
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
	protected static final String FILENAME_ENCODING = "FilenameEncoding";
	
	public String getType() {
		return "deprecated-sftp";
	}

    public int getDefaultPort() {
        return 22;
    }

    public Usage getUsage() {
        return new UAnd(
        		new Usage[]{
					 new UOptional(FILENAME_ENCODING),
					 super.getUsage()

        		});
    }
    
	public void connect(String userInfo, String host, int port,
			String basePath, Map attributes) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException,
            NoSuccessException {
		super.connect(userInfo, host, port, basePath, attributes);
		
		// start sftp channel
		try {
			Channel channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp) channel;
		} catch (JSchException e) {
			throw new NoSuccessException("Unable to open channel", e);
		}
		if (attributes.containsKey(FILENAME_ENCODING)) {
	        try {
				channelSftp.setFilenameEncoding((String) attributes.get(FILENAME_ENCODING));
			} catch (SftpException e) {
				throw new NoSuccessException("Unable to set filename encoding", e);
			}
		}
	}

	public void disconnect() throws NoSuccessException {
		channelSftp.exit();
		super.disconnect();
	}

	public void getToStream(String absolutePath, String additionalArgs,
			OutputStream stream) throws PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
		try {
			channelSftp.get(absolutePath, stream);
		} catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE)
				throw new DoesNotExistException(e);
			else if (e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED)
				throw new PermissionDeniedException(e);
			else
				throw new NoSuccessException(e);
		}
	}

	public boolean exists(String absolutePath, String additionalArgs)
			throws PermissionDeniedException, TimeoutException, NoSuccessException {
		try {
            return (channelSftp.lstat(absolutePath) != null);
        } catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE)
				return false;
			else
				throw new NoSuccessException(e);
		}
	}

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        try {
            String filename = new EntryPath(absolutePath).getEntryName();
            SftpATTRS attrs = channelSftp.lstat(absolutePath);
            return new SFTPFileAttributes(filename, attrs);
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE)
                throw new DoesNotExistException(e);
            if (e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED)
                throw new PermissionDeniedException(e);
            throw new NoSuccessException(e);
        }
    }

    public FileAttributes[] listAttributes(String absolutePath,
			String additionalArgs) throws PermissionDeniedException, DoesNotExistException,
            TimeoutException, NoSuccessException {
		try {
			Vector<LsEntry> vv = channelSftp.ls(absolutePath);
			if (vv != null && vv.size() > 2) {
				// remove . and .. in the list
				FileAttributes[] list = new SFTPFileAttributes[vv.size() - 2];
				int index=0;
				for (int ii = 0; ii < vv.size(); ii++) {
					Object obj = vv.elementAt(ii);
					if (obj instanceof LsEntry) {
                        LsEntry entry = (LsEntry) obj;
                        if (!".".equals(entry.getFilename()) && !"..".equals(entry.getFilename())) {
                        	list[index++] = new SFTPFileAttributes(entry.getFilename(), entry.getAttrs());
                        }
					}
				}
				return list;
			} else {
				return new SFTPFileAttributes[0];
			}
		} catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE)
				throw new DoesNotExistException(e);
			if (e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED)
				throw new PermissionDeniedException(e);
			throw new NoSuccessException(e);
		}
	}

	public void putFromStream(String absolutePath, boolean append,
			String additionalArgs, InputStream stream) throws PermissionDeniedException,
            BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
		try {
			if (append)
				channelSftp.put(stream, absolutePath, ChannelSftp.APPEND);
			else
				channelSftp.put(stream, absolutePath, ChannelSftp.OVERWRITE);
		} catch (SftpException e) {
			if (!exists(absolutePath, additionalArgs))
				throw new AlreadyExistsException(e);
			if (e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED)
				throw new PermissionDeniedException(e);
			throw new NoSuccessException(e);
		}
	}

	public void makeDir(String parentAbsolutePath, String directoryName,
			String additionalArgs) throws PermissionDeniedException, BadParameterException,
            AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        String absolutePath = parentAbsolutePath+"/"+directoryName;
		try {
			channelSftp.mkdir(absolutePath);
		} catch (SftpException e) {
            switch(e.id) {
                case ChannelSftp.SSH_FX_PERMISSION_DENIED:
                    throw new PermissionDeniedException(e);
                case ChannelSftp.SSH_FX_NO_SUCH_FILE:
                    // check parent entry
                    while(parentAbsolutePath.endsWith("/")) {
                        parentAbsolutePath = parentAbsolutePath.substring(0, parentAbsolutePath.length()-1);
                    }
                    try {
                        switch(this.getAttributes(parentAbsolutePath, additionalArgs).getType()) {
                            case FileAttributes.TYPE_FILE:
                                throw new BadParameterException("Parent entry is a file: "+parentAbsolutePath);
                            case FileAttributes.TYPE_LINK:
                                throw new BadParameterException("Parent entry is a link: "+parentAbsolutePath);
                            case FileAttributes.TYPE_UNKNOWN:
                                throw new NoSuccessException("Parent entry type is unknown: "+parentAbsolutePath, e);
                            default:
                                throw new NoSuccessException("Unexpected error");
                        }
                    } catch (DoesNotExistException e2) {
                        throw new ParentDoesNotExist("Parent entry does not exist: "+parentAbsolutePath, e);
                    }
                case ChannelSftp.SSH_FX_FAILURE:
                    // check entry
                    if (exists(absolutePath, additionalArgs)) {
                        throw new AlreadyExistsException("Entry already exists: "+absolutePath);
                    }
                    throw new NoSuccessException(e);
                default:
                    throw new NoSuccessException(e);
            }
		}
	}

	public void removeDir(String parentAbsolutePath, String directoryName,
			String additionalArgs) throws PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        String absolutePath = parentAbsolutePath+"/"+directoryName;
		try {
			channelSftp.rmdir(absolutePath);
		} catch (SftpException e) {
            switch(e.id) {
                case ChannelSftp.SSH_FX_PERMISSION_DENIED:
                    throw new PermissionDeniedException(e);
                case ChannelSftp.SSH_FX_NO_SUCH_FILE:
                    try {
                        switch(this.getAttributes(absolutePath, additionalArgs).getType()) {
                            case FileAttributes.TYPE_FILE:
                                throw new BadParameterException("Entry is a file: "+absolutePath);
                            case FileAttributes.TYPE_LINK:
                                throw new BadParameterException("Entry is a link: "+absolutePath);
                            case FileAttributes.TYPE_UNKNOWN:
                                throw new NoSuccessException("Entry type is unknown: "+absolutePath, e);
                            default:
                                throw new NoSuccessException("Unexpected error");
                        }
                    } catch(DoesNotExistException e2) {
                        throw new DoesNotExistException("Entry does not exist: "+absolutePath, e);
                    }
                default:
                    throw new NoSuccessException(e);
            }
		}
	}

	public void removeFile(String parentAbsolutePath, String fileName,
			String additionalArgs) throws PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        String absolutePath = parentAbsolutePath+"/"+fileName;
        try {
			channelSftp.rm(absolutePath);
		} catch (SftpException e) {
            switch(e.id) {
                case ChannelSftp.SSH_FX_PERMISSION_DENIED:
                    try {
                        switch(this.getAttributes(absolutePath, additionalArgs).getType()) {
                            case FileAttributes.TYPE_DIRECTORY:
                                throw new BadParameterException("Entry is a directory: "+absolutePath);
                            default:
                                throw new PermissionDeniedException(e);
                        }
                    } catch(DoesNotExistException e2) {
                        throw new DoesNotExistException("Entry does not exist: "+absolutePath, e);
                    }
                case ChannelSftp.SSH_FX_NO_SUCH_FILE:
                    throw new DoesNotExistException("Entry does not exist: "+absolutePath, e);
                default:
                    throw new NoSuccessException(e);
            }
		}
	}

	public void rename(String sourceAbsolutePath, String targetAbsolutePath,
			boolean overwrite, String additionalArgs) throws PermissionDeniedException,
            BadParameterException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException {
		if (overwrite)
			throw new NoSuccessException("Overwrite not implemented");

		try {
			channelSftp.rename(sourceAbsolutePath, targetAbsolutePath);
		} catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE)
				throw new DoesNotExistException(e);
			if (e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED)
				throw new PermissionDeniedException(e);
			throw new NoSuccessException(e);
		}
	}
}
