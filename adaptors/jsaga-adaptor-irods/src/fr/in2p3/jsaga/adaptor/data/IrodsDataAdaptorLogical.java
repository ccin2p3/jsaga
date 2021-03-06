package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.optimise.LogicalReaderMetaDataExtended;
import fr.in2p3.jsaga.adaptor.data.optimise.expr.BooleanExpr;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReaderMetaData;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriterMetaData;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;

import java.io.IOException;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   IrodsDataAdaptorLogical
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   4 nov. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class IrodsDataAdaptorLogical extends IrodsDataAdaptorAbstract implements LogicalReaderMetaData /*LogicalReaderMetaDataExtended, LogicalWriterMetaData LogicalReader*/ {

    public String getType() {
        return "irodsl";
    }

    protected boolean isClassic(){
        return false;
    }        

    public Map listMetaData(String logicalEntry, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
//        MetaDataRecordList[] rl  = null;
        IRODSQueryResultSetInterface rl;
        try {
            IRODSFile  remoteFile = m_fileFactory.instanceIRODSFile(logicalEntry);
            IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
            
            if (remoteFile.isFile()) {
//                MetaDataCondition[] conditions = {MetaDataSet.newCondition(IRODSMetaDataSet.DIRECTORY_NAME,
//                MetaDataCondition.EQUAL, remoteFile.getParent() ),
//                MetaDataSet.newCondition( IRODSMetaDataSet.FILE_NAME,
//                MetaDataCondition.EQUAL, remoteFile.getName() ) };
                builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_D_DATA_PATH, QueryConditionOperators.EQUAL, logicalEntry);
                builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME);
                builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE);
//                MetaDataSelect[] selects = {
//                MetaDataSet.newSelection( IRODSMetaDataSet.META_DATA_ATTR_NAME ), 
//                MetaDataSet.newSelection( IRODSMetaDataSet.META_DATA_ATTR_VALUE)};
                IRODSGenQueryFromBuilder irodsQuery = builder.exportIRODSQueryFromBuilder(1);
                IRODSGenQueryExecutor irodsGenQueryExecutor = IRODSFileSystem.instance().getIRODSAccessObjectFactory().getIRODSGenQueryExecutor(m_account);
                rl = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);

//                rl = remoteFile.query( conditions, selects);
            } else {
//                String[] selectFieldNames = {  IRODSMetaDataSet.META_COLL_ATTR_NAME ,  
//                IRODSMetaDataSet.META_COLL_ATTR_VALUE, IRODSMetaDataSet.META_COLL_ATTR_UNITS};
//                MetaDataSelect selects[] =  MetaDataSet.newSelection( selectFieldNames );
                builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_COLL_ATTR_NAME);
                builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_COLL_ATTR_VALUE);
                builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_COLL_ATTR_UNITS);
                
//                rl = remoteFile.query( selects );
                IRODSGenQueryFromBuilder irodsQuery = builder.exportIRODSQueryFromBuilder(1);
                IRODSGenQueryExecutor irodsGenQueryExecutor = IRODSFileSystem.instance().getIRODSAccessObjectFactory().getIRODSGenQueryExecutor(m_account);
                rl = irodsGenQueryExecutor.executeIRODSQuery(irodsQuery, 0);
            }
        } catch (JargonException e) {
            throw new NoSuccessException(e);
        } catch (GenQueryBuilderException e) {
            throw new NoSuccessException(e);
        } catch (JargonQueryException e) {
            throw new NoSuccessException(e);
        }

        if (rl==null) {return null;}
        Map map = new HashMap();
    
//        for (int i=0; i<rl.length; i++) {
//                if (map.containsKey(rl[i].getStringValue(0))){
//                    String[] value = (String[])map.get(rl[i].getStringValue(0));
//                    String[] newValue = new String[value.length+1];
//                    System.arraycopy(value,0,newValue,0,value.length);
//                    newValue[value.length]=rl[i].getStringValue(1);
//                    map.put(rl[i].getStringValue(0),newValue);
//                } else {
//                    String[] value = {rl[i].getStringValue(1)};
//                    map.put(rl[i].getStringValue(0),value);
//                }
//        }
        for (IRODSQueryResultRow row : rl.getResults()) {
            // TODO: put in map
            System.out.println(row.getColumnsAsList().toString());
        }
        
        return map;
    }

    public FileAttributes[] findAttributes(String logicalDir, Map keyValuePatterns, boolean recursive, String additionalArgs)  throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        throw new NoSuccessException("not yet implemented");
        
        // TODO:check this code that I don't understand...
//        String key = null, value=null;
//        Iterator iterator = keyValuePatterns.keySet().iterator();
//        while (iterator.hasNext()){
//            key = (String)iterator.next();
//            value = (String)keyValuePatterns.get(key);
//        }
        
        
        //MetaDataCondition[] conditions = null;
        //MetaDataSelect[] selects = null;
//        MetaDataRecordList[] rlFile  = null;
//        MetaDataRecordList[] rlDir  = null;
//        
//        // search files or directories with metadata
//        try {
//            // Search files matching with the key valu metadata attributes
//            MetaDataCondition[] conditions = new MetaDataCondition[]{
//                MetaDataSet.newCondition( key, MetaDataCondition.EQUAL, value),
//                MetaDataSet.newCondition(IRODSMetaDataSet.DIRECTORY_NAME,MetaDataCondition.LIKE,logicalDir+"%")
//            };
//            
//            MetaDataSelect[] selects = new MetaDataSelect[] {
//                MetaDataSet.newSelection( IRODSMetaDataSet.DIRECTORY_NAME ), 
//                MetaDataSet.newSelection( IRODSMetaDataSet.FILE_NAME),
//                MetaDataSet.newSelection( IRODSMetaDataSet.SIZE)};
//            
//            rlFile= fileSystem.query( conditions, selects);
//            
//            // Search directories matching with the key valu metadata attributes
//            conditions = new MetaDataCondition[]{
//                //MetaDataSet.newCondition( key, MetaDataCondition.EQUAL, value),
//                MetaDataSet.newCondition(IRODSMetaDataSet.META_DATA_ATTR_VALUE, MetaDataCondition.EQUAL, value),
//                MetaDataSet.newCondition(IRODSMetaDataSet.PARENT_DIRECTORY_NAME,MetaDataCondition.LIKE,logicalDir+"%")
//            };
//            
//            selects = new MetaDataSelect[] {MetaDataSet.newSelection( IRODSMetaDataSet.DIRECTORY_NAME )
//                };
//            
//            rlDir = fileSystem.query( conditions, selects );
//            
//            int file =0, dir = 0, ind=0;
//            if (rlDir != null) {dir=rlDir.length;}
//            if (rlFile != null) {file=rlFile.length;}
//
//            if (dir+file==0) return null;
//            
//            boolean findAtttributes = true;
//            
//            FileAttributes[] fileAttributes = new FileAttributes[dir+file];
//
//            for (int i = 0; i < dir; i++) {
//                //name =(String) rlMetadataNames[i].getValue(rlDir[i].getFieldIndex(IRODSMetaDataSet.META_DATA_ATTR_NAME));
//                fileAttributes[ind] = new IrodsFileAttributesOptimized( rlDir[i],null);
//                ind++;
//            }
//
//            for (int i = 0; i < file; i++) {
//                fileAttributes[ind] = new IrodsFileAttributesOptimized(null,rlFile[i]);
//                ind++;
//            }
//            return fileAttributes;
//        } catch (IOException e) {
//            throw new NoSuccessException(e);
//        }

    }

//    public String[] listMetadataNames(String baseLogicalDir, Map<String, String> keyValuePatterns, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
//        try {
//            // Search directories matching with the key valu metadata attributes
//            MetaDataCondition[] conditions = new MetaDataCondition[]{
//                //MetaDataSet.newCondition( key, MetaDataCondition.EQUAL, value),
//                MetaDataSet.newCondition(IRODSMetaDataSet.DIRECTORY_NAME,MetaDataCondition.LIKE,"%"+baseLogicalDir+"%")
//            };
//            
//            MetaDataSelect[] selects = new MetaDataSelect[1];
//            selects[0] = MetaDataSet.newSelection(IRODSMetaDataSet.META_DATA_ATTR_NAME );
//            MetaDataRecordList[] rlMetadataNames = fileSystem.query( conditions, selects );
//            
//            if (rlMetadataNames ==null) return null;
//            String[] listMetadatNames = new String[rlMetadataNames.length];
//            
//            for (int i = 0; i < rlMetadataNames.length; i++) {
//                listMetadatNames[i] = 
//                (String) rlMetadataNames[i].getValue(rlMetadataNames[i].getFieldIndex(IRODSMetaDataSet.META_DATA_ATTR_NAME));
//            }
//            return listMetadatNames;
//        } catch (IOException e) {
//            throw new NoSuccessException(e);
//        }
//    }
//
//    public String[] listMetadataValues(String baseLogicalDir, String key, Map<String, String> keyValuePatterns, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
//        return new String[]{};  //todo
//    }
//
//    public void create(String logicalEntry, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
//        //todo
//    }
//
//    public void addLocation(String logicalEntry, URL replicaEntry, String additionalArgs) throws PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
//        //todo
//    }
//    
//    public void removeLocation(String logicalEntry, URL replicaEntry, String additionalArgs) throws PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
//        //todo
//    }
//
//    public void setMetaData(String logicalEntry, String name, String[] values, String additionalArgs) throws PermissionDeniedException,         TimeoutException, NoSuccessException {
//        try {
//            GeneralFile remoteFile= FileFactory.newFile(fileSystem, logicalEntry);
//            for (int i=0;i<values.length;i++) {
//                ((IRODSFile)remoteFile).modifyMetaData(new String[]{name,values[i],""});
//            }
//        } catch (IOException e) {
//            throw new NoSuccessException(e);
//        }
//    }
//
//    public void removeMetaData(String logicalEntry, String name, String additionalArgs) throws PermissionDeniedException, 
//    TimeoutException, NoSuccessException, DoesNotExistException {
//        try {
//            IRODSFile  remoteFile = (IRODSFile)FileFactory.newFile(fileSystem, logicalEntry);
//            
//            // List metadata before remove
//            if (metadataValue==null) {
//                Map metadatas = listMetaData(logicalEntry, additionalArgs);
//                Set cles = metadatas.keySet();
//                Iterator it = cles.iterator();
//                String[] values=null;
//                while (it.hasNext()){
//                    String cle = (String)it.next();
//                    if (cle.equals(name)) {
//                        values= (String[])metadatas.get(cle);
//                    }
//                }
//                
//                if (values != null){
//                    for (int i=0;i<values.length;i++) {
//                        String[] metaData = new String[] { name, values[i] };
//                        remoteFile.deleteMetaData(metaData);
//                    }
//                }
//            } else {
//                String[] metaData = new String[] { name, metadataValue };
//                remoteFile.deleteMetaData(metaData);
//            }
//        } catch (IOException e) {
//            throw new NoSuccessException(e);
//        }
//    }
//
    public String[] listLocations(String logicalEntry, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        throw new NoSuccessException("not yet implemented");
    }
    
}
