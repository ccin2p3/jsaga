package fr.in2p3.jsaga.adaptor.data;

import edu.sdsc.grid.io.*;
import edu.sdsc.grid.io.irods.*;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityCredential;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;

import java.io.File;
import java.io.IOException;
import java.util.*;


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
public class  IrodsDataAdaptor extends IrodsDataAdaptorAbstract {
	private static final String IRODSENV ="IrodsEnv";
	private static final String USERID=Context.USERID;

    public String getType() {
        return "irods";
    }

	protected boolean isClassic(){
		return false;
	}		

	public Usage getUsage() {
        return new UAnd(new Usage[]{
				new UFile(IRODSENV),
                new UOptional(USERID)
        });
    }

	public int getDefaultPort() {
        return NO_PORT;
    }
	
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
		EnvironmentVariables env = EnvironmentVariables.getInstance();
        return new Default[]{
			new Default(IRODSENV, new File[]{
			new File(env.getProperty("irodsEnvFile")+""),
			new File(System.getProperty("user.home")+"/.irods/.irodsEnv")}),
		};
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        // URL attributes
        parseValue(attributes, userInfo);
        //if (defaultStorageResource == null) {
        //    throw new BadParameterException("The default storage resource cannot be null");
        //}

		try {
			IRODSAccount account = null;
			
			if (credential instanceof GSSCredentialSecurityCredential) {
				cert = ((GSSCredentialSecurityCredential) credential).getGSSCredential();
				
				if (userName == null) {
					throw new BadParameterException("Missing required attribute: "+USERID);
				}
				account = new IRODSAccount(host, port, userName, passWord, basePath, mcatZone, defaultStorageResource);
				account.setGSSCredential(cert);
			} else {
				if (host == null) {
					account = new IRODSAccount();
				} else {
					account = new IRODSAccount(host, port, userName, passWord, basePath, mcatZone, defaultStorageResource);
				}
			}
			
			fileSystem = FileFactory.newFileSystem(account);
		} catch (IOException ioe) {
			throw new AuthenticationFailedException(ioe);
        }
	}

    public void disconnect() throws NoSuccessException {
        try {
			((IRODSFileSystem)fileSystem).close();
		} catch (IOException e) {
			throw new NoSuccessException(e);
        }
    }

	private FileAttributes[] listAttributesClassic(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		// methode offcielle	
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
					fileAttributes[ind] = new IrodsFileAttributesOptimized(rlDir[i],null);
					ind++;
				}
			}
			
			for (int i = 0; i < file; i++) {
				fileAttributes[ind] = new IrodsFileAttributesOptimized(null,rlFile[i]);
				ind++;
			}
			return fileAttributes;
		} catch (IOException e) {throw new NoSuccessException(e);}
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		if (this.isClassic()) {
			return this.listAttributesClassic(absolutePath, additionalArgs);
		} else {
			return this.listAttributesOptimized(absolutePath, additionalArgs);
		}
    }

	public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
		IRODSFile irodsFile = new IRODSFile((IRODSFileSystem)fileSystem, parentAbsolutePath + directoryName+SEPARATOR);
		boolean  bool= irodsFile.delete(); 
		FileAttributes[] test = listAttributes( parentAbsolutePath + directoryName+SEPARATOR,additionalArgs);
		if (!bool) {throw new NoSuccessException("Directory not empty"+test.length);}
	}

	public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
		IRODSFile irodsFile = new IRODSFile((IRODSFileSystem)fileSystem, parentAbsolutePath +SEPARATOR + fileName);
		irodsFile.delete();
	}
	
	
	void parseValue(Map attributes, String userInfo) throws NoSuccessException {
		
		try {
			if (credential instanceof GSSCredentialSecurityCredential) {
				userName =  (String) attributes.get(USERID);
			} else {
				userName = credential.getUserID();
			}
			
			if (userInfo!=null) {
				int pos =userInfo.indexOf(":");
				if (pos<0) {
					userName = userInfo;
				} else {
					userName = userInfo.substring(0, pos); 
					passWord = userInfo.substring(pos+1, userInfo.length());
				}
			}
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}

		if (credential instanceof UserPassSecurityCredential) {
            passWord = ((UserPassSecurityCredential) credential).getUserPass();
		} else if (credential instanceof GSSCredentialSecurityCredential) {
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
				mcatZone  = value;
			} else if (key.equals(METADATAVALUE)) {
				metadataValue = value;
			}
        }
    }

}