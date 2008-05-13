package fr.in2p3.jsaga.adaptor.data;

import edu.sdsc.grid.io.FileFactory;
import edu.sdsc.grid.io.GeneralFile;
import edu.sdsc.grid.io.irods.*;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.UFile;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityAdaptor;
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

	public Usage getUsage() {
        return new UFile(IRODSENV);
    }

	public BaseURL getBaseURL() throws IncorrectURL {
        //todo: parse IRODSENV file
        return null;
    }
	
	public long getSize(String absolute, String absolutePath) {
		return 476160;
	}
	
    public Default[] getDefaults(Map attributes) throws IncorrectState {
		EnvironmentVariables env = EnvironmentVariables.getInstance();
        return new Default[]{
			new Default(IRODSENV, new File[]{
			new File(env.getProperty("irodsEnvFile")+""),
			new File(System.getProperty("user.home")+"/.irods/.irodsEnv")}),
		};
    }

    public Class[] getSupportedSecurityAdaptorClasses() {
        return new Class[]{UserPassSecurityAdaptor.class};
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        //todo: save and use provided security adaptor        
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
		try {
			IRODSAccount account = new IRODSAccount();
			fileSystem = FileFactory.newFileSystem(account);
		} catch (IOException ioe) {
			throw new AuthenticationFailed(ioe);
		} catch (java.lang.Exception e) {
			throw new NoSuccess(e);
        }    
	}

    public void disconnect() throws NoSuccess {
        try {
			((IRODSFileSystem)fileSystem).close();
		} catch (IOException e) {
			throw new NoSuccess(e);
        }
    }
	
	public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
		GeneralFile[] files = FileFactory.newFile(fileSystem, absolutePath).listFiles();
		FileAttributes[] fileAttributes = new FileAttributes[files.length];
		for (int i=0; i<files.length;i++) {
			fileAttributes[i] = new IrodsFileAttributes(files[i]);
		}
		return fileAttributes;
	}
	
	public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
		try {
			String[] split = absolutePath.split(separator);
			String fileName = split[split.length-1];
			
			String dir = absolutePath.substring(0,absolutePath.length()-fileName.length());
			IRODSFile generalFile =  (IRODSFile)FileFactory.newFile(fileSystem, dir, fileName );
			
			return new BufferedInputStream(new IRODSFileInputStream(generalFile));
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
			String[] split = parentAbsolutePath.split(separator);
			String dir = parentAbsolutePath.substring(0,parentAbsolutePath.length()-fileName.length());
			IRODSFile generalFile =  (IRODSFile)FileFactory.newFile((IRODSFileSystem)fileSystem, dir, fileName );
			
			return new BufferedOutputStream(new IRODSFileOutputStream(generalFile));
       	} catch (java.lang.Exception e) {
			throw new NoSuccess(e);
        }    
	}
	
	public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDenied, BadParameter, AlreadyExists, ParentDoesNotExist, Timeout, NoSuccess {
		IRODSFile irodsFile = new IRODSFile((IRODSFileSystem)fileSystem, parentAbsolutePath +separator + directoryName);
		irodsFile.mkdir();
	}

	public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
		IRODSFile irodsFile = new IRODSFile((IRODSFileSystem)fileSystem, parentAbsolutePath +separator + directoryName);
		irodsFile.delete();
	}

	public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
		IRODSFile irodsFile = new IRODSFile((IRODSFileSystem)fileSystem, parentAbsolutePath +separator + fileName);
		irodsFile.delete();
	}
	
}