package fr.in2p3.jsaga.adaptor.data;

import edu.sdsc.grid.io.*;
import edu.sdsc.grid.io.irods.*;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.UFile;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
import org.ogf.saga.error.*;

import java.io.*;
import java.util.Map;


/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   IrodsDataAdaptor
 * Author: Pascal Calvat (pcalvat@cc.in2p3.fr)
 * Date:   13 may 2008
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class IrodsDataAdaptor extends IrodsDataAdaptorAbstract {
	private static final String IRODSENV ="IrodsEnv";

    public String getType() {
        return "irods";
    }

    /** TODO: remove this method when GSI will be supported */
    public Class[] getSupportedSecurityAdaptorClasses() {
        return new Class[]{UserPassSecurityAdaptor.class};
    }

	public Usage getUsage() {
        return new UFile(IRODSENV);
    }

	public BaseURL getBaseURL() throws IncorrectURL {
        //todo: parse IRODSENV file
        return null;
    }
	
    public Default[] getDefaults(Map attributes) throws IncorrectState {
		EnvironmentVariables env = EnvironmentVariables.getInstance();
        return new Default[]{
			new Default(IRODSENV, new File[]{
			new File(env.getProperty("irodsEnvFile")+""),
			new File(System.getProperty("user.home")+"/.irods/.irodsEnv")}),
		};
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
        // URL attributes
        parseValue(attributes);
        if (defaultStorageResource == null) {
            throw new BadParameter("The default storage resource cannot be null");
        }

		try {
			IRODSAccount account = null;
			
			if (securityAdaptor instanceof GSSCredentialSecurityAdaptor) { 
				cert = ((GSSCredentialSecurityAdaptor)securityAdaptor).getGSSCredential();
				account = new IRODSAccount(host, port, userName, passWord, basePath, mcatZone, defaultStorageResource);
				account.setAuthenticationScheme("GSI");
			} else {
				if (host == null) {
					account = new IRODSAccount();
				} else {
					account = new IRODSAccount(host, port, userName, passWord, basePath, mcatZone, defaultStorageResource);
				}
			}
			
			fileSystem = FileFactory.newFileSystem(account);
		} catch (IOException ioe) {
			throw new AuthenticationFailed(ioe);
        }
	}

    public void disconnect() throws NoSuccess {
        try {
			((IRODSFileSystem)fileSystem).close();
		} catch (IOException e) {
			throw new NoSuccess(e);
        }
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        throw new NoSuccess("Not implemented yet");
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
		/*
		GeneralFile[] files = FileFactory.newFile(fileSystem, absolutePath).listFiles();
		FileAttributes[] fileAttributes = new FileAttributes[files.length];
		for (int i=0; i<files.length;i++) {
			fileAttributes[i] = new IrodsFileAttributes(files[i]);
		}
		return fileAttributes;
*/

		boolean listDir = true;
		boolean listFile = true;
		
		if (additionalArgs != null && additionalArgs.equals(DIR)) { listFile=false;}
		if (additionalArgs != null && additionalArgs.equals(FILE)) { listDir=false;}
		
		if (!absolutePath.equals("/")) {
			absolutePath = absolutePath.substring(0,absolutePath.length()-1);
		}

		try {
			// Select for directories
			MetaDataRecordList[] rlDir = null;
			if (listDir) {
				MetaDataCondition conditionsDir[] = new MetaDataCondition[1];
				MetaDataSelect selectsDir[] ={	MetaDataSet.newSelection(IRODSMetaDataSet.DIRECTORY_NAME) };
				conditionsDir[0] = IRODSMetaDataSet.newCondition(IRODSMetaDataSet.PARENT_DIRECTORY_NAME,MetaDataCondition.EQUAL, absolutePath);
				rlDir = fileSystem.query(conditionsDir, selectsDir);
			}
			
			// Select for files
			MetaDataRecordList[] rlFile = null;
			if (listFile) {	
				MetaDataCondition conditionsFile[] = new MetaDataCondition[1];
				MetaDataSelect selectsFile[] ={MetaDataSet.newSelection(MetaDataSet.FILE_NAME),
				MetaDataSet.newSelection(IRODSMetaDataSet.SIZE),
				MetaDataSet.newSelection(IRODSMetaDataSet.MODIFICATION_DATE)};
				
				conditionsFile[0] = IRODSMetaDataSet.newCondition(
				IRODSMetaDataSet.DIRECTORY_NAME,MetaDataCondition.EQUAL, absolutePath);
				rlFile = fileSystem.query(conditionsFile, selectsFile);
			}
			
			int file =0;
			int dir = 0;
			if (rlDir != null) {dir=rlDir.length;}
			if (rlFile != null) {file=rlFile.length;}
			
			// Supppres "/" when list /
			int root =0;
			for (int i = 0; i < dir; i++) {
				String m_name = (String) rlDir[i].getValue(rlDir[i].getFieldIndex(IRODSMetaDataSet.DIRECTORY_NAME));
				if (m_name.equals(SEPARATOR)) {root++;}
			}
			
			int ind=0;
			FileAttributes[] fileAttributes = new FileAttributes[dir+file-root];
			for (int i = 0; i < dir; i++) {
				String m_name = (String) rlDir[i].getValue(rlDir[i].getFieldIndex(IRODSMetaDataSet.DIRECTORY_NAME));
				if (!m_name.equals(SEPARATOR)) {
					fileAttributes[ind] = new IrodsFileAttributes(rlDir[i],null);
					ind++;
				}
			}
			
			for (int i = 0; i < file; i++) {
				fileAttributes[ind] = new IrodsFileAttributes(null,rlFile[i]);
				ind++;
			}
			return fileAttributes;
		} catch (IOException e) {throw new NoSuccess(e);}

		}
	
	public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess
	{
		try {
			String[] split = absolutePath.split(SEPARATOR);
			String fileName = split[split.length-1];
			
			String dir = absolutePath.substring(0,absolutePath.length()-fileName.length());
			IRODSFile generalFile =  (IRODSFile)FileFactory.newFile(fileSystem, dir, fileName );
			
			return new BufferedInputStream(new IRODSFileInputStream(generalFile));
			/*
			GeneralRandomAccessFile generalRandomAccessFile = FileFactory.newRandomAccessFile( generalFile, "r" );
			int filesize = (int)generalFile.length();
			byte[] buffer = new byte[filesize];
			generalRandomAccessFile.readFully(buffer);
			ByteArrayInputStream bais = new ByteArrayInputStream(buffer); 
			
			return bais;*/
       	} catch (java.lang.Exception e) {
			throw new NoSuccess(e);
        }    
	}
	
	public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append, String additionalArgs) throws PermissionDenied, BadParameter, AlreadyExists, ParentDoesNotExist, Timeout, NoSuccess {
		try {
			IRODSFile generalFile =  (IRODSFile)FileFactory.newFile((IRODSFileSystem)fileSystem, parentAbsolutePath, fileName );
			return new BufferedOutputStream(new IRODSFileOutputStream(generalFile));
       	} catch (java.lang.Exception e) {
			throw new NoSuccess(e);
        }    
	}
	
	public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDenied, BadParameter, AlreadyExists, ParentDoesNotExist, Timeout, NoSuccess {
		IRODSFile irodsFile = new IRODSFile((IRODSFileSystem)fileSystem, parentAbsolutePath +SEPARATOR + directoryName);
		irodsFile.mkdir();
	}

	public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
		IRODSFile irodsFile = new IRODSFile((IRODSFileSystem)fileSystem, parentAbsolutePath +SEPARATOR + directoryName);
		irodsFile.delete();
	}

	public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
		IRODSFile irodsFile = new IRODSFile((IRODSFileSystem)fileSystem, parentAbsolutePath +SEPARATOR + fileName);
		irodsFile.delete();
	}
	
}