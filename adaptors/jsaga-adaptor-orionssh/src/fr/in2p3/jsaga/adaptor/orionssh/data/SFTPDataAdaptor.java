package fr.in2p3.jsaga.adaptor.orionssh.data;

import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.SFTPException;
import com.trilead.ssh2.SFTPv3Client;
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
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   16 juillet 2013
 * ***************************************************/

public class SFTPDataAdaptor extends SSHAdaptorAbstract implements
		FileReaderGetter, FileWriterPutter, DataRename {
//	private ChannelSftp channelSftp;
	protected static final String FILENAME_ENCODING = "FilenameEncoding";
	public final static String TYPE = "sftp";
	
	public String getType() {
		return TYPE;
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
    
	public void getToStream(String absolutePath, String additionalArgs, OutputStream stream) 
			throws PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
		// SCPClient is fast but requires the command 'scp' to be on the PATH on server side!!!
//		try {
//			SCPClient scp = m_conn.createSCPClient();
//			scp.get(absolutePath, stream);
//			stream.flush();
//		} catch (IOException ioe) {
//			if (ioe.getMessage().contains("No such file")) {
//				throw new DoesNotExistException(ioe);
//			}
//			throw new NoSuccessException(ioe);
//		}
		try {
			SFTPv3Client cl = new SFTPv3Client(m_conn);
			SFTPv3FileHandle f = cl.openFileRO(absolutePath);
			byte[] buffer = new byte[SSHAdaptorAbstract.READ_BUFFER_LEN];
			int len = 0;
			int offset = 0;
			while ((len=cl.read(f, offset, buffer, 0, buffer.length)) > 0) {
				stream.write(buffer, 0, len);
				offset += len;
			}
			stream.flush();
			cl.closeFile(f);
			cl.close();
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
		} finally {
			
		}
	}

	public boolean exists(String absolutePath, String additionalArgs)
			throws PermissionDeniedException, TimeoutException, NoSuccessException {
		try {
			// if m_sftp.stat(...) is used, test_list_and_getAttributes fails with "the server sent an invalid id field" ...
			return (new SFTPv3Client(m_conn).stat(absolutePath) != null);
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
			// if m_sftp.stat(...) is used, test_list_and_getAttributes fails with "the server sent an invalid id field" ...
			attrs = new SFTPv3Client(m_conn).stat(absolutePath);
		} catch (SFTPException sftpe) {
			switch (sftpe.getServerErrorCode()) {
				case ErrorCodes.SSH_FX_NO_SUCH_FILE:
					throw new DoesNotExistException(sftpe);
				case ErrorCodes.SSH_FX_PERMISSION_DENIED:
					throw new PermissionDeniedException(sftpe);
				default:
					throw new NoSuccessException(sftpe);
			}
		} catch (IOException e) {
			throw new NoSuccessException(e);
		}
        return new SFTPFileAttributes(filename, attrs);
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) 
    		throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		Vector<SFTPv3DirectoryEntry> vv;
		SFTPv3Client sftp = null;
		try {
			sftp = new SFTPv3Client(m_conn);
			vv = sftp.ls(absolutePath);
		} catch (SFTPException e) {
			if (e.getServerErrorCode() == ErrorCodes.SSH_FX_NO_SUCH_FILE)
				throw new DoesNotExistException(e);
			if (e.getServerErrorCode() == ErrorCodes.SSH_FX_PERMISSION_DENIED)
				throw new PermissionDeniedException(e);
			throw new NoSuccessException(e);
		} catch (IOException e) {
			throw new NoSuccessException(e);
		} finally {
			if (sftp!=null) sftp.close();
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
	}

	public void putFromStream(String absolutePath, boolean append, String additionalArgs, InputStream stream) 
			throws PermissionDeniedException, BadParameterException, AlreadyExistsException, 
			ParentDoesNotExist, TimeoutException, NoSuccessException {
		byte[] buffer = new byte[SSHAdaptorAbstract.READ_BUFFER_LEN];
		long offset = 0;
		SFTPv3Client sftp = null;
		SFTPv3FileHandle f;
		try {
			sftp = new SFTPv3Client(m_conn);
			f = sftp.openFileRW(absolutePath);
			// The file already exists
			if (append) {
				SFTPv3FileAttributes attrs = sftp.fstat(f);
				offset = attrs.size;
			} else {
				sftp.closeFile(f);
				f = sftp.createFileTruncate(absolutePath);
			}
		} catch (SFTPException sftpe) {
			switch (sftpe.getServerErrorCode()) {
			case ErrorCodes.SSH_FX_NO_SUCH_FILE:
				try {
					f = sftp.createFile(absolutePath);
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
				sftp.write(f, offset, buffer, 0, rsz);
				// shift offset
				offset += rsz;
			}
		} catch (IOException ex) {
			throw new NoSuccessException(ex);
		}
		if (sftp != null) sftp.close();
	}

	public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) 
			throws PermissionDeniedException, BadParameterException, AlreadyExistsException, 
			ParentDoesNotExist, TimeoutException, NoSuccessException {
		SFTPv3Client sftp = null;
        String absolutePath = parentAbsolutePath+"/"+directoryName;
        try {
			sftp = new SFTPv3Client(m_conn);
			sftp.mkdir(absolutePath, AttribPermissions.S_IRGRP
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
		} catch (IOException e) {
            throw new NoSuccessException(e);
		} finally {
			if (sftp != null) sftp.close();
		}
 	}

	public void removeDir(String parentAbsolutePath, String directoryName,	String additionalArgs) 
			throws PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
		SFTPv3Client sftp = null;
        String absolutePath = parentAbsolutePath+"/"+directoryName;
        try {
			sftp = new SFTPv3Client(m_conn);
			sftp.rmdir(absolutePath);
		} catch (SFTPException e) {
			switch (e.getServerErrorCode()) {
			case ErrorCodes.SSH_FX_PERMISSION_DENIED:
				throw new PermissionDeniedException(e);
			case ErrorCodes.SSH_FX_NO_SUCH_FILE:
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
			}
		} catch (IOException e) {
            throw new NoSuccessException(e);
		} finally {
			if (sftp != null) sftp.close();
		}
	}

	public void removeFile(String parentAbsolutePath, String fileName,	String additionalArgs) 
			throws PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
		SFTPv3Client sftp = null;
        String absolutePath = parentAbsolutePath+"/"+fileName;
        try {
			sftp = new SFTPv3Client(m_conn);
			sftp.rm(absolutePath);
		} catch (SFTPException e) {
			switch (e.getServerErrorCode()) {
			case ErrorCodes.SSH_FX_PERMISSION_DENIED:
				throw new PermissionDeniedException(e);
			case ErrorCodes.SSH_FX_NO_SUCH_FILE:
                throw new DoesNotExistException(e);
            default:
            	throw new NoSuccessException(e);
			}
		} catch (IOException e) {
        	throw new NoSuccessException(e);
		} finally {
			if (sftp != null) sftp.close();
		}
	}

	public void rename(String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite, String additionalArgs) 
			throws PermissionDeniedException, BadParameterException, DoesNotExistException, 
			AlreadyExistsException, TimeoutException, NoSuccessException {
		SFTPv3Client sftp = null;

		try {
			sftp = new SFTPv3Client(m_conn);
			sftp.mv(sourceAbsolutePath, targetAbsolutePath);
		} catch (SFTPException e) {
			switch (e.getServerErrorCode()) {
			case ErrorCodes.SSH_FX_PERMISSION_DENIED:
				throw new PermissionDeniedException(e);
			case ErrorCodes.SSH_FX_NO_SUCH_FILE:
                throw new DoesNotExistException(e);
            default:
            	throw new NoSuccessException(e);
			}
		} catch (IOException e) {
        	throw new NoSuccessException(e);
		} finally {
			if (sftp != null) sftp.close();
		}
	}
}
