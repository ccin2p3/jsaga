package fr.in2p3.jsaga.adaptor.data;

import edu.sdsc.grid.io.*;
import edu.sdsc.grid.io.srb.*;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.UFile;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityAdaptor;
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

    public String getType() {
        return "srb";
    }

    public Usage getUsage() {
         return new UFile(MDASENV);
    }

	public BaseURL getBaseURL() throws IncorrectURL {
        //todo: parse MDASENV file
        return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        EnvironmentVariables env = EnvironmentVariables.getInstance();
		return new Default[]{
			new Default(MDASENV, new File[]{
			new File(env.getProperty("srbEnvFile")+""),
			new File(System.getProperty("user.home")+"/.srb/.MdasEnv")}),
		};
    }

    public Class[] getSupportedSecurityAdaptorClasses() {
        return new Class[]{UserPassSecurityAdaptor.class, GSSCredentialSecurityAdaptor.class};
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        //todo: save and use provided security adaptor
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
        // connect to server
		try {
			SRBAccount account = new SRBAccount();
			fileSystem = FileFactory.newFileSystem(account);
		} catch (IOException ioe) {
			throw new AuthenticationFailed(ioe);
		} catch (java.lang.Exception e) {
			throw new NoSuccess(e);
        }
    }

    public void disconnect() throws NoSuccess {
        try {
			((SRBFileSystem)fileSystem).close();
		} catch (IOException e) {
			throw new NoSuccess(e);
        }
    }

	public long getSize(String absolute, String absolutePath) {
		return 476160;
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
		if (additionalArgs != null)
		{
			System.out.println("additionalArgs:"+additionalArgs);
		}
		
		absolutePath = absolutePath.substring(0,absolutePath.length()-1);
		try {
			// Select for directories
			MetaDataCondition conditionsDir[] = new MetaDataCondition[1];
			MetaDataSelect selectsDir[] ={	MetaDataSet.newSelection(SRBMetaDataSet.DIRECTORY_NAME) };
			conditionsDir[0] = SRBMetaDataSet.newCondition(SRBMetaDataSet.PARENT_DIRECTORY_NAME,MetaDataCondition.EQUAL, absolutePath);
			MetaDataRecordList[] rlDir = fileSystem.query(conditionsDir, selectsDir);
			
			// Select for files
			MetaDataCondition conditionsFile[] = new MetaDataCondition[1];
			MetaDataSelect selectsFile[] ={MetaDataSet.newSelection(MetaDataSet.FILE_NAME),
			MetaDataSet.newSelection(SRBMetaDataSet.SIZE),
			MetaDataSet.newSelection(SRBMetaDataSet.FILE_LAST_ACCESS_TIMESTAMP)	};
			
			System.out.println("absolutePath:"+absolutePath);
			// MetaDataSet.newSelection(SRBMetaDataSet.ACCESS_CONSTRAINT),
			// MetaDataSet.newSelection(SRBMetaDataSet.USER_TYPE_NAME),
			//MetaDataSet.newSelection(SRBMetaDataSet.USER_NAME)
			conditionsFile[0] = SRBMetaDataSet.newCondition(
			SRBMetaDataSet.DIRECTORY_NAME,MetaDataCondition.EQUAL, absolutePath);

			MetaDataRecordList[] rlFile = fileSystem.query(conditionsFile, selectsFile);
			
			int file =0;
			int dir = 0;
			if (rlDir != null) {dir=rlDir.length;}
			if (rlFile != null) {file=rlFile.length;}

			int ind=0;
			FileAttributes[] fileAttributes = new FileAttributes[dir+file];
			for (int i = 0; i < dir; i++) {
				fileAttributes[ind] = new SrbFileAttributes(rlDir[i],null);
				ind++;
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
			String[] split = absolutePath.split(separator);
			String fileName = split[split.length-1];
			
			String dir = absolutePath.substring(0,absolutePath.length()-fileName.length());
			SRBFile generalFile =  (SRBFile)FileFactory.newFile(fileSystem, dir, fileName );
			
			return new BufferedInputStream(new SRBFileInputStream(generalFile));
			/*
			System.out.println("generalFile:"+generalFile);
			GeneralRandomAccessFile generalRandomAccessFile = FileFactory.newRandomAccessFile( generalFile, "r" );
			int filesize = (int)generalFile.length();
		
			System.out.println("filesize:"+generalFile.length());
			
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
			System.out.println("parentAbsolutePath:"+parentAbsolutePath);
			System.out.println("fileName:"+fileName);
			SRBFile generalFile =  (SRBFile)FileFactory.newFile((SRBFileSystem)fileSystem, parentAbsolutePath, fileName );
			
			return new BufferedOutputStream(new SRBFileOutputStream(generalFile));
       	} catch (java.lang.Exception e) {
			throw new NoSuccess(e);
        }    
	}
	
	public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDenied, BadParameter, AlreadyExists, ParentDoesNotExist, Timeout, NoSuccess {
		SRBFile srbFile = new SRBFile((SRBFileSystem)fileSystem, parentAbsolutePath +separator + directoryName);
		srbFile.mkdir();
	}

	public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
		SRBFile srbFile = new SRBFile((SRBFileSystem)fileSystem, parentAbsolutePath +separator + directoryName);
		srbFile.delete();
	}

	public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
		SRBFile srbFile = new SRBFile((SRBFileSystem)fileSystem, parentAbsolutePath +separator + fileName);
		srbFile.delete();
	}
}
