package fr.in2p3.jsaga.adaptor.data;

import edu.sdsc.grid.io.*;
import edu.sdsc.grid.io.irods.IRODSFile;
import edu.sdsc.grid.io.irods.IRODSMetaDataSet;
import fr.in2p3.jsaga.adaptor.data.optimise.LogicalReaderMetaDataExtended;
import fr.in2p3.jsaga.adaptor.data.optimise.expr.BooleanExpr;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriterMetaData;
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
public class IrodsDataAdaptorLogical extends IrodsDataAdaptor implements LogicalReaderMetaDataExtended, LogicalWriterMetaData {
    public String[] listLocations(String logicalEntry, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
	
	public String getType() {
        return "irodsl";
    }

    public Map listMetaData(String logicalEntry, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		MetaDataRecordList[] rl  = null;
		try {
			IRODSFile  remoteFile = (IRODSFile)FileFactory.newFile(fileSystem, logicalEntry);
			
			if (remoteFile.isFile()) {
				MetaDataCondition[] conditions = {MetaDataSet.newCondition(IRODSMetaDataSet.DIRECTORY_NAME,
				MetaDataCondition.EQUAL, remoteFile.getParent() ),
				MetaDataSet.newCondition( IRODSMetaDataSet.FILE_NAME,
				MetaDataCondition.EQUAL, remoteFile.getName() ) };
				
				MetaDataSelect[] selects = {
				MetaDataSet.newSelection( IRODSMetaDataSet.META_DATA_ATTR_NAME ), 
				MetaDataSet.newSelection( IRODSMetaDataSet.META_DATA_ATTR_VALUE)};
				rl = remoteFile.query( conditions, selects);
			} else {
				String[] selectFieldNames = {  IRODSMetaDataSet.META_COLL_ATTR_NAME ,  
				IRODSMetaDataSet.META_COLL_ATTR_VALUE, IRODSMetaDataSet.META_COLL_ATTR_UNITS};
				MetaDataSelect selects[] =  MetaDataSet.newSelection( selectFieldNames );
				rl = remoteFile.query( selects );
			}
		} catch (IOException e) {
			throw new NoSuccessException(e);
        }

		if (rl==null) {return null;}
		Map map = new HashMap();
	
		for (int i=0; i<rl.length; i++) {
				if (map.containsKey(rl[i].getStringValue(0))){
					String[] value = (String[])map.get(rl[i].getStringValue(0));
					String[] newValue = new String[value.length+1];
					System.arraycopy(value,0,newValue,0,value.length);
					newValue[value.length]=rl[i].getStringValue(1);
					map.put(rl[i].getStringValue(0),newValue);
				} else {
					String[] value = {rl[i].getStringValue(1)};
					map.put(rl[i].getStringValue(0),value);
				}
		}
		return map;
	}

    public FileAttributes[] findAttributes(String logicalDir, String namePattern, BooleanExpr filter, boolean recursive, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException, BadParameterException	{
	  return null;
    }

    public FileAttributes[] findAttributes(String logicalDir, Map keyValuePatterns, boolean recursive, String additionalArgs)  throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		
		String key = null, value=null;
		Iterator iterator = keyValuePatterns.keySet().iterator();
		while (iterator.hasNext()){
			key = (String)iterator.next();
			value = (String)keyValuePatterns.get(key);
		}
		
		//MetaDataCondition[] conditions = null;
		//MetaDataSelect[] selects = null;
		MetaDataRecordList[] rlFile  = null;
		MetaDataRecordList[] rlDir  = null;
		
		// search files or directories with metadata
		try {
			// Search files matching with the key valu metadata attributes
			MetaDataCondition[] conditions = new MetaDataCondition[]{
				MetaDataSet.newCondition( key, MetaDataCondition.EQUAL, value),
				MetaDataSet.newCondition(IRODSMetaDataSet.DIRECTORY_NAME,MetaDataCondition.LIKE,logicalDir+"%")
			};
			
			MetaDataSelect[] selects = new MetaDataSelect[] {
				MetaDataSet.newSelection( IRODSMetaDataSet.DIRECTORY_NAME ), 
				MetaDataSet.newSelection( IRODSMetaDataSet.FILE_NAME),
				MetaDataSet.newSelection( IRODSMetaDataSet.SIZE)};
			
			rlFile= fileSystem.query( conditions, selects);
			
			// Search directories matching with the key valu metadata attributes
			conditions = new MetaDataCondition[]{
				//MetaDataSet.newCondition( key, MetaDataCondition.EQUAL, value),
				MetaDataSet.newCondition(IRODSMetaDataSet.META_DATA_ATTR_VALUE, MetaDataCondition.EQUAL, value),
				MetaDataSet.newCondition(IRODSMetaDataSet.PARENT_DIRECTORY_NAME,MetaDataCondition.LIKE,logicalDir+"%")
			};
			
			selects = new MetaDataSelect[] {MetaDataSet.newSelection( IRODSMetaDataSet.DIRECTORY_NAME )
				};
			
			rlDir = fileSystem.query( conditions, selects );
			
			int file =0, dir = 0, ind=0;
			if (rlDir != null) {dir=rlDir.length;}
			if (rlFile != null) {file=rlFile.length;}

			if (dir+file==0) return null;
			
			boolean findAtttributes = true;
			
			FileAttributes[] fileAttributes = new FileAttributes[dir+file];

			for (int i = 0; i < dir; i++) {
				//name =(String) rlMetadataNames[i].getValue(rlDir[i].getFieldIndex(IRODSMetaDataSet.META_DATA_ATTR_NAME));
				fileAttributes[ind] = new IrodsFileAttributes( rlDir[i],null);
				ind++;
			}

			for (int i = 0; i < file; i++) {
				fileAttributes[ind] = new IrodsFileAttributes(null,rlFile[i]);
				ind++;
			}
			return fileAttributes;
		} catch (IOException e) {
			throw new NoSuccessException(e);
		}

    }

    public String[] listMetadataNames(String baseLogicalDir, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		try {
			// Search directories matching with the key valu metadata attributes
			MetaDataCondition[] conditions = new MetaDataCondition[]{
				//MetaDataSet.newCondition( key, MetaDataCondition.EQUAL, value),
				MetaDataSet.newCondition(IRODSMetaDataSet.DIRECTORY_NAME,MetaDataCondition.LIKE,"%"+baseLogicalDir+"%")
			};
			
			MetaDataSelect[] selects = new MetaDataSelect[1];
			selects[0] = MetaDataSet.newSelection(IRODSMetaDataSet.META_DATA_ATTR_NAME );
			MetaDataRecordList[] rlMetadataNames = fileSystem.query( conditions, selects );
			
			if (rlMetadataNames ==null) return null;
			String[] listMetadatNames = new String[rlMetadataNames.length];
			
			for (int i = 0; i < rlMetadataNames.length; i++) {
				listMetadatNames[i] = 
				(String) rlMetadataNames[i].getValue(rlMetadataNames[i].getFieldIndex(IRODSMetaDataSet.META_DATA_ATTR_NAME));
			}
			return listMetadatNames;
		} catch (IOException e) {
			throw new NoSuccessException(e);
        }
    }
	
	public void addLocation(String logicalEntry, URL replicaEntry, String additionalArgs) throws PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
		
	}
	
	public void removeLocation(String logicalEntry, URL replicaEntry, String additionalArgs) throws PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
		
	}

	public void setMetaData(String logicalEntry, String name, String[] values, String additionalArgs) throws PermissionDeniedException, 		TimeoutException, NoSuccessException {
		try {
			GeneralFile remoteFile= FileFactory.newFile(fileSystem, logicalEntry);
			for (int i=0;i<values.length;i++) {
				((IRODSFile)remoteFile).modifyMetaData(new String[]{name,values[i],""});
			}
		} catch (IOException e) {
			throw new NoSuccessException(e);
        }
	}

	public void removeMetaData(String logicalEntry, String name, String additionalArgs) throws PermissionDeniedException, 
	TimeoutException, NoSuccessException, DoesNotExistException {
		try {
			IRODSFile  remoteFile = (IRODSFile)FileFactory.newFile(fileSystem, logicalEntry);
			
			// List metadata before remove
			if (metadataValue==null) {
				Map metadatas = listMetaData(logicalEntry, additionalArgs);
				Set cles = metadatas.keySet();
				Iterator it = cles.iterator();
				String[] values=null;
				while (it.hasNext()){
					String cle = (String)it.next();
					if (cle.equals(name)) {
						values= (String[])metadatas.get(cle);
					}
				}
				
				if (values != null){
					for (int i=0;i<values.length;i++) {
						String[] metaData = new String[] { name, values[i] };
						remoteFile.deleteMetaData(metaData);
					}
				}
			} else {
				String[] metaData = new String[] { name, metadataValue };
				remoteFile.deleteMetaData(metaData);
			}
		} catch (IOException e) {
			throw new NoSuccessException(e);
        }
	}
}