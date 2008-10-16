package fr.in2p3.jsaga.adaptor.data;

import edu.sdsc.grid.io.*;
import edu.sdsc.grid.io.srb.*;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
import org.ogf.saga.error.*;

import java.io.*;
import java.util.Map;


/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   SrbDataAdaptor
 * Author: Pascal Calvat (pcalvat@cc.in2p3.fr)
 * Date:   13 may 2008
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class SrbDataAdaptor extends IrodsDataAdaptorAbstract {
    private static final String MDASENV ="MdasEnv";
    private static final String USE_TRASH = "UseTrash";

    private boolean m_useTrash;

    public String getType() {
        return "srb";
    }

    public Usage getUsage() {
        return new UAnd(new Usage[]{
                new UFile(MDASENV),
                new UOptional(USE_TRASH)
        });
    }

	public BaseURL getBaseURL() throws IncorrectURL {
        //todo: parse MDASENV file
		//return new BaseURL(String userInfo, String host, int port, "/", "domain=mydomain&zone=myzone&resource=myresource");
		return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        EnvironmentVariables env = EnvironmentVariables.getInstance();
		return new Default[]{
                new Default(MDASENV, new File[]{
                        new File(env.getProperty("srbEnvFile")+""),
                        new File(System.getProperty("user.home")+"/.srb/.MdasEnv")}),
                new Default(USE_TRASH, "false")
        };
    }

	// syntax: srb:// [username.mdasdomain [.zone] [:password] @] [host] [:port] /path
    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
        // configuration attributes
        m_useTrash = "true".equalsIgnoreCase((String) attributes.get(USE_TRASH));

        // URL attributes
        parseValue(attributes);
        if (defaultStorageResource == null) {
            throw new BadParameter("The default storage resource cannot be null");
        }

        try {
			// connect to server
			SRBAccount account = null;
			
			if (securityAdaptor instanceof GSSCredentialSecurityAdaptor) { 
				cert = ((GSSCredentialSecurityAdaptor)securityAdaptor).getGSSCredential();
				account = new SRBAccount(host, port, cert, basePath, defaultStorageResource, SRBAccount.GSI_AUTH);
            } else {
				if (host == null) {
					account = new SRBAccount();
				} else {
					account = new SRBAccount(host, port, userName, passWord, basePath, mdasDomainName, defaultStorageResource, mcatZone);
				}
			}

			fileSystem = FileFactory.newFileSystem(account);
		} catch (IOException ioe) {
			throw new AuthenticationFailed(ioe);
        }
    }

    public void disconnect() throws NoSuccess {
        try {
			((SRBFileSystem)fileSystem).close();
		} catch (IOException e) {
			throw new NoSuccess(e);
        }
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        throw new NoSuccess("Not implemented yet");
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
		/* methode offcielle
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
		
		absolutePath = absolutePath.substring(0,absolutePath.length()-1);
		
		try {
			// Select for directories
			MetaDataRecordList[] rlDir = null;
			 
			if (listDir) {
				MetaDataCondition conditionsDir[] = new MetaDataCondition[1];
				MetaDataSelect selectsDir[] ={	MetaDataSet.newSelection(SRBMetaDataSet.DIRECTORY_NAME) };
				conditionsDir[0] = SRBMetaDataSet.newCondition(SRBMetaDataSet.PARENT_DIRECTORY_NAME,MetaDataCondition.EQUAL, absolutePath);
				rlDir = fileSystem.query(conditionsDir, selectsDir);
			}
			
			// Select for files
			MetaDataRecordList[] rlFile = null;
			
			if (listFile) {
				MetaDataCondition conditionsFile[] = new MetaDataCondition[1];
				MetaDataSelect selectsFile[] ={MetaDataSet.newSelection(MetaDataSet.FILE_NAME),
				MetaDataSet.newSelection(SRBMetaDataSet.SIZE),
				MetaDataSet.newSelection(SRBMetaDataSet.FILE_LAST_ACCESS_TIMESTAMP)	};
				
				// MetaDataSet.newSelection(SRBMetaDataSet.ACCESS_CONSTRAINT),
				// MetaDataSet.newSelection(SRBMetaDataSet.USER_TYPE_NAME),
				//MetaDataSet.newSelection(SRBMetaDataSet.USER_NAME)
				conditionsFile[0] = SRBMetaDataSet.newCondition(
				SRBMetaDataSet.DIRECTORY_NAME,MetaDataCondition.EQUAL, absolutePath);

				rlFile = fileSystem.query(conditionsFile, selectsFile);
			}
			
			int file =0;
			int dir = 0;
			if (rlDir != null) {dir=rlDir.length;}
			if (rlFile != null) {file=rlFile.length;}
			
			// Supppres "/" when list /
			int root =0;
			for (int i = 0; i < dir; i++) {
				String m_name = (String) rlDir[i].getValue(rlDir[i].getFieldIndex(SRBMetaDataSet.DIRECTORY_NAME));
				if (m_name.equals(SEPARATOR)) {root++;}
			}

			int ind=0;
			FileAttributes[] fileAttributes = new FileAttributes[dir+file-root];
			for (int i = 0; i < dir; i++) {
				String m_name = (String) rlDir[i].getValue(rlDir[i].getFieldIndex(SRBMetaDataSet.DIRECTORY_NAME));
				if (!m_name.equals(SEPARATOR)) {
					fileAttributes[ind] = new SrbFileAttributes(rlDir[i],null);
					ind++;
				}
			}
			for (int i = 0; i < file; i++) {
				fileAttributes[ind] = new SrbFileAttributes(null,rlFile[i]);
				ind++;
			}
			return fileAttributes;
		} catch (IOException e) {throw new NoSuccess(e);}
	}

	public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
		try {
			String[] split = absolutePath.split(SEPARATOR);
			String fileName = split[split.length-1];
			
			String dir = absolutePath.substring(0,absolutePath.length()-fileName.length());
			SRBFile generalFile =  (SRBFile)FileFactory.newFile(fileSystem, dir, fileName );
			
			return new BufferedInputStream(new SRBFileInputStream(generalFile));
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
			//String[] split = parentAbsolutePath.split(separator);
			//String dir = parentAbsolutePath.substring(0,parentAbsolutePath.length()-fileName.length());
			SRBFile generalFile =  (SRBFile)FileFactory.newFile((SRBFileSystem)fileSystem, parentAbsolutePath, fileName );
			
			return new BufferedOutputStream(new SRBFileOutputStream(generalFile));
       	} catch (java.lang.Exception e) {
			throw new NoSuccess(e);
        }    
	}
	
	public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDenied, BadParameter, AlreadyExists, ParentDoesNotExist, Timeout, NoSuccess {
		SRBFile srbFile = new SRBFile((SRBFileSystem)fileSystem, parentAbsolutePath +SEPARATOR + directoryName);
		srbFile.mkdir();
	}

	public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
		SRBFile srbFile = new SRBFile((SRBFileSystem)fileSystem, parentAbsolutePath +SEPARATOR + directoryName);
		srbFile.delete(! m_useTrash);
	}

	public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
		SRBFile srbFile = new SRBFile((SRBFileSystem)fileSystem, parentAbsolutePath +SEPARATOR + fileName);
		srbFile.delete(! m_useTrash);
	}
}
