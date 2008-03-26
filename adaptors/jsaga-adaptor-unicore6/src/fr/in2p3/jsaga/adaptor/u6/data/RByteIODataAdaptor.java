package fr.in2p3.jsaga.adaptor.u6.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.read.*;
import fr.in2p3.jsaga.adaptor.data.write.*;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.JKSSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.u6.TargetSystemInfo;
import fr.in2p3.jsaga.adaptor.u6.U6Abstract;

import org.ogf.saga.error.AlreadyExists;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;

import com.intel.gpe.client2.common.i18n.Messages;
import com.intel.gpe.client2.common.i18n.MessagesKeys;
import com.intel.gpe.client2.common.requests.CreateDirectoryRequest;
import com.intel.gpe.client2.common.requests.DeleteFileRequest;
import com.intel.gpe.client2.common.transfers.byteio.RandomByteIOFileExportImpl;
import com.intel.gpe.client2.common.transfers.byteio.RandomByteIOFileImportImpl;
import com.intel.gpe.client2.providers.FileProvider;
import com.intel.gpe.client2.transfers.FileExport;
import com.intel.gpe.client2.transfers.FileImport;
import com.intel.gpe.client2.transfers.TransferFailedException;
import com.intel.gpe.clients.api.StorageClient;
import com.intel.gpe.clients.api.exceptions.GPEFileTransferProtocolNotSupportedException;
import com.intel.gpe.clients.api.exceptions.GPESecurityException;
import com.intel.gpe.gridbeans.GPEFile;
import com.intel.gpe.util.sets.Pair;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   RByteIODataAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   5 mars 2008
* ***************************************************
/**
 *
 */
public class RByteIODataAdaptor extends U6Abstract 
		implements FileWriterPutter, FileReaderGetter {
		
	protected JKSSecurityAdaptor m_credential;
    protected String m_serverFileSeparator ;
    protected StorageClient m_client;
    private String rootDirectory = ".";
	
    public RByteIODataAdaptor() {
    }

    public String getType() {
        return "rbyteio";
    }

    public Usage getUsage() {
    	return new UAnd(new Usage[]{new U(SERVICE_NAME), 
    								new U(APPLICATION_NAME)});
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
    	return new Default[]{
    			new Default(SERVICE_NAME, "Registry"), 
    			new Default(APPLICATION_NAME, "Bash shell")};
    }

    public Class[] getSupportedSecurityAdaptorClasses() {
    	return new Class[]{JKSSecurityAdaptor.class};
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
    	 m_credential = (JKSSecurityAdaptor) securityAdaptor;
    }

    public int getDefaultPort() {
        return 8080;
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
       
    	// get SERVICE_NAME
    	if(attributes.containsKey(SERVICE_NAME))
    		m_serviceName = (String) attributes.get(SERVICE_NAME);
    	
    	// get APPLICATION_NAME
    	if(attributes.containsKey(APPLICATION_NAME))
    		m_applicationName = (String) attributes.get(APPLICATION_NAME);
    	
    	// get registry
    	if(basePath.indexOf(m_serviceName) == -1 ) {
    		throw new BadParameter("Invalid base path:"+basePath);
    	}
    	String registryPath = basePath.substring(0,basePath.indexOf(m_serviceName)+m_serviceName.length());

    	// get storage name
    	String storageName =  basePath.substring(basePath.indexOf(m_serviceName)+m_serviceName.length()+1,basePath.length());
    	storageName = storageName.substring(0,storageName.indexOf("/"));

    	// connect
    	m_serverUrl = "https://"+host+":"+port+registryPath;    

    	// set security
    	m_securityManager = setSecurity(m_credential);
    	
		// get client that talks to registry
        try {
        	  // find target system that supports the specific application
        	TargetSystemInfo targetSystemInfo = findTargetSystem();
        	m_client = targetSystemInfo.getTargetSystem().getStorage(storageName);
        	if(m_client == null)
        		throw new NoSuccess("Unable to get storage:"+storageName);
        	m_serverFileSeparator = m_client.getFileSeparator();
		} catch (NoSuccess e) {
			throw new NoSuccess(e);
		} catch (Exception e) {
			throw new NoSuccess(e);
		}
    }

    public void disconnect() throws NoSuccess {
        //
    }
    
    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDenied, Timeout, NoSuccess {

    	// prepare path
		absolutePath = getEntryPath(absolutePath);
    	
		// get parent
		String parentDirectory = rootDirectory;            
		if(absolutePath.lastIndexOf(m_serverFileSeparator) > 0) {
		    parentDirectory = absolutePath.substring(0,absolutePath.lastIndexOf(m_serverFileSeparator));
		 }
		
		// if absolutePath is the root
		if(absolutePath.equals(rootDirectory)) {
			return true;
		}
		
		try {			
	        // check
	        List<com.intel.gpe.clients.api.GridFile> directoryList = m_client.listDirectory(parentDirectory);
	        for (com.intel.gpe.clients.api.GridFile file : directoryList) {
	            if(file.getPath().endsWith(absolutePath)) {
	            	return true;
	            }
	        }
	        return false;
		} catch (GPESecurityException e) {
			throw new PermissionDenied("Unable to check entry",e);
		} catch (Throwable e) {
			if(!exists(parentDirectory, additionalArgs)) {
				return false;
			}
			else {
				throw new NoSuccess(e);
			}
		}
    }

    public boolean isDirectory(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
    	// prepare path and connection
		absolutePath = getEntryPath(absolutePath);
    	
		// is root path
		if(absolutePath.equals(rootDirectory)) {
			return true;
		}				
		
		// get parent
    	String parentDirectory = rootDirectory;            
        if(absolutePath.lastIndexOf(m_serverFileSeparator) > 0) {
            parentDirectory = absolutePath.substring(0,absolutePath.lastIndexOf(m_serverFileSeparator));
        }
    	try {
	        // check
	        List<com.intel.gpe.clients.api.GridFile> directoryList = m_client.listDirectory(parentDirectory);
	        for (com.intel.gpe.clients.api.GridFile file : directoryList) {
	        	if(file.getPath().endsWith(absolutePath)) {
	            	if(file.isDirectory()) {
	            		return true;
	            	}
	            	else {
	            		return false;
	            	}
	            }
	        }
	        // to be catch by Throwable
	        throw new DoesNotExist("The directory does not exist.");
        } catch (GPESecurityException e) {
        	throw new PermissionDenied("Unable to check directory",e);
		} catch (Throwable e) {
			// check
			if(!exists(absolutePath, additionalArgs)) {
				throw new DoesNotExist("The directory does not exist.");
			}
			throw new NoSuccess(e);			
		}
    }

    public boolean isEntry(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        return !isDirectory(absolutePath, additionalArgs);
    }
    
    public long getSize(String absolutePath, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
    	      	
		//prepare path and connection
		absolutePath = getEntryPath(absolutePath);
		
		// get parent
		String parentDirectory = rootDirectory;               
		if(absolutePath.lastIndexOf(m_serverFileSeparator) > 0) {
		    parentDirectory = absolutePath.substring(0,absolutePath.lastIndexOf(m_serverFileSeparator));
		 }
		
		try {		
	        // check
	        List<com.intel.gpe.clients.api.GridFile> directoryList = m_client.listDirectory(parentDirectory);
	        for (com.intel.gpe.clients.api.GridFile file : directoryList) {
	            if(file.getPath().endsWith(absolutePath)) {
	            	return file.getSize();
	            }
	        }
	        // to be catch by Throwable
	        throw new Exception("To be catched");
		} catch (GPESecurityException e) {
			throw new PermissionDenied("Unable to get size",e);
        } catch (Throwable e) {
        	// check source
    		if(!exists(absolutePath, additionalArgs)) {
    			throw new DoesNotExist("The file does not exist.");
    		}
    		
    		// check source
    		if(!isEntry(absolutePath, additionalArgs)) {
    			throw new BadParameter("The entry is not a file.");
    		}
    	
    		throw new NoSuccess("Unable to get size",e);
        }
    }

    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
    	
    	//prepare path
		String absolutePath = getEntryPath(parentAbsolutePath + fileName);    	

		try {
        	// remove
	        List<com.intel.gpe.clients.api.GridFile> directoryList = m_client.listDirectory(getEntryPath(parentAbsolutePath));
	        for (com.intel.gpe.clients.api.GridFile gridFile : directoryList) {
	            if(gridFile.getPath().endsWith(fileName)) {
	                DeleteFileRequest requestRmFile = new DeleteFileRequest(m_client, gridFile);
	                requestRmFile.perform();
	                return;
	            }
	        }
	        // 
	        throw new Exception("To be catched");
    	} catch (GPESecurityException e) {
			throw new PermissionDenied("Failed to remove file ["+fileName+"]", e);
        } catch (Throwable e) {
    		// check file
    		if(!exists(absolutePath, additionalArgs)) {
    			throw new DoesNotExist("The file ["+absolutePath+"] does not exist.");
    		}
    		
    		// check file
    		if(isDirectory(absolutePath, additionalArgs)) {
    			throw new BadParameter("The entry ["+absolutePath+"] is a directory.");
    		}
            throw new NoSuccess("Failed to remove file ["+fileName+"]", e);
        }
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {        
    	// prepare path
		absolutePath = getEntryPath(absolutePath);
		// check parent
		if(!exists(absolutePath, additionalArgs)) {
			throw new DoesNotExist("The entry ["+absolutePath+"] does not exist.");
		} 
		
		try {
	        List<com.intel.gpe.clients.api.GridFile> directoryList = m_client.listDirectory(absolutePath);
	        Vector<RByteIOFileAttributes> entries = new Vector<RByteIOFileAttributes>();
	        for (com.intel.gpe.clients.api.GridFile file : directoryList) {
	        	entries.add(new RByteIOFileAttributes(file,m_serverFileSeparator));
	        }
	        FileAttributes[] list = new FileAttributes[entries.size()];
	        for (int i = 0; i < list.length; i++) {
				list[i] = entries.get(i);
			}
	        return list;
		} catch (GPESecurityException e) {
			throw new NoSuccess(e);
		} catch (Throwable e) {
			throw new NoSuccess("Failed to list attributes", e);
		}     
    }

    public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDenied, BadParameter, AlreadyExists, ParentDoesNotExist, Timeout, NoSuccess {
    	String absolutePath = getEntryPath(parentAbsolutePath + directoryName);
    	
    	try {
    		// check parent here, else no exception returned during creation
    		if(!exists(parentAbsolutePath, additionalArgs)) {
    			throw new ParentDoesNotExist("The parent directory ["+parentAbsolutePath+"] of ["+directoryName+"] does not exist.");
    		}
    		
        	// make
    		CreateDirectoryRequest request = new CreateDirectoryRequest(m_client, absolutePath);
			request.perform();
        } catch (GPESecurityException e) {
			throw new PermissionDenied("Failed to make directory ["+directoryName+"]", e);
		} catch (Throwable e) {

			// get NoSuccess exception
			if(e.getClass() == ParentDoesNotExist.class) {
				throw new ParentDoesNotExist(e);
			}
			
			// check already exist
			try {			
				if(isDirectory(absolutePath, additionalArgs))
					throw new AlreadyExists ("The directory ["+absolutePath+"] already exists.");
			}
			catch(DoesNotExist e1) {
				// means that the directory does not exist
			}
			
			// check parent
			try {
				if(!isDirectory(parentAbsolutePath, additionalArgs))
					throw new BadParameter("The parent directory is not a directory.");
			}
			catch(DoesNotExist e1) {
				throw new BadParameter("The parent directory is not a directory.",e1);
			}
			throw new NoSuccess("Failed to make directory ["+directoryName+"]", e);
		}
    }

    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
    	// prepare path
		String absolutePath = getEntryPath(parentAbsolutePath + directoryName) ;
				
		try {  
			// check children here, else no exception returned during deletion !			
			List<com.intel.gpe.clients.api.GridFile> directoryList = m_client.listDirectory(absolutePath);
	        if(directoryList.size() > 0) {
	        	throw new NoSuccess("The entry ["+absolutePath+"] is not a empty directory.");
	        }
	        
    		// remove
        	List<com.intel.gpe.clients.api.GridFile> parentDirectoryList = m_client.listDirectory(getEntryPath(parentAbsolutePath));       
	        for (com.intel.gpe.clients.api.GridFile file : parentDirectoryList) {
	            if(file.getPath().endsWith(directoryName)) {
	                DeleteFileRequest requestRmFile = new DeleteFileRequest(m_client, file);
	                requestRmFile.perform();
	                return ;
	            }
	        }
	        // to be catch by Throwable
	        throw new Exception("To be catched");
        } catch (GPESecurityException e) {
			throw new PermissionDenied("Failed to remove directory ["+directoryName+"]", e);
		} catch (Throwable e) {
			
			// get NoSuccess exception
			if(e.getClass() == NoSuccess.class) {
				throw new NoSuccess(e);
			}
			
			// check directory
			if(!exists(absolutePath, additionalArgs)) {
				throw new DoesNotExist("The directory ["+absolutePath+"] does not exist.");
			}
			
			// check directory
			if(!isDirectory(absolutePath, additionalArgs)) {
				throw new BadParameter("The entry ["+absolutePath+"] is not a directory.");
			}
				        
			throw new NoSuccess("Failed to remove directory ["+directoryName+"]", e);
		}
    }    

	public void putFromStream(String absolutePath, boolean append, String additionalArgs,
			InputStream stream) throws PermissionDenied,
			BadParameter, AlreadyExists, ParentDoesNotExist, Timeout, NoSuccess {
		if(append) {
			throw new NoSuccess("Append not supported.");
		}
		
		try {
    		// prepare path
    		absolutePath = getEntryPath(absolutePath);
    		
    		Pair<GPEFile, String> inputFile = new Pair<GPEFile, String>(null, absolutePath);
    		FileProvider fileProvider = new FileProvider();
	        fileProvider.addFilePutter("RBYTEIO", RandomByteIOFileImportImpl.class);	        
    		List<FileImport> putters = fileProvider.preparePutters(m_client);
        	int i;
			for (i = 0; i < putters.size(); i++) {
			    try {
			         putters.get(i).putFile(m_securityManager, stream, inputFile.getM2());
			     }
			     catch (GPEFileTransferProtocolNotSupportedException e) {
			         continue;
			     }
			     break;
			}
			if (i == putters.size()) {
			     throw new Exception(
			             Messages.getString(MessagesKeys.common_requests_PutFilesRequest_Cannot_put_file_to_remote_location__no_suitable_protocol_found));
			}            
        } catch (Throwable e) {
			throw new NoSuccess("Unable to put file", e);
		}
	}

	public void getToStream(String absolutePath, String additionalArgs,
			OutputStream stream) throws PermissionDenied, BadParameter,
			DoesNotExist, Timeout, NoSuccess {
		try {
    		// prepare path
    		absolutePath = getEntryPath(absolutePath);
    		
    		// copy
            Pair<GPEFile, String> outputFile = new Pair<GPEFile, String>(null, absolutePath);
            FileProvider fileProvider = new FileProvider();
	        fileProvider.addFileGetter("RBYTEIO", RandomByteIOFileExportImpl.class);
	        List<FileExport> getters = fileProvider.prepareGetters(m_client);
            int i;
            for (i = 0; i < getters.size(); i++) {
                try {
                    getters.get(i).getFile(m_securityManager, outputFile.getM2(), stream, null);
                }
                catch (GPEFileTransferProtocolNotSupportedException e) {
                    continue;
                }
                catch (TransferFailedException e) {
                    throw e;
                }
                break;
            }
            if (i == getters.size()) {
                throw new Exception(Messages.getString(MessagesKeys.common_requests_GetFilesRequest_Cannot_fetch_file_from_remote_location__no_suitable_protocol_found));
            }
            
        } catch (Throwable e) {
			throw new NoSuccess("Unable to get file", e);
		}
	}

    private String getEntryPath(String path) {
        // the path must be like /DEMOSITE/services/<service name>/<storage name>/directory/file.txt
    	// if the request path is a full path, containing service name
    	if(path.indexOf(m_serviceName) > -1) {
        	path  = path.substring(path.indexOf(m_serviceName)+m_serviceName.length()+1,path.length());
        
            // remove the registry path and the storage path in the path name
			path = path.substring(path.indexOf(m_serverFileSeparator)+1 ,path.length());
        }
        // remove last file separator, a path cannot ends with a file separator 
        if(path.endsWith(m_serverFileSeparator)) {
            path = path.substring(0,path.length()-1);
        }
        // remove first file separator, a path cannot starts with a file separator 
        if(path.startsWith(m_serverFileSeparator)) {
            path = path.substring(1,path.length());
        }
        // replace root directory
        if(path.equals("")) {
            path = rootDirectory;
        }
        return path;
    }
}
