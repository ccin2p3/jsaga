package fr.in2p3.jsaga.adaptor.data;

import edu.sdsc.grid.io.*;
import edu.sdsc.grid.io.srb.*;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityAdaptor;
import org.ogf.saga.error.*;

import java.io.File;
import java.io.IOException;
import java.util.*;


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

	protected boolean isClassic(){
		return false;
	}		

    public Usage getUsage() {
        return new UAnd(new Usage[]{
                new UFile(MDASENV),
                new UOptional(USE_TRASH)
        });
    }

	public BaseURL getBaseURL() throws IncorrectURLException {
        //todo: parse MDASENV file
		//return new BaseURL(String userInfo, String host, int port, "/", "domain=mydomain&zone=myzone&resource=myresource");
		return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        EnvironmentVariables env = EnvironmentVariables.getInstance();
		return new Default[]{
                new Default(MDASENV, new File[]{
                        new File(env.getProperty("srbEnvFile")+""),
                        new File(System.getProperty("user.home")+"/.srb/.MdasEnv")}),
                new Default(USE_TRASH, "false")
        };
    }

	// syntax: srb:// [username.mdasdomain [.zone] [:password] @] [host] [:port] /path
    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        // configuration attributes
        m_useTrash = "true".equalsIgnoreCase((String) attributes.get(USE_TRASH));

        // URL attributes
        parseValue(attributes);
        if (defaultStorageResource == null) {
            throw new BadParameterException("The default storage resource cannot be null");
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
			throw new AuthenticationFailedException(ioe);
        }
    }

    public void disconnect() throws NoSuccessException {
        try {
			((SRBFileSystem)fileSystem).close();
		} catch (IOException e) {
			throw new NoSuccessException(e);
        }
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		if (this.isClassic()) {
			return this.listAttributesClassic(absolutePath, additionalArgs);
		} else {
			return this.listAttributesOptimized(absolutePath, additionalArgs);
		}
    }

	private FileAttributes[] listAttributesClassic(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		//methode offcielle	
		try {
			GeneralFile[] files = FileFactory.newFile(fileSystem, absolutePath).listFiles();
			FileAttributes[] fileAttributes = new FileAttributes[files.length];
			for (int i=0; i<files.length;i++) {
				fileAttributes[i] = new GeneralFileAttributes(files[i]);
			}
			return fileAttributes;
		} catch (Exception e) {throw new NoSuccessException(e);}
	}	

 	private FileAttributes[] listAttributesOptimized(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		// URL attributes for optimized request to SRB
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
				MetaDataSelect selectsDir[] ={MetaDataSet.newSelection(SRBMetaDataSet.DIRECTORY_NAME)};
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
			boolean findAtttributes = false;
			FileAttributes[] fileAttributes = new FileAttributes[dir+file-root];
			for (int i = 0; i < dir; i++) {
				String m_name = (String) rlDir[i].getValue(rlDir[i].getFieldIndex(SRBMetaDataSet.DIRECTORY_NAME));
				if (!m_name.equals(SEPARATOR)) {
					fileAttributes[ind] = new SrbFileAttributesOptimized(absolutePath, rlDir[i],null,findAtttributes);
					ind++;
				}
			}
			for (int i = 0; i < file; i++) {
				fileAttributes[ind] = new SrbFileAttributesOptimized(absolutePath, null,rlFile[i],findAtttributes);
				ind++;
			}
			return fileAttributes;
		} catch (IOException e) {throw new NoSuccessException(e);}
	}

	
	public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
		SRBFile srbFile = new SRBFile((SRBFileSystem)fileSystem, parentAbsolutePath +SEPARATOR + directoryName);
		boolean  bool= srbFile.delete(true); 
		if (!bool) {throw new NoSuccessException("Directory not empty");}
	}

	public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
		SRBFile srbFile = new SRBFile((SRBFileSystem)fileSystem, parentAbsolutePath +SEPARATOR + fileName);
		srbFile.delete(! m_useTrash);
	}
	
	
	void parseValue(Map attributes) throws NoSuccessException {
	
		if (securityAdaptor instanceof UserPassSecurityAdaptor) {
            try {
                userName = securityAdaptor.getUserID();
            } catch (Exception e) {
                throw new NoSuccessException(e);
            }
            passWord = ((UserPassSecurityAdaptor)securityAdaptor).getUserPass();
		} else if (securityAdaptor instanceof GSSCredentialSecurityAdaptor) {
			userName = null;
            passWord = null;
        }
		
		// Parsing for defaultResource
		Set set = attributes.entrySet();
		Iterator iterator = set.iterator();

        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();
            String key = ((String)me.getKey()).toLowerCase();
            String value =(String)me.getValue();
			
            if (key.equals(DEFAULTRESOURCE)) {
                defaultStorageResource = value;
            } else if (key.equals(DOMAIN)) {
				mdasDomainName  = value;
			} else if (key.equals(ZONE)) {
				mcatZone = value;
			} else if (key.equals(METADATAVALUE)) {
				metadataValue = value;
			}
        }
    }
}
