package fr.in2p3.jsaga.adaptor.data;

import edu.sdsc.grid.io.*;
import edu.sdsc.grid.io.srb.*;
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
* File:   SrbDataAdaptorLogical
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   4 nov. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SrbDataAdaptorLogical extends SrbDataAdaptor implements LogicalReaderMetaDataExtended, LogicalWriterMetaData  {
	//TODO: remove this when supporting by jsaga engine
	public String getType() {
        return "srbl";
    }


    public String[] listLocations(String logicalEntry, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map listMetaData(String logicalEntry, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		try {
			SRBFile remoteFile = (SRBFile)FileFactory.newFile(fileSystem, logicalEntry);
			MetaDataRecordList[] rl = new MetaDataRecordList[0];
			
			if (!remoteFile.isDirectory()) {
				MetaDataCondition[] conditions = new MetaDataCondition[]{
					MetaDataSet.newCondition(
						SRBMetaDataSet.FILE_NAME,
						MetaDataCondition.EQUAL,
						remoteFile.getName())
					};
				MetaDataSelect[] selects = 
				new MetaDataSelect[]{MetaDataSet.newSelection(SRBMetaDataSet.DEFINABLE_METADATA_FOR_FILES)};
				rl = fileSystem.query(conditions, selects);	
			} else {
				MetaDataCondition[]  conditions = new MetaDataCondition[]{
					MetaDataSet.newCondition(
						SRBMetaDataSet.DIRECTORY_NAME,
						MetaDataCondition.EQUAL,
						remoteFile.getPath())
					};
				MetaDataSelect[] selects = 
				new MetaDataSelect[]{MetaDataSet.newSelection(SRBMetaDataSet.DEFINABLE_METADATA_FOR_DIRECTORIES)};
				rl = fileSystem.query(conditions, selects);
			}
			
			// transfrom rl into map of String[]	
			if (rl==null) {return null;}
			Map map = new HashMap();
		
			for (int i=0; i<rl.length; i++) {
				int metaDataIndex;
				if (remoteFile.isDirectory()) {
					metaDataIndex = rl[i].getFieldIndex(SRBMetaDataSet.DEFINABLE_METADATA_FOR_DIRECTORIES);
				} else {
					metaDataIndex = rl[i].getFieldIndex(SRBMetaDataSet.DEFINABLE_METADATA_FOR_FILES);
				}
				if (metaDataIndex > -1) {
					MetaDataTable t = rl[i].getTableValue(metaDataIndex);
					for (int j = 0; j < t.getRowCount(); j++) {
						if (map.containsKey(t.getStringValue(j, 0))){
							String[] value = (String[])map.get(t.getStringValue(j, 0));
							String[] newValue = new String[value.length+1];
							System.arraycopy(value,0,newValue,0,value.length);
							newValue[value.length]=t.getStringValue(j,1);
							map.put(t.getStringValue(j,0),newValue);
						} else {
							String[] value = {t.getStringValue(j,1)};
							map.put(t.getStringValue(j,0),value);
						}		
					}
				}
			}
			
			return map;
		} catch (IOException e) {
			throw new NoSuccessException(e);
		}
    }

    public FileAttributes[] findAttributes(String logicalDir, String namePattern, BooleanExpr filter, boolean recursive, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException, BadParameterException	{
        return null;
    }

    public FileAttributes[] findAttributes(String logicalDir, Map keyValuePatterns, boolean recursive, String additionalArgs)  throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		logicalDir=logicalDir.substring(0,logicalDir.length()-1);
		
		String key = null, value=null;
		Iterator iterator = keyValuePatterns.keySet().iterator();
		while (iterator.hasNext()){
			key = (String)iterator.next();
			value = (String)keyValuePatterns.get(key);
		}
		
		// search files or directories with metadata
		try {
			String[][] definableMetaDataValues = new String[1][2];
			definableMetaDataValues[0][0] = key;
			definableMetaDataValues[0][1] = value;
	    
			int[] operators = new int[definableMetaDataValues.length];
			operators[0] = MetaDataCondition.EQUAL;
			MetaDataTable metaDataTable  = new MetaDataTable( operators, definableMetaDataValues );

			// Search files matching with the key valu metadata attributes
			MetaDataCondition[] conditions = new MetaDataCondition[]{
				MetaDataSet.newCondition(SRBMetaDataSet.DEFINABLE_METADATA_FOR_FILES, metaDataTable),
				MetaDataSet.newCondition(SRBMetaDataSet.DIRECTORY_NAME,MetaDataCondition.LIKE,logicalDir)
			};
			
			MetaDataSelect[] selects = new MetaDataSelect[]{
				MetaDataSet.newSelection(SRBMetaDataSet.DIRECTORY_NAME),
				MetaDataSet.newSelection(SRBMetaDataSet.FILE_NAME),
				MetaDataSet.newSelection(SRBMetaDataSet.FILE_LAST_ACCESS_TIMESTAMP),
				MetaDataSet.newSelection(SRBMetaDataSet.SIZE)
			};
			
			MetaDataRecordList[] rlFile = fileSystem.query( conditions, selects );
			
			// Search directories matching with the key valu metadata attributes
			conditions = new MetaDataCondition[]{
				MetaDataSet.newCondition(SRBMetaDataSet.DEFINABLE_METADATA_FOR_DIRECTORIES, metaDataTable),
				MetaDataSet.newCondition(SRBMetaDataSet.DIRECTORY_NAME,MetaDataCondition.LIKE,logicalDir)
			};

			selects = new MetaDataSelect[1];
			selects[0] = MetaDataSet.newSelection(SRBMetaDataSet.DIRECTORY_NAME);
			MetaDataRecordList[] rlDir = fileSystem.query( conditions, selects );
			
			int file =0, dir = 0, ind=0;
			if (rlDir != null) {dir=rlDir.length;}
			if (rlFile != null) {file=rlFile.length;}
			
			if (dir+file==0) return null;
			
			boolean findAtttributes = true;
			
			FileAttributes[] fileAttributes = new FileAttributes[dir+file];

			for (int i = 0; i < dir; i++) {
				String m_name = (String) rlDir[i].getValue(rlDir[i].getFieldIndex(SRBMetaDataSet.DIRECTORY_NAME));
				if (!m_name.equals(SEPARATOR)) {
					// Construct SrbFileAttributes from the result of the request 
					fileAttributes[ind] = new SrbFileAttributes(logicalDir, rlDir[i],null,findAtttributes);
					ind++;
				}
			}

			for (int i = 0; i < file; i++) {
				fileAttributes[ind] = new SrbFileAttributes(logicalDir, null,rlFile[i],findAtttributes);
				ind++;
			}
			return fileAttributes;
		} catch (IOException e) {
			throw new NoSuccessException(e);
		}
    }

    public String[] listMetadataNames(String baseLogicalDir, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        return null;
    }
	
	
	public void setMetaData(String logicalEntry, String name, String[] values, String additionalArgs) throws PermissionDeniedException, 		TimeoutException, NoSuccessException {
		
		try {
			SRBFile file = (SRBFile)FileFactory.newFile(fileSystem, logicalEntry);
			String[][] definableMetaDataValues = new String[values.length][2];
	
			int[] operators = new int[values.length];
			
			for (int i=0;i<values.length;i++) {
				definableMetaDataValues[i][0] = name;
				definableMetaDataValues[i][1] = values[i];
				System.out.println("definableMetaDataValues[i][0]:"+definableMetaDataValues[i][0]);
				System.out.println("definableMetaDataValues[i][1]:"+definableMetaDataValues[i][1]);
			}

			String fieldName = null;
			if (file.isDirectory()) {
				fieldName = SRBMetaDataSet.DEFINABLE_METADATA_FOR_DIRECTORIES;
			}else {
				fieldName = SRBMetaDataSet.DEFINABLE_METADATA_FOR_FILES;
			}
			
			MetaDataTable metaDataTable = new MetaDataTable(operators,definableMetaDataValues);
			MetaDataRecordList rl= 
			new SRBMetaDataRecordList(SRBMetaDataSet.getField(fieldName),metaDataTable);

			file.modifyMetaData(rl);
		 } catch (IOException e) {
			throw new NoSuccessException(e);
		}
	}
 
	public void removeMetaData(String logicalEntry, String name, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException, DoesNotExistException {
		
		try {
			SRBFile file = (SRBFile)FileFactory.newFile(fileSystem, logicalEntry);		

			// List all metadata
			HashMap metadatas = (HashMap)listMetaData(logicalEntry,additionalArgs);
			
			// synchronized because not transactional operation.
			synchronized (this) {
				// suppress all metadatas
				String fieldName = null;
				if (file.isDirectory()) {
					fieldName = SRBMetaDataSet.DEFINABLE_METADATA_FOR_DIRECTORIES;
				} else {
					fieldName = SRBMetaDataSet.DEFINABLE_METADATA_FOR_FILES;
				}

				MetaDataRecordList rl= 
				new SRBMetaDataRecordList(SRBMetaDataSet.getField(fieldName),(MetaDataTable) null);

				file.modifyMetaData(rl);
					
				// Store the metadatas with excluding the metadata name
				Set cles = metadatas.keySet();
				Iterator it = cles.iterator();
				while (it.hasNext()){
					String cle = (String)it.next(); // tu peux typer plus finement ici
					if (!cle.equals(name)) {
						setMetaData(logicalEntry, cle,(String[])metadatas.get(cle),additionalArgs);
					}
				}
			}
		} catch (IOException e) {
			throw new NoSuccessException(e);
		}
	}
	
	public void addLocation(String logicalEntry, URL replicaEntry, String additionalArgs) throws PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
		
	}
	
	public void removeLocation(String logicalEntry, URL replicaEntry, String additionalArgs) throws PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
		
	}	
}
