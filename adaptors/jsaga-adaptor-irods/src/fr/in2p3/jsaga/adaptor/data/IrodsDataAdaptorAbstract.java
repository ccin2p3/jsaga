package fr.in2p3.jsaga.adaptor.data;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityCredential;
import org.ietf.jgss.GSSCredential;
import org.irods.jargon.core.connection.GSIIRODSAccount;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   IrodsDataAdaptorAbstract
 * Author: Pascal Calvat (pcalvat@cc.in2p3.fr)
 * Date:   13 may 2008
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class IrodsDataAdaptorAbstract implements DataReaderAdaptor {
	protected final static String SEPARATOR = "/";
	protected final static String DEFAULTRESOURCE	= "defaultresource", DOMAIN="domain", ZONE="zone", METADATAVALUE="metadatavalue";
	protected String srbHost, srbPort, userName, passWord, mdasDomainName, mcatZone, defaultStorageResource, metadataValue;
	protected SecurityCredential credential;
	protected GSSCredential cert;
	protected IRODSAccount m_account;
	protected IRODSFileFactory m_fileFactory;

    public Usage getUsage() {
        return new UOptional(Context.USERID);
    }

    public int getDefaultPort() {
        return 1247;
    }
    
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        EnvironmentVariables env = EnvironmentVariables.getInstance();
        return new Default[]{
            new Default(Context.USERID, System.getProperty("user.name"))
        };
    }

    public Class[] getSupportedSecurityCredentialClasses() {
        return new Class[]{UserPassSecurityCredential.class, GSSCredentialSecurityCredential.class};
    }

    public void setSecurityCredential(SecurityCredential credential) {
        this.credential = credential;
    }
    
    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
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
        try {
            IRODSAccount account = null;
            
            if (credential instanceof GSSCredentialSecurityCredential) {
                cert = ((GSSCredentialSecurityCredential) credential).getGSSCredential();
                /* 3.1.4 */
//                m_account = IRODSAccount.instance(host, port, cert);
                /* 3.2.1.4 and 3.3.1.1 */
                m_account = GSIIRODSAccount.instance(host, port, cert, defaultStorageResource);
                ((GSIIRODSAccount)m_account).setCertificateAuthority(((GSSCredentialSecurityCredential) credential).getCertRepository().getAbsolutePath());
                m_account.setZone(mcatZone);
                
                m_account.setDefaultStorageResource(defaultStorageResource);
                m_account.setHomeDirectory(basePath);
            } else {
                if (userInfo!=null) {
                    int pos =userInfo.indexOf(":");
                    if (pos<0) {
                        userName = userInfo;
                    } else {
                        userName = userInfo.substring(0, pos); 
                        passWord = userInfo.substring(pos+1, userInfo.length());
                    }
                } else {
                    userName = credential.getUserID();
                    passWord = ((UserPassSecurityCredential) credential).getUserPass();
                }
                
                m_account = IRODSAccount.instance(host, port, userName, passWord, basePath, mcatZone, defaultStorageResource);
            }
            
            m_fileFactory = IRODSFileSystem.instance().getIRODSFileFactory(m_account);
        } catch (AuthenticationException je) {
            throw new AuthenticationFailedException(je.getMessage());
        } catch (JargonException je) {
            throw new NoSuccessException(je);
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
    }

    public void disconnect() throws NoSuccessException {
    }

    private FileAttributes[] listAttributesClassic(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        // methode offcielle    
        try {
            File[] files = m_fileFactory.instanceIRODSFile(absolutePath).listFiles();
            FileAttributes[] fileAttributes = new FileAttributes[files.length];
            for (int i=0; i<files.length;i++) {
                fileAttributes[i] = new GeneralFileAttributes(files[i]);
            }
            return fileAttributes;
        } catch (Exception e) {throw new NoSuccessException(e);}
    }

    // TODO
//    private FileAttributes[] listAttributesOptimized(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
//        boolean listDir = true;
//        boolean listFile = true;
//
//        if (additionalArgs != null && additionalArgs.equals(DIR)) { listFile=false;}
//        if (additionalArgs != null && additionalArgs.equals(FILE)) { listDir=false;}
//        
//        if (!absolutePath.equals("/")) {
//            absolutePath = absolutePath.substring(0,absolutePath.length()-1);
//        }
//
//        try {
//            // Select for directories
//            MetaDataRecordList[] rlDir = null;
//            if (listDir) {
//                MetaDataCondition conditionsDir[] = new MetaDataCondition[1];
//                MetaDataSelect selectsDir[] ={    MetaDataSet.newSelection(IRODSMetaDataSet.DIRECTORY_NAME) };
//                conditionsDir[0] = IRODSMetaDataSet.newCondition(IRODSMetaDataSet.PARENT_DIRECTORY_NAME,MetaDataCondition.EQUAL, absolutePath);
//                rlDir = fileSystem.query(conditionsDir, selectsDir);
//            }
//            
//            // Select for files
//            MetaDataRecordList[] rlFile = null;
//            if (listFile) {    
//                MetaDataCondition conditionsFile[] = new MetaDataCondition[1];
//                MetaDataSelect selectsFile[] ={MetaDataSet.newSelection(MetaDataSet.FILE_NAME),
//                MetaDataSet.newSelection(IRODSMetaDataSet.SIZE),
//                MetaDataSet.newSelection(IRODSMetaDataSet.MODIFICATION_DATE)};
//                
//                conditionsFile[0] = IRODSMetaDataSet.newCondition(
//                IRODSMetaDataSet.DIRECTORY_NAME,MetaDataCondition.EQUAL, absolutePath);
//                rlFile = fileSystem.query(conditionsFile, selectsFile);
//            }
//            
//            int file =0;
//            int dir = 0;
//            if (rlDir != null) {dir=rlDir.length;}
//            if (rlFile != null) {file=rlFile.length;}
//            
//            // Supppres "/" when list /
//            int root =0;
//            for (int i = 0; i < dir; i++) {
//                String m_name = (String) rlDir[i].getValue(rlDir[i].getFieldIndex(IRODSMetaDataSet.DIRECTORY_NAME));
//                if (m_name.equals(SEPARATOR)) {root++;}
//            }
//            
//            int ind=0;
//            FileAttributes[] fileAttributes = new FileAttributes[dir+file-root];
//            for (int i = 0; i < dir; i++) {
//                String m_name = (String) rlDir[i].getValue(rlDir[i].getFieldIndex(IRODSMetaDataSet.DIRECTORY_NAME));
//                if (!m_name.equals(SEPARATOR)) {
//                    fileAttributes[ind] = new IrodsFileAttributesOptimized(rlDir[i],null);
//                    ind++;
//                }
//            }
//            
//            for (int i = 0; i < file; i++) {
//                fileAttributes[ind] = new IrodsFileAttributesOptimized(null,rlFile[i]);
//                ind++;
//            }
//            return fileAttributes;
//        } catch (IOException e) {throw new NoSuccessException(e);}
//    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        // TODO
//        if (this.isClassic()) {
            return this.listAttributesClassic(absolutePath, additionalArgs);
//        } else {
//            return this.listAttributesOptimized(absolutePath, additionalArgs);
//        }
    }

    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        try {
            m_fileFactory.instanceIRODSFile(parentAbsolutePath, directoryName).delete();
        } catch (JargonException e) {
            // TODO specialize exception
            throw new NoSuccessException(e);
        }
    }

    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        try {
            m_fileFactory.instanceIRODSFile(parentAbsolutePath, fileName).delete();
        } catch (JargonException e) {
            // TODO specialize exception
            throw new NoSuccessException(e);
        }
    }
    
    
	public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		IRODSFile generalFile;
        try {
            generalFile = m_fileFactory.instanceIRODSFile(absolutePath);
        } catch (JargonException e) {
            throw new NoSuccessException(e);
        }
        return generalFile.exists();
	}

	public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		IRODSFile generalFile;
        try {
            generalFile = m_fileFactory.instanceIRODSFile(absolutePath);
        } catch (JargonException e) {
            throw new NoSuccessException(e);
        }
		return new GeneralFileAttributes(generalFile.getAbsoluteFile());
    }

	public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        try {
            IRODSFile parentFile = m_fileFactory.instanceIRODSFile(parentAbsolutePath);
            if (!parentFile.exists()) {throw new ParentDoesNotExist(parentAbsolutePath);}
            IRODSFile generalFile = m_fileFactory.instanceIRODSFile(parentAbsolutePath, directoryName);
            if (generalFile.exists()) {throw new AlreadyExistsException(parentAbsolutePath+SEPARATOR + directoryName);}
            generalFile.mkdir();
        } catch (JargonException e) {
            throw new NoSuccessException(e);
        }
	
	}
}
