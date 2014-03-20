package fr.in2p3.jsaga.adaptor.data;


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

    public String getType() {
        return "irods";
    }

    protected boolean isClassic(){
        return false;
    }        

//    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
//        // URL attributes
//        parseValue(attributes, userInfo);
//        //if (defaultStorageResource == null) {
//        //    throw new BadParameterException("The default storage resource cannot be null");
//        //}
//
//        try {
//            IRODSAccount account = null;
//            
//            if (credential instanceof GSSCredentialSecurityCredential) {
//                cert = ((GSSCredentialSecurityCredential) credential).getGSSCredential();
//                account = GSIIRODSAccount.instance(host, port, cert, defaultStorageResource);
////                account.setGSSCredential(cert);
//            } else {
////                if (host == null) {
////                    account = new IRODSAccount();
////                } else {
//                    account = IRODSAccount.instance(host, port, userName, passWord, basePath, mcatZone, defaultStorageResource);
////                }
//            }
//            
//            m_fileFactory = IRODSFileSystem.instance().getIRODSFileFactory(account);
//        } catch (JargonException je) {
//            throw new AuthenticationFailedException(je);
//        }
//    }

//    public void disconnect() throws NoSuccessException {
//        try {
//            ((IRODSFileSystem)fileSystem).close();
//        } catch (IOException e) {
//            throw new NoSuccessException(e);
//        }
//    }

//    private FileAttributes[] listAttributesClassic(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
//        // methode offcielle    
//        try {
//            File[] files = m_fileFactory.instanceIRODSFile(absolutePath).listFiles();
//            FileAttributes[] fileAttributes = new FileAttributes[files.length];
//            for (int i=0; i<files.length;i++) {
//                fileAttributes[i] = new GeneralFileAttributes(files[i]);
//            }
//            return fileAttributes;
//        } catch (Exception e) {throw new NoSuccessException(e);}
//    }

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

//    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
//        if (this.isClassic()) {
//            return this.listAttributesClassic(absolutePath, additionalArgs);
//        } else {
//            return this.listAttributesOptimized(absolutePath, additionalArgs);
//        }
//    }

    

}
