package fr.in2p3.jsaga.adaptor.orionssh.data;

import com.trilead.ssh2.SFTPException;
import com.trilead.ssh2.SFTPv3DirectoryEntry;
import com.trilead.ssh2.SFTPv3FileAttributes;
import com.trilead.ssh2.SFTPv3FileHandle;
import com.trilead.ssh2.sftp.AttribPermissions;
import com.trilead.ssh2.sftp.ErrorCodes;

import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderGetter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterPutter;
import fr.in2p3.jsaga.adaptor.orionssh.SSHAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.orionssh.data.SFTPFileAttributes;
import fr.in2p3.jsaga.helpers.EntryPath;
import org.ogf.saga.error.*;

import java.io.FileNotFoundException;
import java.io.IOException;
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
//	private ChannelSftp channelSftp;
	protected static final String FILENAME_ENCODING = "FilenameEncoding";
	
	public String getType() {
		return "orionsftp";
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
    
//	public void connect(String userInfo, String host, int port,
//			String basePath, Map attributes) throws NotImplementedException,
//            AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException,
//            NoSuccessException {
//		super.connect(userInfo, host, port, basePath, attributes);
		
		// start sftp channel
//		try {
//			Channel channel = session.openChannel("sftp");
//			channel.connect();
//			channelSftp = (ChannelSftp) channel;
//		} catch (JSchException e) {
//			throw new NoSuccessException("Unable to open channel", e);
//		}
//		if (attributes.containsKey(FILENAME_ENCODING)) {
//	        try {
//				channelSftp.setFilenameEncoding((String) attributes.get(FILENAME_ENCODING));
//			} catch (SftpException e) {
//				throw new NoSuccessException("Unable to set filename encoding", e);
//			}
//		}
//	}

//	public void disconnect() throws NoSuccessException {
//		channelSftp.exit();
//		super.disconnect();
//	}

	public void getToStream(String absolutePath, String additionalArgs,
			OutputStream stream) throws PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
		try {
			SFTPv3FileHandle f = m_sftp.openFileRO(absolutePath);
			byte[] buffer = new byte[SSHAdaptorAbstract.READ_BUFFER_LEN];
			int len = 0;
			int offset = 0;
			while ((len=m_sftp.read(f, offset, buffer, 0, buffer.length)) > 0) {
				stream.write(buffer, offset, len);
				stream.flush();
				offset += len;
			}
		} catch (SFTPException sftpe) {
			switch (sftpe.getServerErrorCode()) {
				case ErrorCodes.SSH_FX_NO_SUCH_FILE:
					throw new DoesNotExistException(sftpe);
				case ErrorCodes.SSH_FX_PERMISSION_DENIED:
					throw new PermissionDeniedException(sftpe);
				default:
					throw new NoSuccessException(sftpe);
			}
		} catch (IOException ioe) {
			throw new NoSuccessException(ioe);
		}
	}

	public boolean exists(String absolutePath, String additionalArgs)
			throws PermissionDeniedException, TimeoutException, NoSuccessException {
		try {
			return (m_sftp.stat(absolutePath) != null);
		} catch (SFTPException sftpe) {
			switch (sftpe.getServerErrorCode()) {
				case ErrorCodes.SSH_FX_NO_SUCH_FILE:
					return false;
				case ErrorCodes.SSH_FX_PERMISSION_DENIED:
					throw new PermissionDeniedException(sftpe);
				default:
					throw new NoSuccessException(sftpe);
			}
		} catch (IOException ioe) {
			throw new NoSuccessException(ioe);
		}
	}

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) 
    		throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
            String filename = new EntryPath(absolutePath).getEntryName();
            SFTPv3FileAttributes attrs;
			try {
				attrs = m_sftp.stat(absolutePath);
			} catch (IOException e) {
				// TODO handle exception
				throw new NoSuccessException(e);
			}
            return new SFTPFileAttributes(filename, attrs);
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) 
    		throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
			Vector<SFTPv3DirectoryEntry> vv;
			try {
				vv = m_sftp.ls(absolutePath);
			} catch (FileNotFoundException fnfe) {
				throw new DoesNotExistException(fnfe);
			} catch (IOException e) {
				// TODO handle exception
				throw new NoSuccessException(e);
			}
			if (vv != null && vv.size() > 2) {
				// remove . and .. in the list
				FileAttributes[] list = new SFTPFileAttributes[vv.size() - 2];
				int index=0;
				for (int ii = 0; ii < vv.size(); ii++) {
					Object obj = vv.elementAt(ii);
					if (obj instanceof SFTPv3DirectoryEntry) {
						SFTPv3DirectoryEntry entry = (SFTPv3DirectoryEntry) obj;
                        if (!".".equals(entry.filename) && !"..".equals(entry.filename)) {
                        	list[index++] = new SFTPFileAttributes(entry.filename, entry.attributes);
                        }
					}
				}
				return list;
			} else {
				return new SFTPFileAttributes[0];
			}
            // TODO: handle DoesNotExist
//		} catch (SftpException e) {
//			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE)
//				throw new DoesNotExistException(e);
//			if (e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED)
//				throw new PermissionDeniedException(e);
//			throw new NoSuccessException(e);
//		}
	}

	public void putFromStream(String absolutePath, boolean append, String additionalArgs, InputStream stream) 
			throws PermissionDeniedException, BadParameterException, AlreadyExistsException, 
			ParentDoesNotExist, TimeoutException, NoSuccessException {
		byte[] buffer = new byte[SSHAdaptorAbstract.READ_BUFFER_LEN];
	  // TODO: change offset if append
		int offset = 0;
		SFTPv3FileHandle f;
		try {
			f = m_sftp.openFileRW(absolutePath);
		} catch (SFTPException sftpe) {
			switch (sftpe.getServerErrorCode()) {
			case ErrorCodes.SSH_FX_NO_SUCH_FILE:
				try {
					f = m_sftp.createFile(absolutePath);
				} catch (SFTPException e) {
					throw new NoSuccessException(e);
				} catch (IOException e) {
					throw new NoSuccessException(e);
				}
				break;
			case ErrorCodes.SSH_FX_PERMISSION_DENIED:
				throw new PermissionDeniedException(sftpe);
			default:
				throw new NoSuccessException(sftpe);
			}
		} catch (IOException e) {
			throw new NoSuccessException(e);
		}
		try {
			for (;;) {
				int rsz = stream.read(buffer, 0, buffer.length);
				if (rsz < 0)
					break;
				m_sftp.write(f, offset, buffer, 0, rsz);
				// shift offset
				offset += rsz;
			}
		}
		catch (IOException ex) {
	      /* ... */
		}
//		try {
//			if (append)
//				channelSftp.put(stream, absolutePath, ChannelSftp.APPEND);
//			else
//				channelSftp.put(stream, absolutePath, ChannelSftp.OVERWRITE);
//		} catch (SftpException e) {
//			if (!exists(absolutePath, additionalArgs))
//				throw new AlreadyExistsException(e);
//			if (e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED)
//				throw new PermissionDeniedException(e);
//			throw new NoSuccessException(e);
//		}
	}

	public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) 
			throws PermissionDeniedException, BadParameterException, AlreadyExistsException, 
			ParentDoesNotExist, TimeoutException, NoSuccessException {
        String absolutePath = parentAbsolutePath+"/"+directoryName;
        try {
			m_sftp.mkdir(absolutePath, AttribPermissions.S_IRGRP
					| AttribPermissions.S_IROTH
					| AttribPermissions.S_IRUSR
					| AttribPermissions.S_IWUSR
					| AttribPermissions.S_IXUSR);
		} catch (SFTPException e) {
            switch(e.getServerErrorCode()) {
            	case ErrorCodes.SSH_FX_PERMISSION_DENIED:
            		throw new PermissionDeniedException(e);
            	case ErrorCodes.SSH_FX_NO_SUCH_FILE:
            		// check parent entry
            		while(parentAbsolutePath.endsWith("/")) {
            			parentAbsolutePath = parentAbsolutePath.substring(0, parentAbsolutePath.length()-1);
            		}
            		e.printStackTrace();
            		try {
            			switch(this.getAttributes(parentAbsolutePath, additionalArgs).getType()) {
  	                  		case FileAttributes.TYPE_FILE:
  	                  			throw new BadParameterException("Parent entry is a file: "+parentAbsolutePath);
  	                  		case FileAttributes.TYPE_LINK:
  	                  			throw new BadParameterException("Parent entry is a link: "+parentAbsolutePath);
  	                  		case FileAttributes.TYPE_UNKNOWN:
  	                  			throw new NoSuccessException("Parent entry type is unknown: "+parentAbsolutePath, e);
  	                  		default:
  	                  			throw new NoSuccessException("Unexpected error", e);
            			}
            		} catch (DoesNotExistException e2) {
            			throw new ParentDoesNotExist("Parent entry does not exist: "+parentAbsolutePath, e);
            		}
            	case ErrorCodes.SSH_FX_FAILURE:
	                // check entry
	                if (exists(absolutePath, additionalArgs)) {
	                    throw new AlreadyExistsException("Entry already exists: "+absolutePath);
	                }
	                throw new NoSuccessException(e);
            	default:
            		throw new NoSuccessException(e);
            }
		}
        catch (IOException e) {
            throw new NoSuccessException(e);
		}
//		try {
//			channelSftp.mkdir(absolutePath);
//		} catch (SftpException e) {
//		}
 	}

	public void removeDir(String parentAbsolutePath, String directoryName,	String additionalArgs) 
			throws PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        String absolutePath = parentAbsolutePath+"/"+directoryName;
        try {
			m_sftp.rmdir(absolutePath);
		} catch (IOException e) {
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
		}
//		try {
//			channelSftp.rmdir(absolutePath);
//		} catch (SftpException e) {
//            switch(e.id) {
//                case ChannelSftp.SSH_FX_PERMISSION_DENIED:
//                    throw new PermissionDeniedException(e);
//                case ChannelSftp.SSH_FX_NO_SUCH_FILE:
//                default:
//                    throw new NoSuccessException(e);
//            }
//		}
	}

	public void removeFile(String parentAbsolutePath, String fileName,	String additionalArgs) 
			throws PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        String absolutePath = parentAbsolutePath+"/"+fileName;
        try {
			m_sftp.rm(absolutePath);
		} catch (FileNotFoundException fnfe) {
			throw new DoesNotExistException(fnfe);
		} catch (IOException e) {
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
		}
//        try {
//			channelSftp.rm(absolutePath);
//		} catch (SftpException e) {
//            switch(e.id) {
//                case ChannelSftp.SSH_FX_PERMISSION_DENIED:
//                case ChannelSftp.SSH_FX_NO_SUCH_FILE:
//                    throw new DoesNotExistException("Entry does not exist: "+absolutePath, e);
//                default:
//                    throw new NoSuccessException(e);
//            }
//		}
	}

	public void rename(String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite, String additionalArgs) 
			throws PermissionDeniedException, BadParameterException, DoesNotExistException, 
			AlreadyExistsException, TimeoutException, NoSuccessException {
//		if (overwrite)
//			throw new NoSuccessException("Overwrite not implemented");

		try {
			m_sftp.mv(sourceAbsolutePath, targetAbsolutePath);
		} catch (FileNotFoundException fnfe) {
			throw new DoesNotExistException(fnfe);
		} catch (IOException e) {
			// TODO handle exception
			throw new NoSuccessException(e);
		}
//		try {
//			channelSftp.rename(sourceAbsolutePath, targetAbsolutePath);
//		} catch (SftpException e) {
//			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE)
//				throw new DoesNotExistException(e);
//			if (e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED)
//				throw new PermissionDeniedException(e);
//			throw new NoSuccessException(e);
//		}
	}
}
